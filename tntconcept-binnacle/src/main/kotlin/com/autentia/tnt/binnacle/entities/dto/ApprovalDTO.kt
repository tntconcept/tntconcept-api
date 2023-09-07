package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

class ApprovalDTO (
    val state: ApprovalState,
    val canBeApproved: Boolean = false,
    val approvedByUserId: Long? = null,
    val approvalDate: LocalDateTime? = null
)
