package com.autentia.tnt.binnacle.entities

import com.autentia.tnt.binnacle.core.domain.TimeInterval
import com.autentia.tnt.binnacle.core.utils.toDate
import com.autentia.tnt.binnacle.core.utils.toLocalDateTime
import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.DateCreated
import java.time.LocalDateTime
import java.util.*
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
    var approvalState: ApprovalState,

    var approvedByUserId: Long? = null,
    var approvalDate: LocalDateTime? = null,


    @OneToMany(fetch = LAZY)
    @JoinTable(name="ActivityEvidence",
        joinColumns = [JoinColumn(name = "activityId",
            referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "attachmentId",
            referencedColumnName = "id")]
    )
    var evidences: MutableList<AttachmentInfo> = ArrayList()

) {
    fun getTimeInterval() = TimeInterval.of(start, end)


    fun toDomain() =
        com.autentia.tnt.binnacle.core.domain.Activity.of(
            id,
            getTimeInterval(),
            duration,
            description,
            projectRole.toDomain(),
            userId,
            billable,
            departmentId,
            toLocalDateTime(insertDate),
            hasEvidences,
            approvalState,
            getAttachmentInfoIds(evidences),
            approvedByUserId,
            approvalDate
        )

    companion object {

        fun of(
            activity: com.autentia.tnt.binnacle.core.domain.Activity, projectRole: ProjectRole,
        ) =
            Activity(
                activity.id,
                activity.getStart(),
                activity.getEnd(),
                activity.duration,
                activity.description,
                projectRole,
                activity.userId,
                activity.billable,
                activity.departmentId,
                toDate(activity.insertDate),
                activity.hasEvidences,
                activity.approvalState,
                activity.approvedByUserId,
                activity.approvalDate
            )

        fun of(
            activity: com.autentia.tnt.binnacle.core.domain.Activity, projectRole: ProjectRole, attachmentInfos: MutableList<AttachmentInfo>
        ) =
            Activity(
                activity.id,
                activity.getStart(),
                activity.getEnd(),
                activity.duration,
                activity.description,
                projectRole,
                activity.userId,
                activity.billable,
                activity.departmentId,
                toDate(activity.insertDate),
                activity.hasEvidences,
                activity.approvalState,
                activity.approvedByUserId,
                activity.approvalDate,
                attachmentInfos
            )

        fun of(
            id: Long?,
            start: LocalDateTime,
            end: LocalDateTime,
            duration: Int,
            description: String,
            projectRole: ProjectRole,
            userId: Long,
            billable: Boolean,
            departmentId: Long?,
            insertDate: Date?,
            hasEvidences: Boolean,
            approvalState: ApprovalState,
            approvedByUser: Long?,
            approvalDate: LocalDateTime?
        ) =
            Activity(
                id,
                start,
                end,
                duration,
                description,
                projectRole,
                userId,
                billable,
                departmentId,
                insertDate,
                hasEvidences,
                approvalState,
                approvedByUser,
                approvalDate
            )

        fun emptyActivity(projectRole: ProjectRole): Activity = Activity(
            0, LocalDateTime.MIN, LocalDateTime.MIN, 0, "Empty activity", projectRole, 0L,
            false, 0, null, false, ApprovalState.NA, null, null, arrayListOf()
        )

        fun getAttachmentInfoIds(attachmentInfos: MutableList<AttachmentInfo>): List<UUID> {
            val ids: MutableList<UUID> = mutableListOf()
            for (attachmentInfo in attachmentInfos) {
                attachmentInfo.id?.let { ids.add(it) }
            }
            return ids
        }

    }
}