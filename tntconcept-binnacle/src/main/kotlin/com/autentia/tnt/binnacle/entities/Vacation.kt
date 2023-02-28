package com.autentia.tnt.binnacle.entities

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.LocalDate
import java.util.Date
import javax.persistence.*

enum class VacationState {
    PENDING, ACCEPT, REJECT, CANCELLED
}

@Entity
@Table(name = "RequestHoliday")
data class Vacation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @Column(name = "beginDate")
    val startDate: LocalDate,

    @Column(name = "finalDate")
    val endDate: LocalDate,

    @Enumerated(EnumType.STRING)
    val state: VacationState,

    val userId: Long,
    var observations: String? = null,
    var departmentId: Long? = null,

    @Column(name = "userComment")
    var description: String,

    val chargeYear: LocalDate,
)
