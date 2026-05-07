package com.assetcoach.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assetcoach.data.db.entity.UserProfileEntity
import com.assetcoach.data.repo.UserProfileRepository
import com.assetcoach.domain.segment.SegmentClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 온보딩 ViewModel — 4 질문 진단 → 18 세그먼트 매핑 → UserProfile 저장.
 * 와이어프레임 §1.2-§1.3 기반 (간소화된 4 step).
 */
class OnboardingViewModel(
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun next() {
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(5)) }
    }

    fun back() {
        _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    fun setNameLabel(name: String) = _state.update { it.copy(nameLabel = name) }
    fun setBirthYear(year: Int) = _state.update { it.copy(birthYear = year) }

    fun setIncome(p: SegmentClassifier.IncomePattern) =
        _state.update { it.copy(income = p) }

    fun setHousehold(h: SegmentClassifier.Household) =
        _state.update { it.copy(household = h) }

    fun setChildAges(ages: List<Int>) = _state.update { it.copy(childAges = ages) }

    fun computeAndPreviewResult() {
        val s = _state.value
        if (s.income == null || s.household == null) return
        val result = SegmentClassifier.classify(
            birthYear = s.birthYear,
            income = s.income!!,
            household = s.household!!,
            childAges = s.childAges
        )
        _state.update { it.copy(diagnosed = result) }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val result = s.diagnosed ?: return
        viewModelScope.launch {
            profileRepo.save(
                UserProfileEntity(
                    id = 1,
                    nameLabel = s.nameLabel.ifBlank { "사용자" },
                    birthYear = s.birthYear,
                    lifeStage = result.lifeStage.code,
                    incomePattern = result.incomePattern.code,
                    household = result.household.code,
                    childAgesCsv = s.childAges.joinToString(",").ifBlank { null },
                    segmentId = result.segmentId,
                    displayMode = result.displayMode,
                    onboardedAt = System.currentTimeMillis()
                )
            )
            onDone()
        }
    }

    class Factory(private val repo: UserProfileRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(OnboardingViewModel::class.java))
            return OnboardingViewModel(repo) as T
        }
    }
}

data class OnboardingState(
    val step: Int = 0,           // 0: welcome, 1: name+birth, 2: income, 3: household, 4: result
    val nameLabel: String = "",
    val birthYear: Int = 1992,
    val income: SegmentClassifier.IncomePattern? = null,
    val household: SegmentClassifier.Household? = null,
    val childAges: List<Int> = emptyList(),
    val diagnosed: SegmentClassifier.Result? = null
)
