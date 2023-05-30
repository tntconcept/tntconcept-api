package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.utils.BinnacleApiIllegalArgumentException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
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

    private val activityEvidenceService =
        ActivityEvidenceService(AppProperties().apply { files.activityImages = "/tmp" })
    private val date = Date.from(LocalDate.parse("2022-04-08").atStartOfDay(ZoneId.systemDefault()).toInstant())
    private val imageFilename = "/tmp/2022/4/2.jpg"

    @Nested
    inner class StoreImage {
        @Test
        fun `should create a new file with the decoded value of the image`() {
            val image = "SGVsbG8gV29ybGQh"

            activityEvidenceService.storeActivityEvidence(2L, image, date)

            val file = File(imageFilename)
            val content = file.readText()
            assertThat(content, `is`(equalTo("Hello World!")))

            file.delete()
        }

        @Test
        fun `should throw BinnacleApiIllegalArgumentException when image file is null`() {
            assertThrows<BinnacleApiIllegalArgumentException> {
                activityEvidenceService.storeActivityEvidence(1L, null, Date())
            }
        }

        @Test
        fun `should throw BinnacleApiIllegalArgumentException when image file is an empty string`() {
            assertThrows<BinnacleApiIllegalArgumentException> {
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

            assertThat(result, `is`(equalTo("SGVsbG8gV29ybGQh")))

            file.delete()
        }
    }
}
