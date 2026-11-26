package com.nexun.nexres.shell

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class CmdResult(val ok: Boolean, val output: String, val error: String, val exitCode: Int) {
    val detail: String get() = (if (ok) output else error.ifBlank { output }).trim()
}

object Shell {
    fun isRootAvailable(): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val out = p.inputStream.bufferedReader().readText()
        val err = p.errorStream.bufferedReader().readText()
        val ok = p.waitFor(3, TimeUnit.SECONDS) && p.exitValue() == 0 && (out.contains("uid=0") || err.contains("uid=0"))
        runCatching { p.destroy() }; ok
    } catch (_: Throwable) { false }

    fun isShizukuReady(): Boolean = try {
        val binder = Shizuku.pingBinder()
        val perm = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        binder && perm
    } catch (_: Throwable) { false }

    fun run(command: String, backend: com.nexun.nexres.Backend): CmdResult = when (backend) {
        com.nexun.nexres.Backend.SHIZUKU -> runShizuku(command)
        com.nexun.nexres.Backend.ROOT -> runRoot(command)
        else -> CmdResult(false, "", "No backend", -1)
    }

    private fun runRoot(command: String): CmdResult = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        val out = BufferedReader(InputStreamReader(p.inputStream)).readText().trim()
        val err = BufferedReader(InputStreamReader(p.errorStream)).readText().trim()
        val code = if (p.waitFor(15, TimeUnit.SECONDS)) p.exitValue() else { runCatching { p.destroyForcibly() }; -1 }
        val joined = "$out\n$err".trim()
        val ok = code == 0 && !isFailure(joined)
        CmdResult(ok, out, if (ok) err else err.ifBlank { joined }, code)
    } catch (e: Throwable) { CmdResult(false, "", e.message ?: "root error", -1) }

    private fun runShizuku(command: String): CmdResult = try {
        if (!Shizuku.pingBinder()) return CmdResult(false, "", "Shizuku not ready", -1)
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return CmdResult(false, "", "Permission denial: Shizuku", -1)
        val process = newProcess(arrayOf("sh", "-c", command))
        val out = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
        val err = BufferedReader(InputStreamReader(process.errorStream)).readText().trim()
        val code = if (process.waitFor(15, TimeUnit.SECONDS)) process.exitValue() else { runCatching { process.destroyForcibly() }; -1 }
        val joined = "$out\n$err".trim()
        val ok = code == 0 && !isFailure(joined)
        CmdResult(ok, out, if (ok) err else err.ifBlank { joined }, code)
    } catch (e: Throwable) { CmdResult(false, "", e.message ?: "shizuku error", -1) }

    fun isFailure(text: String): Boolean {
        val t = text.lowercase()
        return t.contains("securityexception") || t.contains("permission denial") ||
            t.contains("not allowed") || t.contains("requires android.permission") ||
            t.contains("unknown command") || t.contains("java.lang.securityexception")
    }

    private fun newProcess(cmd: Array<String>): Process {
        val m = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        m.isAccessible = true
        return m.invoke(null, cmd, null, null) as Process
    }
}
