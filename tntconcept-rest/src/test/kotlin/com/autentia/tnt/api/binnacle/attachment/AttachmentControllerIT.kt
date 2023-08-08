package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.core.domain.AttachmentType
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
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

//    @get:MockBean(ActivityEvidenceCreationUseCase::class)
//    internal val activityEvidenceCreationUseCase = mock<ActivityEvidenceCreationUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get an attachment by id`() {

        doReturn(ATTACHMENT).whenever(attachmentRetrievalUseCase)
            .getAttachment(ATTACHMENT_UUID)

        val response = client.exchangeObject<ByteArray>(
            HttpRequest.GET("/api/attachment/$ATTACHMENT_UUID")
        )

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(ATTACHMENT_MIME_TYPE, response.headers.get("Content-type"))
        assertTrue(Arrays.equals(IMAGE_RAW, response.body()))
        assertEquals("attachment; filename=\"$ATTACHMENT_FILENAME\"", response.headers.get("Content-disposition"))
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

    }



    @Test
    fun `create an attachment`() {

//        whenever(activityEvidenceCreationUseCase.createActivityEvidence(any(), any(), any())).thenReturn(ACTIVITY_ID)
        val multipartBody = MultipartBody.builder()
            .addPart("attachmentFile", "attachment.png", MediaType.IMAGE_PNG_TYPE, IMAGE_RAW)
            .build()

        val response = client.exchangeObject<UUID>(
            HttpRequest.POST("/api/attachment", multipartBody).contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        )
        assertNotNull(response.body)
        assertEquals(HttpStatus.OK, response.status)
    }


    // TODO generate tests for POST errors

    @Test
    fun `delete an evidence associated to an activity`() {

        // TODO review exchangeObjectType
        val response = client.exchangeObject<Boolean>(
            HttpRequest.DELETE("/api/attachment/$ATTACHMENT_UUID")
        )

        assertEquals(HttpStatus.OK, response.status)
    }

    // TODO generate tests for DELETE errors

    private companion object {
        private val ATTACHMENT_UUID = UUID.randomUUID()
        private const val ATTACHMENT_MIME_TYPE = "image/png"
        private  val ATTACHMENT_FILENAME = "$ATTACHMENT_UUID.png"
        private const val IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
        private val IMAGE_RAW = Base64.getDecoder().decode(IMAGE_BASE64)
        private val ATTACHMENT_INFO = AttachmentInfo(
            ATTACHMENT_UUID, AttachmentType.EVIDENCE, "/", ATTACHMENT_FILENAME, ATTACHMENT_MIME_TYPE,
            LocalDateTime.now(), false)
        private val ATTACHMENT = Attachment(ATTACHMENT_INFO, IMAGE_RAW)
    }

}