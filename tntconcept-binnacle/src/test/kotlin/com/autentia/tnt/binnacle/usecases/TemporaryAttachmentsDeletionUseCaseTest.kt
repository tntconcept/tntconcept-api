package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntity
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemporaryAttachmentsDeletionUseCaseTest {

    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val attachmentFileRepository = mock<AttachmentFileRepository>()
    private val temporaryAttachmentsDeletionUseCase =
        TemporaryAttachmentsDeletionUseCase(attachmentInfoRepository, attachmentFileRepository)

    @Test
    fun `should filter the activities that are less than one day and call the repositories`() {
        val outDatedAttachment = ATTACHMENT_INFO_LIST.get(2)
        whenever(attachmentInfoRepository.findByIsTemporaryTrue()).thenReturn(ATTACHMENT_INFO_LIST)

        temporaryAttachmentsDeletionUseCase.delete()

        verify(attachmentInfoRepository).findByIsTemporaryTrue()
        verify(attachmentFileRepository).deleteActivityEvidence(listOf(outDatedAttachment).map { it.toDomain() })
        verify(attachmentInfoRepository).deleteTemporaryList(listOf(outDatedAttachment).map { it.id!! })
    }

    companion object {
        private val ATTACHMENT_INFO_LIST = listOf(
            createAttachmentInfoEntity().copy(isTemporary = true),
            createAttachmentInfoEntity().copy(isTemporary = true),
            createAttachmentInfoEntity().copy(uploadDate = LocalDateTime.now().minusDays(5), isTemporary = true)
        )
    }
}