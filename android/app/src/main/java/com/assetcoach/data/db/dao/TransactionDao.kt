package com.assetcoach.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assetcoach.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY ts DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE ts >= :fromTs AND ts < :toTs ORDER BY ts DESC")
    fun observeBetween(fromTs: Long, toTs: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT category_id, SUM(amount) AS total FROM transactions WHERE amount < 0 AND ts >= :fromTs AND ts < :toTs GROUP BY category_id ORDER BY total ASC")
    fun observeCategorySumsBetween(fromTs: Long, toTs: Long): Flow<List<CategorySumRow>>

    @Query("SELECT SUM(amount) FROM transactions WHERE amount < 0 AND ts >= :fromTs AND ts < :toTs")
    fun observeTotalSpendBetween(fromTs: Long, toTs: Long): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

data class CategorySumRow(
    val category_id: Int,
    val total: Long  // negative sum
)
