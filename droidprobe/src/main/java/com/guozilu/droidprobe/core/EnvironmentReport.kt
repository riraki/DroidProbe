package com.guozilu.droidprobe.core

class EnvironmentReport(
    private val timestamp: Long,
    private val results: MutableList<DetectionResult>) {

    fun getDetectedResults(): MutableList<DetectionResult> {
        return results.filter { it.status == DetectionStatus.DETECTED }.toMutableList()
    }

    fun getResults(category: DetectionCategory): MutableList<DetectionResult> {
        return results.filter { it.category == category }.toMutableList()
    }

    override fun toString(): String {
        return "EnvironmentReport{timestamp=$timestamp" +
                ", results=$results}"
    }

    companion object {
        private const val TAG = "EnvironmentReport"
    }
}
