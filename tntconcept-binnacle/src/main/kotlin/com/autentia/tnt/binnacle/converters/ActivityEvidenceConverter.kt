package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.ActivityEvidence
import com.autentia.tnt.binnacle.exception.InvalidEvidenceFormatException
import com.autentia.tnt.binnacle.exception.InvalidEvidenceMimeTypeException
import jakarta.inject.Singleton
import java.util.*

@Singleton
internal class ActivityEvidenceConverter(private val appProperties: AppProperties) {
    companion object {
        private val regex = Regex("data:(\\w+/[-+.\\w]+),(\\w+)")
    }

    fun convertB64StringToActivityEvidence(evidenceBase64: String): ActivityEvidence {
        checkEvidenceFormat(evidenceBase64)

        val mimeType = getMimeType(evidenceBase64)
        checkValidMimeType(mimeType)

        val content = getContent(evidenceBase64)
        return ActivityEvidence(mimeType, content)
    }

    private fun checkEvidenceFormat(evidenceBase64: String) {
        regex.matches(evidenceBase64) || throw InvalidEvidenceFormatException()
    }

    private fun getMimeType(evidenceBase64: String): String {
        val regexGroupValues = regex.find(evidenceBase64)?.groupValues
        return regexGroupValues?.get(1) ?: ""
    }

    private fun checkValidMimeType(mimeType: String) {
        appProperties.files.validMimeTypes.contains(mimeType) || throw InvalidEvidenceMimeTypeException(mimeType)
    }

    private fun getContent(evidenceBase64: String): ByteArray {
        val regexGroupValues = regex.find(evidenceBase64)?.groupValues
        val contentString = regexGroupValues?.get(2) ?: throw IllegalStateException()
        return Base64.getDecoder().decode(contentString)
    }
}