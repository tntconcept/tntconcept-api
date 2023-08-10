package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.binnacle.entities.AttachmentType
import com.autentia.tnt.binnacle.entities.dto.AttachmentDTO
import com.autentia.tnt.binnacle.entities.dto.AttachmentInfoDTO
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.usecases.AttachmentCreationUseCase
import com.autentia.tnt.binnacle.usecases.AttachmentRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentControllerIT {

    @Inject
    @field:Client(value = "/", errorType = String::class)
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(AttachmentRetrievalUseCase::class)
    internal val attachmentRetrievalUseCase = mock<AttachmentRetrievalUseCase>()

    @get:MockBean(AttachmentCreationUseCase::class)
    internal val attachmentCreationUseCase = mock<AttachmentCreationUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get an attachment by id`() {
        doReturn(ATTACHMENT_DTO).whenever(attachmentRetrievalUseCase)
            .getAttachment(ATTACHMENT_UUID)

        val response = client.exchangeObject<ByteArray>(
            HttpRequest.GET("/api/attachment/$ATTACHMENT_UUID")
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(SUPPORTED_ATTACHMENT_MIME_TYPE, response.headers.get("Content-type"))
        assertTrue(Arrays.equals(IMAGE_RAW, response.body()))
        assertEquals(
            "attachment; filename=\"$SUPPORTED_ATTACHMENT_FILENAME\"",
            response.headers.get("Content-disposition")
        )
    }

    @Test
    fun `returns HttpStatus NOT_FOUND when get an attachment that doesn't exist`() {
        doThrow(AttachmentNotFoundException()).whenever(attachmentRetrievalUseCase)
            .getAttachment(ATTACHMENT_UUID)

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<ByteArray>(
                HttpRequest.GET("/api/attachment/$ATTACHMENT_UUID")
            )
        }

        assertEquals(HttpStatus.NOT_FOUND, ex.status)
        assertEquals("Attachment does not exist", ex.message)
    }

    @Test
    fun `create an attachment`() {
        whenever(
            attachmentCreationUseCase.storeAttachment(
                IMAGE_RAW,
                SUPPORTED_ATTACHMENT_FILENAME,
                SUPPORTED_ATTACHMENT_MIME_TYPE
            )
        ).thenReturn(
            ATTACHMENT_INFO_DTO
        )

        val multipartBody = MultipartBody.builder()
            .addPart("attachmentFile", SUPPORTED_ATTACHMENT_FILENAME, MediaType.IMAGE_PNG_TYPE, IMAGE_RAW)
            .build()

        val response = client.exchangeObject<UUID>(
            HttpRequest.POST("/api/attachment", multipartBody).contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        )

        assertNotNull(response.body)
        assertEquals(HttpStatus.OK, response.status)
    }


    @Test
    fun `throw AttachmentMimeTypeNotSupportedException when create an attachment with invalid mimeType`() {

        doThrow(AttachmentMimeTypeNotSupportedException()).whenever(attachmentCreationUseCase).storeAttachment(
            IMAGE_RAW, UNSUPPORTED_ATTACHMENT_FILENAME, UNSUPPORTED_ATTACHMENT_MIME_TYPE
        )

        val multipartBody = MultipartBody.builder()
            .addPart("attachmentFile", UNSUPPORTED_ATTACHMENT_FILENAME, MediaType.APPLICATION_JSON_TYPE, IMAGE_RAW)
            .build()

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<UUID>(
                HttpRequest.POST("/api/attachment", multipartBody).contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, ex.status)
        assertEquals("Attachment mimetype is not valid", ex.message)
    }


    private companion object {

        private val ATTACHMENT_UUID = UUID.randomUUID()
        private const val SUPPORTED_ATTACHMENT_MIME_TYPE = "image/png"
        private const val UNSUPPORTED_ATTACHMENT_MIME_TYPE = "application/json"
        private const val SUPPORTED_ATTACHMENT_FILENAME = "filename.png"
        private const val UNSUPPORTED_ATTACHMENT_FILENAME = "filename.json"
        private val IMAGE_RAW = Base64.getDecoder()
            .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=")
        private const val ATTACHMENT_USERID = 1L
        private val ATTACHMENT_INFO_DTO = AttachmentInfoDTO(
            ATTACHMENT_UUID,
            ATTACHMENT_USERID,
            AttachmentType.EVIDENCE,
            "/",
            SUPPORTED_ATTACHMENT_FILENAME,
            SUPPORTED_ATTACHMENT_MIME_TYPE,
            LocalDateTime.now(),
            false
        )
        private val ATTACHMENT_DTO = AttachmentDTO(ATTACHMENT_INFO_DTO, IMAGE_RAW)
    }
}