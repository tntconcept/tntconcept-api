package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.ActivityEvidenceConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

internal class ActivityEvidenceServiceTest {
    private val appProperties = AppProperties().apply {
        files.evidencesPath = "/tmp"
        files.validMimeTypes = listOf(
            "application/pdf",
            "image/png",
            "image/jpg",
            "image/jpeg",
            "image/gif"
        )
    }
    private val activityEvidenceConverter = ActivityEvidenceConverter(appProperties)
    private val date = Date.from(LocalDate.parse("2022-04-08").atStartOfDay(ZoneId.systemDefault()).toInstant())
    private val activityEvidenceService = ActivityEvidenceService(activityEvidenceConverter, appProperties)
    private val imageFilename = "/tmp/2022/4/2.jpg"

    @Nested
    inner class StoreImage {
        @Test
        fun `should create a new file with the decoded value of the image`() {
            val evidenceBase64 = "data:image/jpg,SGVsbG8gV29ybGQh"

            activityEvidenceService.storeActivityEvidence(2L, evidenceBase64, date)

            val expectedEvidenceFilename = "/tmp/2022/4/2.jpg"
            val file = File(expectedEvidenceFilename)
            val content = File(expectedEvidenceFilename).readText()
            assertThat(content).isEqualTo("Hello World!")

            file.delete()
        }

        @Test
        fun `should throw BinnacleApiIllegalArgumentException when image file is null`() {
            assertThrows<IllegalArgumentException> {
                activityEvidenceService.storeActivityEvidence(1L, null, Date())
            }
        }

        @Test
        fun `should throw BinnacleApiIllegalArgumentException when image file is an empty string`() {
            assertThrows<IllegalArgumentException> {
                activityEvidenceService.storeActivityEvidence(1L, "", Date())
            }
        }
    }

    @Nested
    inner class DeleteImage {
        @Test
        fun `should return true when the file with the image has been deleted`() {
            File(imageFilename).createNewFile()

            val result = activityEvidenceService.deleteActivityEvidence(2L, date)

            assertTrue(result)
        }

        @Test
        fun `should return false when the file couldn't be deleted`() {
            val result = activityEvidenceService.deleteActivityEvidence(2L, date)

            assertFalse(result)
        }
    }

    @Nested
    inner class RetrievalImage {
        @Test
        fun `should return the stored image of the activity in base 64`() {
            val file = File(imageFilename)
            file.writeText("Hello World!")

            val result = activityEvidenceService.getActivityEvidenceAsBase64String(2L, date)

            assertThat(result).isEqualTo("SGVsbG8gV29ybGQh")

            file.delete()
        }
    }
}
