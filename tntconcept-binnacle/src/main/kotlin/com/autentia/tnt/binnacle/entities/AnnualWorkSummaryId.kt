package com.autentia.tnt.binnacle.entities

import java.io.Serializable
import javax.persistence.Embeddable

@Embeddable
data class AnnualWorkSummaryId(
    val userId: Long,
    val year: Int
) : Serializable
