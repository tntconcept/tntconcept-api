package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createAttachmentInfoEntity
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ActivityEvidenceResponseConverterTest {

    private val activityEvidenceResponseConverter = ActivityEvidenceResponseConverter()

    @Test
    fun `given entity Activity should return domain the evidences UUID's`() {
        val attachmentInfo1 = createAttachmentInfoEntity()
        val attachmentInfo2 = createAttachmentInfoEntity()
        val evidences = mutableListOf(attachmentInfo1, attachmentInfo2)

        val expectedUrl1 =  attachmentInfo1.id
        val expectedUrl2 =  attachmentInfo2.id

        val activityWithEvidences = createActivity(evidences = evidences)
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUUIDs(activityWithEvidences)
        assertTrue(evidencesUrls.isNotEmpty())
        assertTrue(evidencesUrls.contains(expectedUrl1.toString()))
        assertTrue(evidencesUrls.contains(expectedUrl2.toString()))

    }

    @Test
    fun `given entity Activity without evidences should return empty evidences UUID's`() {
        val activityWithoutEvidence = createActivity().toDomain()
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUUIDs(activityWithoutEvidence)
        assertTrue(evidencesUrls.isEmpty())
    }


    @Test
    fun `given domain Activity should return domain the evidences UUID's`() {
        val attachmentInfo1 = createAttachmentInfoEntity()
        val attachmentInfo2 = createAttachmentInfoEntity()
        val evidences = mutableListOf(attachmentInfo1, attachmentInfo2)

        val expectedUrl1 = attachmentInfo1.id
        val expectedUrl2 = attachmentInfo2.id

        val activityWithEvidences = createActivity(evidences = evidences).toDomain()
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUUIDs(activityWithEvidences)
        assertTrue(evidencesUrls.isNotEmpty())
        assertTrue(evidencesUrls.contains(expectedUrl1.toString()))
        assertTrue(evidencesUrls.contains(expectedUrl2.toString()))

    }

    @Test
    fun `given domain Activity without evidences should return empty evidences UUID's`() {
        val activityWithoutEvidence = createActivity().toDomain()
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUUIDs(activityWithoutEvidence)
        assertTrue(evidencesUrls.isEmpty())
    }


}