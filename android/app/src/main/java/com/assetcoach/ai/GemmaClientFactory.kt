package com.assetcoach.ai

/**
 * GemmaClient 구현체 선택 — 모델 파일 가용성에 따라 자동 분기.
 *
 * Phase 5 시점:
 *  - Real 통합 (MediaPipe LLM Inference) 미구현 — 항상 Mock 반환
 *  - 향후 GemmaRealClient 가 구현되면 modelManager.isReady() == true 시 자동 사용
 *
 * 호출자(ChatScreen·ChatViewModel) 는 이 팩토리를 통해 인스턴스 받음 → 구현체 모름.
 */
class GemmaClientFactory(
    private val modelManager: ModelManager
) {
    private val mock = GemmaMockClient()

    fun get(): GemmaClient {
        // Phase 5+: 모델 파일 있으면 GemmaRealClient(MediaPipe).
        //          현재는 Mock 만.
        return if (modelManager.isReady()) {
            // GemmaRealClient(modelManager.modelFile())
            mock   // TODO Phase 5+: 실제 통합 시 위 줄로 교체
        } else {
            mock
        }
    }
}
