package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.ApprovalState
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Approval(
        val state: ApprovalState,
        val canBeApproved: Boolean = false,
        val approvedByUserId: Long? = null,
        val approvalDate: LocalDateTime? = null
)
