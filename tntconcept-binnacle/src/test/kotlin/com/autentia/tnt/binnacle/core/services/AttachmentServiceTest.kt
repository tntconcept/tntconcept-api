package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.services.DateService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.mockito.kotlin.*
import java.io.File
import java.time.LocalDateTime
import java.util.*

@TestInstance(PER_CLASS)
class AttachmentServiceTest {
    private val attachmentStorage = mock<AttachmentStorage>()
    private val attachmentInfoRepository = mock<AttachmentInfoRepository>()
    private val dateService = mock<DateService>()

    private val appProperties = AppProperties().apply {
        files.supportedMimeTypes = mapOf(
                Pair("image/jpg", "jpg,jpeg"),
        )
    }

    private val sut = AttachmentService(
            attachmentStorage, attachmentInfoRepository, dateService, appProperties
    )

    @BeforeEach
    fun setUp() {
        reset(attachmentStorage, attachmentInfoRepository, dateService)
    }

    @Test
    fun `create and persist an attachment with a supported mime type and use file storage to store it`() {
        // Given
        doReturn(SOME_DATE).whenever(this.dateService).getDateNow()
        doNothing().whenever(this.attachmentStorage).storeAttachmentFile(any(), any())
        doNothing().whenever(this.attachmentInfoRepository).save(any<AttachmentInfo>())

        val fileName = "some_image.jpg"
        val mimetype = "image/jpg"
        val file = IMAGE_BYTEARRAY
        val userId = CURRENT_USER

        // When
        val result = this.sut.createAttachment(
                fileName = fileName,
                mimeType = mimetype,
                file = file,
                userId = userId
        )

        // Then
        assertThat(result.info.fileName).isEqualTo(fileName)
        assertThat(result.info.mimeType).isEqualTo(mimetype)
        assertThat(result.info.userId).isEqualTo(userId)
        assertThat(result.file.contentEquals(file)).isTrue()
        assertThat(result.info.isTemporary).isTrue()
        assertThat(result.info.uploadDate).isEqualTo(SOME_DATE)

        val expectedPath = "/2023/2/${result.info.id}.jpg"
        assertThat(result.info.path).isEqualTo(expectedPath)

        // Verify
        verify(this.attachmentStorage).storeAttachmentFile(expectedPath, file)
        verify(this.attachmentInfoRepository).save(any<AttachmentInfo>())
        verify(this.dateService).getDateNow()
    }

    @Test
    fun `create and persist an attachment with a supported mime type and extension  and use file storage to store it`() {
        // Given
        doReturn(SOME_DATE).whenever(this.dateService).getDateNow()
        doNothing().whenever(this.attachmentStorage).storeAttachmentFile(any(), any())
        doNothing().whenever(this.attachmentInfoRepository).save(any<AttachmentInfo>())

        val fileName = "some_image.jpeg"
        val mimetype = "image/jpg"
        val file = IMAGE_BYTEARRAY
        val userId = CURRENT_USER

        // When
        val result = this.sut.createAttachment(
                fileName = fileName,
                mimeType = mimetype,
                file = file,
                userId = userId
        )

        // Then
        assertThat(result.info.fileName).isEqualTo(fileName)
        assertThat(result.info.mimeType).isEqualTo(mimetype)
        assertThat(result.info.userId).isEqualTo(userId)
        assertThat(result.file.contentEquals(file)).isTrue()
        assertThat(result.info.isTemporary).isTrue()
        assertThat(result.info.uploadDate).isEqualTo(SOME_DATE)

        val expectedPath = "/2023/2/${result.info.id}.jpeg"
        assertThat(result.info.path).isEqualTo(expectedPath)

        // Verify
        verify(this.attachmentStorage).storeAttachmentFile(expectedPath, file)
        verify(this.attachmentInfoRepository).save(any<AttachmentInfo>())
        verify(this.dateService).getDateNow()
    }

    @Test
    fun `do not allow to create an attachment with a not supported mime type`() {
        // Given
        val notSupportedMimeType = "image/png"

        // When, Then
        assertThatThrownBy {
            this.sut.createAttachment(
                    fileName = "some name.png",
                    mimeType = notSupportedMimeType,
                    file = IMAGE_BYTEARRAY,
                    userId = CURRENT_USER
            )
        }.isInstanceOf(AttachmentMimeTypeNotSupportedException::class.java)
    }

    @Test
    fun `do not allow to create an attachment with a supported mime type that does not match with file extension`() {
        // Given
        val notSupportedMimeType = "image/jpg"

        // When, Then
        assertThatThrownBy {
            this.sut.createAttachment(
                    fileName = "some name.png",
                    mimeType = notSupportedMimeType,
                    file = IMAGE_BYTEARRAY,
                    userId = CURRENT_USER
            )
        }.isInstanceOf(AttachmentMimeTypeNotSupportedException::class.java)
    }

    @Test
    fun `find existing attachment`() {
        // Given
        val existingId = UUID.fromString("7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b")
        val existingInfo = AttachmentInfo(
                id = existingId,
                fileName = "some_image.jpg",
                mimeType = "application/jpg",
                uploadDate = SOME_DATE,
                userId = CURRENT_USER,
                path = "/2023/2/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpg",
                isTemporary = false
        )

        whenever(this.attachmentInfoRepository.findById(existingId)).thenReturn(Optional.of(existingInfo))
        whenever(this.attachmentStorage.retrieveAttachmentFile("/2023/2/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpg")).thenReturn(IMAGE_BYTEARRAY)

        // When
        val result = this.sut.findAttachment(existingId)

        // Then
        assertThat(result.info.fileName).isEqualTo("some_image.jpg")
        assertThat(result.info.mimeType).isEqualTo("application/jpg")
        assertThat(result.info.userId).isEqualTo(CURRENT_USER)
        assertThat(result.info.isTemporary).isFalse()
        assertThat(result.info.uploadDate).isEqualTo(SOME_DATE)
        assertThat(result.file.contentEquals(IMAGE_BYTEARRAY)).isTrue()

        // Verify
        verify(this.attachmentInfoRepository).findById(existingId)
        verify(this.attachmentStorage).retrieveAttachmentFile("/2023/2/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpg")
    }

    @Test
    fun `find attachment results in AttachmentNotFoundException when attachment info is not found`() {
        // Given
        val someId = UUID.fromString("7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b")

        whenever(this.attachmentInfoRepository.findById(someId)).thenReturn(Optional.empty())

        // When, Then
        assertThatThrownBy {
            this.sut.findAttachment(someId)
        }.isInstanceOf(AttachmentNotFoundException::class.java)

        // Verify
        verify(this.attachmentInfoRepository).findById(someId)
        verifyNoInteractions(this.attachmentStorage)
    }

    @Test
    fun `find attachment results in AttachmentNotFoundException when file is not found`() {
        // Given
        val someId = UUID.fromString("7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b")
        val existingInfo = AttachmentInfo(
                id = someId,
                fileName = "some_image.jpg",
                mimeType = "application/jpg",
                uploadDate = SOME_DATE,
                userId = CURRENT_USER,
                path = "/2023/2/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpg",
                isTemporary = false
        )

        whenever(this.attachmentInfoRepository.findById(someId)).thenReturn(Optional.of(existingInfo))
        whenever(this.attachmentStorage.retrieveAttachmentFile("/2023/2/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpg")).thenThrow(AttachmentNotFoundException())

        // When, Then
        assertThatThrownBy { this.sut.findAttachment(someId) }.isInstanceOf(AttachmentNotFoundException::class.java)

        // Verify
        verify(this.attachmentInfoRepository).findById(someId)
        verify(this.attachmentStorage).retrieveAttachmentFile("/2023/2/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpg")
    }

    @Test
    fun `will not remove attachments when ids are empty`() {
        this.sut.removeAttachments(emptyList())
        verifyNoInteractions(this.attachmentStorage, this.attachmentInfoRepository)
    }

    @Test
    fun `remove an existing attachment`() {
        val listOfEvidences = listOf(`get evidence for current user`(), `get evidence for current user`())
        val evidenceIds = listOfEvidences.map { it.id }

        whenever(this.attachmentInfoRepository.findByIds(evidenceIds)).thenReturn(listOfEvidences)
        whenever(this.attachmentStorage.retrieveAttachmentFile(any())).thenReturn(IMAGE_BYTEARRAY)
        doNothing().`when`(this.attachmentStorage).deleteAttachmentFile(any())

        sut.removeAttachments(listOfEvidences)

        verify(attachmentInfoRepository).delete(evidenceIds)
        listOfEvidences.forEach {
            verify(attachmentStorage).deleteAttachmentFile(it.path)
        }
    }

    private fun `get evidence for current user`(): AttachmentInfo {
        val id = UUID.randomUUID()
        return AttachmentInfo(
                id = id,
                fileName = "some_image.jpg",
                mimeType = "application/jpg",
                uploadDate = SOME_DATE,
                userId = CURRENT_USER,
                path = "/2023/2/$id.jpg",
                isTemporary = false
        )
    }

    companion object {
        private val SOME_DATE = LocalDateTime.of(2023, 2, 1, 10, 0)

        private const val CURRENT_USER: Long = 1L

        val IMAGE_BYTEARRAY = File("src/test/resources/attachments_test/evidences/7a5a56cf-03c3-42fb-8c1a-91b4cbf6b42b.jpeg").readBytes()
    }

}