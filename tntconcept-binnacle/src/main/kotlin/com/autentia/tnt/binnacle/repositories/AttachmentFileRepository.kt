package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.entities.AttachmentType
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Singleton
internal class AttachmentFileRepository(
    appProperties: AppProperties,
) {

    private val evidencesPath: String


    init {
        evidencesPath = appProperties.files.evidencesPath
    }

    fun getAttachment(path: String, type: AttachmentType, fileName: String): ByteArray {
        val filePath = Path.of(getBasePath(type), path, fileName)

        if (!Files.exists(filePath)) {
            throw AttachmentNotFoundException("Attachment file does not exist: $filePath")
        }

        return FileUtils.readFileToByteArray(File(filePath.toUri()))
    }

    fun getBasePath(type: AttachmentType): String {
        // TODO extract to a different class
        return when (type) {
            AttachmentType.EVIDENCE -> evidencesPath
        }
    }

    fun storeAttachment(attachmentInfo: AttachmentInfo, fileByteArray: ByteArray) {

        val pathFile = Path.of(getBasePath(attachmentInfo.type), attachmentInfo.path, attachmentInfo.fileName)

        val attachmentFile = File(pathFile.toUri())

        FileUtils.writeByteArrayToFile(attachmentFile, fileByteArray)
    }
}
