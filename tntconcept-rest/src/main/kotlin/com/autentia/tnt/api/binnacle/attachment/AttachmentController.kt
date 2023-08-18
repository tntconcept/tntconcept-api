package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.api.OpenApiTag
import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.exception.AttachmentMimeTypeNotSupportedException
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
import com.autentia.tnt.binnacle.usecases.AttachmentCreationUseCase
import com.autentia.tnt.binnacle.usecases.AttachmentRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*

@Controller("/api/attachment")
@Validated
@Tag(name = OpenApiTag.ACTIVITY)
internal class AttachmentController(
    private val attachmentRetrievalUseCase: AttachmentRetrievalUseCase,
    private val attachmentCreationUseCase: AttachmentCreationUseCase,
) {

    @Get("/{id}")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Retrieves an attachment")
    internal fun getAttachment(id: UUID): HttpResponse<ByteArray> {

        val attachment = attachmentRetrievalUseCase.getAttachment(id)

        return HttpResponse.ok(attachment.file)
            .header("Content-type", attachment.info.mimeType)
            .header("Content-disposition", "attachment; filename=\"${attachment.info.fileName}\"")
    }

    @Post(value = "/", consumes = [MediaType.MULTIPART_FORM_DATA], produces = [MediaType.APPLICATION_JSON])
    @Operation(summary = "Create an attachment")
    internal fun createAttachment(
        attachmentFile: CompletedFileUpload,
    ): HttpResponse<AttachmentCreationResponse> {
        val attachmentRequest = AttachmentRequest.of(attachmentFile)

        val createdAttachmentDto = attachmentCreationUseCase.createAttachment(attachmentRequest.toAttachmentCreationRequestDTO())

        return HttpResponse.ok(AttachmentCreationResponse(createdAttachmentDto.id.toString()))
    }

    @Error
    internal fun onAttachmentNotFoundException(request: HttpRequest<*>, e: AttachmentNotFoundException) =
        HttpResponse.notFound(ErrorResponse("ATTACHMENT_NOT_FOUND", e.message))

    @Error
    internal fun onAttachmentMimetypeNotSupportedException(
        request: HttpRequest<*>,
        e: AttachmentMimeTypeNotSupportedException,
    ) =
        HttpResponse.badRequest(ErrorResponse("ATTACHMENT_MIMETYPE_NOT_SUPPORTED", e.message))

}