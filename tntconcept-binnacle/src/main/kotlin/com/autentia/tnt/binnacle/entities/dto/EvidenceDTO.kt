package com.autentia.tnt.binnacle.entities.dto


data class EvidenceDTO private constructor(
    val mediaType: String, val base64Data: String
) {
    companion object {
        fun from(value: String): EvidenceDTO {
            if (!isInMediaTypeFormat(value)) {
                throw IllegalArgumentException("Value not supported")
            }

            val values = splitValues(value)
            return EvidenceDTO(values[0], values[1])
        }

        private fun splitValues(value: String): Array<String> {
            return value.split(",").toTypedArray()
        }

        private fun isInMediaTypeFormat(value: String): Boolean {
            return value.startsWith("data:")
        }
    }
}