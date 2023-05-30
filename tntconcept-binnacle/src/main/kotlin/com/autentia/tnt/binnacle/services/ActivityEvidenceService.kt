package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.converters.ActivityEvidenceConverter
import com.autentia.tnt.binnacle.core.utils.takeMonth
import com.autentia.tnt.binnacle.core.utils.takeYear
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Singleton
internal class ActivityEvidenceService(
    private val activityEvidenceConverter: ActivityEvidenceConverter,
    private val appProperties: AppProperties,
) {

    fun storeActivityEvidence(activityId: Long, evidenceBase64: String?, insertDate: Date) {
        require(!evidenceBase64.isNullOrEmpty()) { "With hasEvidences = true, imageFile could not be null" }

        val evidence = activityEvidenceConverter.convertB64StringToActivityEvidence(evidenceBase64)
        val fileName = filePath(insertDate, activityId)
        FileUtils.writeByteArrayToFile(File(fileName), evidence.content)
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
}
