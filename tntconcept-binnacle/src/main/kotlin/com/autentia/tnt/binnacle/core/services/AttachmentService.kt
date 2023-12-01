package com.autentia.tnt.binnacle.core.services

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.entities.AttachmentInfo
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
        private val attachmentStorage: AttachmentStorage,
        private val attachmentInfoRepository: AttachmentInfoRepository,
        private val dateService: DateService,
        appProperties: AppProperties) {

    private val supportedMimeExtensions: Map<String, List<String>> = appProperties.files.supportedMimeTypes.mapValues { entry ->
        entry.value.split(",").map { it.trim() }
    }

    fun createAttachment(fileName: String, mimeType: String, file: ByteArray, userId: Long): Attachment {
        if (!isMimeTypeSupported(mimeType, fileName))
            throw AttachmentMimeTypeNotSupportedException("Mime type $mimeType with extension ${FilenameUtils.getExtension(fileName)} is not supported")

        val currentDate = this.dateService.getDateNow()
        val id = UUID.randomUUID()
        val attachmentInfo = AttachmentInfo(
                id = id,
                fileName = fileName,
                mimeType = mimeType,
                uploadDate = currentDate,
                isTemporary = true,
                userId = userId,
                path = this.createPathForAttachment(id, currentDate, fileName).toString()
        )

        val attachment = Attachment(attachmentInfo.toDomain(), file)

        attachmentStorage.storeAttachmentFile(attachment.info.path, attachment.file)
        attachmentInfoRepository.save(attachmentInfo)

        return attachment
    }

    fun findAttachment(id: UUID): Attachment {
        val attachmentInfo = attachmentInfoRepository.findById(id).orElseThrow { AttachmentNotFoundException() }
        val attachmentFile = attachmentStorage.retrieveAttachmentFile(attachmentInfo.path)
        return Attachment(attachmentInfo.toDomain(), attachmentFile)
    }

    fun removeAttachments(attachments: List<AttachmentInfo>) {
        if (attachments.isEmpty()) return

        attachments.forEach {
            attachmentStorage.deleteAttachmentFile(it.path)
        }

        attachmentInfoRepository.delete(attachments.map { it.id })
    }

    private fun isMimeTypeSupported(mimeType: String, fileName: String): Boolean = supportedMimeExtensions.containsKey(mimeType)
            && supportedMimeExtensions[mimeType]?.contains(FilenameUtils.getExtension(fileName)) ?: false

    fun createPathForAttachment(id: UUID, currentDate: LocalDateTime, fileName: String): Path {
        val fileExtension = FilenameUtils.getExtension(fileName)
        return Path.of("/", "${currentDate.year}", "${currentDate.monthValue}", "$id.$fileExtension")
    }
}
