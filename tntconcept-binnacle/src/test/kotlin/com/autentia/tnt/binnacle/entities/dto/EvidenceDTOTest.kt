package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.exception.InvalidEvidenceFormatException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EvidenceDTOTest {
    @ParameterizedTest
    @MethodSource("wrongEvidencesInput")
    fun `should detect invalid format missing comma after mime type`(plainBase64Content: String) {
        assertThatThrownBy { EvidenceDTO.from(plainBase64Content) }.isInstanceOf(
            InvalidEvidenceFormatException::class.java
        )
    }

    private fun wrongEvidencesInput() = arrayOf(
        arrayOf("data:application/pdf:SGVsbG8gV29ybGQh"),
        arrayOf("data,application/pdf,SGVsbG8gV29ybGQh"),
        arrayOf("data:data:;baseSGVsbG8gV29ybGQh"),
    )

    @ParameterizedTest
    @MethodSource("evidencesInput")
    fun `should generate evidence with mime type`(
        plainBase64Content: String, expectedMediaType: String, expectedBase64data: String
    ) {
        val activityEvidence = EvidenceDTO.from(plainBase64Content)

        assertThat(activityEvidence.mediaType).isEqualTo(expectedMediaType)
        assertThat(activityEvidence.base64data).isEqualTo(expectedBase64data)
    }

    private fun evidencesInput() = arrayOf(
        arrayOf("data:application/pdf;base64,SGVsbG8gV29ybGQh", "application/pdf", "SGVsbG8gV29ybGQh"),
        arrayOf("data:image/png;base64,ABCsbG8gV29ybGQh", "image/png", "ABCsbG8gV29ybGQh"),
        arrayOf("data:image/jpg;base64,SGVsbG8gV29ybGQh", "image/jpg", "SGVsbG8gV29ybGQh"),
        arrayOf("data:customType;base64,SGVsbG8gV29ybGQh", "customType", "SGVsbG8gV29ybGQh")
    )

    @Test
    fun `should build a data url`() {
        // Given
        val aDataUrl = "data:image/jpg;base64,SGVsbG8gV29ybGQh"
        val sampleEvidence = EvidenceDTO.from(aDataUrl)

        // When
        val result = sampleEvidence.getDataUrl()

        // Then
        assertThat(result).isEqualTo(aDataUrl)
    }


}