package com.autentia.tnt.api.binnacle.attachment

import com.autentia.tnt.api.OpenApiTag
//import com.autentia.tnt.binnacle.usecases.ActivityEvidenceCreationUseCase
import com.autentia.tnt.binnacle.usecases.ActivityEvidenceRetrievalUseCase
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
    private val activityEvidenceRetrievalUseCase: ActivityEvidenceRetrievalUseCase,
//    private val activityEvidenceCreationUseCase: ActivityEvidenceCreationUseCase,

    ){

    @Get("/{id}")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Retrieves an attachment")
    internal fun getAttachment(id: UUID): HttpResponse<ByteArray> {

        val attachment = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII="
        println(Base64.getDecoder().decode(attachment))

        return HttpResponse.ok(Base64.getDecoder().decode(attachment))
            .header("Content-type",  MediaType.IMAGE_PNG_TYPE)
            .header("Content-disposition", "attachment; filename=\"${id}.${getExtension(MediaType.IMAGE_PNG)}\"")
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


    fun getExtension(mediaType: String) = mediaType.split("/")[1]

}