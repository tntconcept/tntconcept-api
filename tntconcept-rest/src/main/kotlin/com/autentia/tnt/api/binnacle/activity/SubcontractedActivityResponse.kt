package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityResponseDTO
import java.time.YearMonth

data class SubcontractedActivityResponse(
    val duration: Int,
    val description: String,
    val billable: Boolean,
    val id: Long,
    val projectRoleId: Long,
    val month: YearMonth,
    val userId: Long,
) {
    companion object {
        fun from(subcontractingActivityResponseDTO: SubcontractedActivityResponseDTO) =
            SubcontractedActivityResponse(
                subcontractingActivityResponseDTO.duration,
                subcontractingActivityResponseDTO.description,
                subcontractingActivityResponseDTO.billable,
                subcontractingActivityResponseDTO.id,
                subcontractingActivityResponseDTO.projectRoleId,
                subcontractingActivityResponseDTO.month,
                subcontractingActivityResponseDTO.userId,
            )
    }
}