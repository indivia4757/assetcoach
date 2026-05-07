package com.assetcoach.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assetcoach.ai.ModelState
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Faint3
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.Mustard
import com.assetcoach.ui.theme.MustardDeep
import com.assetcoach.ui.theme.Negative
import com.assetcoach.ui.theme.Neutral
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Positive
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.paperNoise

/**
 * 모델 다운로드 동의·진행 화면 — 와이어프레임 §1.7-A / §1.7-B 기반.
 *
 * 4 상태 분기:
 *  - Absent: consent 화면 (필요 공간·시간·사용 가능 / 다운로드·모바일 데이터·샘플 모드)
 *  - Downloading: 진행 게이지 + 남은 시간
 *  - Ready: 완료 + "시작하기"
 *  - Error: 에러 + 재시도
 */
@Composable
fun ModelDownloadScreen(
    state: ModelState,
    availableSpaceBytes: Long,
    requiredBytes: Long = 2_500_000_000L,
    onDownloadWiFi: () -> Unit,
    onDownloadCellular: () -> Unit,
    onSampleMode: () -> Unit,
    onRetry: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.s5, vertical = Spacing.s7)
        ) {
            when (state) {
                is ModelState.Absent -> ConsentContent(
                    availableSpaceBytes = availableSpaceBytes,
                    requiredBytes = requiredBytes,
                    onDownloadWiFi = onDownloadWiFi,
                    onDownloadCellular = onDownloadCellular,
                    onSampleMode = onSampleMode
                )
                is ModelState.Downloading -> ProgressContent(state)
                is ModelState.Ready -> ReadyContent(onContinue = onContinue)
                is ModelState.Error -> ErrorContent(state.message, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun ColumnScopeBoxLikeColumn(
    block: @Composable () -> Unit
) {
    Column { block() }
}

@Composable
private fun ConsentContent(
    availableSpaceBytes: Long,
    requiredBytes: Long,
    onDownloadWiFi: () -> Unit,
    onDownloadCellular: () -> Unit,
    onSampleMode: () -> Unit
) {
    val sufficient = availableSpaceBytes >= requiredBytes + 1_000_000_000L

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "AI 코치를\n준비할게요",
            style = AppType.h1.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            "기기 안에서만 작동하는 대화형 코치를 받아옵니다. 한 번만 받고 나면 외부 통신 없이 작동해요.",
            style = AppType.body.copy(color = ForestSoft)
        )
        Spacer(Modifier.height(Spacing.s5))

        // 정보 카드 — 필요 공간 / 시간 / 사용 가능
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.r3))
                .background(Paper)
                .border(1.dp, Mustard, RoundedCornerShape(Radius.r3))
                .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
        ) {
            InfoRow(
                emoji = "📦",
                label = "필요한 공간",
                value = formatGB(requiredBytes),
                accent = false
            )
            DividerLine()
            InfoRow(
                emoji = "📡",
                label = "예상 다운로드",
                value = "Wi-Fi · 5~10분",
                accent = false
            )
            DividerLine()
            InfoRow(
                emoji = "💾",
                label = "사용 가능 공간",
                value = formatGB(availableSpaceBytes) + " / " + formatGB(requiredBytes * 50) +
                    if (sufficient) " ✓" else " ⚠",
                accent = !sufficient
            )
        }

        Spacer(Modifier.height(Spacing.s4))

        // "왜 이렇게 클까요?" expander
        Expander(title = "왜 이렇게 클까요?") {
            Text(
                "모든 분석을 외부 서버 없이 본인 기기에서 직접 하기 위한 AI 모델이에요. " +
                "한 번만 받고 나면 거래 데이터·대화 내용 모두 기기를 떠나지 않습니다.",
                style = AppType.bodySm.copy(color = ForestSoft)
            )
        }

        Spacer(Modifier.weight(1f))

        // CTAs
        if (sufficient) {
            PrimaryButton("Wi-Fi에서 다운로드", onClick = onDownloadWiFi)
        } else {
            PrimaryButton("공간 정리 가이드", onClick = {})
        }
        Spacer(Modifier.height(Spacing.s2))
        TertiaryButton("모바일 데이터로 진행", onClick = onDownloadCellular)
        TertiaryButton("샘플로 먼저 둘러보기 ↓", onClick = onSampleMode)
    }
}

@Composable
private fun ProgressContent(state: ModelState.Downloading) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(Spacing.s7))
        Text(
            "AI 코치를\n받아오는 중",
            style = AppType.h1.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            "완료되면 첫 투어로 안내해 드릴게요.",
            style = AppType.body.copy(color = ForestSoft)
        )

        Spacer(Modifier.height(Spacing.s7))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.r3))
                .background(Paper)
                .border(1.dp, Mustard, RoundedCornerShape(Radius.r3))
                .padding(Spacing.s5),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PROGRESS",
                style = AppType.caption.copy(color = Neutral)
            )
            Spacer(Modifier.height(Spacing.s2))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "%.1f".format(state.downloadedBytes / 1e9),
                    style = AppType.numXl.copy(color = Ink)
                )
                Spacer(Modifier.width(Spacing.s1))
                Text(
                    "/ ${formatGB(state.totalBytes)}",
                    style = AppType.numLg.copy(color = ForestSoft),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(Modifier.height(Spacing.s4))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Faint)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Terracotta)
                )
            }
            Spacer(Modifier.height(Spacing.s2))
            Text(
                "${(state.progress * 100).toInt()}% · 남은 시간 약 ${estimatedRemaining(state)}",
                style = AppType.caption.copy(color = Neutral)
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            "완료까지 백그라운드으로 진행돼요.\n다른 화면 보시거나 잠시 닫으셔도 돼요.",
            style = AppType.body.copy(fontFamily = GowunBatang, color = ForestSoft),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(Spacing.s5))
    }
}

@Composable
private fun ReadyContent(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Positive),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", style = AppType.numXl.copy(color = Cream, fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.height(Spacing.s5))
        Text(
            "AI 코치 준비 완료",
            style = AppType.h1.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s4))
        Text(
            "이제 함께 거래 데이터를 보면서\n코치와 대화할 수 있어요.",
            style = AppType.bodyLg.copy(fontFamily = GowunBatang, color = ForestSoft),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.s7))
        Box(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton("시작하기", onClick = onContinue)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠", style = AppType.numXl.copy(color = Negative))
        Spacer(Modifier.height(Spacing.s4))
        Text(
            "다운로드에 문제가 있어요",
            style = AppType.h2.copy(color = Forest, fontFamily = GowunBatang)
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            message,
            style = AppType.bodySm.copy(color = ForestSoft),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.s5))
        Box(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton("다시 시도", onClick = onRetry)
        }
    }
}

// ─────────────────────────────────────────────────────────
// Reusable components
// ─────────────────────────────────────────────────────────
@Composable
private fun InfoRow(emoji: String, label: String, value: String, accent: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.s3)
    ) {
        Text(emoji, style = AppType.bodyLg)
        Spacer(Modifier.width(Spacing.s3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label.uppercase(),
                style = AppType.caption.copy(color = Neutral)
            )
            Text(
                value,
                style = AppType.numLg.copy(
                    color = if (accent) Negative else Ink,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Faint3)
    )
}

@Composable
private fun Expander(title: String, content: @Composable () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(CreamDeep)
            .clickable { open = !open }
            .padding(horizontal = Spacing.s4, vertical = Spacing.s3)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = AppType.body.copy(
                    color = Forest,
                    fontFamily = GowunBatang,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
            Text(if (open) "▴" else "▾", style = AppType.body.copy(color = Forest))
        }
        AnimatedVisibility(open) {
            Box(modifier = Modifier.padding(top = Spacing.s2)) { content() }
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
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
            style = AppType.bodyLg.copy(color = Cream, fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun TertiaryButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.s3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = AppType.body.copy(color = ForestSoft)
        )
    }
}

private fun formatGB(bytes: Long): String =
    "%.1f GB".format(bytes / 1e9)

private fun estimatedRemaining(s: ModelState.Downloading): String {
    if (s.progress <= 0f || s.progress >= 1f) return "0분"
    // Wi-Fi 가정 — 분당 250MB. 매우 단순한 예측.
    val remainingBytes = s.totalBytes - s.downloadedBytes
    val minutesLeft = (remainingBytes / 250_000_000.0).coerceAtLeast(0.1)
    return when {
        minutesLeft < 1 -> "1분 미만"
        else -> "${minutesLeft.toInt()}분"
    }
}
