package com.autentia.tnt.binnacle.exception

class AttachmentMimeTypeNotSupportedException(message: String) : BinnacleException(message) {

    constructor() : this("Attachment mimetype is not supported")
}