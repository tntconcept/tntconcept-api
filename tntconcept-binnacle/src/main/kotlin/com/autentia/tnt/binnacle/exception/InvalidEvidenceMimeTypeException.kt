package com.autentia.tnt.binnacle.exception

class InvalidEvidenceMimeTypeException(mimeType: String) :
    BinnacleException("Unsupported evidence file format: ${mimeType}")