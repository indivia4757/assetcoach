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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assetcoach.AssetCoachApp
import com.assetcoach.data.db.entity.TransactionEntity
import com.assetcoach.ui.components.DonutChart
import com.assetcoach.ui.components.DonutLegend
import com.assetcoach.ui.components.DonutSegment
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.MustardDeep
import com.assetcoach.ui.theme.Neutral
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Positive
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.paperNoise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class AnalysisTab(val label: String) {
    Spend("지출"), Income("수입"), Asset("자산"), Subs("구독"), Budget("예산")
}

@Composable
fun AnalysisScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as AssetCoachApp
    val vm: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory(app.transactionRepository))
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(AnalysisTab.Spend) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.s5, vertical = Spacing.s5),
            verticalArrangement = Arrangement.spacedBy(Spacing.s4)
        ) {
            item { Header() }
            item { TabStrip(active = tab, onChange = { tab = it }) }

            when (tab) {
                AnalysisTab.Spend -> spendItems(state)
                AnalysisTab.Income -> placeholder("수입 흐름은 거래 데이터가 더 모이면 표시돼요.")
                AnalysisTab.Asset -> placeholder("자산 정보를 입력하시면 구성을 보여드릴게요.")
                AnalysisTab.Subs -> subsItems(state)
                AnalysisTab.Budget -> placeholder("예산을 설정하면 카테고리별 잔여를 추적해요.")
            }

            item { Spacer(Modifier.height(Spacing.s7)) }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(top = Spacing.s5, bottom = Spacing.s2)) {
        Text(
            "분석",
            style = AppType.h1.copy(color = Forest, fontFamily = GowunBatang, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(Spacing.s1))
        Text(
            "거래·지출 흐름",
            style = AppType.body.copy(color = ForestSoft)
        )
    }
}

@Composable
private fun TabStrip(active: AnalysisTab, onChange: (AnalysisTab) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
        items(AnalysisTab.entries) { t ->
            val selected = active == t
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) Forest else CreamDeep)
                    .clickable { onChange(t) }
                    .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
            ) {
                Text(
                    t.label,
                    style = AppType.bodySm.copy(
                        color = if (selected) Cream else Forest,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.spendItems(state: AnalysisUiState) {
    item { TotalSpendCard(state.totalSpend, state.transactions.size) }

    if (state.categoryTotals.isNotEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.r4))
                    .background(Paper)
                    .border(1.dp, Faint, RoundedCornerShape(Radius.r4))
                    .padding(Spacing.s4),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DonutChart(
                        segments = state.categoryTotals.take(6).map {
                            DonutSegment(
                                label = it.category.name,
                                value = -it.amount,
                                emoji = it.category.emoji
                            )
                        },
                        centerLabel = "이번 달 지출",
                        centerValue = formatCompact(-state.totalSpend)
                    )
                    Spacer(Modifier.height(Spacing.s4))
                    DonutLegend(
                        segments = state.categoryTotals.take(6).map {
                            DonutSegment(
                                label = it.category.name,
                                value = -it.amount,
                                emoji = it.category.emoji
                            )
                        },
                        formatValue = { formatWon(it) }
                    )
                }
            }
        }
    }

    if (state.transactions.isNotEmpty()) {
        item { SectionHeader("거래 내역", "${state.transactions.size}건") }
        items(state.transactions, key = { it.id }) { tx ->
            TransactionRow(
                tx = tx,
                emoji = state.categoriesById[tx.categoryId]?.emoji ?: "🔘",
                categoryName = state.categoriesById[tx.categoryId]?.name ?: "기타"
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.subsItems(state: AnalysisUiState) {
    val subs = state.transactions.filter {
        state.categoriesById[it.categoryId]?.name == "구독"
    }
    if (subs.isEmpty()) {
        item {
            placeholderInline("등록된 구독이 아직 없어요. 거래 데이터가 모이면 자동 감지돼요.")
        }
        return
    }

    val total = -subs.sumOf { it.amount }
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.r3))
                .background(Paper)
                .border(1.dp, Faint, RoundedCornerShape(Radius.r3))
                .padding(Spacing.s4)
        ) {
            Column {
                Text("활성 구독", style = AppType.caption.copy(color = MustardDeep))
                Spacer(Modifier.height(Spacing.s1))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${subs.size}건", style = AppType.numLg.copy(color = Ink))
                    Spacer(Modifier.size(Spacing.s2))
                    Text(
                        "월 ${formatWon(total)}",
                        style = AppType.body.copy(color = ForestSoft),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }

    item { SectionHeader("전체 활성", "${subs.size}건") }
    items(subs, key = { it.id }) { tx ->
        TransactionRow(
            tx = tx,
            emoji = state.categoriesById[tx.categoryId]?.emoji ?: "📺",
            categoryName = "구독"
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.placeholder(message: String) {
    item {
        placeholderInline(message)
    }
}

@Composable
private fun placeholderInline(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.s7),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✦", style = AppType.numXl.copy(color = MustardDeep))
            Spacer(Modifier.height(Spacing.s2))
            Text(
                message,
                style = AppType.body.copy(fontFamily = GowunBatang, color = ForestSoft),
                modifier = Modifier.padding(horizontal = Spacing.s5),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TotalSpendCard(totalSpend: Long, count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r4))
            .background(Paper)
            .border(1.dp, Faint, RoundedCornerShape(Radius.r4))
            .padding(Spacing.s5)
    ) {
        Text("이번 달 총 지출", style = AppType.caption.copy(color = MustardDeep))
        Spacer(Modifier.height(Spacing.s2))
        Text(formatWon(-totalSpend), style = AppType.numXl.copy(color = Ink))
        Spacer(Modifier.height(Spacing.s2))
        Text("$count 건", style = AppType.caption.copy(color = Neutral))
    }
}

@Composable
private fun SectionHeader(name: String, meta: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.s2, bottom = Spacing.s1),
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
private fun TransactionRow(tx: TransactionEntity, emoji: String, categoryName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r2))
            .background(Paper)
            .border(1.dp, Faint, RoundedCornerShape(Radius.r2))
            .padding(horizontal = Spacing.s3, vertical = Spacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(Radius.r2))
                .background(CreamDeep),
            contentAlignment = Alignment.Center
        ) { Text(emoji, style = AppType.bodyLg) }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                tx.merchantNorm.ifBlank { tx.rawText },
                style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = Forest)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${formatDate(tx.ts)} · $categoryName",
                style = AppType.caption.copy(color = Neutral)
            )
        }
        Text(
            formatWon(tx.amount),
            style = AppType.num.copy(color = if (tx.amount < 0) Forest else Positive)
        )
    }
}

private val dateFmt = SimpleDateFormat("M월 d일 HH:mm", Locale.KOREA).apply {
    timeZone = TimeZone.getTimeZone("Asia/Seoul")
}

private fun formatDate(epoch: Long): String = dateFmt.format(Date(epoch))

private fun formatWon(amount: Long): String {
    val sign = if (amount < 0) "−" else ""
    val abs = kotlin.math.abs(amount)
    return when {
        abs >= 100_000_000 -> "$sign%.1f억원".format(abs / 100_000_000.0)
        abs >= 10_000 -> "$sign%,d만원".format(abs / 10_000)
        else -> "$sign%,d원".format(abs)
    }
}

private fun formatCompact(amount: Long): String {
    val abs = kotlin.math.abs(amount)
    return when {
        abs >= 100_000_000 -> "%.1f억".format(abs / 100_000_000.0)
        abs >= 10_000 -> "%,d만".format(abs / 10_000)
        else -> "%,d".format(abs)
    }
}
