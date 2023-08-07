package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import jakarta.inject.Singleton
import java.util.*

@Singleton
class AttachmentRetrievalUseCase internal constructor(
    private val attachmentInfoRepository: AttachmentInfoRepository,
    private val attachmentFileRepository: AttachmentFileRepository,
) {

    fun getAttachmentFile(id: UUID): ByteArray {
        val IMAGE_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="

        //val attachmentInfo = attachmentInfoRepository.findById(id) ?: throw EvidenceNotFoundException()

        //return attachmentFileRepository.getAttachment(
        //    attachmentInfo.path,
        //    attachmentInfo.type,
        //    attachmentInfo.fileName
        //)
        return Base64.getDecoder().decode(IMAGE_BASE64)
    }


}