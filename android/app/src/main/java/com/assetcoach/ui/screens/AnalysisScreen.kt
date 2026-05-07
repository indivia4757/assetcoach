package com.assetcoach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.assetcoach.ui.theme.MustardDeep
import com.assetcoach.ui.theme.Negative
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

/**
 * 분석 탭 — 지출 뷰 (와이어프레임 §3.1).
 *
 * Phase 2 단계 노출:
 *  - 총 지출 + narrative
 *  - 카테고리 상위 5
 *  - 거래 내역 리스트
 *
 * Phase 3 추가 예정: 지출/수입/자산/구독/예산 5탭 분기, 도넛 차트, 세그먼트 벤치마크.
 */
@Composable
fun AnalysisScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as AssetCoachApp
    val vm: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory(app.transactionRepository))
    val state by vm.state.collectAsStateWithLifecycle()

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
            item { TotalSpendCard(state.totalSpend, state.transactions.size) }

            if (state.categoryTotals.isNotEmpty()) {
                item { SectionHeader("카테고리 상위 5", "${state.categoryTotals.size}개") }
                item {
                    CategoryRanking(state.categoryTotals.take(5))
                }
            }

            if (state.transactions.isNotEmpty()) {
                item { SectionHeader("거래 내역", "${state.transactions.size}건") }
                items(state.transactions, key = { it.id }) { tx ->
                    TransactionRow(tx, categoryEmoji = state.categoriesById[tx.categoryId]?.emoji ?: "🔘",
                        categoryName = state.categoriesById[tx.categoryId]?.name ?: "기타")
                }
            } else {
                item { EmptyState() }
            }

            item { Spacer(Modifier.height(Spacing.s7)) }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(top = Spacing.s5, bottom = Spacing.s3)) {
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
        Text(
            text = formatWon(-totalSpend),  // 표시용은 양수
            style = AppType.numXl.copy(color = Ink)
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            text = "$count 건",
            style = AppType.caption.copy(color = Neutral)
        )
    }
}

@Composable
private fun CategoryRanking(rows: List<CategoryTotal>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Paper)
            .border(1.dp, Faint, RoundedCornerShape(Radius.r3))
            .padding(horizontal = Spacing.s4, vertical = Spacing.s3)
    ) {
        rows.forEachIndexed { idx, row ->
            CategoryRankRow(row)
            if (idx < rows.lastIndex) {
                HorizontalDivider(color = Faint3, modifier = Modifier.padding(vertical = Spacing.s2))
            }
        }
    }
}

@Composable
private fun CategoryRankRow(row: CategoryTotal) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CreamDeep),
            contentAlignment = Alignment.Center
        ) {
            Text(row.category.emoji, style = AppType.body)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    row.category.name,
                    style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = Forest)
                )
                Text(
                    formatWon(-row.amount),
                    style = AppType.num.copy(color = Forest)
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Faint)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(row.ratio.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(MustardDeep)
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TransactionEntity, categoryEmoji: String, categoryName: String) {
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
        ) {
            Text(categoryEmoji, style = AppType.bodyLg)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    tx.merchantNorm.ifBlank { tx.rawText },
                    style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = Forest)
                )
                if (tx.installmentMonths > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Highlight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${tx.installmentMonths}개월 할부",
                            style = AppType.caption.copy(color = MustardDeep)
                        )
                    }
                }
                if (tx.isDuplicate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Terracotta)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("중복?", style = AppType.caption.copy(color = Cream))
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${formatDate(tx.ts)} · $categoryName",
                style = AppType.caption.copy(color = Neutral)
            )
        }
        Text(
            text = formatWon(tx.amount),
            style = AppType.num.copy(
                color = if (tx.amount < 0) Forest else Positive
            )
        )
    }
}

@Composable
private fun SectionHeader(name: String, meta: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s2, bottom = Spacing.s1),
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
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.s7),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📭", style = AppType.h1)
            Spacer(Modifier.height(Spacing.s3))
            Text(
                "거래 데이터가 아직 없어요",
                style = AppType.bodyLg.copy(fontFamily = GowunBatang, color = ForestSoft)
            )
            Spacer(Modifier.height(Spacing.s2))
            Text(
                "샘플 데이터 import 중…",
                style = AppType.caption.copy(color = Neutral)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// Formatters
// ─────────────────────────────────────────────────────────

private val winFormatter = SimpleDateFormat("M월 d일 HH:mm", Locale.KOREA).apply {
    timeZone = TimeZone.getTimeZone("Asia/Seoul")
}

private fun formatDate(epoch: Long): String = winFormatter.format(Date(epoch))

private fun formatWon(amount: Long): String {
    val sign = if (amount < 0) "−" else ""
    val abs = kotlin.math.abs(amount)
    return when {
        abs >= 100_000_000 -> "$sign${"%.1f".format(abs / 100_000_000.0)}억원"
        abs >= 10_000 -> "$sign${"%,d".format(abs / 10_000)}만원"
        else -> "$sign${"%,d".format(abs)}원"
    }
}
