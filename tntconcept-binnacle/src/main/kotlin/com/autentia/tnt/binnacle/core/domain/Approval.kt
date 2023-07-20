package com.autentia.tnt.binnacle.core.domain

import com.autentia.tnt.binnacle.entities.ApprovalState
import java.time.LocalDateTime

class Approval (
    val approvalState: ApprovalState,
    val approvedByUserId: Long? = null,
    val approvalDate: LocalDateTime? = null
)
