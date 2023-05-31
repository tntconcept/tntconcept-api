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
    @Test
    fun `should detect invalid format missing comma after mime type`() {
        assertThatThrownBy { EvidenceDTO.from("data:application/pdf:SGVsbG8gV29ybGQh") }.isInstanceOf(
            InvalidEvidenceFormatException::class.java
        )
        assertThatThrownBy { EvidenceDTO.from("data,application/pdf,SGVsbG8gV29ybGQh") }.isInstanceOf(
            InvalidEvidenceFormatException::class.java
        )

    }

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
        arrayOf("data:image/jpg;base64,SGVsbG8gV29ybGQh", "image/jpg", "SGVsbG8gV29ybGQh")
    )

}