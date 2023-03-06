package com.autentia.tnt.binnacle.config

import com.autentia.tnt.AppProperties
import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppPropertiesTest {

    @Test
    fun `recover the first level property version`() {
        val context = ApplicationContext.run()

        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals("@project.version@", appProperties.version)
    }

    @Test
    fun `recover a second level property files activity-images`() {
        val context = ApplicationContext.run()

        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals("/tmp/activity/images", appProperties.files.activityImages)
    }

    @Test
    fun `recover a 4th level property binnacle work-summary report nameSuffix`() {
        val context = ApplicationContext.run()

        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals("summary", appProperties.binnacle.workSummary.report.nameSuffix)
    }

    @Test
    fun `resolve a declarative indirection variable to binnacle vacations_approvers`() {
        val context = ApplicationContext.run()

        val appProperties = context.getBean(AppProperties::class.java)

        assertEquals(listOf("approver@example.com"), appProperties.binnacle.workSummary.mail.to)
    }

}