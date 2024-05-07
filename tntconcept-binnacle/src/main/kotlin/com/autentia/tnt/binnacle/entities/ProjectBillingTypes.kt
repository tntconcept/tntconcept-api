package com.autentia.tnt.binnacle.entities

class ProjectBillingTypes{
    private val noBillingDefault = ProjectBillingType("NO_BILLABLE", Billable.NEVER, BillingDefault.NO_BILLABLE)
    private val closedPrice = ProjectBillingType("CLOSED_PRICE", Billable.ALWAYS, BillingDefault.BILLABLE)
    private val timeMaterials = ProjectBillingType("TIME_AND_MATERIALS", Billable.OPTIONAL, BillingDefault.BILLABLE)
    private val timeMaterialsLimited = ProjectBillingType("TIME_AND_MATERIALS_LIMITED", Billable.OPTIONAL, BillingDefault.BILLABLE)
    private val bagHours = ProjectBillingType("BAG_OF_HOURS", Billable.OPTIONAL, BillingDefault.BILLABLE)
    fun getProjectBillingType(type: String):ProjectBillingType{
        when (type){
            "NO_BILLABLE" -> return noBillingDefault
            "CLOSED_PRICE" ->return closedPrice
            "TIME_AND_MATERIALS" -> return timeMaterials
            "TIME_AND_MATERIALS_LIMITED" ->return timeMaterialsLimited
            "BAG_OF_HOURS" ->return bagHours
            else -> throw IllegalArgumentException(type + " is not a valid project billing type")
        }
    }
}

data class ProjectBillingType(
    val name: String,
    val type: Billable,
    val preferences: BillingDefault
)
enum class Billable{
    ALWAYS, NEVER, OPTIONAL
}
enum class BillingDefault{
    BILLABLE, NO_BILLABLE
}