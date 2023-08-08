package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.core.domain.AttachmentType
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import jakarta.inject.Singleton
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Singleton
internal class AttachmentFileRepository(
    appProperties: AppProperties,
) {

    private val evidencesPath: String
    private val supportedMimeExtensions: Map<String, String>

    init {
        evidencesPath = appProperties.files.evidencesPath
        supportedMimeExtensions = appProperties.files.supportedMimeTypes
    }

    fun getAttachment(path: String, type: AttachmentType, fileName: String): ByteArray {
        val filePath = Path.of(getBasePath(type), path, fileName)

        if (!Files.exists(filePath)) {
            throw AttachmentNotFoundException("Attachment file does not exist: $filePath")
        }

        return FileUtils.readFileToByteArray(File(filePath.toUri()))
    }

    fun getBasePath(type: AttachmentType): String {
        // TODO extract to a different class
        return when (type) {
            AttachmentType.EVIDENCE -> evidencesPath
        }
    }

    private fun getSupportedExtensions() = supportedMimeExtensions.values.distinct().toList()


    /*
        fun storeActivityEvidence(activityId: Long, evidence: Evidence, insertDate: Date) {
            require(isMimeTypeSupported(evidence.mediaType)) { "Mime type ${evidence.mediaType} is not supported" }

            val evidenceFile = getEvidenceFile(evidence, insertDate, activityId)
            val decodedFileContent = Base64.getDecoder().decode(evidence.base64data)
            FileUtils.writeByteArrayToFile(evidenceFile, decodedFileContent)
        }

        fun deleteActivityEvidence(id: Long, insertDate: Date): Boolean {
            val supportedExtensions = getSupportedExtensions()
            for (supportedExtension in supportedExtensions) {
                val filePath = getFilePath(insertDate, id, supportedExtension)
                if (Files.deleteIfExists(filePath)) {
                    return true
                }
            }
            return false
        }

        private fun getEvidenceFile(
            evidence: Evidence,
            insertDate: Date,
            activityId: Long,
        ): File {
            val fileExtension = getExtensionForMimeType(evidence.mediaType)
            val fileName = getFilePath(insertDate, activityId, fileExtension)
            return File(fileName.toUri())
        }

        private fun isMimeTypeSupported(mimeType: String): Boolean = supportedMimeExtensions.containsKey(mimeType)

        private fun getExtensionForMimeType(mimeType: String): String =
            supportedMimeExtensions[mimeType] ?: throw InvalidEvidenceMimeTypeException(mimeType)*/

}
