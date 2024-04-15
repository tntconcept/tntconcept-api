package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.AppProperties
import com.autentia.tnt.binnacle.config.createProjectRole
import com.autentia.tnt.binnacle.config.createUser
import com.autentia.tnt.binnacle.converters.ActivityIntervalResponseConverter
import com.autentia.tnt.binnacle.converters.ActivityResponseConverter
import com.autentia.tnt.binnacle.entities.Activity
import com.autentia.tnt.binnacle.entities.ApprovalState
import com.autentia.tnt.binnacle.entities.dto.SubcontractedActivityFilterDTO
import com.autentia.tnt.binnacle.repositories.ActivityRepository
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.predicates.*
import com.autentia.tnt.binnacle.repositories.predicates.ActivityPredicates.userId
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.ClientAuthentication
import io.micronaut.security.utils.SecurityService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*


internal class SubcontractedActivitiesByFilterUseCaseTest {
    private val activityRepository = Mockito.mock<ActivityRepository>()
    private val securityService = Mockito.mock<SecurityService>()
    private val activityResponseConverter = ActivityResponseConverter(ActivityIntervalResponseConverter())
    private val appProperties = AppProperties()
    private val userRepository = Mockito.mock<UserRepository>()

    private val subcontractedActivitiesByFilterUseCase =
            SubcontractedActivitiesByFilterUseCase(activityRepository, securityService, activityResponseConverter,appProperties,userRepository)


    @BeforeEach
    fun authenticate(){
        whenever(securityService.authentication).thenReturn(Optional.of(AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE))
    }

    @BeforeEach
    fun generateSubcontractedUser(){
        appProperties.binnacle.subcontractedUser.username = "subcontracted"
        whenever(userRepository.findByUsername("subcontracted")).thenReturn(USER_SUBCONTRACTED)
    }

    @Test
    fun `get subcontracted activities by role id`() {
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(predicate, userId(USER_SUBCONTRACTED.id))
        val subcontractedActivityFilterDTO = SubcontractedActivityFilterDTO(
                roleId = 1L
        )

        predicate = PredicateBuilder.and(
                predicate,
                ActivityRoleIdSpecification(subcontractedActivityFilterDTO.roleId!!)
        )

        subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivityFilterDTO)

        Mockito.verify(activityRepository).findAll(predicate)
    }

    @Test
    fun `get subcontracted activities between dates`() {
        val subcontractedActivityFilterDTO = SubcontractedActivityFilterDTO(
                startDate = startDate,
                endDate = endDate,
        )
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(predicate, userId(USER_SUBCONTRACTED.id))
        predicate = PredicateBuilder.and(predicate,ActivityStartDateLessOrEqualSpecification(subcontractedActivityFilterDTO.endDate!!))
        predicate = PredicateBuilder.and(
                predicate,
                ActivityEndDateGreaterOrEqualSpecification(subcontractedActivityFilterDTO.startDate!!)
        )

        whenever(activityRepository.findAll(predicate)).thenReturn(listOf(activity))

        val activities = subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivityFilterDTO)

        val expectedActivity = activityResponseConverter.toSubcontractedActivityResponseDTO(activity.toDomain())

        Mockito.verify(activityRepository).findAll(predicate)

        Assertions.assertThat(activities[0])
                .usingRecursiveComparison()
                .isEqualTo(expectedActivity)
    }

    @Test
    fun `get subcontracted activities should ignore project id when role id`() {
        val subcontractedActivityFilterDTO = SubcontractedActivityFilterDTO(
                projectId = 1L,
                roleId = 1L
        )
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(predicate, userId(USER_SUBCONTRACTED.id))
        predicate = PredicateBuilder.and(
            predicate,
            ActivityRoleIdSpecification(subcontractedActivityFilterDTO.roleId!!)
        )

        subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivityFilterDTO)

        Mockito.verify(activityRepository).findAll(predicate)
    }

    @Test
    fun `get subcontracted activities by organization id`() {
        val subcontractedActivityFilterDTO = SubcontractedActivityFilterDTO(
                organizationId = 1L,
        )
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(predicate, userId(USER_SUBCONTRACTED.id))

        predicate = PredicateBuilder.and(
            predicate,
            ActivityOrganizationIdSpecification(subcontractedActivityFilterDTO.organizationId!!)
        )

        subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivityFilterDTO)

        Mockito.verify(activityRepository).findAll(predicate)
    }

    @Test
    fun `get subcontracted activities should ignore organization id when project id`() {
        val subcontractedActivityFilterDTO = SubcontractedActivityFilterDTO(
                startDate = null,
                endDate = null,
                organizationId = 1L,
                projectId = 1L,
                roleId = null
        )
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(predicate, userId(USER_SUBCONTRACTED.id))
        predicate = PredicateBuilder.and(
            predicate,
            ActivityProjectIdSpecification(subcontractedActivityFilterDTO.projectId!!)
        )

        subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivityFilterDTO)

        Mockito.verify(activityRepository).findAll(predicate)
    }

    @Test
    fun `get subcontracted activities when multiple parameters`() {
        val subcontractedActivityFilterDTO = SubcontractedActivityFilterDTO(
                startDate,
                endDate,
                null,
                1L,
                1L,
        )
        var predicate: Specification<Activity> = ActivityPredicates.ALL
        predicate = PredicateBuilder.and(predicate, userId(USER_SUBCONTRACTED.id))
        predicate = PredicateBuilder.and(
            predicate,
            ActivityStartDateLessOrEqualSpecification(subcontractedActivityFilterDTO.endDate!!)
        )
        predicate = PredicateBuilder.and(
            predicate,
            ActivityEndDateGreaterOrEqualSpecification(subcontractedActivityFilterDTO.startDate!!)
        )
        predicate = PredicateBuilder.and(
            predicate,
            ActivityRoleIdSpecification(subcontractedActivityFilterDTO.roleId!!)
            )


        subcontractedActivitiesByFilterUseCase.getActivities(subcontractedActivityFilterDTO)

        Mockito.verify(activityRepository).findAll(predicate)
    }

    private companion object {

        private val USER_ID_1 = 1L

        private val AUTHENTICATION_WITH_SUBCONTRACTED_MANAGER_ROLE: Authentication =
                ClientAuthentication(USER_ID_1.toString(), mapOf("roles" to listOf("subcontracted-activity-manager")))

        private val USER_SUBCONTRACTED = createUser(LocalDate.now(), 2, "subcontracted")

        val startDate: LocalDate = LocalDate.of(2023, 4, 15)
        val endDate: LocalDate = LocalDate.of(2023, 4, 17)

        val projectRole = createProjectRole(1L)
        val activity =
                Activity(
                        1,
                        startDate.atStartOfDay(),
                        endDate.atStartOfDay(),
                        45,
                        "New activity",
                        projectRole,
                        1,
                        false,
                        null,
                        null,
                        false,
                        approvalState = ApprovalState.PENDING
                )
    }
}