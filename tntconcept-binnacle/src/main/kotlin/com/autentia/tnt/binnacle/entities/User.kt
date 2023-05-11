package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = ["password", "active"])
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "login")
    val username: String,

    val password: String,
    val departmentId: Long,
    val name: String,

    @Column(name = "photo")
    val photoUrl: String,

    val dayDuration: Int,

    @OneToOne
    @JoinColumn(name = "agreementId")
    val agreement: WorkingAgreement,

    val agreementYearDuration: Int? = null,

    @Column(name = "startDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val hiringDate: LocalDate,

    val email: String,

    @OneToOne
    @JoinColumn(name = "roleId")
    val role: Role,

    val active: Boolean,
) {

    fun toDomain(): com.autentia.tnt.binnacle.core.domain.User =
        com.autentia.tnt.binnacle.core.domain.User(id, username, name, departmentId, hiringDate, email)

    fun getAnnualWorkingHoursByYear(year: Int): Duration {
        if (agreementYearDuration != null && agreementYearDuration > 0) {
            return agreementYearDuration.toDuration(DurationUnit.MINUTES)
        }

        return getAgreementTermsByYear(year)
            .annualWorkingTime
            .toDuration(DurationUnit.MINUTES)
    }

    fun getAgreementTermsByYear(year: Int): WorkingAgreementTerms =
        agreement.terms
            .filter { it.effectiveFrom.year <= year }
            .maxByOrNull { it.effectiveFrom } ?: WorkingAgreementTerms.empty()

}
