package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.dto.ActivityResponseDTO
import com.autentia.tnt.binnacle.exception.ActivityNotFoundException
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class ActivityRetrievalByIdUseCase internal constructor(
    private val activityRepository: ActivityRepository,
    private val activityResponseConverter: ActivityResponseConverter,
) {
    @Transactional
    @ReadOnly
    fun getActivityById(id: Long): ActivityResponseDTO? {
        val activity = activityRepository.findById(id)?.toDomain() ?: throw ActivityNotFoundException(id)
        return activityResponseConverter.toActivityResponseDTO(activity)
    }
}
