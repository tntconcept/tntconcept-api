package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.utils.takeMonth
import com.autentia.tnt.binnacle.core.utils.takeYear
import com.autentia.tnt.binnacle.utils.BinnacleApiIllegalArgumentException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*

@Singleton
internal class ActivityEvidenceService(
    private val appProperties: AppProperties,
) {

    fun storeActivityEvidence(activityId: Long, imageFile: String?, insertDate: Date) {
        if (imageFile == null || imageFile == "") {
            throw BinnacleApiIllegalArgumentException("With hasEvidences = true, imageFile could not be null")
        }

        val fileContent = Base64.getDecoder().decode(imageFile)
        val fileName = filePath(insertDate, activityId)

        FileUtils.writeByteArrayToFile(File(fileName), fileContent)
    }

    fun deleteActivityEvidence(id: Long, insertDate: Date): Boolean =
        File(filePath(insertDate, id)).delete()

    fun getActivityEvidenceAsBase64String(id: Long, insertDate: Date): String {
        val year = insertDate.takeYear()
        val month = insertDate.takeMonth()
        val fileContent = FileUtils.readFileToByteArray(File(filePath(year, month, id)))
        return Base64.getEncoder().encodeToString(fileContent)
    }

    private fun filePath(date: Date, id: Long) = filePath(date.takeYear(), date.takeMonth(), id)

    private fun filePath(year: Int, month: Int, id: Long) =
        "${appProperties.files.activityImages}/$year/$month/$id.jpg"
}
