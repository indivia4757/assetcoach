package com.assetcoach.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.Mustard
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.paperNoise

/**
 * 잠금 화면 — 콜드 스타트 시 표시. 생체 인증 통과 후에만 본 화면 진입.
 *
 * 사용 가능한 생체가 있으면 진입 즉시 BiometricPrompt 자동 호출.
 * 없으면 "잠금 해제" 버튼만 — 본 단말 가용성 조회 후 결정.
 */
@Composable
fun LockScreen(onUnlock: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var status by remember { mutableStateOf<UnlockStatus>(UnlockStatus.Idle) }

    val biometricCapable = remember {
        val manager = BiometricManager.from(context)
        manager.canAuthenticate(BIOMETRIC_LEVEL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun startBiometric() {
        if (activity == null) {
            status = UnlockStatus.Error("Activity not available")
            return
        }
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlock()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    status = if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) UnlockStatus.Idle else UnlockStatus.Error(errString.toString())
                }
                override fun onAuthenticationFailed() {
                    status = UnlockStatus.Error("인식되지 않았어요. 다시 시도하세요.")
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("에셋코치 잠금 해제")
            .setSubtitle("생체 인증으로 본인 확인")
            .setNegativeButtonText("취소")
            .setAllowedAuthenticators(BIOMETRIC_LEVEL)
            .build()
        prompt.authenticate(info)
    }

    // Phase 2.5: 자동 prompt 끄고 사용자 탭으로 시작.
    // (자동 prompt 가 콜드 스타트 직후 뜨면 일부 OEM 에서 액티비티가 백그라운드로 가는 케이스 발견)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.s5, vertical = Spacing.s7),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo placeholder — A wordmark
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Mustard),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "A",
                    style = AppType.numXl.copy(
                        color = Ink,
                        fontFamily = GowunBatang,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.height(Spacing.s5))
            Text(
                "에셋코치",
                style = AppType.h1.copy(
                    color = Ink,
                    fontFamily = GowunBatang,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(Spacing.s2))
            Text(
                "🔒 데이터는 이 기기 안에만",
                style = AppType.body.copy(color = ForestSoft, fontFamily = GowunBatang),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.s8))

            when (val s = status) {
                is UnlockStatus.Error -> {
                    Text(
                        s.message,
                        style = AppType.bodySm.copy(color = Terracotta),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = Spacing.s4)
                    )
                }
                else -> {}
            }

            // CTA
            if (biometricCapable) {
                UnlockButton("생체 인증으로 잠금 해제", onClick = ::startBiometric)
            } else {
                UnlockButton("계속하기 (생체 인증 미설정)", onClick = onUnlock)
                Spacer(Modifier.height(Spacing.s2))
                Text(
                    "기기에 생체 인증이 등록되어 있지 않아요. 설정 → 보안 에서 등록하면 더 안전해져요.",
                    style = AppType.caption.copy(color = ForestSoft),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun UnlockButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Terracotta)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.s4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = AppType.bodyLg.copy(
                color = Cream,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private sealed class UnlockStatus {
    data object Idle : UnlockStatus()
    data class Error(val message: String) : UnlockStatus()
}

private const val BIOMETRIC_LEVEL =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or
    BiometricManager.Authenticators.BIOMETRIC_WEAK
