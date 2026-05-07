package com.assetcoach.ai

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest

/**
 * Gemma 4 INT4 모델 가중치 관리.
 *
 * 책임:
 *  1. 모델 파일 존재 여부 + 무결성 (SHA-256) 확인
 *  2. 다운로드 진행 상태 관리
 *  3. 사용 가능 디스크 공간 검사
 *  4. 모델 메타데이터 (버전·크기) 조회
 *
 * Phase 5: 인프라만 — 실제 다운로드 워커는 ModelDownloadWorker (Phase 5+).
 *          MediaPipe LLM Inference 통합은 Phase 5+ — 현재는 GemmaMockClient 사용.
 */
class ModelManager(private val context: Context) {

    private val _state = MutableStateFlow<ModelState>(determineInitialState())
    val state: StateFlow<ModelState> = _state.asStateFlow()

    fun modelFile(): File =
        File(context.filesDir, "gemma4-e4b-int4.task")

    fun modelMetaFile(): File =
        File(context.filesDir, "gemma4-meta.json")

    fun isReady(): Boolean = _state.value is ModelState.Ready

    fun availableSpaceBytes(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.availableBytes
    }

    fun totalSpaceBytes(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.totalBytes
    }

    /**
     * 다운로드 워커가 호출하는 진행 보고 hook.
     */
    fun reportProgress(downloadedBytes: Long, totalBytes: Long) {
        _state.value = ModelState.Downloading(
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes
        )
    }

    fun reportSuccess() {
        _state.value = ModelState.Ready(version = MODEL_VERSION)
    }

    fun reportError(message: String) {
        _state.value = ModelState.Error(message)
    }

    /**
     * SHA-256 해시 검증. Phase 5+ 에서 다운로드 워커가 호출.
     */
    fun verifyHash(expectedHashHex: String): Boolean {
        val file = modelFile()
        if (!file.exists()) return false
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buf = ByteArray(1024 * 64)
            var read: Int
            while (stream.read(buf).also { read = it } > 0) {
                md.update(buf, 0, read)
            }
        }
        val computed = md.digest().joinToString("") { "%02x".format(it) }
        return computed.equals(expectedHashHex, ignoreCase = true)
    }

    fun deleteModel() {
        modelFile().delete()
        modelMetaFile().delete()
        _state.value = ModelState.Absent
    }

    private fun determineInitialState(): ModelState {
        val file = modelFile()
        return if (file.exists() && file.length() >= MIN_VALID_SIZE) {
            ModelState.Ready(version = MODEL_VERSION)
        } else {
            ModelState.Absent
        }
    }

    companion object {
        const val MODEL_VERSION = "gemma4-e4b-int4-1.0"
        const val EXPECTED_SIZE_BYTES = 2_500_000_000L   // ≈ 2.5 GB
        const val MIN_REQUIRED_FREE_SPACE_BYTES = 3_500_000_000L   // 모델 + 1GB 마진
        const val MIN_VALID_SIZE = 100_000_000L           // 100 MB 미만이면 손상
    }
}

sealed class ModelState {
    data object Absent : ModelState()
    data class Downloading(val downloadedBytes: Long, val totalBytes: Long) : ModelState() {
        val progress: Float get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
    }
    data class Ready(val version: String) : ModelState()
    data class Error(val message: String) : ModelState()
}
