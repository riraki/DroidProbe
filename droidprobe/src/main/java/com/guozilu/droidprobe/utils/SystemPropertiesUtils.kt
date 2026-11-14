package com.guozilu.droidprobe.utils

import android.util.Log

object SystemPropertiesUtils {
    private const val TAG: String = "SystemPropertiesUtils"

    init {
        System.loadLibrary("droidprobe")
    }

    /**
     * 类似于 adb shell getprop key 得到的值
     * 本来想用 Runtime.getRuntime().exec(new String[]{"getprop", key}) 来写的
     * 但是发现当 key 为 ro.debuggable 时有 bug，我暂时排查不出来，所以保险起见用了 __system_property_get
     * @param key 要获取的属性
     * @return 获取到的属性
     */
    external fun getprop(key: String): String?

    fun getprop(): String? {
        return runCatching {
            Runtime.getRuntime().exec(arrayOf("getprop")).let { process ->
                try {
                    val output = process.inputStream.bufferedReader().use { it.readText() }
                    process.outputStream.close()
                    process.errorStream.close()

                    process.waitFor()

                    output
                }
                finally {
                    process.destroy()
                }
            }
        }.onFailure {
            Log.e(TAG, "An exception threw in SystemPropertiesUtils.getprop()", it)
        }.getOrNull()
    }
}
