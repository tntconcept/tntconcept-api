package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.config.createAttachmentInfo
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentRetrievalUseCaseTest {

    private val attachmentRepository = mock<AttachmentInfoRepository>()
    private val attachmentFileRepository = mock<AttachmentFileRepository>()
    private val attachmentRetrievalUseCase = AttachmentRetrievalUseCase(
        attachmentRepository,
        attachmentFileRepository
    )


    @Test
    fun `retrieve attachment by uuid`() {
        //whenever(attachmentRepository.findById(ATTACHMENT_UUID)).thenReturn(Optional.of(ATTACHMENT_ENTITY))

        val attachment = attachmentRetrievalUseCase.getAttachmentFile(ATTACHMENT_UUID)
        assertTrue(Arrays.equals(IMAGE_RAW, attachment))
    }

    companion object {
        private val ATTACHMENT_ENTITY = createAttachmentInfo()
        private const val IMAGE_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
        private const val ATTACHMENT_MIME_TYPE = "image/png"
        private val ATTACHMENT_UUID = UUID.randomUUID()
        private val IMAGE_RAW = Base64.getDecoder().decode(IMAGE_BASE64)
        private val ATTACHMENT_FILENAME = "$ATTACHMENT_UUID.png"
    }
}