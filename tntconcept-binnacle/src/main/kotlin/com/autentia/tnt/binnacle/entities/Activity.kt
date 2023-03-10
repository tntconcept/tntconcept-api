package com.autentia.tnt.binnacle.entities

import com.autentia.tnt.binnacle.core.domain.Interval
import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.DateCreated
import java.time.LocalDateTime
import java.util.Date
import javax.persistence.Embedded
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
import javax.persistence.Transient

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

    @Embedded
    val interval: Interval,

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

    constructor(
        id: Long? = null, start: LocalDateTime, end: LocalDateTime, description: String, projectRole: ProjectRole,
        userId: Long, billable: Boolean, departmentId: Long? = null, insertDate: Date? = null,
        hasEvidences: Boolean = false, approvalState: ApprovalState
    ) : this(
        id, Interval(start, end), description, projectRole, userId, billable, departmentId, insertDate,
        hasEvidences, approvalState
    )
    
    fun duration(): Int = interval.getDuration(projectRole.timeUnit)
}