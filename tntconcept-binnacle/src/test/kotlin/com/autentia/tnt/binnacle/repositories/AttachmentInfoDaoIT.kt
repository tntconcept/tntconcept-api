package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentInfoDaoIT {

    @Inject
    private lateinit var attachmentInfoDao: AttachmentInfoDao

    @Test
    fun `should save an attachment and find it by id`() {
        val savedAttachment = attachmentInfoDao.save(createAttachmentInfoEntity())

        val result = attachmentInfoDao.findById(savedAttachment.id)

        Assertions.assertEquals(savedAttachment, result.get())
    }

    @Test
    fun `should find an attachment by id and user id`() {
        val savedAttachment = attachmentInfoDao.save(createAttachmentInfoEntity())

        val result = attachmentInfoDao.findByIdAndUserId(savedAttachment.id, savedAttachment.userId)

        Assertions.assertEquals(savedAttachment, result.get())
    }

    @Test
    fun `should change isTemporary to correct value`() {
        attachmentInfoDao.updateIsTemporary(attachmentId, false)

        val result = attachmentInfoDao.findById(attachmentId).get()

        assertFalse(result.isTemporary)
    }

    private fun createAttachmentInfoEntity() = AttachmentInfo(
            id = UUID.randomUUID(),
            userId = 1L,
            path = "/",
            fileName = "Evidence001",
            mimeType = "application/png",
            uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
            isTemporary = false
    )

    companion object {
        private val attachmentId = UUID.fromString("4d3cbe3f-369f-11ee-99c2-0242ac180003")
    }

}