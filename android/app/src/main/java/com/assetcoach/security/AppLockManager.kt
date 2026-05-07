package com.assetcoach.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 앱 단 잠금 상태 관리 (싱글턴, AssetCoachApp 보유).
 *
 * Phase 2.5: 콜드 스타트 시 항상 LOCKED — 생체 인증 후 UNLOCKED.
 * Phase 2.6 예정: 백그라운드 5 분 후 자동 LOCKED 재진입.
 */
class AppLockManager {

    private val _state = MutableStateFlow<LockState>(LockState.Locked)
    val state: StateFlow<LockState> = _state.asStateFlow()

    fun unlock() {
        _state.value = LockState.Unlocked
    }

    fun lock() {
        _state.value = LockState.Locked
    }
}

sealed class LockState {
    data object Locked : LockState()
    data object Unlocked : LockState()
}
