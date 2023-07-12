package com.autentia.tnt.api.binnacle.hook

import com.autentia.tnt.binnacle.entities.dto.ActivityRequestBodyHookDTO
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.validation.constraints.Size

@Introspected
data class ActivityRequest(
    val id: Long? = null,
    val startDate: LocalDateTime,
    val duration: Int,
    @field:Size(max = 2048, message = "Description must not exceed 2048 characters")
    val description: String,
    val billable: Boolean,
    val projectRoleId: Long,
    val hasImage: Boolean,
    val imageFile: String? = null,
    val userName: String
) {
    fun toDto(): ActivityRequestBodyHookDTO =
        ActivityRequestBodyHookDTO(
            id,
            startDate,
            duration,
            description,
            billable,
            projectRoleId,
            hasImage,
            imageFile,
            userName,
        )

}