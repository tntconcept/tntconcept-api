package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.services.AnnualWorkSummaryAuditJobService
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.time.LocalDate

internal class AnnualWorkSummaryJobUseCaseTest {

    private val createAnnualSummaryReportUseCase =  mock<CreateAnnualSummaryReportUseCase>()
    private val annualWorkSummaryAuditJobService =  mock<AnnualWorkSummaryAuditJobService>()

    private var annualWorkSummaryJobUseCase = AnnualWorkSummaryJobUseCase(createAnnualSummaryReportUseCase, annualWorkSummaryAuditJobService)

    @ParameterizedTest
    @MethodSource("createWorkSummariesYearBeforeParametersProvider")
    fun `should create annual summaries and audit job`(
        testDescription: String,
        exception: Exception?,
    ) {
        //Given
        val yearBefore = LocalDate.now().year - 1

        if (exception != null)  whenever(createAnnualSummaryReportUseCase.createAnnualSummaryFromYear(yearBefore)).then { exception }

        //When
        annualWorkSummaryJobUseCase.createWorkSummariesYearBefore()

        //Then
        verify(createAnnualSummaryReportUseCase, times(1)).createAnnualSummaryFromYear(yearBefore)
        verify(annualWorkSummaryAuditJobService, times(1)).createAuditJob(any(), any())
    }

    private companion object {
        @JvmStatic
        private fun createWorkSummariesYearBeforeParametersProvider() = listOf(
            Arguments.of(
                "given exception creating summaries",
                RuntimeException("error creating summary")
            ),
            Arguments.of(
                "not given exception creating summaries",
                null
            ),
        )
    }

}
