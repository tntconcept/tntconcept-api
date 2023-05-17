package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import jakarta.persistence.*

@Entity
data class Organization(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String,

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    val projects: List<Project>
) {
    fun toDomain() = com.autentia.tnt.binnacle.core.domain.Organization(id, name)
}