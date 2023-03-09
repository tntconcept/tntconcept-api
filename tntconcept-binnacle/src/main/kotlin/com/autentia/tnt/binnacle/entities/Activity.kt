package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.DateCreated
import java.time.Duration
import java.time.LocalDateTime
import java.util.Date
import javax.persistence.*
import javax.persistence.FetchType.LAZY

enum class ApprovalState {
    NA, PENDING, ACCEPTED
}

@Entity
@NamedEntityGraph(
    name = "fetch-activity-with-project-and-organization",
    attributeNodes = [
        NamedAttributeNode(value = "projectRole", subgraph = "fetch-project-eager")
    ],
    subgraphs = [
        NamedSubgraph(
            name = "fetch-project-eager",
            attributeNodes = [
                NamedAttributeNode(value = "project", subgraph = "fetch-organization-eager")
            ]
        ),
        NamedSubgraph(
            name = "fetch-organization-eager",
            attributeNodes = [
                NamedAttributeNode("organization")
            ]
        )
    ]
)
data class Activity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val start: LocalDateTime,
    val end: LocalDateTime,
    val description: String,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "roleId")
    val projectRole: ProjectRole,

    val userId: Long,
    val billable: Boolean,
    var departmentId: Long? = null,

    @DateCreated
    @JsonIgnore
    var insertDate: Date? = null,

    var hasEvidences: Boolean = false,

    @Enumerated(EnumType.STRING)
    val approvalState: ApprovalState
) {
    companion object {
        fun emptyActivity(projectRole: ProjectRole) : Activity = Activity(0, LocalDateTime.MIN, LocalDateTime.MIN, "Empty activity", projectRole, 0L, false, 0, null, false, ApprovalState.NA)
    }
    val duration: Int =
        if (projectRole.timeUnit == TimeUnit.MINUTES) Duration.between(start, end).toMinutes().toInt()
        else Duration.between(start, end).toDays().toInt() * 8 * 60
}
