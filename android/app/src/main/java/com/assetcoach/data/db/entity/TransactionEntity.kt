package com.assetcoach.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 단일 거래 — CSV 또는 SMS 파서가 생성하는 단위.
 *
 * amount: 원 단위 정수, 음수 = 지출, 양수 = 입금/환불
 */
@Entity(
    tableName = "transactions",
    indices = [Index("ts"), Index("category_id"), Index("merchant_norm")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "ts") val ts: Long,                    // epoch millis
    @ColumnInfo(name = "amount") val amount: Long,            // 원 단위
    @ColumnInfo(name = "raw_text") val rawText: String,       // 가맹점 원문
    @ColumnInfo(name = "merchant_norm") val merchantNorm: String,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "category_confidence") val categoryConfidence: Float,
    @ColumnInfo(name = "category_source") val categorySource: String,  // 'rule' | 'llm' | 'user'
    @ColumnInfo(name = "is_duplicate") val isDuplicate: Boolean = false,
    @ColumnInfo(name = "duplicate_group_id") val duplicateGroupId: String? = null,
    @ColumnInfo(name = "installment_months") val installmentMonths: Int = 0,
    @ColumnInfo(name = "is_refund") val isRefund: Boolean = false,
    @ColumnInfo(name = "raw_source") val rawSource: String,   // 'shinhan-card-v1' 등
    @ColumnInfo(name = "notes") val notes: String? = null
)
