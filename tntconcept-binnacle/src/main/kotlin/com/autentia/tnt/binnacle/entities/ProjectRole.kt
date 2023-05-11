package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

enum class RequireEvidence {
    NO, ONCE, WEEKLY;

    companion object {
        fun isRequired(requireEvidence: RequireEvidence) = listOfRequired().contains(requireEvidence)
        private fun listOfRequired() = listOf(ONCE, WEEKLY)
    }
}

enum class TimeUnit {
    MINUTES, DAYS
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

    val isApprovalRequired: Boolean,

    @Enumerated(EnumType.STRING)
    val timeUnit: TimeUnit
) {
    fun toDomain() =
        com.autentia.tnt.binnacle.core.domain.ProjectRole(
            id,
            name,
            requireEvidence,
            project.toDomain(),
            maxAllowed,
            timeUnit,
            isWorkingTime,
            isApprovalRequired
        )

    companion object {
        fun of(projectRole: com.autentia.tnt.binnacle.core.domain.ProjectRole, project: Project) = ProjectRole(
            projectRole.id,
            projectRole.name,
            projectRole.requireEvidence,
            project,
            projectRole.maxAllowed,
            projectRole.isWorkingTime,
            projectRole.isApprovalRequired,
            projectRole.timeUnit
        )
    }
}
