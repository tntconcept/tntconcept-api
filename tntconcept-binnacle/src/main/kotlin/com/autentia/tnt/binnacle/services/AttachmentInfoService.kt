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
    fun getActivityEvidences(ids: List<UUID>): List<AttachmentInfo> {
        val attachmentInfos: MutableList<AttachmentInfo> = arrayListOf()
        for(id in ids) {
            val attachmentInfo = attachmentInfoRepository.findById(id) ?: throw AttachmentNotFoundException()
            attachmentInfos.add(attachmentInfo)
        }
        return attachmentInfos
    }


}
