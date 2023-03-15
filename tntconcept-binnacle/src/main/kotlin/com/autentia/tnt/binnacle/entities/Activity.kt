package com.autentia.tnt.binnacle.entities

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.DateCreated
import java.time.LocalDateTime
import java.util.Date
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType.LAZY
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedSubgraph

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
    val duration: Int,
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
        fun emptyActivity(projectRole: ProjectRole): Activity = Activity(
            0, LocalDateTime.MIN, LocalDateTime.MIN, 0, "Empty activity", projectRole, 0L,
            false, 0, null, false, ApprovalState.NA
        )
    }

    fun getDateInterval() = DateInterval.of(start.toLocalDate(), end.toLocalDate())
    fun getTimeInterval() = TimeInterval.of(start, end)
    fun isOneDay() = start.toLocalDate().isEqual(end.toLocalDate())
}