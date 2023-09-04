package com.autentia.tnt.binnacle.converters

import jakarta.inject.Singleton

@Singleton
class ActivityEvidenceResponseConverter {

    fun getEvidencesUUIDs(activity: com.autentia.tnt.binnacle.core.domain.Activity) : List<String> {
        return activity.evidences.map { it.value.toString() }
    }

    fun getEvidencesUUIDs(activity: com.autentia.tnt.binnacle.entities.Activity) : List<String> {
        return activity.evidences.map { it.id.toString()}
    }
}