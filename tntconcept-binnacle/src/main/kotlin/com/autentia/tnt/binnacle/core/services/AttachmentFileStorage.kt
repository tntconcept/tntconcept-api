package com.autentia.tnt.binnacle.core.services

internal interface AttachmentFileStorage {

    fun storeAttachmentFile(filePath: String, file: ByteArray)

    fun retrieveAttachmentFile(filePath: String) : ByteArray
}