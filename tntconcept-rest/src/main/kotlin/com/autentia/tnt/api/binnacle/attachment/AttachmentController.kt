package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.api.OpenApiTag
import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.exception.AttachmentNotFoundException
//import com.autentia.tnt.binnacle.usecases.ActivityEvidenceCreationUseCase
import com.autentia.tnt.binnacle.usecases.AttachmentRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.StreamingFileUpload
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*

@Controller("/api/attachment")
@Validated
@Tag(name = OpenApiTag.ACTIVITY)
internal class AttachmentController (
    private val attachmentRetrievalUseCase: AttachmentRetrievalUseCase,

    ){

    @Get("/{id}")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Retrieves an attachment")
    internal fun getAttachment(id: UUID): HttpResponse<ByteArray> {

        val attachment = attachmentRetrievalUseCase.getAttachment(id)

        return HttpResponse.ok(attachment.file)
            .header("Content-type",  attachment.info.mimeType)
            .header("Content-disposition", "attachment; filename=\"${attachment.info.fileName}\"")
    }

    @Post
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create an attachment")
    internal fun createAttachment(attachmentFile: StreamingFileUpload):UUID {
//        val tempFile = File.createTempFile(evidenceFile.filename, "temp_")
//        val uploadPublisher = evidenceFile.transferTo(tempFile)
//        return Single.fromPublisher(uploadPublisher) .map { success ->
//            if (success) {
//                HttpResponse.ok("Uploaded")
//            }
//            else {
//                HttpResponse.status<String>(HttpStatus.CONFLICT) .body("Upload Failed")
//            }
//        }
//        activityEvidenceCreationUseCase.createActivityEvidence(activityId, MediaType.APPLICATION_PDF_TYPE  , tempFile)
        return UUID.randomUUID()
    }


    @Delete("/{id}")
    @Operation(summary = "Delete an attachment")
    internal fun deleteAttachment(id: UUID) {
    }

    @Error
    internal fun onAttachmentNotFoundException (request: HttpRequest<*>, e: AttachmentNotFoundException) =
        HttpResponse.notFound(ErrorResponse("Evidence not found", e.message))


    fun getExtension(mediaType: String) = mediaType.split("/")[1]

}