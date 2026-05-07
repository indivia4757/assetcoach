package com.assetcoach.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 단일 사용자 프로파일 (id=1 fixed).
 * 출처: AssetCoach_segment_matrix.md §Layer 1 + §Layer 3 성향.
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,

    @ColumnInfo(name = "name_label") val nameLabel: String,
    @ColumnInfo(name = "birth_year") val birthYear: Int,

    // Layer 1: 인구통계 세그먼트
    @ColumnInfo(name = "life_stage") val lifeStage: String,        // L1~L6
    @ColumnInfo(name = "income_pattern") val incomePattern: String,// I1~I6
    @ColumnInfo(name = "household") val household: String,         // H1, H2, H3a-d, H4
    @ColumnInfo(name = "child_ages_csv") val childAgesCsv: String? = null,
    @ColumnInfo(name = "segment_id") val segmentId: String,        // S01~S18

    // Layer 3: 성향 (1~5점, 행동 데이터로 보정)
    @ColumnInfo(name = "consumption_score") val consumptionScore: Float = 3f,
    @ColumnInfo(name = "engagement_score") val engagementScore: Float = 3f,
    @ColumnInfo(name = "risk_score") val riskScore: Float = 3f,

    // 표시 모드
    @ColumnInfo(name = "display_mode") val displayMode: String = "normal",  // normal / retirement / freelance

    @ColumnInfo(name = "onboarded_at") val onboardedAt: Long
)
