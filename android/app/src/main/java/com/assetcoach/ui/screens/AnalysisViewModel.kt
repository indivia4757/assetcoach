package com.assetcoach.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assetcoach.data.db.entity.CategoryEntity
import com.assetcoach.data.db.entity.TransactionEntity
import com.assetcoach.data.repo.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AnalysisUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val categoriesById: Map<Int, CategoryEntity> = emptyMap(),
    val totalSpend: Long = 0L,
    val categoryTotals: List<CategoryTotal> = emptyList()
)

data class CategoryTotal(
    val category: CategoryEntity,
    val amount: Long,        // 음수 (지출)
    val ratio: Float
)

class AnalysisViewModel(
    private val repo: TransactionRepository
) : ViewModel() {

    val state: StateFlow<AnalysisUiState> = combine(
        repo.observeTransactions(),
        repo.observeCategories()
    ) { txs, cats ->
        val byId = cats.associateBy { it.id }
        val totalSpend = txs.filter { it.amount < 0 }.sumOf { it.amount }
        val perCategory = txs
            .filter { it.amount < 0 }
            .groupBy { it.categoryId }
            .map { (catId, list) ->
                val sum = list.sumOf { it.amount }
                CategoryTotal(
                    category = byId[catId] ?: CategoryEntity(catId, "기타", "🔘"),
                    amount = sum,
                    ratio = if (totalSpend == 0L) 0f else (sum.toFloat() / totalSpend.toFloat())
                )
            }
            .sortedBy { it.amount }   // 가장 음수 (큰 지출) 우선
        AnalysisUiState(
            transactions = txs,
            categoriesById = byId,
            totalSpend = totalSpend,
            categoryTotals = perCategory
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisUiState()
    )

    class Factory(private val repo: TransactionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AnalysisViewModel::class.java))
            return AnalysisViewModel(repo) as T
        }
    }
}
