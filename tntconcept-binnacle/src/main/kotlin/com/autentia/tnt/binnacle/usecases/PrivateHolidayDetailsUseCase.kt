package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.entities.VacationState
import com.autentia.tnt.binnacle.entities.dto.VacationDTO
import com.autentia.tnt.binnacle.entities.dto.VacationDetailsDTO
import com.autentia.tnt.binnacle.services.MyVacationsDetailService
import com.autentia.tnt.binnacle.services.UserService
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class PrivateHolidayDetailsUseCase internal constructor(
    private val userService: UserService,
    private val myVacationsDetailService: MyVacationsDetailService
) {

    @Transactional
    @ReadOnly
    fun get(
        chargeYear: Int,
        vacationsByChargeYear: List<VacationDTO>
    ): VacationDetailsDTO {
        val user = userService.getAuthenticatedUser()

        val correspondingVacations = myVacationsDetailService.getCorrespondingVacationDaysSinceHiringDate(
            user,
            chargeYear
        )

        val acceptedVacations = vacationsByChargeYear
            .filter { it.state === VacationState.ACCEPT }
            .fold(0) { acc, vacationDTO -> acc + vacationDTO.days.size }

        val requestedVacations = vacationsByChargeYear
            .filter { it.state === VacationState.ACCEPT || it.state === VacationState.PENDING }
            .fold(0) { acc, vacationDTO -> acc + vacationDTO.days.size }

        return VacationDetailsDTO(
            holidaysAgreement = user.getAgreementTermsByYear(chargeYear).vacation,
            correspondingVacations = correspondingVacations,
            acceptedVacations = acceptedVacations,
            remainingVacations = correspondingVacations - requestedVacations
        )
    }

}
