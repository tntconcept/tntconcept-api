package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import jakarta.persistence.*

@Entity
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String,
    val open: Boolean,
    val billable: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizationId")
    val organization: Organization,

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    val projectRoles: List<ProjectRole>
) {
    fun toDomain() = com.autentia.tnt.binnacle.core.domain.Project(id, name, open, billable, organization.toDomain())
}