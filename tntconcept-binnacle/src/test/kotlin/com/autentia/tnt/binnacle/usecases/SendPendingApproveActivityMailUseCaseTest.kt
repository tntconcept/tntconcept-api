package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.repositories.ArchimedesRepository
import com.autentia.tnt.binnacle.services.PendingApproveActivityMailService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SendPendingApproveActivityMailUseCaseTest {

    private val archimedesRepository = mock<ArchimedesRepository>()
    private val pendingApproveActivityMailService = mock<PendingApproveActivityMailService>()

    private val sut = SendPendingApproveActivityMailUseCase(archimedesRepository, pendingApproveActivityMailService)

    @Test
    fun `should send email to activity-approval users`() {
        // Given
        val roleName = "activity-approval"
        val anActivity = createActivity().toDomain()
        val aUser = createUser().toDomain()
        val aLocale = Locale.ENGLISH

        val listOfApprovalUsers = listOf("one@mail.com", "two@email.com", "three@email.com")
        whenever(this.archimedesRepository.findAllPrincipalNameByRoleName(roleName)).thenReturn(listOfApprovalUsers)

        // When
        sut.send(anActivity, aUser.username, aLocale)

        // Then
        verify(this.pendingApproveActivityMailService).
            sendApprovalActivityMail(anActivity, aUser.username, aLocale, listOfApprovalUsers)
    }
}