package com.example.service

import com.example.data.local.TrustedContactEntity

enum class CallOutcome(val label: String) {
    DIALING("Calling..."),
    NO_ANSWER_ESCALATED("Unanswered (Escalated)"),
    ANSWERED_CONNECTED("Answered & Connected"),
    SKIPPED("Manually Skipped"),
    CANCELLED("Cancelled")
}

data class ChainCallRecord(
    val contactName: String,
    val phoneNumber: String,
    val relationship: String,
    val attemptNumber: Int,
    val cycleNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: CallOutcome = CallOutcome.DIALING
)

data class CallChainState(
    val isActive: Boolean = false,
    val currentContactIndex: Int = 0,
    val currentContact: TrustedContactEntity? = null,
    val cycleNumber: Int = 1,
    val timeoutSecondsRemaining: Int = 25,
    val totalContactsInQueue: Int = 0,
    val isConnected: Boolean = false,
    val isPaused: Boolean = false,
    val statusMessage: String = "Idle",
    val callHistory: List<ChainCallRecord> = emptyList()
)
