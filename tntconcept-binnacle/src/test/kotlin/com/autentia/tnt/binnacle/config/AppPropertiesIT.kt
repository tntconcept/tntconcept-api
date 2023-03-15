package com.autentia.tnt.binnacle.config

import com.autentia.tnt.AppProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
internal class AppPropertiesIT {

    @Inject
    private lateinit var context: ApplicationContext

    @Test
    fun `recover the first level property version`() {
        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals("@project.version@", appProperties.version)
    }

    @Test
    fun `recover a second level property files activity-images`() {
        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals("/tmp/activity/images", appProperties.files.activityImages)
    }

    @Test
    fun `recover a 4th level property binnacle work-summary report nameSuffix`() {
        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals("summary", appProperties.binnacle.workSummary.report.nameSuffix)
    }

    @Test
    fun `resolve a declarative indirection variable to binnacle vacations_approvers`() {
        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals(listOf("approver@example.com", "other@example.com"), appProperties.binnacle.workSummary.mail.to)
    }

}
