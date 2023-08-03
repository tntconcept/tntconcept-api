package com.autentia.tnt.api.binnacle.activity.evidence

import com.autentia.tnt.api.binnacle.exchangeObject
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.usecases.ActivityEvidenceRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EvidenceControllerIT {

    @Inject
    @field:Client(value = "/", errorType = String::class)
    private lateinit var httpClient: HttpClient

    private lateinit var client: BlockingHttpClient

    @get:MockBean(ActivityEvidenceRetrievalUseCase::class)
    internal val activityEvidenceRetrievalUseCase = mock<ActivityEvidenceRetrievalUseCase>()

    @BeforeAll
    fun setup() {
        client = httpClient.toBlocking()
    }

    @Test
    fun `get an evidence activity by id`() {

        doReturn(EvidenceDTO.from(ACTIVITY_IMAGE)).whenever(activityEvidenceRetrievalUseCase)
            .getActivityEvidenceByActivityId(ACTIVITY_ID)

        val response = client.exchangeObject<String>(
            HttpRequest.GET("/api/activity/$ACTIVITY_ID/evidence")
        )

        Assertions.assertEquals(HttpStatus.OK, response.status)
        Assertions.assertEquals(ACTIVITY_IMAGE, response.body())
    }

    private companion object {
        private const val ACTIVITY_ID = 2L
        private const val ACTIVITY_IMAGE =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
    }
}