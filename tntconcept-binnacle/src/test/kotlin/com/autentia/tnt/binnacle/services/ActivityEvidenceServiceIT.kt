package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.InvalidEvidenceMimeTypeException
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityEvidenceServiceIT {
    private val appProperties = AppProperties().apply {
        files.evidencesPath = "src/test/resources/testfolder"
        files.supportedMimeTypes = mapOf(
            Pair("application/pdf", "pdf"),
            Pair("image/jpg", "jpg"),
            Pair("image/jpeg", "jpeg"),
            Pair("image/png", "png"),
            Pair("image/gif", "gif")
        )
    }
    private val date = Date.from(LocalDate.parse("2022-04-08").atStartOfDay(ZoneId.systemDefault()).toInstant())
    private val activityEvidenceService = ActivityEvidenceService(appProperties)

    @BeforeAll
    fun createTestFolder() {
        if (Files.notExists(Path.of(appProperties.files.evidencesPath))) {
            Files.createDirectory(Path.of(appProperties.files.evidencesPath))
        }
    }

    @AfterAll
    fun deleteContents() {
        FileUtils.deleteDirectory(File(appProperties.files.evidencesPath))
    }

    @Nested
    inner class StoreImage {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/pdf",
                "image/png",
                "image/jpg",
                "image/jpeg",
                "image/gif"
            ]
        )
        fun `should create a new file with the decoded value of the image`(mimeType: String) {
            val evidence = EvidenceDTO.from("data:$mimeType;base64,SGVsbG8gV29ybGQh").toDomain()
            activityEvidenceService.storeActivityEvidence(2L, evidence, date)

            val expectedExtension = getExtensionForMimeType(mimeType)
            val expectedEvidenceFilename = "${appProperties.files.evidencesPath}/2022/4/2.$expectedExtension"
            val file = File(expectedEvidenceFilename)
            val content = File(expectedEvidenceFilename).readText()

            file.delete()
            assertThat(content).isEqualTo("Hello World!")
            assertThat(file.exists()).isFalse()
        }

        private fun getExtensionForMimeType(mimeType: String): String {
            return appProperties.files.supportedMimeTypes[mimeType] ?: throw InvalidEvidenceMimeTypeException(mimeType)
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    inner class DeleteImage {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "jpg",
                "jpeg",
                "pdf",
                "png",
                "gif"
            ]
        )
        fun `should delete evidence with any valid file type`(fileExtension: String) {
            val file = File("${appProperties.files.evidencesPath}/2022/4/2.$fileExtension")
            file.createNewFile()

            val result = activityEvidenceService.deleteActivityEvidence(2L, date)

            assertTrue(result)
            assertThat(file.exists()).isFalse()
        }

        @Test
        fun `should return false when the file couldn't be deleted`() {
            val result = activityEvidenceService.deleteActivityEvidence(2L, date)

            assertFalse(result)
        }
    }

    @Nested
    inner class RetrievalImage {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/pdf",
                "image/png",
                "image/jpeg",
                "image/gif"
            ]
        )
        fun `should return the stored image of the activity as evidence dto`(mimeType: String) {
            val fileExtension = appProperties.files.supportedMimeTypes[mimeType] ?: fail("Invalid mime type")
            val file = File("${appProperties.files.evidencesPath}/2022/4/2.$fileExtension")
            file.writeText("Hello World!")

            val result = activityEvidenceService.getActivityEvidence(2L, date)

            file.delete()
            assertThat(result).isEqualTo(EvidenceDTO(mimeType, "SGVsbG8gV29ybGQh"))
        }
    }
}
