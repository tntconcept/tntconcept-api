package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.DateCreated
import java.time.LocalDateTime
import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType.LAZY
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedAttributeNode
import javax.persistence.NamedEntityGraph
import javax.persistence.NamedSubgraph

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

    val startDate: LocalDateTime,
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

    var hasEvidences: Boolean = false
)
