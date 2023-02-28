package com.autentia.tnt.binnacle.integration

import com.autentia.tnt.binnacle.MailTestUtils
import com.autentia.tnt.binnacle.services.MailService
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.io.FileWriter

@MicronautTest
@TestInstance(PER_CLASS)
internal class MailServiceIT {

    private val from = "davidtestemail09@gmail.com"
    private val to1 = "david97vj@gmail.com"
    private val to2 = "to-admin@gmail.com"
    private val to = "$to1,$to2"
    private val subject = "subject"
    private val bodyText = "bodyText"

    @Inject
    @field:Client("/")
    private lateinit var mailService: MailService

    @Inject
    @field:Client("/")
    private lateinit var mailTestUtils: MailTestUtils

    @Test
    @Disabled("Ignored because get pending emails process does not retrieve emails")
    fun `given from, to, subject, body without attachment should send email`() {

        val result = mailService.send(from, to1, subject, bodyText)
        assertTrue(result.isSuccess)

        /*val email = mailTestUtils.getSentEmail()
        assertEquals(email, HttpStatus.OK)*/
    }

    @Test
    @Disabled("Ignored because get pending emails process does not retrieve emails")
    fun `given from, to, subject, body and attachment should send email`() {
        //Given
        val fileContent = "Hi, this is a test!"
        val file = File("target/file")
        FileWriter(file, false).use { writer -> writer.write(fileContent) }

        //When
        val result = mailService.send(from, to1, subject, bodyText, file)

        //Then
        assertTrue(result.isSuccess)

        //val email = mailTestUtils.getSentEmail()
        //assertThat(email.statusCode, `is`(equalTo(HttpStatus.OK)))

        //assertTrue( email.contains(file.name) )
        //assertTrue( email.contains(fileContent) )
    }

}
