package com.nexun.nexres.shell

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

/**
 * Abstraction over the two privileged backends: Shizuku (no root required) and
 * a plain root shell. Commands always run as shell/root, never in-app.
 */
object Shell {

    const val SHIZUKU_PACKAGE = "moe.shizuku.manager"

    private const val RUN_TIMEOUT_SECONDS = 15L
    private const val PROBE_TIMEOUT_SECONDS = 3L

    fun isShizukuInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    } catch (t: Throwable) {
        false
    }

    fun isShizukuRunning(): Boolean = try {
        Shizuku.pingBinder()
    } catch (t: Throwable) {
        false
    }

    fun hasShizukuPermission(): Boolean = try {
        isShizukuRunning() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (t: Throwable) {
        false
    }

    /**
     * Superuser binaries are not always reachable through the app's PATH, so we
     * probe the usual locations besides the bare command.
     */
    private val suCandidates = listOf(
        "su",
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/debug_ramdisk/su",
        "/system/sbin/su",
    )

    fun hasRoot(): Boolean = suCandidates.any { probe(it) }

    /**
     * Two strategies per candidate: `su -c` (works on Magisk/KernelSU) and an
     * interactive `su` shell fed through stdin (works on older/ROM binaries that
     * reject `-c`). A hit is any output containing `uid=0`. Probing uses a short
     * timeout so an unanswered prompt never stalls the UI.
     */
    private fun probe(su: String): Boolean {
        runCatching { exec(listOf(su, "-c", "id"), PROBE_TIMEOUT_SECONDS) }.getOrNull()?.let {
            if (it.contains("uid=0")) return true
        }
        runCatching { execInteractive(su, "id", PROBE_TIMEOUT_SECONDS) }.getOrNull()?.let {
            if (it.contains("uid=0")) return true
        }
        return false
    }

    fun runRoot(command: String): String {
        var lastError: Throwable? = null
        for (su in suCandidates) {
            runCatching { exec(listOf(su, "-c", command), RUN_TIMEOUT_SECONDS) }
                .onSuccess { return it }
                .onFailure { lastError = it }
            runCatching { execInteractive(su, command, RUN_TIMEOUT_SECONDS) }
                .onSuccess { return it }
                .onFailure { lastError = it }
        }
        throw lastError ?: IllegalStateException("su is not available")
    }

    private fun exec(cmd: List<String>, timeoutSeconds: Long): String {
        val process = ProcessBuilder(cmd).redirectErrorStream(true).start()
        return drain(process, timeoutSeconds)
    }

    private fun execInteractive(su: String, command: String, timeoutSeconds: Long): String {
        val process = ProcessBuilder(su).redirectErrorStream(true).start()
        runCatching {
            process.outputStream.bufferedWriter().use { stdin ->
                stdin.write(command)
                stdin.write("\n")
                stdin.write("exit\n")
            }
        }
        return drain(process, timeoutSeconds)
    }

    /**
     * Reads the merged stdout/stderr on a background thread and waits for that
     * reader to hit EOF (stdout EOF means the process exited), killing it if it
     * outlives the timeout so callers never hang forever.
     *
     * We deliberately avoid `Process.waitFor(timeout, TimeUnit)`: for Shizuku's
     * remote process the server-side `IllegalThreadStateException` loses its type
     * across binder and surfaces as `IllegalArgumentException`, so the stock
     * implementation throws instead of polling. Waiting on the reader works for
     * both the local root process and the remote Shizuku process.
     */
    private fun drain(process: Process, timeoutSeconds: Long): String {
        val output = StringBuilder()
        val reader = Thread {
            try {
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { output.append(it).append('\n') }
                }
            } catch (_: Throwable) {
                // stream closed after kill; ignore
            }
        }
        reader.isDaemon = true
        reader.start()

        reader.join(TimeUnit.SECONDS.toMillis(timeoutSeconds))
        if (reader.isAlive) {
            runCatching { process.destroyForcibly() }
            reader.join(2000)
        }
        return output.toString().trim()
    }

    /**
     * Shizuku's public API does not expose process spawning (the internal
     * `newProcess` is private since API 13). We reach it reflectively; the
     * class ships inside this APK, so setAccessible is allowed.
     */
    private val newProcess: Method? by lazy {
        runCatching {
            Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java,
            ).apply { isAccessible = true }
        }.getOrNull()
    }

    fun runShizuku(command: String): String {
        val method = newProcess
            ?: error("Shizuku process API unavailable; update Shizuku")
        // Shizuku cannot merge stdout/stderr via ProcessBuilder, so fold stderr
        // into stdout here; otherwise an unread stderr pipe can block the child.
        val process = method.invoke(null, arrayOf("sh", "-c", "$command 2>&1"), null, null) as Process
        return drain(process, RUN_TIMEOUT_SECONDS)
    }
}
