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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assetcoach.AssetCoachApp
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Highlight
import com.assetcoach.ui.theme.Mustard
import com.assetcoach.ui.theme.Neutral
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.TerracottaDeep
import com.assetcoach.ui.theme.paperNoise

/**
 * 상담 탭 — 와이어프레임 §4 기반.
 *
 * - AI 코치 인사 + 추천 질문 칩
 * - 사용자 / 코치 버블
 * - Citation block (실 거래 데이터 인용)
 * - 입력창 + 전송 버튼
 *
 * Phase 3: GemmaMockClient 사용. Phase 3+ 에서 GemmaRealClient 로 교체.
 */
@Composable
fun ChatScreen(segmentId: String, nameLabel: String) {
    val context = LocalContext.current
    val app = context.applicationContext as AssetCoachApp

    // Phase 5: GemmaClientFactory 가 모델 가용성에 따라 Real/Mock 자동 선택
    val gemma = remember { app.gemmaFactory.get() }

    val vm: ChatViewModel = viewModel(
        factory = ChatViewModel.Factory(gemma, app.transactionRepository, segmentId, nameLabel)
    )
    val state by vm.state.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text?.length) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.s5, start = Spacing.s5, end = Spacing.s5, bottom = Spacing.s3)
            ) {
                Column {
                    Text(
                        "상담",
                        style = AppType.h1.copy(
                            color = Forest,
                            fontFamily = GowunBatang,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(Spacing.s1))
                    Text(
                        "AI 코치 · 거래 데이터 기반",
                        style = AppType.body.copy(color = ForestSoft)
                    )
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = Spacing.s5,
                    end = Spacing.s5,
                    top = Spacing.s2,
                    bottom = Spacing.s4
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s3)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    Bubble(msg)
                }

                // 추천 질문 (첫 진입 + 응답 대기 아님)
                if (state.messages.size <= 1 && !state.isStreaming) {
                    item { SuggestedRow(state.suggestedPrompts, onPick = { vm.send(it) }) }
                }
            }

            // Input
            ChatInputBar(
                value = input,
                disabled = state.isStreaming,
                onChange = { input = it },
                onSend = {
                    val txt = input.trim()
                    if (txt.isNotEmpty()) {
                        vm.send(txt)
                        input = ""
                    }
                }
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(
    msgs: List<ChatMessage>,
    key: (ChatMessage) -> Long,
    content: @Composable (ChatMessage) -> Unit
) {
    msgs.forEach { item(key = key(it)) { content(it) } }
}

@Composable
private fun Bubble(msg: ChatMessage) {
    when (msg.role) {
        ChatRole.User -> UserBubble(msg.text)
        ChatRole.Coach -> CoachBubble(msg)
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp))
                .background(Forest)
                .padding(horizontal = Spacing.s4, vertical = Spacing.s3)
        ) {
            Text(
                text,
                style = AppType.body.copy(color = Cream)
            )
        }
    }
}

@Composable
private fun CoachBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(CreamDeep)
                .border(1.5.dp, Terracotta, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "A",
                style = AppType.bodySm.copy(
                    color = TerracottaDeep,
                    fontFamily = GowunBatang,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp))
                .background(Paper)
                .border(1.dp, Faint, RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp))
                .padding(horizontal = Spacing.s4, vertical = Spacing.s3)
        ) {
            if (msg.text.isEmpty() && msg.isStreaming) {
                TypingIndicator()
            } else {
                Text(
                    msg.text + if (msg.isStreaming) "▍" else "",
                    style = AppType.body.copy(color = Forest)
                )
            }
            if (msg.citations.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.s2))
                CitationBlock(msg.citations)
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Neutral)
            )
        }
    }
}

@Composable
private fun CitationBlock(citations: List<CitationItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp, Radius.r2, Radius.r2, 0.dp))
            .background(Color(0x14C66A4A))   // terracotta tint
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((citations.size * 22 + 24).dp)
                    .background(Terracotta)
            )
            Column(modifier = Modifier.padding(horizontal = Spacing.s3, vertical = Spacing.s2)) {
                Text(
                    "인용한 데이터",
                    style = AppType.caption.copy(
                        color = TerracottaDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(2.dp))
                citations.forEach { c ->
                    Text(
                        c.line,
                        style = AppType.bodySm.copy(color = ForestSoft)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedRow(prompts: List<String>, onPick: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = Spacing.s2)) {
        Text(
            "이런 걸 물어볼 수 있어요",
            style = AppType.caption.copy(color = ForestSoft)
        )
        Spacer(Modifier.height(Spacing.s2))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s2)) {
            items(prompts) { p ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(CreamDeep)
                        .clickable { onPick(p) }
                        .padding(horizontal = Spacing.s4, vertical = Spacing.s2)
                ) {
                    Text(
                        p,
                        style = AppType.bodySm.copy(color = Forest)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items(
    list: List<String>,
    content: @Composable (String) -> Unit
) {
    list.forEach { item { content(it) } }
}

@Composable
private fun ChatInputBar(
    value: String,
    disabled: Boolean,
    onChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cream)
            .border(1.dp, Faint)
            .padding(horizontal = Spacing.s5, vertical = Spacing.s3)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(Paper)
                .border(1.dp, Faint, RoundedCornerShape(999.dp))
                .padding(horizontal = Spacing.s4, vertical = Spacing.s2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = AppType.body.copy(color = Forest),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.s2),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("메시지 입력...", style = AppType.body.copy(color = Neutral))
                    }
                    inner()
                }
            )
            Spacer(Modifier.width(Spacing.s2))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (disabled) Neutral else Terracotta)
                    .clickable(enabled = !disabled) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Text("↑", style = AppType.bodyLg.copy(color = Cream, fontWeight = FontWeight.Bold))
            }
        }
    }
}
