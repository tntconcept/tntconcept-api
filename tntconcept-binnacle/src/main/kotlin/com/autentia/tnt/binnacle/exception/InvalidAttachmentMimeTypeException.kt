package com.autentia.tnt.binnacle.exception

class InvalidAttachmentMimeTypeException(mimeType: String) :
    BinnacleException("Unsupported attachment file format: ${mimeType}")