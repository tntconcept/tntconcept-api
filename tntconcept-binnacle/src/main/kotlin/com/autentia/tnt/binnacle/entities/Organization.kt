package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.persistence.*

enum class OrganizationType(val id: Long) {
    CLIENT(1), PROVIDER(2), CLIENT_PROVIDER(3), PROSPECT(4);

    companion object {
        fun valueOfById(id: Long): OrganizationType? =
            OrganizationType.values().find { it.id == id }
    }
}

@Entity
data class Organization(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String,

    val organizationTypeId: Long,

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    val projects: List<Project>
) {
    fun toDomain() = com.autentia.tnt.binnacle.core.domain.Organization(id, name)

    fun isClient() : Boolean {
        val type = OrganizationType.valueOfById(organizationTypeId)
        return if (type !== null) {
            type == OrganizationType.CLIENT || type == OrganizationType.CLIENT_PROVIDER
        } else {
            false
        }
    }

    fun isProvider() : Boolean {
        val type = OrganizationType.valueOfById(organizationTypeId)
        return if (type !== null) {
            type == OrganizationType.PROVIDER || type == OrganizationType.CLIENT_PROVIDER
        } else {
            false
        }
    }

    fun isProspect(): Boolean {
        val type = OrganizationType.valueOfById(organizationTypeId)
        return if (type !== null) {
            type == OrganizationType.PROSPECT
        } else {
            false
        }
    }
}