package de.eiswind.xino.datalayer.json

import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.datalayer.entities.User
import de.eiswind.xino.datalayer.repositories.UserRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by thomas on 13.09.16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(UserPermissionsConfiguration::class))
class UserPermissionsTest {

    @Autowired
    private lateinit var userRepository: UserRepository;

    @Before
    fun before() {
        val user = User();
        user.email = "test@eiswind.de"
        user.active = true
        user.tenant = "demo"
        user.permissions.add(Permission.USER)
        user.permissions.add(Permission.ADMIN)
        userRepository.save(user)
    }

    @After
    fun after() {
        val user = userRepository.findByEmail("test@eiswind.de")
        userRepository.delete(user)
    }

    @Test
    fun testUserPermissions() {
        var user2: User?
        user2 = userRepository.findByEmail("test@eiswind.de")
        assertFalse(user2 == null)
        val user3 = user2 ?: throw  IllegalStateException("User not found")
        assertTrue(user3.permissions.contains(Permission.USER));
        assertTrue(user3.permissions.contains(Permission.ADMIN));


    }
}