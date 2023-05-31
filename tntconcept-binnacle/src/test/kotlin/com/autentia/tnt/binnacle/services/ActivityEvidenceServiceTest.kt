package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
    private val date = Date.from(LocalDate.parse("2022-04-08").atStartOfDay(ZoneId.systemDefault()).toInstant())
    private val activityEvidenceService = ActivityEvidenceService(appProperties)
    private val imageFilename = "/tmp/2022/4/2.jpg"

    private val evidence = EvidenceDTO.from("data:image/jpg;base64,SGVsbG8gV29ybGQh")

    @Nested
    inner class StoreImage {
        @Test
        fun `should create a new file with the decoded value of the image`() {
            val evidenceBase64 = "data:image/jpg,SGVsbG8gV29ybGQh"

            activityEvidenceService.storeActivityEvidence(2L, evidence, date)

            val expectedEvidenceFilename = "/tmp/2022/4/2.jpg"
            val file = File(expectedEvidenceFilename)
            val content = File(expectedEvidenceFilename).readText()
            assertThat(content).isEqualTo("Hello World!")

            file.delete()
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
