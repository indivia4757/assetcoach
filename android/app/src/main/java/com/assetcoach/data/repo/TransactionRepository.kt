package com.assetcoach.data.repo

import android.content.Context
import com.assetcoach.data.csv.ShinhanCardParser
import com.assetcoach.data.db.dao.CategoryDao
import com.assetcoach.data.db.dao.CategorySumRow
import com.assetcoach.data.db.dao.TransactionDao
import com.assetcoach.data.db.entity.CategoryEntity
import com.assetcoach.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    fun observeTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observeAll()

    fun observeCategories(): Flow<List<CategoryEntity>> =
        categoryDao.observeAll()

    fun observeCategorySums(fromTs: Long, toTs: Long): Flow<List<CategorySumRow>> =
        transactionDao.observeCategorySumsBetween(fromTs, toTs)

    fun observeTotalSpend(fromTs: Long, toTs: Long): Flow<Long?> =
        transactionDao.observeTotalSpendBetween(fromTs, toTs)

    suspend fun count(): Int = transactionDao.count()

    /**
     * 첫 실행 시 assets/sample CSV 자동 import.
     * 이미 거래가 있으면 skip.
     */
    suspend fun importSampleIfEmpty(context: Context): Int {
        if (transactionDao.count() > 0) return 0
        val parser = ShinhanCardParser(charset = Charsets.UTF_8)
        val transactions = context.assets.open("sample/S06-shinhan-card-2026-04-week1.csv").use { stream ->
            parser.parse(stream)
        }
        // 중복 거래 그룹 식별 — 같은 일자, 비슷한 가맹점, 비슷한 금액
        val withDuplicates = markDuplicates(transactions)
        transactionDao.insertAll(withDuplicates)
        return withDuplicates.size
    }

    private fun markDuplicates(txs: List<TransactionEntity>): List<TransactionEntity> {
        val groups = mutableMapOf<String, String>()  // signature -> groupId
        val result = mutableListOf<TransactionEntity>()
        txs.forEach { tx ->
            // signature: 같은 날짜·정규화된 가맹점·비슷한 금액(±10%)
            val rootMerchant = tx.merchantNorm.split(" ").firstOrNull() ?: tx.merchantNorm
            val sig = "${tx.ts / 86_400_000}|$rootMerchant"
            val existing = groups[sig]
            if (existing != null) {
                // 같은 그룹
                result.add(tx.copy(isDuplicate = true, duplicateGroupId = existing))
                // 첫 거래도 이 그룹에 포함시키기 위해 별도 처리는 생략
            } else {
                val groupId = "g-${tx.ts}-$rootMerchant"
                groups[sig] = groupId
                result.add(tx)
            }
        }
        return result
    }
}
