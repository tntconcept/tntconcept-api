package com.autentia.tnt.binnacle.converters

import jakarta.inject.Singleton

@Singleton
class ActivityEvidenceResponseConverter {

    fun getEvidencesUrls(activity: com.autentia.tnt.binnacle.core.domain.Activity) : List<String> {
        return activity.evidences.map { ATTACHMENT_API_PREFIX.plus(it.toString()) }
    }

    fun getEvidencesUrls(activity: com.autentia.tnt.binnacle.entities.Activity) : List<String> {
        return activity.evidences.map { ATTACHMENT_API_PREFIX.plus(it.id.toString()) }
    }

    companion object {
        private const val ATTACHMENT_API_PREFIX = "/api/attachment/"
    }
}