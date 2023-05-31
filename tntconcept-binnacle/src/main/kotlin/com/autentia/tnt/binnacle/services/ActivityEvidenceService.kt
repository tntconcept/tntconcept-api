package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.utils.takeMonth
import com.autentia.tnt.binnacle.core.utils.takeYear
import com.autentia.tnt.binnacle.entities.dto.EvidenceDTO
import com.autentia.tnt.binnacle.exception.InvalidEvidenceMimeTypeException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Singleton
internal class ActivityEvidenceService(
    private val appProperties: AppProperties,
) {

    fun storeActivityEvidence(activityId: Long, evidence: EvidenceDTO, insertDate: Date) {
        val fileName = filePath(insertDate, activityId)
        FileUtils.writeByteArrayToFile(File(fileName), Base64.getDecoder().decode(evidence.base64data))
    }

    fun deleteActivityEvidence(id: Long, insertDate: Date): Boolean =
        Files.deleteIfExists(Path.of(filePath(insertDate, id)))

    fun getActivityEvidenceAsBase64String(id: Long, insertDate: Date): String {
        val year = insertDate.takeYear()
        val month = insertDate.takeMonth()
        val fileContent = FileUtils.readFileToByteArray(File(filePath(year, month, id)))
        return Base64.getEncoder().encodeToString(fileContent)
    }

    private fun filePath(date: Date, id: Long) = filePath(date.takeYear(), date.takeMonth(), id)

    private fun filePath(year: Int, month: Int, id: Long) =
        "${appProperties.files.evidencesPath}/$year/$month/$id.jpg"

    private fun checkValidMimeType(mimeType: String) {
        appProperties.files.validMimeTypes.contains(mimeType) || throw InvalidEvidenceMimeTypeException(mimeType)
    }
}
