package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createAttachmentInfoEntityWithFilenameAndMimetype
import com.autentia.tnt.binnacle.core.services.AttachmentFileSystemStorage
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemporaryAttachmentsDeletionUseCaseTest {

    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val attachmentFileSystemStorage = mock<AttachmentFileSystemStorage>()
    private val appProperties = AppProperties()
    private val temporaryAttachmentsDeletionUseCase =
        TemporaryAttachmentsDeletionUseCase(attachmentInfoRepository, attachmentFileSystemStorage, appProperties)

    @Test
    fun `should not delete temporary files if flag is not enabled`() {
        appProperties.binnacle.temporaryAttachments.enabled = false

        temporaryAttachmentsDeletionUseCase.delete()

        verifyNoInteractions(attachmentInfoRepository)
        verifyNoInteractions(attachmentFileSystemStorage)
    }

    @Test
    fun `should filter the activities that are less than one day and call the repositories`() {
        appProperties.binnacle.temporaryAttachments.enabled = true
        val attachmentInfosDomain = ATTACHMENT_INFO_LIST.map { it.toDomain() }
        val outDatedAttachment = attachmentInfosDomain[2]

        whenever(attachmentInfoRepository.findByIsTemporaryTrue()).thenReturn(attachmentInfosDomain)

        temporaryAttachmentsDeletionUseCase.delete()

        verify(attachmentInfoRepository).findByIsTemporaryTrue()
        verify(attachmentFileSystemStorage).deleteAttachmentFile(outDatedAttachment.path)
        verify(attachmentInfoRepository).delete(listOf(outDatedAttachment).map { it.id })
    }

    companion object {
        private const val IMAGE_SUPPORTED_FILENAME = "Evidence001.png"
        private const val IMAGE_SUPPORTED_MIMETYPE = "image/png"

        private val ATTACHMENT_INFO_LIST = listOf(
            createAttachmentInfoEntityWithFilenameAndMimetype(IMAGE_SUPPORTED_FILENAME, IMAGE_SUPPORTED_MIMETYPE).copy(
                isTemporary = true
            ),
            createAttachmentInfoEntityWithFilenameAndMimetype(IMAGE_SUPPORTED_FILENAME, IMAGE_SUPPORTED_MIMETYPE).copy(
                isTemporary = true
            ),
            createAttachmentInfoEntityWithFilenameAndMimetype(IMAGE_SUPPORTED_FILENAME, IMAGE_SUPPORTED_MIMETYPE).copy(
                uploadDate = LocalDateTime.now().minusDays(5),
                isTemporary = true
            )
        )
    }
}