package com.autentia.tnt.binnacle.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.utils.takeMonth
import com.autentia.tnt.binnacle.core.utils.takeYear
import com.autentia.tnt.binnacle.utils.BinnacleApiIllegalArgumentException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Base64
import java.util.Date

@Singleton
internal class ActivityImageService(
    private val appProperties: AppProperties,
) {

    fun storeActivityImage(activityId: Long, imageFile: String?, insertDate: Date) {
        if (imageFile == null || imageFile == "") {
            throw BinnacleApiIllegalArgumentException("With hasEvidences = true, imageFile could not be null")
        }

        val fileContent = Base64.getDecoder().decode(imageFile)
        val fileName = filePath(insertDate, activityId)

        FileUtils.writeByteArrayToFile(File(fileName), fileContent)
    }

    fun deleteActivityImage(id: Long, insertDate: Date): Boolean =
        File(filePath(insertDate, id)).delete()

    fun getActivityImageAsBase64(id: Long, insertDate: Date): String {
        val year = insertDate.takeYear()
        val month = insertDate.takeMonth()
        val fileContent = FileUtils.readFileToByteArray(File(filePath(year, month, id)))
        return Base64.getEncoder().encodeToString(fileContent)
    }

    private fun filePath(date: Date, id: Long) = filePath(date.takeYear(), date.takeMonth(), id)

    private fun filePath(year: Int, month: Int, id: Long) =
        "${appProperties.files.activityImages}/$year/$month/$id.jpg"
}
