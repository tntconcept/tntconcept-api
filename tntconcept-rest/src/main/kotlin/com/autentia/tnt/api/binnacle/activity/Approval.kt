package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

data class Approval(
        val state: ApprovalState,
        val approvedByUserId: Long? = null,
        val approvalDate: LocalDateTime? = null
)
