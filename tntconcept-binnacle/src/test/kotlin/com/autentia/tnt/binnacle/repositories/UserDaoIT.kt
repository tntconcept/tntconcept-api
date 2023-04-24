package com.autentia.tnt.binnacle.repositories

import com.autentia.tnt.binnacle.entities.Role
import com.autentia.tnt.binnacle.entities.User
import com.autentia.tnt.binnacle.entities.WorkingAgreement
import com.autentia.tnt.binnacle.entities.WorkingAgreementTerms
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@MicronautTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserDaoIT {

    @Inject
    private lateinit var userDao: UserDao

    @Test
    fun `should find user by id`() {
        val user = User(
            id = 1L,
            hiringDate = LocalDate.now(),
            username = "admin",
            password = "dd94709528bb1c83d08f3088d4043f4742891f4f",
            name = "Administrador",
            email = "",
            dayDuration = 480,
            photoUrl = "",
            departmentId = 1L,
            role = Role(1L, "Administrador"),
            agreementYearDuration = null,
            agreement = WorkingAgreement(
                1L, setOf(
                    WorkingAgreementTerms(2, LocalDate.of(2022, 7, 22), 23, 105900),
                    WorkingAgreementTerms(1, LocalDate.of(1970, 1, 1), 22, 105900),
                )
            ),
            active = true
        )

        val result = userDao.findById(user.id)

        assertEquals(user, result.get())
    }

    @Test
    fun `should find list of users`() {
        val user = User(
            id = 1L,
            hiringDate = LocalDate.now(),
            username = "admin",
            password = "dd94709528bb1c83d08f3088d4043f4742891f4f",
            name = "Administrador",
            email = "",
            dayDuration = 480,
            photoUrl = "",
            departmentId = 1L,
            role = Role(1L, "Administrador"),
            agreementYearDuration = null,
            agreement = WorkingAgreement(
                1L, setOf(
                    WorkingAgreementTerms(2, LocalDate.of(2022, 7, 22), 23, 105900),
                    WorkingAgreementTerms(1, LocalDate.of(1970, 1, 1), 22, 105900),
                )
            ),
            active = true
        )

        val result = userDao.find()

        assertEquals(user, result.first())
    }

    @Test
    fun `should find by username`() {
        val user = User(
            id = 1L,
            hiringDate = LocalDate.now(),
            username = "admin",
            password = "dd94709528bb1c83d08f3088d4043f4742891f4f",
            name = "Administrador",
            email = "",
            dayDuration = 480,
            photoUrl = "",
            departmentId = 1L,
            role = Role(1L, "Administrador"),
            agreementYearDuration = null,
            agreement = WorkingAgreement(
                1L, setOf(
                    WorkingAgreementTerms(2, LocalDate.of(2022, 7, 22), 23, 105900),
                    WorkingAgreementTerms(1, LocalDate.of(1970, 1, 1), 22, 105900),
                )
            ),
            active = true
        )

        val result = userDao.findByUsername(user.username)
        assertEquals(user, result)
    }

    @Test
    fun `should find by active user`() {
        val user = User(
            id = 1L,
            hiringDate = LocalDate.now(),
            username = "admin",
            password = "dd94709528bb1c83d08f3088d4043f4742891f4f",
            name = "Administrador",
            email = "",
            dayDuration = 480,
            photoUrl = "",
            departmentId = 1L,
            role = Role(1L, "Administrador"),
            agreementYearDuration = null,
            agreement = WorkingAgreement(
                1L, setOf(
                    WorkingAgreementTerms(2, LocalDate.of(2022, 7, 22), 23, 105900),
                    WorkingAgreementTerms(1, LocalDate.of(1970, 1, 1), 22, 105900),
                )
            ),
            active = true
        )

        val result = userDao.findByActiveTrue()
        assertEquals(user, result.first())
    }
}