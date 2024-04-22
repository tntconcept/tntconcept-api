package com.autentia.tnt.binnacle.entities

class ProjectBillingTypes{
    private val noBillable = ProjectBillingType("NO_BILLABLE", BillingType.NEVER, BillingPreference.NO_BILLABLE)
    private val closedPrice = ProjectBillingType("CLOSED_PRICE", BillingType.ALWAYS, BillingPreference.BILLABLE)
    private val timeMaterials = ProjectBillingType("TIME_AND_MATERIALS", BillingType.DEPENDS, BillingPreference.BILLABLE)
    private val timeMaterialsLimited = ProjectBillingType("TIME_AND_MATERIALS_LIMITED", BillingType.DEPENDS, BillingPreference.BILLABLE)
    private val bagHours = ProjectBillingType("BAG_OF_HOURS", BillingType.DEPENDS, BillingPreference.BILLABLE)
    fun getProjectBillingType(type: String):ProjectBillingType{
        when (type){
            "NO_BILLABLE" -> return noBillable
            "CLOSED_PRICE" ->return closedPrice
            "TIME_AND_MATERIALS" -> return timeMaterials
            "TIME_AND_MATERIALS_LIMITED" ->return timeMaterialsLimited
            "BAG_OF_HOURS" ->return bagHours
            else -> throw IllegalArgumentException(type + " is not a valid project billing type")
        }
    }
}


//projectBillingType = "NO_BILLABLE"
//ProjectBillingTypes().getProjectBillingType("NO_BILLABLE"),
data class ProjectBillingType(
    val name: String,
    val type: BillingType,
    val preferences: BillingPreference
)
enum class BillingType{
    ALWAYS, NEVER, DEPENDS
}
enum class BillingPreference{
    BILLABLE, NO_BILLABLE
}