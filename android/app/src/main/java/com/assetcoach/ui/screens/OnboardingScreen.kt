package com.assetcoach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assetcoach.AssetCoachApp
import com.assetcoach.domain.segment.SegmentClassifier
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Faint2
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Highlight
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.Mustard
import com.assetcoach.ui.theme.MustardDeep
import com.assetcoach.ui.theme.Neutral
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.TerracottaDeep
import com.assetcoach.ui.theme.paperNoise

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as AssetCoachApp
    val vm: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(app.userProfileRepository)
    )
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.s5)) {
            ProgressBar(step = state.step, total = 5)
            Spacer(Modifier.height(Spacing.s4))

            Box(modifier = Modifier.weight(1f)) {
                when (state.step) {
                    0 -> WelcomeStep()
                    1 -> NameBirthStep(state, vm)
                    2 -> IncomeStep(state, vm)
                    3 -> HouseholdStep(state, vm)
                    else -> ResultStep(state, vm)
                }
            }

            FooterBar(
                state = state,
                onNext = {
                    if (state.step == 3) {
                        vm.computeAndPreviewResult()
                    }
                    if (state.step >= 4) {
                        vm.save(onComplete)
                    } else {
                        vm.next()
                    }
                },
                onBack = vm::back
            )
        }
    }
}

@Composable
private fun ProgressBar(step: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s7),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= step) Forest else Faint)
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = Spacing.s7),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            "조용한 코치를\n만나보세요",
            style = AppType.h1.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.s4))
        Text(
            "당신의 상황을 이해하고\n함께 생각해주는 AI 코치",
            style = AppType.bodyLg.copy(
                fontFamily = GowunBatang,
                color = ForestSoft
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.s7))
        Text(
            "🔒 데이터는 이 기기 안에만 머물러요",
            style = AppType.body.copy(color = ForestSoft),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NameBirthStep(state: OnboardingState, vm: OnboardingViewModel) {
    Column(modifier = Modifier.padding(top = Spacing.s5)) {
        Text(
            "어떻게 부르면 될까요?",
            style = AppType.h2.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            "이름이나 닉네임이면 충분해요.",
            style = AppType.body.copy(color = ForestSoft)
        )
        Spacer(Modifier.height(Spacing.s5))
        TextInput(
            label = "이름·닉네임",
            value = state.nameLabel,
            onChange = vm::setNameLabel,
            keyboard = KeyboardType.Text
        )
        Spacer(Modifier.height(Spacing.s5))
        Text(
            "몇 년에 태어나셨어요?",
            style = AppType.h2.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            "대략적인 시기만 알려주셔도 맞춤형 가이드가 가능해요.",
            style = AppType.body.copy(color = ForestSoft)
        )
        Spacer(Modifier.height(Spacing.s4))
        TextInput(
            label = "출생 연도",
            value = state.birthYear.toString(),
            onChange = { v -> v.toIntOrNull()?.let { vm.setBirthYear(it) } },
            keyboard = KeyboardType.Number
        )
    }
}

@Composable
private fun IncomeStep(state: OnboardingState, vm: OnboardingViewModel) {
    Column(modifier = Modifier.padding(top = Spacing.s5)) {
        Text(
            "수입은 어떻게\n들어오나요?",
            style = AppType.h1.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            "가장 가까운 형태 하나를 골라주세요. 나중에 바꿀 수 있어요.",
            style = AppType.body.copy(color = ForestSoft)
        )
        Spacer(Modifier.height(Spacing.s4))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.s2)) {
            items(SegmentClassifier.IncomePattern.entries) { p ->
                ChoiceCard(
                    icon = incomeIcon(p),
                    title = p.koreanLabel,
                    desc = incomeDesc(p),
                    selected = state.income == p,
                    onClick = { vm.setIncome(p) }
                )
            }
        }
    }
}

@Composable
private fun HouseholdStep(state: OnboardingState, vm: OnboardingViewModel) {
    Column(modifier = Modifier.padding(top = Spacing.s5)) {
        Text(
            "가족 구성은\n어떻게 되세요?",
            style = AppType.h1.copy(
                color = Ink,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s4))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.s2)) {
            items(SegmentClassifier.Household.entries) { h ->
                ChoiceCard(
                    icon = householdIcon(h),
                    title = h.koreanLabel,
                    desc = "",
                    selected = state.household == h,
                    onClick = { vm.setHousehold(h) }
                )
            }
        }
    }
}

@Composable
private fun ResultStep(state: OnboardingState, vm: OnboardingViewModel) {
    val r = state.diagnosed ?: return
    Column(
        modifier = Modifier.fillMaxSize().padding(top = Spacing.s7),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "맞춤 세그먼트",
            style = AppType.caption.copy(color = MustardDeep)
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            r.segmentId,
            style = AppType.numXl.copy(color = Ink)
        )
        Spacer(Modifier.height(Spacing.s2))
        Text(
            r.lifeStage.koreanLabel,
            style = AppType.h2.copy(
                color = Forest,
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(Modifier.height(Spacing.s5))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.r4))
                .background(Paper)
                .border(1.dp, Mustard, RoundedCornerShape(Radius.r4))
                .padding(Spacing.s5)
        ) {
            Column {
                ResultRow("수입", r.incomePattern.koreanLabel)
                Spacer(Modifier.height(Spacing.s3))
                ResultRow("가구", r.household.koreanLabel)
                Spacer(Modifier.height(Spacing.s3))
                ResultRow("표시 모드", when (r.displayMode) {
                    "retirement" -> "은퇴 모드 (큰 글씨)"
                    "freelance" -> "프리랜서 모드 (분기·연간)"
                    else -> "일반"
                })
            }
        }
        Spacer(Modifier.height(Spacing.s5))
        Text(
            "${state.nameLabel.ifBlank { "당신" }}님 같은 상황의 사람들을 기준으로 가이드를 드릴게요.",
            style = AppType.body.copy(fontFamily = GowunBatang, color = ForestSoft),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AppType.bodySm.copy(color = Neutral))
        Text(
            value,
            style = AppType.body.copy(
                fontWeight = FontWeight.SemiBold,
                color = Forest
            )
        )
    }
}

@Composable
private fun ChoiceCard(
    icon: String,
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Paper)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Forest else Faint,
                shape = RoundedCornerShape(Radius.r3)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.s4, vertical = Spacing.s4),
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
            Text(icon, style = AppType.bodyLg)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = Forest)
            )
            if (desc.isNotEmpty()) {
                Text(desc, style = AppType.caption.copy(color = ForestSoft))
            }
        }
        if (selected) {
            Text(
                "✓",
                style = AppType.bodyLg.copy(color = Forest, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun TextInput(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType
) {
    Column {
        Text(label, style = AppType.caption.copy(color = MustardDeep))
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.r2))
                .background(Paper)
                .border(1.5.dp, Faint2, RoundedCornerShape(Radius.r2))
                .padding(horizontal = Spacing.s4, vertical = Spacing.s4)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = AppType.bodyLg.copy(color = Forest),
                keyboardOptions = KeyboardOptions(keyboardType = keyboard)
            )
        }
    }
}

@Composable
private fun FooterBar(
    state: OnboardingState,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val canProceed = when (state.step) {
        0 -> true
        1 -> state.nameLabel.isNotBlank() && state.birthYear in 1900..2026
        2 -> state.income != null
        3 -> state.household != null
        else -> true
    }
    Column(modifier = Modifier.padding(vertical = Spacing.s5)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
        ) {
            if (state.step > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.r3))
                        .border(1.5.dp, Forest, RoundedCornerShape(Radius.r3))
                        .clickable(onClick = onBack)
                        .padding(horizontal = Spacing.s5, vertical = Spacing.s4)
                ) {
                    Text("이전", style = AppType.body.copy(fontWeight = FontWeight.SemiBold, color = Forest))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.r3))
                    .background(if (canProceed) Terracotta else Neutral)
                    .clickable(enabled = canProceed, onClick = onNext)
                    .padding(vertical = Spacing.s4),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (state.step >= 4) "시작하기" else "다음",
                    style = AppType.bodyLg.copy(
                        color = Cream,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(
    list: List<SegmentClassifier.IncomePattern>,
    content: @Composable (SegmentClassifier.IncomePattern) -> Unit
) {
    list.forEach { item { content(it) } }
}

@JvmName("itemsHousehold")
private fun androidx.compose.foundation.lazy.LazyListScope.items(
    list: List<SegmentClassifier.Household>,
    content: @Composable (SegmentClassifier.Household) -> Unit
) {
    list.forEach { item { content(it) } }
}

private fun incomeIcon(p: SegmentClassifier.IncomePattern): String = when (p) {
    SegmentClassifier.IncomePattern.I1 -> "💼"
    SegmentClassifier.IncomePattern.I2 -> "🏛"
    SegmentClassifier.IncomePattern.I3 -> "🎨"
    SegmentClassifier.IncomePattern.I4 -> "⚕"
    SegmentClassifier.IncomePattern.I5 -> "📚"
    SegmentClassifier.IncomePattern.I6 -> "🌾"
}

private fun incomeDesc(p: SegmentClassifier.IncomePattern): String = when (p) {
    SegmentClassifier.IncomePattern.I1 -> "매달 비슷한 금액 · 직장인"
    SegmentClassifier.IncomePattern.I2 -> "공무원·교사·군인"
    SegmentClassifier.IncomePattern.I3 -> "프리랜서·자영업·영업직"
    SegmentClassifier.IncomePattern.I4 -> "의사·변호사·시니어 개발자"
    SegmentClassifier.IncomePattern.I5 -> "학생·구직자·경력단절"
    SegmentClassifier.IncomePattern.I6 -> "은퇴자 (자동 은퇴 모드)"
}

private fun householdIcon(h: SegmentClassifier.Household): String = when (h) {
    SegmentClassifier.Household.H1 -> "👤"
    SegmentClassifier.Household.H2 -> "💑"
    SegmentClassifier.Household.H3a -> "👶"
    SegmentClassifier.Household.H3b -> "🎒"
    SegmentClassifier.Household.H3c -> "🎓"
    SegmentClassifier.Household.H3d -> "🏠"
    SegmentClassifier.Household.H4 -> "👵"
}
