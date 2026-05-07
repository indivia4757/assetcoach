package com.assetcoach.data.sms

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.assetcoach.AssetCoachApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Phase 4 — 카드사 알림만 분석하는 NotificationListenerService.
 *
 * 정책:
 *  - cardIssuerWhitelist 외 알림은 무시·저장 안 함
 *  - 정규식 매칭 성공 → 거래 DB 자동 추가
 *  - 정규식 실패 → LLM 폴백 (Phase 3+ A4 호출) 큐에 enqueue
 *
 * AndroidManifest.xml 에 등록 필요 + 사용자가 시스템 설정에서 권한 허용.
 * 본 클래스는 Phase 4 골격 — 실제 활성화는 사용자가 수동으로 권한 부여 후.
 */
class AssetCoachNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (!isMessagingPackage(pkg)) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val combined = "$title\n$text".trim()

        if (!SmsParser.isFromKnownIssuer(combined) && !SmsParser.isFromKnownIssuer(title)) return

        scope.launch {
            try {
                val tx = SmsParser.parse(combined)
                if (tx != null) {
                    val app = applicationContext as? AssetCoachApp ?: return@launch
                    app.database.transactionDao().insertAll(listOf(tx))
                    Log.i(TAG, "Saved tx via SMS: ${tx.merchantNorm} ${tx.amount}")
                } else {
                    // Phase 3 후속: LLM 폴백 큐에 enqueue
                    Log.w(TAG, "SMS parse failed, would enqueue LLM fallback: $combined")
                }
            } catch (e: Exception) {
                Log.e(TAG, "SMS handling error", e)
            }
        }
    }

    private fun isMessagingPackage(pkg: String): Boolean = pkg in setOf(
        "com.android.mms",
        "com.samsung.android.messaging",
        "com.google.android.apps.messaging",
        "com.android.messaging"
    )

    companion object {
        private const val TAG = "AC.NotifListener"
    }
}
