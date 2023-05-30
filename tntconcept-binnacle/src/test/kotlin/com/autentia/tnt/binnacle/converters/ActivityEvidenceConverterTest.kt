package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.exception.InvalidEvidenceFormatException
import com.autentia.tnt.binnacle.exception.InvalidEvidenceMimeTypeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class ActivityEvidenceConverterTest {
    private val appProperties = AppProperties()
        .apply {
            files.validMimeTypes = listOf(
                "application/pdf",
                "image/png",
                "image/jpg",
                "image/jpeg",
                "image/gif"
            )
        }
    private val activityEvidenceConverter = ActivityEvidenceConverter(appProperties)

    @Test
    fun `should detect invalid format missing comma after mime type`() {
        val imageBase64 = "data:application/pdf:SGVsbG8gV29ybGQh"

        assertThrows<InvalidEvidenceFormatException> {
            activityEvidenceConverter.convertB64StringToActivityEvidence(
                imageBase64
            )
        }
    }

    @Test
    fun `should detect invalid format missing double dots after data`() {
        val imageBase64 = "data,application/pdf,SGVsbG8gV29ybGQh"

        assertThrows<InvalidEvidenceFormatException> {
            activityEvidenceConverter.convertB64StringToActivityEvidence(
                imageBase64
            )
        }
    }

    @Test
    fun `should throw invalid mime type when is not valid`() {
        val imageBase64 = "data:application/txt,SGVsbG8gV29ybGQh"

        assertThrows<InvalidEvidenceMimeTypeException> {
            activityEvidenceConverter.convertB64StringToActivityEvidence(
                imageBase64
            )
        }
    }

    @Test
    fun `should generate evidence with mime type pdf`() {
        val imageBase64 = "data:application/pdf,SGVsbG8gV29ybGQh"

        val activityEvidence = activityEvidenceConverter.convertB64StringToActivityEvidence(
            imageBase64
        )

        assertThat(activityEvidence.mimeType).isEqualTo("application/pdf")
    }

    @Test
    fun `should generate evidence content`() {
        val imageBase64 = "data:application/pdf,SGVsbG8gV29ybGQh"

        val activityEvidence = activityEvidenceConverter.convertB64StringToActivityEvidence(
            imageBase64
        )

        val expectedContent = Base64.getDecoder().decode("SGVsbG8gV29ybGQh")
        assertThat(activityEvidence.content).isEqualTo(expectedContent)
    }
}