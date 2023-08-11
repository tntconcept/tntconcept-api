package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createAttachmentInfo
import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class AttachmentInfoServiceTest {

    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()


    private val attachmentInfoService = AttachmentInfoService(attachmentInfoRepository)

    @Test
    fun `Should return empty if there are no uuids`() {
        val ids: List<UUID> = arrayListOf()
        val activityEvidences = attachmentInfoService.getActivityEvidences(ids)
        assertTrue(activityEvidences.isEmpty())
    }


    @Test
    fun `Should throw an exception if one of the attachments does not exist`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val ids = arrayListOf(id1, id2)
        val emptyAttachmentInfo: Optional<AttachmentInfo> = Optional.empty()

        Mockito.doReturn(emptyAttachmentInfo)
            .whenever(attachmentInfoRepository)
            .findById(id2)

        val exception = assertThrows<AttachmentNotFoundException> {
            attachmentInfoService.getActivityEvidences(ids)
        }

        assertEquals("Attachment does not exist", exception.message)
    }


    @Test
    fun `Should get the activities if there is no problem`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val ids = arrayListOf(id1, id2)

        Mockito.doReturn(Optional.of(createAttachmentInfo()))
            .whenever(attachmentInfoRepository)
            .findById(id1)

        Mockito.doReturn(Optional.of(createAttachmentInfo()))
            .whenever(attachmentInfoRepository)
            .findById(id2)

        val attachmentInfos = attachmentInfoService.getActivityEvidences(ids)
        assertEquals(ids.size, attachmentInfos.size)
    }

}