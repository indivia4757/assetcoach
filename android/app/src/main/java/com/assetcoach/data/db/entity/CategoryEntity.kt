package com.assetcoach.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 18 카테고리 (디자인 시스템 §아이콘 카테고리 18종과 1:1 매핑).
 * 시드 데이터: AssetCoachDatabase.onCreate 에서 한 번 채움.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "emoji") val emoji: String,
    @ColumnInfo(name = "parent_id") val parentId: Int? = null
) {
    companion object {
        // 디자인 시스템 §아이콘 카테고리 18종
        val SEED = listOf(
            CategoryEntity(1, "식비", "🍱"),
            CategoryEntity(2, "카페", "☕"),
            CategoryEntity(3, "외식", "🍽"),
            CategoryEntity(4, "교통", "🚇"),
            CategoryEntity(5, "쇼핑", "🛍"),
            CategoryEntity(6, "의료", "⚕"),
            CategoryEntity(7, "교육", "🎓"),
            CategoryEntity(8, "통신", "📡"),
            CategoryEntity(9, "주거", "🏠"),
            CategoryEntity(10, "보험", "🛡"),
            CategoryEntity(11, "구독", "📺"),
            CategoryEntity(12, "여가", "🎨"),
            CategoryEntity(13, "여행", "✈"),
            CategoryEntity(14, "자녀양육", "👶"),
            CategoryEntity(15, "부모부양", "👵"),
            CategoryEntity(16, "자기계발", "📚"),
            CategoryEntity(17, "경조사", "🎁"),
            CategoryEntity(18, "세금금융", "🏦"),
            CategoryEntity(99, "기타", "🔘")  // fallback
        )
    }
}
