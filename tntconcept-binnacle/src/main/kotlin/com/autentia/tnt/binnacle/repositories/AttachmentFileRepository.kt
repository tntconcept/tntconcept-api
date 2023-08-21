package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Singleton
internal class AttachmentFileRepository(
        appProperties: AppProperties,
) {

    private val attachmentsPath = appProperties.files.attachmentsPath

    fun getAttachmentContent(attachmentInfo: AttachmentInfo): ByteArray {
        val fileName = this.getFileName(attachmentInfo)
        val filePath = Path.of(attachmentsPath, attachmentInfo.path, fileName)

        if (!Files.exists(filePath)) {
            throw AttachmentNotFoundException("Attachment file does not exist: $filePath")
        }

        return FileUtils.readFileToByteArray(File(filePath.toUri()))
    }

    fun storeAttachment(attachment: Attachment) {
        val extension = FilenameUtils.getExtension(attachment.info.fileName)
        require(isValidExtension(extension))

        val fileName = this.getFileName(attachment.info)
        val pathFile = Path.of(attachmentsPath, attachment.info.path, fileName)
        val attachmentFile = File(pathFile.toUri())

        FileUtils.writeByteArrayToFile(attachmentFile, attachment.file)
    }

    private fun isValidExtension(extension: String?) = extension != null && extension != ""

    private fun getFileName(attachmentInfo: AttachmentInfo) = "${attachmentInfo.id}.${FilenameUtils.getExtension(attachmentInfo.fileName)}"

}
