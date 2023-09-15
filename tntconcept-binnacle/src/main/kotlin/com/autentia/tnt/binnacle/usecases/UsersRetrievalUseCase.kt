package com.autentia.tnt.binnacle.usecases

import com.autentia.tnt.binnacle.converters.UserResponseConverter
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.dto.UserResponseDTO
import com.autentia.tnt.binnacle.repositories.UserRepository
import com.autentia.tnt.binnacle.repositories.predicates.PredicateBuilder
import com.autentia.tnt.binnacle.repositories.predicates.UserPredicates
import io.micronaut.data.jpa.repository.criteria.Specification
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class UsersRetrievalUseCase internal constructor(
    private val userRepository: UserRepository,
    private val userResponseConverter: UserResponseConverter
) {
    @Transactional
    fun getUsers(userIds: List<Long>?, active: Boolean?): List<UserResponseDTO> {
       val predicate: Specification<User> = getPredicateFromUsersFilter(userIds, active)
        val users = userRepository.findAll(predicate)
        return users.map { userResponseConverter.mapUserToUserResponseDTO(it) }
    }

    private fun getPredicateFromUsersFilter(userIds: List<Long>?, active: Boolean?): Specification<User>{
        var predicate: Specification<User> = UserPredicates.ALL

        if(active !== null){
            predicate = PredicateBuilder.and(predicate, UserPredicates.isActive(active))
        }

        if(!userIds.isNullOrEmpty()){
            predicate = PredicateBuilder.and(predicate, UserPredicates.fromUserIds(userIds))
        }

        return predicate
    }
}
