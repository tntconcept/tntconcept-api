package com.autentia.tnt.binnacle.services

import io.micronaut.email.Attachment
import io.micronaut.email.Email
import io.micronaut.email.EmailSender
import io.micronaut.email.MultipartBody
import io.micronaut.http.MediaType
import jakarta.inject.Singleton
import java.io.File
import jakarta.mail.internet.InternetAddress

@Singleton
internal class MailService(
    private val emailSender: EmailSender<Any, Any>,
) {

    fun send(
        from: String,
        to: List<String>,
        subject: String,
        bodyText: String,
        attachment: File? = null
    ): Result<String> {

        val email = Email.builder()
            .from(InternetAddress(from).address)
            .subject(subject)
            .body(MultipartBody(bodyText, bodyText))

        to.forEach { email.to(it) }

        if (attachment != null) {
            email.attachment(
                Attachment.builder()
                    .filename("report.csv")
                    .contentType(MediaType.ALL)
                    .content(readFile(attachment.absolutePath))
                    .build()
            )
        }

        return try {
            emailSender.send(email)
            Result.success("Email sent!!!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readFile(file : String): ByteArray {
        return File(file).readBytes()
    }


}
