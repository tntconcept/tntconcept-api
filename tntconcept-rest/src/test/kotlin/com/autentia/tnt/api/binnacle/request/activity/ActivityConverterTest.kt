package com.autentia.tnt.api.binnacle.request.activity

import com.autentia.tnt.api.binnacle.activity.ActivityConverter
import com.autentia.tnt.api.binnacle.activity.ActivityRequest
import com.autentia.tnt.api.binnacle.activity.TimeInterval
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityConverterTest {

    private val sut = ActivityConverter()

    @Test
    fun convertTo_shouldConvertActivityRequestToActivityUseCaseRequest_WithEvidence() {
        // Arrange
        val startDateTime = LocalDateTime.of(2023, 5, 30, 10, 0, 0)
        val endDateTime = LocalDateTime.of(2023, 5, 30, 12, 0, 0)

        val activityRequest = ActivityRequest(
            id = 1,
            interval = TimeInterval(start = startDateTime, end = endDateTime),
            description = "Test activity",
            billable = true,
            projectRoleId = 2,
            hasEvidences = true,
            evidence = "data:image/jpg;base64,SGVsbG8gV29ybGQh"
        )

        // Act
        val result = sut.convertTo(activityRequest)

        // Assert
        assertThat(result.id).isEqualTo(activityRequest.id)
        assertThat(result.interval.start).isEqualTo(startDateTime)
        assertThat(result.interval.end).isEqualTo(endDateTime)
        assertThat(result.description).isEqualTo(activityRequest.description)
        assertThat(result.billable).isEqualTo(activityRequest.billable)
        assertThat(result.projectRoleId).isEqualTo(activityRequest.projectRoleId)
        assertThat(result.hasEvidences).isEqualTo(activityRequest.hasEvidences)
        assertThat(result.evidence).isNotNull()
    }

    @Test
    fun convertTo_shouldConvertActivityRequestToActivityUseCaseRequest_WithoutEvidence() {
        // Arrange
        val startDateTime = LocalDateTime.of(2023, 5, 30, 10, 0, 0)
        val endDateTime = LocalDateTime.of(2023, 5, 30, 12, 0, 0)
        val activityRequest = ActivityRequest(
            id = 1,
            interval = TimeInterval(start = startDateTime, end = endDateTime),
            description = "Test activity",
            billable = true,
            projectRoleId = 2,
            hasEvidences = false,
            evidence = null
        )

        // Act
        val result = sut.convertTo(activityRequest)

        // Assert
        assertThat(result.id).isEqualTo(activityRequest.id)
        assertThat(result.interval.start).isEqualTo(startDateTime)
        assertThat(result.interval.end).isEqualTo(endDateTime)
        assertThat(result.description).isEqualTo(activityRequest.description)
        assertThat(result.billable).isEqualTo(activityRequest.billable)
        assertThat(result.projectRoleId).isEqualTo(activityRequest.projectRoleId)
        assertThat(result.hasEvidences).isEqualTo(activityRequest.hasEvidences)
        assertThat(result.evidence).isNull()
    }
}
