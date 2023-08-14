package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createAttachmentInfoEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.*

class InternalAttachmentInfoRepositoryTest {

    private val attachmentInfoDao = mock<AttachmentInfoDao>()

    private val internalAttachmentInfoRepository = InternalAttachmentInfoRepository(attachmentInfoDao)

    @Test
    fun `should return attachment when findById`() {
        whenever(attachmentInfoDao.findById(attachmentId)).thenReturn(Optional.of(attachmentCreated))

        val result = internalAttachmentInfoRepository.findById(attachmentId)

        assertEquals(attachmentCreated, result.get())
    }

    @Test
    fun `should store attachment when save`() {
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
    fun `should call updateIsTemporary in attachment info dao`() {
        val isTemporary = true

        internalAttachmentInfoRepository.updateIsTemporary(attachmentId, isTemporary)

        verify(attachmentInfoDao).updateIsTemporary(attachmentId, isTemporary)
    }

    @Test
    fun `should call findByIsTemporary in attachment info dao`() {
        internalAttachmentInfoRepository.findByIsTemporaryTrue()

        verify(attachmentInfoDao).findByIsTemporaryTrue()
    }

    @Test
    fun `should call findByIdAndUserId in attachment info dao`() {
        internalAttachmentInfoRepository.findByIdAndUserId(attachmentId, 1L)

        verify(attachmentInfoDao).findByIdAndUserId(attachmentId, 1L)
    }

    @Test
    fun `should call deleteTemporaryList in attachment info dao`() {
        internalAttachmentInfoRepository.deleteTemporaryList(listOf(attachmentCreated.id!!))

        verify(attachmentInfoDao).deleteTemporaryList(listOf(attachmentCreated.id!!))
    }

    companion object {
        val attachmentId: UUID = UUID.randomUUID()
        val attachmentCreated = createAttachmentInfoEntity()
        val attachmentToCreate = createAttachmentInfoEntity().copy(id = null)
    }
}