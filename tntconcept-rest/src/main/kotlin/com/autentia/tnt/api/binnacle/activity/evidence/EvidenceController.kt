package com.autentia.tnt.api.binnacle.activity.evidence

import com.autentia.tnt.api.OpenApiTag
import com.autentia.tnt.api.binnacle.ErrorResponse
import com.autentia.tnt.binnacle.exception.NoEvidenceInActivityException
import com.autentia.tnt.binnacle.usecases.ActivityEvidenceRetrievalUseCase
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*

@Controller("/api/activity/{activityId}/evidence")
@Validated
@Tag(name = OpenApiTag.ACTIVITY)
internal class EvidenceController (
    private val activityEvidenceRetrievalUseCase: ActivityEvidenceRetrievalUseCase,
){

    @Get
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Retrieves an activity evidence by the activity id.")
    internal fun getEvidenceByActivityId(activityId: Long): HttpResponse<ByteArray> {
        val evidence = activityEvidenceRetrievalUseCase.getActivityEvidenceByActivityId(activityId)

        return HttpResponse.ok(Base64.getDecoder().decode(evidence.base64data))
            .header("Content-type",  evidence.mediaType)
            .header("Content-disposition", "attachment; filename=\"${activityId}_evidence.${getExtension(evidence.mediaType)}\"");
    }

    fun getExtension(mediaType: String) = mediaType.split("/")[1]


    @Error
    internal fun onNoEvidenceInActivityException(request: HttpRequest<*>, e: NoEvidenceInActivityException) =
        HttpResponse.badRequest(ErrorResponse("No evidence", e.message))


}