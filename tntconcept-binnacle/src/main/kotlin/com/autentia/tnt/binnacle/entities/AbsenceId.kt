package com.autentia.tnt.binnacle.entities

import java.io.Serializable
import javax.persistence.Embeddable

@Embeddable
data class AbsenceId(
    val id: Long,
    val type: String,
) : Serializable