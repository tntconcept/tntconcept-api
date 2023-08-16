package com.autentia.tnt.binnacle.converters

import com.autentia.tnt.binnacle.config.createActivity
import com.autentia.tnt.binnacle.config.createAttachmentInfoEntity
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ActivityEvidenceResponseConverterTest {

    private val activityEvidenceResponseConverter = ActivityEvidenceResponseConverter()

    @Test
    fun `given entity Activity should return domain the evidences urls`() {
        val attachmentInfo1 = createAttachmentInfoEntity()
        val attachmentInfo2 = createAttachmentInfoEntity()
        val evidences = mutableListOf(attachmentInfo1, attachmentInfo2)

        val expectedUrl1 = ActivityEvidenceResponseConverter.ATTACHMENT_API_PREFIX + attachmentInfo1.id
        val expectedUrl2 = ActivityEvidenceResponseConverter.ATTACHMENT_API_PREFIX + attachmentInfo2.id

        val activityWithEvidences = createActivity(evidences = evidences)
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUrls(activityWithEvidences)
        assertTrue(evidencesUrls.isNotEmpty())
        assertTrue(evidencesUrls.contains(expectedUrl1))
        assertTrue(evidencesUrls.contains(expectedUrl2))

    }

    @Test
    fun `given entity Activity without evidences should return empty evidences urls`() {
        val activityWithoutEvidence = createActivity().toDomain()
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUrls(activityWithoutEvidence)
        assertTrue(evidencesUrls.isEmpty())
    }


    @Test
    fun `given domain Activity should return domain the evidences urls`() {
        val attachmentInfo1 = createAttachmentInfoEntity()
        val attachmentInfo2 = createAttachmentInfoEntity()
        val evidences = mutableListOf(attachmentInfo1, attachmentInfo2)

        val expectedUrl1 = ActivityEvidenceResponseConverter.ATTACHMENT_API_PREFIX + attachmentInfo1.id
        val expectedUrl2 = ActivityEvidenceResponseConverter.ATTACHMENT_API_PREFIX + attachmentInfo2.id

        val activityWithEvidences = createActivity(evidences = evidences).toDomain()
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUrls(activityWithEvidences)
        assertTrue(evidencesUrls.isNotEmpty())
        assertTrue(evidencesUrls.contains(expectedUrl1))
        assertTrue(evidencesUrls.contains(expectedUrl2))

    }

    @Test
    fun `given domain Activity without evidences should return empty evidences urls`() {
        val activityWithoutEvidence = createActivity().toDomain()
        val evidencesUrls = activityEvidenceResponseConverter.getEvidencesUrls(activityWithoutEvidence)
        assertTrue(evidencesUrls.isEmpty())
    }


}