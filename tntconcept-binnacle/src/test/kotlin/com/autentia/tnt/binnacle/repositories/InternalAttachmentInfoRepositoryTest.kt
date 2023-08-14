package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createAttachmentInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.*

class InternalAttachmentInfoRepositoryTest {

    private val attachmentInfoDao = mock<AttachmentInfoDao>()

    private val internalAttachmentInfoRepository = InternalAttachmentRepository(attachmentInfoDao)

    @Test
    fun findById() {
        whenever(attachmentInfoDao.findById(attachmentId)).thenReturn(Optional.of(attachmentCreated))

        val result = internalAttachmentInfoRepository.findById(attachmentId)

        assertEquals(attachmentCreated, result.get())
    }

    @Test
    fun save() {
        whenever(attachmentInfoDao.save(attachmentToCreate)).thenReturn(attachmentCreated)

        val result = internalAttachmentInfoRepository.save(attachmentToCreate)

        assertEquals(attachmentCreated, result)
    }

    @Test
    fun `should return true if attachment exists`() {
        whenever(attachmentInfoDao.findById(attachmentId)).thenReturn(Optional.of(attachmentCreated))

        val result = internalAttachmentInfoRepository.isPresent(attachmentId)

        assertEquals(true, result)
    }

    @Test
    fun `should return false if attachment does not exist`() {
        whenever(attachmentInfoDao.findById(attachmentId)).thenReturn(Optional.empty())

        val result = internalAttachmentInfoRepository.isPresent(attachmentId)

        assertEquals(false, result)
    }

    @Test
    fun updateIsTemporary() {
        internalAttachmentInfoRepository.updateIsTemporary(attachmentId, true)

        verify(attachmentInfoDao).updateIsTemporary(attachmentId, true)
    }

    @Test
    fun findByIdAndUserId() {
        internalAttachmentInfoRepository.findByIdAndUserId(attachmentId, 1L)

        verify(attachmentInfoDao).findByIdAndUserId(attachmentId, 1L)
    }

    companion object {
        val attachmentId: UUID = UUID.randomUUID()
        val attachmentCreated = createAttachmentInfo()
        val attachmentToCreate = createAttachmentInfo().copy(id = null)
    }
}