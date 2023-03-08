package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

enum class RequireEvidence{
    NO, ONCE, WEEKLY
}

@Entity
@NamedEntityGraph(
    name = "roleWithProject",
    attributeNodes = [NamedAttributeNode(value = "project")]
)
data class ProjectRole(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    val name: String,

    @Enumerated(EnumType.STRING)
    val requireEvidence: RequireEvidence,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    @JsonIgnore
    val project: Project,

    val maxAllowed: Int,

    val isWorkingTime: Boolean,
)
