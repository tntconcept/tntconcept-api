package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.entities.AttachmentInfo
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.repositories.AttachmentInfoRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import java.util.*
import javax.transaction.Transactional

@Singleton
internal class AttachmentInfoService(
    private val attachmentInfoRepository: AttachmentInfoRepository
) {

    @Transactional
    @ReadOnly
    fun getAttachments(ids: List<UUID>): List<AttachmentInfo> {
        val attachmentInfos: MutableList<AttachmentInfo> = arrayListOf()
        for (id in ids) {
            val attachmentInfo = attachmentInfoRepository.findById(id).orElseThrow { AttachmentNotFoundException() }
            attachmentInfos.add(attachmentInfo)
        }
        return attachmentInfos
    }


    fun markAttachmentsAsNonTemporary(ids: List<UUID>) {
        for (id in ids) {
            attachmentInfoRepository.updateIsTemporary(id, false)
        }
    }

    fun markAttachmentsAsTemporary(ids: List<UUID>) {
        for (id in ids) {
            attachmentInfoRepository.updateIsTemporary(id, true)
        }
    }

}
