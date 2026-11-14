package com.guozilu.droidprobe.root

import android.util.Log
import com.guozilu.droidprobe.core.DetectionEvidence
import java.io.File

/**
 * 检查 su 文件存不存在以及是否有可执行权限，并尝试执行 su -c id 能不能得到类似于
 * uid=0(root) gid=0(root) groups=0(root) context=u:r:magisk:s0 的结果
 */
class SuDetector : AbstractRootBinaryDetector(
    "su",
    "su") {

    /**
     * 检查这个 binary 能不能执行，以及执行的结果是什么
     * @param rootBinaryFile 与 root 有关的二进制文件句柄
     * @return 如果能执行，执行的结果符合 root，那么返回中间采集的证据，如果都不满足返回空的 List
     */
    override fun detectBinaryExecutionEvidences(rootBinaryFile: File): List<DetectionEvidence> {
        return buildList {
            if (rootBinaryFile.canExecute()) {
                add(DetectionEvidence(
                    "SU_EXECUTION",
                    rootBinaryFile.absolutePath + " can execute",
                    "发现可执行的 su 文件"
                ))
                executeBinary()?.let {
                    add(DetectionEvidence(
                    "SU_EXECUTED",
                    it,
                    "执行 su -c id 后 $it"
                )) }
            }
        }
    }

    /**
     * 执行 su -c id 看看能不能得到类似于
     * uid=0(root) gid=0(root) groups=0(root) context=u:r:magisk:s0 的结果
     * @return 如果能正常执行就返回得到的结果，否则返回 null
     */
    override fun executeBinary(): String? {
        return runCatching {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id")).let { process ->
                val output = process.inputStream.bufferedReader().use { reader ->
                    process.errorStream.bufferedReader().use { errorReader ->
                        // 如果用户拒绝了权限，那么 result 就应该是 null，error 是 Permission denied
                        // 如果用户允许了，那么 result 就应该是 uid=0(root) gid=0(root) groups=0(root) context=u:r:magisk:s0
                        // error 是 null
                        reader.readLine() ?: errorReader.readLine()
                        ?: "command out: null, error message: null"
                        // 按道理来说是执行不到这里的，这两个不可能都是 null
                    }
                }
                process.outputStream.close()

                process.waitFor()

                output
            }
        }.onFailure {
            Log.e(TAG, "An exception threw in SuDetector.executeBinary()", it)
        }.getOrNull()
    }

    companion object {
        private const val TAG: String = "SuDetector"
    }
}
