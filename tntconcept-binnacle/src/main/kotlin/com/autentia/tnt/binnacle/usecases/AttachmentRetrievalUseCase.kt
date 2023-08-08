package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.core.domain.Attachment
import com.autentia.tnt.binnacle.exception.EvidenceNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentFileRepository
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import jakarta.inject.Singleton
import java.util.*

@Singleton
class AttachmentRetrievalUseCase internal constructor(
    private val attachmentInfoRepository: AttachmentInfoRepository,
    private val attachmentFileRepository: AttachmentFileRepository,
) {

    fun getAttachment(id: UUID): Attachment {
        val attachmentInfo = attachmentInfoRepository.findById(id).orElseThrow { EvidenceNotFoundException() }

        val attachmentFile =  attachmentFileRepository.getAttachment( attachmentInfo.path, attachmentInfo.type, attachmentInfo.fileName)

        return Attachment(attachmentInfo.toDomain(), attachmentFile)
    }


}