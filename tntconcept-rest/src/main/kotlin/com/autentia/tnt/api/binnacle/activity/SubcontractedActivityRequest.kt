package com.autentia.tnt.api.binnacle.activity

import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityRequestDTO
import io.micronaut.core.annotation.Introspected
import java.time.YearMonth
import javax.annotation.Nullable
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Introspected
data class SubcontractedActivityRequest (
    val id: Long? = null,
    val month: YearMonth,
    val duration: Int,
    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    @field:Pattern(
            regexp = "^data:[^,]+;base64,.+$", message = "String format should be data:<mediatype>;base64,<data>"
    )
    @field:Nullable
    val evidence: String? = null,
    ) {
        fun toDto(): SubcontractedActivityRequestDTO = SubcontractedActivityRequestDTO(
                id,
                month,
                duration,
                description,
                billable,
                projectRoleId
        )
    }
