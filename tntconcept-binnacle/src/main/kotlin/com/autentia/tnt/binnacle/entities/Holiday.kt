package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Holiday(
    @Id
    val id: Long,

    val description: String,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDateTime

)
