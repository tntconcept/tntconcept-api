package com.autentia.tnt.binnacle.entities

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class WorkingAgreementTerms(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val effectiveFrom: LocalDate,
    val vacation: Int,
    val annualWorkingTime: Int
) {

    companion object {
        private val EMPTY = WorkingAgreementTerms(0, LocalDate.MIN, 0, 0)

        fun empty(): WorkingAgreementTerms = EMPTY
    }

}
