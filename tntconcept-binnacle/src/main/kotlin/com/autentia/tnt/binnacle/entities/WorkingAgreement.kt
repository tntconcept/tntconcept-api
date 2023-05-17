package com.autentia.tnt.binnacle.entities

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany

@Entity
data class WorkingAgreement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @OneToMany(fetch = FetchType.EAGER, cascade = [])
    @JoinColumn(name = "workingAgreementId")
    val terms: Set<WorkingAgreementTerms>
)


