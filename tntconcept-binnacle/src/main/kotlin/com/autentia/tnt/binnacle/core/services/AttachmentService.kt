package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.core.domain.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import com.autentia.tnt.binnacle.services.DateService
import jakarta.inject.Singleton
import org.apache.commons.io.FilenameUtils
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*

@Singleton
internal class AttachmentService(
        private val attachmentFileStorage: AttachmentFileStorage,
        private val attachmentInfoRepository: AttachmentInfoRepository,
        private val dateService: DateService,
        appProperties: AppProperties) {

    private val supportedMimeExtensions: Map<String, String> = appProperties.files.supportedMimeTypes

    fun createAttachment(fileName: String, mimeType: String, file: ByteArray, userId: Long): Attachment {
        if (!isMimeTypeSupported(mimeType, fileName))
            throw AttachmentMimeTypeNotSupportedException("Mime type $mimeType} is not supported")

        val attachment = this.createAttachment(
                id = UUID.randomUUID(),
                fileName = fileName,
                mimeType = mimeType,
                file = file,
                userId = userId)

        attachmentFileStorage.storeAttachmentFile(attachment.info.path, attachment.file)
        attachmentInfoRepository.save(attachment.info)

        return attachment
    }

    fun findAttachment(id: UUID): Attachment {
        val attachmentInfo = attachmentInfoRepository.findById(id).orElseThrow { AttachmentNotFoundException() }
        val attachmentFile = attachmentFileStorage.retrieveAttachmentFile(attachmentInfo.path)
        return Attachment(attachmentInfo, attachmentFile)
    }

    private fun isMimeTypeSupported(mimeType: String, fileName: String): Boolean
        = supportedMimeExtensions.containsKey(mimeType) && supportedMimeExtensions[mimeType] == FilenameUtils.getExtension(fileName)

    private fun createAttachment(id: UUID, fileName: String, mimeType: String, file: ByteArray, userId: Long): Attachment {
        val currentDate = this.dateService.getDateNow()
        val attachmentInfo = AttachmentInfo(
                id = id,
                fileName = fileName,
                mimeType = mimeType,
                uploadDate = currentDate,
                isTemporary = true,
                userId = userId,
                path = this.createPathForAttachment(id, currentDate, fileName).toString()
        )

        return Attachment(attachmentInfo, file)
    }

    private fun createPathForAttachment(id: UUID, currentDate: LocalDateTime, fileName: String): Path {
        val fileExtension = FilenameUtils.getExtension(fileName)
        return Path.of("/", "${currentDate.year}", "${currentDate.monthValue}", "$id.$fileExtension")
    }
}
