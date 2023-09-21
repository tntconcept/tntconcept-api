package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Organization
import com.autentia.tnt.binnacle.entities.Project
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOpenSpecification
import com.autentia.tnt.binnacle.repositories.predicates.ProjectOrganizationIdSpecification
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDate

@MicronautTest
@TestInstance(PER_CLASS)
internal class ProjectRepositoryIT {

    @Inject
    private lateinit var projectRepository: ProjectRepository

    @Test
    fun `should find closed project`() {
        val result = projectRepository.findAll(closedProjectPredicate)

        assert(result.any {
            it.name == closedProject.name
                    && it.organization.id == closedProject.organization.id
                    && !it.open
        })
    }

    @Test
    fun `should not find projects`() {
        val result = projectRepository.findAll(noProjectsPredicate)

        assert(result.isEmpty())
    }

    @Test
    fun `should find the projects by organization id`() {
        val organizationId = 1L
        val result = projectRepository.findAllByOrganizationId(organizationId)

        assertTrue(result.isNotEmpty())
        assertEquals("Vacaciones", result[0].name)
    }

    @Test
    fun `should find by id`() {
        val result = projectRepository.findById(1)

        assert(result.isPresent)
    }

    @Test
    fun `should find returns empty optional when id doesn't exist`() {
        val result = projectRepository.findById(Long.MAX_VALUE)

        assert(result.isEmpty)
    }
    
    private companion object {
        private const val closedProjectOrganizationId = 3L
        private val closedProjectPredicate = PredicateBuilder.and(
            ProjectOrganizationIdSpecification(
                closedProjectOrganizationId
            ), ProjectOpenSpecification(false)
        )
        private val noProjectsPredicate = ProjectOrganizationIdSpecification(Long.MAX_VALUE)
        private val closedProject = Project(
            9,
            "Closed project for testing",
            false,
            false,
            LocalDate.now(),
            null,
            null,
            Organization(3, "Organization", 1, emptyList()),
            emptyList()
        )
    }

}
