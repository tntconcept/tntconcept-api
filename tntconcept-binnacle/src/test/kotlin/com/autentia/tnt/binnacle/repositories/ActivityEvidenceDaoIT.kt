package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.entities.*
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActivityEvidenceDaoIT {

    @Inject
    private lateinit var activityDao: ActivityDao

    @Inject
    private lateinit var attachmentInfoRepository: AttachmentInfoRepository

    @Test
    fun `should find activity with attachment`() {
        val attachmentInfo = AttachmentInfo(
            type = AttachmentType.EVIDENCE,
            path = "path/to/attachment",
            fileName = "testAttachment",
            mimeType = "image/jpeg",
            uploadDate = LocalDateTime.now(),
            isTemporary = true,
            userId = userId
        )

        val attachmentInfoUuid = attachmentInfoRepository.save(attachmentInfo)
        val evidences = mutableListOf(attachmentInfoUuid)

        val activity = Activity(
            start = today.atTime(10, 0, 0),
            end = today.atTime(12, 0, 0),
            duration = 120,
            description = "Test activity",
            projectRole = createProjectRole(),
            userId = userId,
            billable = false,
            hasEvidences = false,
            approvalState = ApprovalState.ACCEPTED,
            evidences = evidences
        )
        val savedActivity = activityDao.save(activity)

        val result = activityDao.findById(savedActivity.id!!)

        assertEquals(savedActivity, result.get())
        assertTrue(savedActivity.evidences.isNotEmpty())
        assertEquals(attachmentInfoUuid.id, savedActivity.evidences[0].id)
    }

    private companion object {
        private val today = LocalDate.now()
        private const val userId = 11L
    }
}