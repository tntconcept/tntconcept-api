package com.autentia.tnt.binnacle.core.domain

data class ActivityEvidence(val mimeType: String, val content: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivityEvidence) return false

        if (mimeType != other.mimeType) return false
        return content.contentEquals(other.content)
    }

    override fun hashCode(): Int {
        var result = mimeType.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}