package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
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

    private val attachmentsPath: String


    init {
        attachmentsPath = appProperties.files.attachmentsPath
    }

    fun getAttachment(path: String, fileName: String): ByteArray {
        val filePath = Path.of(attachmentsPath, path, fileName)

        if (!Files.exists(filePath)) {
            throw AttachmentNotFoundException("Attachment file does not exist: $filePath")
        }

        return FileUtils.readFileToByteArray(File(filePath.toUri()))
    }

    fun storeAttachment(attachmentInfo: AttachmentInfo, fileByteArray: ByteArray) {

        val pathFile = Path.of(attachmentsPath, attachmentInfo.path, attachmentInfo.fileName)

        val attachmentFile = File(pathFile.toUri())

        FileUtils.writeByteArrayToFile(attachmentFile, fileByteArray)
    }

    fun deleteActivityEvidence(attachmentInfo: List<AttachmentInfo>) {
        attachmentInfo.forEach {
            val filePath = Path.of(attachmentsPath, it.path, it.fileName)
            Files.deleteIfExists(filePath)
        }
    }
}
