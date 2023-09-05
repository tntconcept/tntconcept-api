package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*
import javax.persistence.PersistenceException

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentInfoDaoIT {

    @Inject
    private lateinit var sut: AttachmentInfoDao

    @Test
    fun `should save an attachment and find it by id`() {
        val savedAttachment = sut.save(createAttachmentInfoEntity())
        val result = sut.findById(savedAttachment.id)
        assertThat(result).isPresent().contains(savedAttachment)
    }

    @Test
    fun `should find an attachment by id and user id`() {
        val savedAttachment = sut.save(createAttachmentInfoEntity())
        val result = sut.findByIdAndUserId(savedAttachment.id, savedAttachment.userId)
        assertThat(result).isPresent().contains(savedAttachment)
    }


    @Test
    fun `should find all temporary attachments`() {
        val temporaryAttachments = sut.findByIsTemporaryTrue()
        assertThat(temporaryAttachments).hasSize(1).allMatch { it.id == attachmentId }
    }

    @Test
    fun `should delete an attachments`() {
        val savedAttachment1 = sut.save(createAttachmentInfoEntity())
        val savedAttachment2 = sut.save(createAttachmentInfoEntity())
        val listOfIds = listOf(savedAttachment1.id, savedAttachment2.id)
        sut.deleteByIdIn(listOfIds)
        assertThat(sut.findByIdIn(listOfIds)).isEmpty()
    }

    @Test
    fun `wil fail when trying to delete attachments associated with activity_evidence`() {
        assertThatThrownBy { sut.deleteByIdIn(listOf(attachmentId)) }.isInstanceOf(PersistenceException::class.java)
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