package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.UserResponseConverter
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.UserFilterDTO
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import io.micronaut.data.jpa.repository.criteria.Specification
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class UsersRetrievalUseCase internal constructor(
    private val userRepository: UserRepository,
    private val userResponseConverter: UserResponseConverter
) {
    @Transactional
    fun getUsers(userFilter: UserFilterDTO): List<UserResponseDTO> {
        val predicate = getPredicateFromUsersFilter(userFilter)
        val users = getUsers(userFilter.limit, predicate)
        return users.map { userResponseConverter.mapUserToUserResponseDTO(it) }
    }

    private fun getUsers(
        limit: Int?,
        predicate: Specification<User>
    ): List<User> {
        return if (limit !== null) {
            val pageable = Pageable.from(0, limit)
            userRepository.findAll(predicate, pageable)
        } else {
            userRepository.findAll(predicate, null)
        }
    }

    private fun getPredicateFromUsersFilter(userFilter: UserFilterDTO): Specification<User> {
        var predicate: Specification<User> = UserPredicates.ALL

        if (userFilter.active !== null) {
            predicate = PredicateBuilder.and(predicate, UserPredicates.isActive(userFilter.active))
        }

        if (!userFilter.ids.isNullOrEmpty()) {
            predicate = PredicateBuilder.and(predicate, UserPredicates.fromUserIds(userFilter.ids))
        }

        if (userFilter.nameLike !== null) {
            predicate = PredicateBuilder.and(predicate, UserPredicates.filterByName(userFilter.nameLike))
        }

        return predicate
    }
}
