package com.nexun.nexres.display

import com.nexun.nexres.Backend
import com.nexun.nexres.shell.CmdResult
import com.nexun.nexres.shell.Shell

data class DisplayInfo(
    val physicalWidth: Int, val physicalHeight: Int, val physicalDensity: Int,
    val currentWidth: Int, val currentHeight: Int, val currentDensity: Int,
) { val isOverride: Boolean get() = currentWidth != physicalWidth || currentHeight != physicalHeight || currentDensity != physicalDensity }

object DisplayController {
    private val sizeRx = Regex("""(\d+)x(\d+)""")
    private val denRx = Regex("""(\d+)""")

    fun read(backend: Backend): DisplayInfo? {
        val size = shellOut(backend, "wm size", "cmd window size")
        val dens = shellOut(backend, "wm density", "cmd window density")
        if (size.isBlank() && dens.isBlank()) return null
        val physSize = sizeRx.find(size.substringBefore("Override")) ?: sizeRx.find(size)
        val curSize = sizeRx.findAll(size).lastOrNull() ?: physSize
        val physDen = denRx.find(dens.substringBefore("Override").substringAfter("density", "")) ?: denRx.find(dens)
        val curDen = denRx.findAll(dens).lastOrNull() ?: physDen
        return DisplayInfo(
            physicalWidth = physSize?.groupValues?.get(1)?.toIntOrNull() ?: 0,
            physicalHeight = physSize?.groupValues?.get(2)?.toIntOrNull() ?: 0,
            physicalDensity = physDen?.groupValues?.get(1)?.toIntOrNull() ?: 0,
            currentWidth = curSize?.groupValues?.get(1)?.toIntOrNull() ?: 0,
            currentHeight = curSize?.groupValues?.get(2)?.toIntOrNull() ?: 0,
            currentDensity = curDen?.groupValues?.get(1)?.toIntOrNull() ?: 0,
        )
    }

    fun apply(backend: Backend, w: Int, h: Int, d: Int): CmdResult {
        val r1 = runCmd(backend, listOf("wm size ${w}x${h}", "cmd window size ${w}x${h}"))
        val r2 = runCmd(backend, listOf("wm density $d", "cmd window density $d"))
        return combine(r1, r2)
    }

    fun reset(backend: Backend): CmdResult {
        val r1 = runCmd(backend, listOf("wm size reset", "cmd window size reset"))
        val r2 = runCmd(backend, listOf("wm density reset", "cmd window density reset"))
        return combine(r1, r2)
    }

    private fun combine(r1: CmdResult, r2: CmdResult): CmdResult = when {
        r1.ok && r2.ok -> CmdResult(true, "${r1.output}\n${r2.output}".trim(), "", 0)
        !r1.ok -> r1
        else -> r2
    }

    private fun runCmd(backend: Backend, commands: List<String>): CmdResult {
        var last: CmdResult? = null
        for (cmd in commands) {
            val r = Shell.run(cmd, backend)
            if (r.ok) return r
            last = r
        }
        return last ?: CmdResult(false, "", "No command executed", -1)
    }

    private fun shellOut(backend: Backend, primary: String, fallback: String): String {
        val a = Shell.run(primary, backend)
        if (a.ok && a.output.isNotBlank()) return a.output
        val b = Shell.run(fallback, backend)
        return if (b.output.isNotBlank()) b.output else a.output
    }
}
