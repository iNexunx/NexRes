package com.nexun.nexres.display

data class DisplayState(
    val physicalWidth: Int,
    val physicalHeight: Int,
    val overrideWidth: Int?,
    val overrideHeight: Int?,
    val physicalDensity: Int,
    val overrideDensity: Int?,
) {
    val width: Int get() = overrideWidth ?: physicalWidth
    val height: Int get() = overrideHeight ?: physicalHeight
    val density: Int get() = overrideDensity ?: physicalDensity
    val hasOverride: Boolean get() = overrideWidth != null || overrideHeight != null || overrideDensity != null
}

/**
 * Reads and writes the display size/density through `wm`, which always runs
 * through the privileged backend supplied as [run].
 */
object DisplayController {

    private val sizeRegex = Regex("""(\d+)\s*x\s*(\d+)""")
    private val densityRegex = Regex("""(\d+)""")

    fun read(run: (String) -> String): DisplayState {
        val sizeOutput = run("wm size")
        val densityOutput = run("wm density")

        val physical = sizeRegex.find(sizeOutput.substringBefore("Override"))?.let {
            it.groupValues[1].toInt() to it.groupValues[2].toInt()
        } ?: (0 to 0)

        val override = sizeOutput.lineSequence()
            .firstOrNull { it.contains("Override") }
            ?.let { sizeRegex.find(it) }
            ?.let { it.groupValues[1].toInt() to it.groupValues[2].toInt() }

        val physicalDensity = densityRegex.find(
            densityOutput.substringBefore("Override").substringAfter("density"),
        )?.groupValues?.get(1)?.toInt() ?: 0

        val overrideDensity = densityOutput.lineSequence()
            .firstOrNull { it.contains("Override") }
            ?.let { densityRegex.find(it) }
            ?.groupValues?.get(1)?.toInt()

        return DisplayState(
            physicalWidth = physical.first,
            physicalHeight = physical.second,
            overrideWidth = override?.first,
            overrideHeight = override?.second,
            physicalDensity = physicalDensity,
            overrideDensity = overrideDensity,
        )
    }

    fun setSize(run: (String) -> String, width: Int, height: Int) {
        run("wm size ${width}x$height")
    }

    fun resetSize(run: (String) -> String) {
        run("wm size reset")
    }

    fun setDensity(run: (String) -> String, density: Int) {
        run("wm density $density")
    }

    fun resetDensity(run: (String) -> String) {
        run("wm density reset")
    }
}
