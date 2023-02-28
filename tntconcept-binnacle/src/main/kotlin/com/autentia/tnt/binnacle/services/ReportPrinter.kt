package com.autentia.tnt.binnacle.services

import com.autentia.tnt.binnacle.core.domain.UserAnnualWorkSummary

internal interface ReportPrinter {
    fun print(appendable: Appendable, summaries: Map<Long, UserAnnualWorkSummary>)
}
