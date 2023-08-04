package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Evidence
import com.autentia.tnt.binnacle.core.utils.takeMonth
import com.autentia.tnt.binnacle.core.utils.takeYear
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.InvalidEvidenceMimeTypeException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Singleton
internal class ActivityEvidenceService(
    appProperties: AppProperties,
) {
    private val evidencesPath: String
    private val supportedMimeExtensions: Map<String, String>

    init {
        evidencesPath = appProperties.files.evidencesPath
        supportedMimeExtensions = appProperties.files.supportedMimeTypes
    }

    fun getActivityEvidence(id: Long, insertDate: Date): EvidenceDTO {
        val supportedExtensions = getSupportedExtensions()
        for (supportedExtension: String in supportedExtensions) {
            val filePathWithExtension = getFilePath(insertDate, id, supportedExtension)
            if (Files.exists(filePathWithExtension)) {
                val fileContents = FileUtils.readFileToByteArray(File(filePathWithExtension.toUri()))
                val base64contents = Base64.getEncoder().encodeToString(fileContents)
                return EvidenceDTO(Files.probeContentType(filePathWithExtension), base64contents)
            }
        }
        throw FileNotFoundException()
    }


    fun storeActivityEvidence(activityId: Long, evidence: Evidence, insertDate: Date) {
        require(isMimeTypeSupported(evidence.mediaType)) { "Mime type ${evidence.mediaType} is not supported" }

        val evidenceFile = getEvidenceFile(evidence, insertDate, activityId)
        val decodedFileContent = Base64.getDecoder().decode(evidence.base64data)
        FileUtils.writeByteArrayToFile(evidenceFile, decodedFileContent)
    }

    fun deleteActivityEvidence(id: Long, insertDate: Date): Boolean {
        val supportedExtensions = getSupportedExtensions()
        for (supportedExtension in supportedExtensions) {
            val filePath = getFilePath(insertDate, id, supportedExtension)
            if (Files.deleteIfExists(filePath)) {
                return true
            }
        }
        return false
    }

    private fun getEvidenceFile(
        evidence: Evidence,
        insertDate: Date,
        activityId: Long,
    ): File {
        val fileExtension = getExtensionForMimeType(evidence.mediaType)
        val fileName = getFilePath(insertDate, activityId, fileExtension)
        return File(fileName.toUri())
    }


    private fun getSupportedExtensions() = supportedMimeExtensions.values.distinct().toList()

    private fun getFilePath(date: Date, id: Long, fileExtension: String) =
        getFilePath(date.takeYear(), date.takeMonth(), id, fileExtension)

    private fun getFilePath(year: Int, month: Int, id: Long, fileExtension: String) =
        Path.of(evidencesPath, "$year", "$month", "$id.$fileExtension")

    private fun isMimeTypeSupported(mimeType: String): Boolean = supportedMimeExtensions.containsKey(mimeType)

    private fun getExtensionForMimeType(mimeType: String): String =
        supportedMimeExtensions[mimeType] ?: throw InvalidEvidenceMimeTypeException(mimeType)

}
