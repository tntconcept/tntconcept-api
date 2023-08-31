package com.autentia.tnt.binnacle.core.services

internal interface AttachmentStorage {

    fun storeAttachmentFile(filePath: String, file: ByteArray)

    fun retrieveAttachmentFile(filePath: String) : ByteArray

    fun deleteAttachmentFile(filePath: String)
}