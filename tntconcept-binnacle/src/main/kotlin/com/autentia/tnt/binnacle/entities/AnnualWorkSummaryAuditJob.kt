package com.autentia.tnt.binnacle.entities

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "AnnualWorkSummaryJob")
data class AnnualWorkSummaryAuditJob(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    var started: LocalDateTime,
    var finished: LocalDateTime,

)
