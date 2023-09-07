package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Activity
import com.autentia.tnt.binnacle.repositories.ArchimedesRepository
import com.autentia.tnt.binnacle.services.PendingApproveActivityMailService
import jakarta.inject.Singleton
import java.util.*

@Singleton
class SendPendingApproveActivityMailUseCase internal constructor(
        private val archimedesRepository: ArchimedesRepository,
        private val pendingApproveActivityMailService: PendingApproveActivityMailService
) {

    private val activityApprovalRoleName = "activity-approval"

    fun send(activity: Activity, username: String, locale: Locale) {
        val listOfActivityApprovalUsers =
                this.archimedesRepository.findAllPrincipalNameByRoleName(activityApprovalRoleName)

        pendingApproveActivityMailService.sendApprovalActivityMail(activity, username, locale, listOfActivityApprovalUsers)
    }
}