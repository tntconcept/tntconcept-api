package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntity
import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class AttachmentInfoServiceTest {

    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()


    private val attachmentInfoService = AttachmentInfoService(attachmentInfoRepository)

    @Test
    fun `Should return empty if there are no uuids`() {
        val ids: List<UUID> = arrayListOf()
        val activityEvidences = attachmentInfoService.getAttachments(ids)
        assertTrue(activityEvidences.isEmpty())
    }


    @Test
    fun `Should throw an exception if one of the attachments does not exist`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val ids = arrayListOf(id1, id2)
        val emptyAttachmentInfo: Optional<AttachmentInfo> = Optional.empty()

        doReturn(emptyAttachmentInfo)
            .whenever(attachmentInfoRepository)
            .findById(id2)

        val exception = assertThrows<AttachmentNotFoundException> {
            attachmentInfoService.getAttachments(ids)
        }

        assertEquals("Attachment does not exist", exception.message)
    }


    @Test
    fun `Should get the activities if there is no problem`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val ids = arrayListOf(id1, id2)

        doReturn(Optional.of(createAttachmentInfoEntity()))
            .whenever(attachmentInfoRepository)
            .findById(id1)

        doReturn(Optional.of(createAttachmentInfoEntity()))
            .whenever(attachmentInfoRepository)
            .findById(id2)

        val attachmentInfos = attachmentInfoService.getAttachments(ids)
        assertEquals(ids.size, attachmentInfos.size)
    }

    @Test
    fun `Should consolidate the attachments`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val ids = arrayListOf(id1, id2)

        doNothing()
            .whenever(attachmentInfoRepository)
            .updateIsTemporary(id1, false)

        doNothing()
            .whenever(attachmentInfoRepository)
            .updateIsTemporary(id1, false)

        attachmentInfoService.markAttachmentsAsNonTemporary(ids)
        verify(attachmentInfoRepository).updateIsTemporary(id1, false)
        verify(attachmentInfoRepository).updateIsTemporary(id2, false)
    }


}