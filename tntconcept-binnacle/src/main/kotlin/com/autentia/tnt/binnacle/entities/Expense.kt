package com.autentia.tnt.binnacle.entities

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.FetchType.LAZY


enum class ExpenseType {
    STRUCTURE, MARKETING, OPERATION
}

@Entity
data class Expense (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val userId:Long,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDateTime,
    val description:String,
    val amount:BigDecimal,
    @Enumerated(EnumType.STRING)
    val type:ExpenseType,
    @Enumerated(EnumType.STRING)
    var state: ApprovalState,

    @OneToMany(fetch = LAZY)
    @JoinTable(name="ExpenseDocument",
        joinColumns = [JoinColumn(name = "expenseId",
            referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "attachmentId",
            referencedColumnName = "id")]
    )
    var attachmens: List<AttachmentInfo> = ArrayList()
)
