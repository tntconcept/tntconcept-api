package com.autentia.tnt.binnacle.entities.dto

import com.autentia.tnt.binnacle.exception.InvalidEvidenceFormatException


data class EvidenceDTO internal constructor(val mediaType: String, val base64data: String) {
    companion object {
        private val regex = Regex("^data:([^,]+);base64,(.+\$)")

        fun from(value: String): EvidenceDTO {
            if (!isInMediaTypeFormat(value)) {
                throw InvalidEvidenceFormatException("$value is not in the correct format")
            }

            val mimeType = getMimeType(value)
            val content = getContent(value)

            return EvidenceDTO(mimeType, content)
        }

        private fun getMimeType(evidenceBase64: String): String {
            val regexGroupValues = regex.find(evidenceBase64)?.groupValues
            return regexGroupValues?.get(1) ?: throw IllegalStateException()
        }

        private fun getContent(evidenceBase64: String): String {
            val regexGroupValues = regex.find(evidenceBase64)?.groupValues
            return regexGroupValues?.get(2) ?: throw IllegalStateException()
        }

        private fun isInMediaTypeFormat(value: String): Boolean = regex.matches(value)
    }

    fun getDataUrl(): String = "data:${mediaType};base64,${base64data}"

}