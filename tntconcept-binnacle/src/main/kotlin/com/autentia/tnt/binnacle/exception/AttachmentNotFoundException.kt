package com.autentia.tnt.binnacle.exception

class AttachmentNotFoundException(message: String) : BinnacleException(message) {

    constructor() : this("Attachment does not exist")
}