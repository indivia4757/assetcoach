package com.assetcoach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.assetcoach.domain.model.GoalBlock
import com.assetcoach.domain.model.HomeData
import com.assetcoach.domain.model.HomeMockData
import com.assetcoach.domain.model.InsightBlock
import com.assetcoach.domain.model.NarrativeBlock
import com.assetcoach.domain.model.WidgetBlock
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Faint3
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Highlight
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.Mustard
import com.assetcoach.ui.theme.MustardDeep
import com.assetcoach.ui.theme.Neutral
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Positive
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.TerracottaDeep
import com.assetcoach.ui.theme.paperNoise

/**
 * 홈 화면 — 4블록 + Coach CTA 구조 (와이어프레임 §2).
 *
 * data 가 결정하는 변주:
 *  - S06: Score 위젯 (재무 건강도)
 *  - S17: BigNumber 위젯 (자산 잔존 N년) + 자동 은퇴 모드
 *  - S04: Months 위젯 (비상금 개월수)
 */
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    data: HomeData = HomeMockData.s06
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.s5, vertical = Spacing.s2),
            verticalArrangement = Arrangement.spacedBy(Spacing.s5)
        ) {
            item { TopHeader(data.segmentId, onProfileClick, onNotificationClick) }
            item { NarrativeSection(data.narrative) }
            item { HeroWidget(data.widget) }
            if (data.insights.isNotEmpty()) item { InsightRail(data.insights) }
            if (data.goals.isNotEmpty()) item { GoalsCard(data.goals) }
            item { CoachCta(data.ctaText) }
            item { Spacer(Modifier.height(Spacing.s7)) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// Header — 좌상 프로필(세그먼트 토글), 우상 알림(모드 토글)
// ─────────────────────────────────────────────────────────
@Composable
private fun TopHeader(
    segmentLabel: String,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s5, bottom = Spacing.s2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconCircle(symbol = segmentLabel.takeLast(2), onClick = onProfileClick)
        Text(
            text = "에셋코치",
            style = AppType.bodyLg.copy(
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
        )
        IconCircle(symbol = "🔔", onClick = onNotificationClick)
    }
}

@Composable
private fun IconCircle(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CreamDeep)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = AppType.bodySm.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

// ─────────────────────────────────────────────────────────
// Block 1 — Narrative
// ─────────────────────────────────────────────────────────
@Composable
private fun NarrativeSection(n: NarrativeBlock) {
    Column {
        Text(
            text = n.eyebrow,
            style = AppType.caption.copy(color = MustardDeep, fontWeight = FontWeight.Medium)
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            text = buildAnnotatedString {
                append(n.firstLine)
                withStyle(SpanStyle(
                    fontStyle = FontStyle.Italic,
                    color = TerracottaDeep,
                    background = Highlight
                )) { append(n.highlight) }
                append(n.tail)
            },
            style = AppType.narrative.copy(color = Forest)
        )
    }
}

// ─────────────────────────────────────────────────────────
// Block 2 — Hero Widget (3 변주)
// ─────────────────────────────────────────────────────────
@Composable
private fun HeroWidget(w: WidgetBlock) {
    when (w) {
        is WidgetBlock.Score -> ScoreWidget(w)
        is WidgetBlock.BigNumber -> BigNumberWidget(w)
        is WidgetBlock.Months -> MonthsWidget(w)
    }
}

@Composable
private fun ScoreWidget(w: WidgetBlock.Score) {
    Column(
        modifier = widgetCardModifier()
    ) {
        Text(w.eyebrow, style = AppType.caption.copy(color = MustardDeep))
        Spacer(Modifier.height(Spacing.s4))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(w.score.toString(), style = AppType.numXl.copy(color = Ink))
            Spacer(Modifier.width(Spacing.s1))
            Text(
                text = "점",
                style = AppType.numLg.copy(color = ForestSoft),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(Spacing.s3))
        LinearGauge(progress = w.gaugeProgress, color = Terracotta)
        Spacer(Modifier.height(Spacing.s4))
        Text(
            text = buildAnnotatedString {
                append("S06 평균 ")
                withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.Bold)) {
                    append(w.benchmarkBoldText)
                }
                append(w.benchmarkSuffixText)
            },
            style = AppType.body.copy(fontFamily = GowunBatang, color = ForestSoft),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BigNumberWidget(w: WidgetBlock.BigNumber) {
    Column(
        modifier = widgetCardModifier()
    ) {
        Text(w.eyebrow, style = AppType.caption.copy(color = MustardDeep))
        Spacer(Modifier.height(Spacing.s5))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                w.number,
                style = AppType.numXl.copy(color = Ink)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                w.unit,
                style = AppType.numLg.copy(color = ForestSoft),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(Spacing.s3))
        LinearGauge(
            progress = w.gaugeProgress,
            color = if (w.gaugeColorMustard) MustardDeep else Terracotta,
            height = 8.dp
        )
        Spacer(Modifier.height(Spacing.s2))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(w.leftCaption, style = AppType.caption.copy(color = Neutral))
            Text(w.rightCaption, style = AppType.caption.copy(color = Neutral))
        }
        Spacer(Modifier.height(Spacing.s4))
        Text(
            text = w.benchmarkText,
            style = AppType.body.copy(fontFamily = GowunBatang, color = Forest),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        if (w.ctaPrimary != null) {
            Spacer(Modifier.height(Spacing.s4))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.r3))
                    .background(Terracotta)
                    .padding(vertical = Spacing.s4),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    w.ctaPrimary,
                    style = AppType.bodyLg.copy(
                        color = Cream,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun MonthsWidget(w: WidgetBlock.Months) {
    Column(
        modifier = widgetCardModifier()
    ) {
        Text(w.eyebrow, style = AppType.caption.copy(color = MustardDeep))
        Spacer(Modifier.height(Spacing.s4))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(w.current, style = AppType.numXl.copy(color = Ink))
            Spacer(Modifier.width(6.dp))
            Text(
                w.unit,
                style = AppType.numLg.copy(color = ForestSoft),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(Spacing.s4))

        // Two-tone proportional bar: current vs (recommended - current)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(w.currentMonths.coerceAtLeast(0.1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Terracotta)
            )
            Spacer(Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight((w.recommendedMonths - w.currentMonths).coerceAtLeast(0.1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Faint)
            )
        }
        Spacer(Modifier.height(Spacing.s2))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(w.leftCaption, style = AppType.caption.copy(color = Neutral))
            Text(w.rightCaption, style = AppType.caption.copy(color = Neutral))
        }
        Spacer(Modifier.height(Spacing.s4))
        Text(
            text = w.benchmarkText,
            style = AppType.body.copy(fontFamily = GowunBatang, color = ForestSoft),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun widgetCardModifier(): Modifier = Modifier
    .fillMaxWidth()
    .clip(RoundedCornerShape(Radius.r4))
    .background(Paper)
    .border(1.dp, Mustard, RoundedCornerShape(Radius.r4))
    .padding(Spacing.s5)

@Composable
private fun LinearGauge(
    progress: Float,
    color: Color,
    height: Dp = 6.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(Faint)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

// ─────────────────────────────────────────────────────────
// Block 3 — Insight Rail
// ─────────────────────────────────────────────────────────
@Composable
private fun InsightRail(insights: List<InsightBlock>) {
    Column {
        SectionHeader(name = "이번 주 인사이트", meta = "${insights.size} / 7")
        Spacer(Modifier.height(Spacing.s3))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s3)) {
            items(insights) { card ->
                InsightCard(card)
            }
        }
    }
}

@Composable
private fun SectionHeader(name: String, meta: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            name,
            style = AppType.bodyLg.copy(
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
        )
        Text(meta, style = AppType.caption.copy(color = Neutral))
    }
}

@Composable
private fun InsightCard(card: InsightBlock) {
    val accent = when (card.accent) {
        InsightBlock.AccentKind.Terracotta -> Terracotta
        InsightBlock.AccentKind.Positive -> Positive
        InsightBlock.AccentKind.Mustard -> MustardDeep
    }
    Row(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(Radius.r3))
            .background(Paper)
            .border(1.dp, Faint, RoundedCornerShape(Radius.r3))
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accent)
        )
        Column(modifier = Modifier.padding(Spacing.s4)) {
            Text(card.eyebrow, style = AppType.caption.copy(color = MustardDeep))
            Spacer(Modifier.height(Spacing.s1))
            Text(
                card.head,
                style = AppType.bodyLg.copy(
                    fontFamily = GowunBatang,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            )
            Spacer(Modifier.height(Spacing.s2))
            Text(
                card.body,
                style = AppType.bodySm.copy(color = ForestSoft)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// Block 4 — Goals
// ─────────────────────────────────────────────────────────
@Composable
private fun GoalsCard(goals: List<GoalBlock>) {
    Column {
        SectionHeader(name = "진행 중인 목표", meta = "${goals.size}개")
        Spacer(Modifier.height(Spacing.s2))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.r3))
                .background(Paper)
                .border(1.dp, Faint, RoundedCornerShape(Radius.r3))
                .padding(horizontal = Spacing.s4, vertical = Spacing.s3)
        ) {
            goals.forEachIndexed { idx, goal ->
                GoalRow(goal)
                if (idx < goals.lastIndex) {
                    HorizontalDivider(
                        color = Faint3,
                        modifier = Modifier.padding(vertical = Spacing.s2)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalRow(g: GoalBlock) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CreamDeep),
            contentAlignment = Alignment.Center
        ) {
            Text(g.emoji, style = AppType.body)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                g.title,
                style = AppType.bodySm.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Forest
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(g.meta, style = AppType.caption.copy(color = Neutral))
            Spacer(Modifier.height(6.dp))
            LinearGauge(
                progress = g.progress,
                color = if (g.mustard) MustardDeep else Terracotta,
                height = 5.dp
            )
        }
        Text(
            g.percentLabel,
            style = AppType.bodySm.copy(
                fontWeight = FontWeight.Bold,
                color = Forest
            )
        )
    }
}

// ─────────────────────────────────────────────────────────
// Coach CTA
// ─────────────────────────────────────────────────────────
@Composable
private fun CoachCta(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Terracotta)
            .padding(horizontal = Spacing.s4, vertical = Spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
    ) {
        Text("💬", style = AppType.bodyLg)
        Text(
            text = text,
            style = AppType.body.copy(
                color = Cream,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )
        Text("→", style = AppType.bodyLg.copy(color = Cream))
    }
}

