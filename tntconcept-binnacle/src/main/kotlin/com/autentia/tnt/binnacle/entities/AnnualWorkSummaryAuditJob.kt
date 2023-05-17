package com.autentia.tnt.binnacle.entities

import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "AnnualWorkSummaryJob")
data class AnnualWorkSummaryAuditJob(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    var started: LocalDateTime,
    var finished: LocalDateTime,

)
