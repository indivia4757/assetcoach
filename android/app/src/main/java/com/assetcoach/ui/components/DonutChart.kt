package com.assetcoach.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.Neutral

/**
 * 도넛 차트 (Compose Canvas 직접 그리기).
 *
 * 디자인 시스템 §3-1: 외곽 24, 내경 12 비율 / 무채색 그라데이션 / 컬러 의미 부여 X.
 * 카테고리 라벨은 별도 legend 컴포넌트로.
 */
data class DonutSegment(
    val label: String,
    val value: Long,
    val emoji: String = ""
)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    centerLabel: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val total = segments.sumOf { it.value }.coerceAtLeast(1L)
    Box(
        modifier = modifier
            .size(220.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 36.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // 배경 트랙
            drawArc(
                color = Faint,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // 무채색 그라데이션 — 진한 forest → neutral
            var startAngle = -90f
            val palette = listOf(
                Color(0xFF1C2A1E),  // forest
                Color(0xFF2E3F30),  // forest-soft
                Color(0xFF5C6B5E),  // mid
                Color(0xFF8B8478),  // neutral
                Color(0xFFA8A399),  // light neutral
                Color(0xFFC9C5BC)   // very light
            )
            segments.forEachIndexed { idx, seg ->
                val sweep = (seg.value.toFloat() / total) * 360f
                val color = palette[idx % palette.size]
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 1.2f,  // 미세 갭
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                centerLabel,
                style = AppType.caption.copy(color = Neutral)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                centerValue,
                style = AppType.numLg.copy(color = Ink)
            )
        }
    }
}

@Composable
fun DonutLegend(
    segments: List<DonutSegment>,
    formatValue: (Long) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEachIndexed { idx, seg ->
            val palette = listOf(
                Color(0xFF1C2A1E),
                Color(0xFF2E3F30),
                Color(0xFF5C6B5E),
                Color(0xFF8B8478),
                Color(0xFFA8A399),
                Color(0xFFC9C5BC)
            )
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette[idx % palette.size])
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    "${seg.emoji} ${seg.label}",
                    style = AppType.bodySm.copy(color = Forest),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatValue(seg.value),
                    style = AppType.bodySm.copy(color = ForestSoft)
                )
            }
        }
    }
}

