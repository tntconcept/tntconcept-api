package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Organization
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

@Repository
internal interface OrganizationRepository : CrudRepository<Organization, Long>
