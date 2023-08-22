package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Singleton
internal class FileSystemAttachmentFileStorage(appProperties: AppProperties): AttachmentFileStorage {

    private val attachmentsPath = appProperties.files.attachmentsPath

    override fun storeAttachmentFile(filePath: String, file: ByteArray) {
        val absolutePath = Path.of(attachmentsPath, filePath)
        val attachmentFile = File(absolutePath.toUri())

        FileUtils.writeByteArrayToFile(attachmentFile, file)
    }

    override fun retrieveAttachmentFile(filePath: String): ByteArray {
        val absoluteFilePath = Path.of(attachmentsPath, filePath)

        if (!Files.exists(absoluteFilePath)) {
            throw AttachmentNotFoundException("Attachment file does not exist: $filePath")
        }

        return FileUtils.readFileToByteArray(File(absoluteFilePath.toUri()))
    }
}