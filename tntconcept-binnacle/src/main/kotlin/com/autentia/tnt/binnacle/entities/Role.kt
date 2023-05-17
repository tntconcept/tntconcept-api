package com.autentia.tnt.binnacle.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Role(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String
)

