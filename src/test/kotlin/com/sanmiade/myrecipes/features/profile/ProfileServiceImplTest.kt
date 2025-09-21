package com.sanmiade.myrecipes.features.profile

import com.sanmiade.myrecipes.utils.UserNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.instancio.Instancio
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import java.util.Optional

class ProfileServiceImplTest {

    private val userRepository: UserRepository = mockk()
    private val sut = ProfileServiceImpl(userRepository)

    @Test
    fun `getUserById returns user`() {
        val userEntity = Instancio.create(UserEntity::class.java)
        ArrangeBuilder().withUser(CONSTANTS.CORRECT_USER_ID, userEntity)

        val result = sut.getUserById(CONSTANTS.CORRECT_USER_ID)

        assertEquals(userEntity.email, result.email)
        assertEquals(userEntity.username, result.username)
    }

    @Test
    fun `getUserById throws error`() {
        ArrangeBuilder().withUser(CONSTANTS.NON_EXISTENT_USER_ID, null)

        val exception = assertThrows<UserNotFoundException> {
            sut.getUserById(CONSTANTS.NON_EXISTENT_USER_ID)
        }

        assert(exception.message!!.contains("User with id ${CONSTANTS.NON_EXISTENT_USER_ID} not found"))
    }

    @Test
    fun updateUser() {
        val userEntity = Instancio.create(UserEntity::class.java)
        ArrangeBuilder().withUser(CONSTANTS.CORRECT_USER_ID, userEntity)

        val result =
            sut.updateUser(CONSTANTS.CORRECT_USER_ID, ProfileRequest(CONSTANTS.NEW_EMAIL, CONSTANTS.NEW_USERNAME))

        assertEquals(userEntity.email, result.email)
        assertEquals(userEntity.username, result.username)
    }

    object CONSTANTS {
        const val CORRECT_USER_ID = 1L
        const val NON_EXISTENT_USER_ID = 555L
        const val NEW_EMAIL = "newEmail@gmail.com"
        const val NEW_USERNAME = "newUsername"
    }

    inner class ArrangeBuilder {

        fun withUser(userId: Long, user: UserEntity? = null) = apply {
            every { userRepository.findById(userId) } returns Optional.ofNullable(user)
        }
    }
}