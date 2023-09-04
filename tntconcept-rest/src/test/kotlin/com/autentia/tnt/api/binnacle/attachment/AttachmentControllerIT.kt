package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.domain.AttachmentInfoId
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationRequestDTO
import com.autentia.tnt.binnacle.entities.dto.AttachmentCreationResponseDTO
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
import org.assertj.core.api.Assertions.assertThat
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
        doReturn(SUPPORTED_ATTACHMENT).whenever(attachmentRetrievalUseCase).getAttachment(ATTACHMENT_UUID)

        val response = client.exchangeObject<ByteArray>(
                HttpRequest.GET("/api/attachment/$ATTACHMENT_UUID")
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(SUPPORTED_ATTACHMENT_MIME_TYPE, response.headers.get("Content-type"))
        assertThat(IMAGE_RAW).isEqualTo(response.body())
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
    fun `returns HttpResponse OK when store an attachment`() {
        whenever(attachmentCreationUseCase.createAttachment(CREATE_ATTACHMENT)).thenReturn(SUPPORTED_ATTACHMENT_INFO_DTO)

        val multipartBody = MultipartBody.builder()
                .addPart("attachmentFile", SUPPORTED_ATTACHMENT_FILENAME, MediaType.IMAGE_PNG_TYPE, IMAGE_RAW)
                .build()

        val response = client.exchangeObject<AttachmentCreationResponse>(
                HttpRequest.POST("/api/attachment", multipartBody).contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        )

        assertNotNull(response.body)
        assertEquals(HttpStatus.OK, response.status)
    }


    @Test
    fun `throw IllegalArgumentException when create an attachment with invalid mimeType`() {
        doThrow(AttachmentMimeTypeNotSupportedException()).whenever(attachmentCreationUseCase).createAttachment(
                UNSUPPORTED_CREATE_ATTACHMENT
        )

        val multipartBody = MultipartBody.builder()
                .addPart("attachmentFile", UNSUPPORTED_ATTACHMENT_FILENAME, MediaType.APPLICATION_JSON_TYPE, IMAGE_RAW)
                .build()

        val ex = assertThrows<HttpClientResponseException> {
            client.exchangeObject<AttachmentCreationResponse>(
                    HttpRequest.POST("/api/attachment", multipartBody).contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
            )
        }

        assertEquals(HttpStatus.BAD_REQUEST, ex.status)
        assertEquals("Attachment mimetype is not supported", ex.message)
    }

    private companion object {

        private val ATTACHMENT_UUID = UUID.randomUUID()
        private const val SUPPORTED_ATTACHMENT_MIME_TYPE = "image/png"
        private const val UNSUPPORTED_ATTACHMENT_MIME_TYPE = "application/json"
        private const val SUPPORTED_ATTACHMENT_FILENAME = "filename.png"
        private const val UNSUPPORTED_ATTACHMENT_FILENAME = "filename.json"
        private val IMAGE_RAW = Base64.getDecoder()
                .decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=")

        private val CREATE_ATTACHMENT = AttachmentCreationRequestDTO(
                fileName = SUPPORTED_ATTACHMENT_FILENAME,
                mimeType = SUPPORTED_ATTACHMENT_MIME_TYPE,
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                file = IMAGE_RAW
        )

        private val UNSUPPORTED_CREATE_ATTACHMENT = AttachmentCreationRequestDTO(
                fileName = UNSUPPORTED_ATTACHMENT_FILENAME,
                mimeType = UNSUPPORTED_ATTACHMENT_MIME_TYPE,
                uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                file = IMAGE_RAW
        )

        private val SUPPORTED_ATTACHMENT_INFO_DTO = AttachmentCreationResponseDTO(
                ATTACHMENT_UUID,
                SUPPORTED_ATTACHMENT_FILENAME,
                SUPPORTED_ATTACHMENT_MIME_TYPE,
                LocalDateTime.now().withSecond(0).withNano(0),
                true
        )
        private val UNSUPPORTED_ATTACHMENT_INFO_DTO = AttachmentCreationResponseDTO(
                ATTACHMENT_UUID,
                UNSUPPORTED_ATTACHMENT_FILENAME,
                UNSUPPORTED_ATTACHMENT_MIME_TYPE,
                LocalDateTime.now().withSecond(0).withNano(0),
                true
        )
        private val SUPPORTED_ATTACHMENT = Attachment(
                info = AttachmentInfo(
                        id = AttachmentInfoId(ATTACHMENT_UUID),
                        fileName = SUPPORTED_ATTACHMENT_FILENAME,
                        mimeType = SUPPORTED_ATTACHMENT_MIME_TYPE,
                        uploadDate = LocalDateTime.now().withSecond(0).withNano(0),
                        isTemporary = true,
                        userId = 3L,
                        path = "/"
                ),
                file = IMAGE_RAW)
    }
}