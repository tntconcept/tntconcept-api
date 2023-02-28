package com.autentia.tnt.binnacle.entities

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity
data class WorkingAgreement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @OneToMany(fetch = FetchType.EAGER, cascade = [])
    @JoinColumn(name = "workingAgreementId")
    val terms: Set<WorkingAgreementTerms>
)


