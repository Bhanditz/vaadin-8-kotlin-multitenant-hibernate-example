package de.eiswind.xino.integration.selenium

import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.datalayer.entities.User
import de.eiswind.xino.integration.AbstractIntegrationTest
import de.eiswind.xino.ui.admin.usermanagement.EditUserDetailsView
import de.eiswind.xino.ui.admin.usermanagement.PasswordMatchValidatorV8

import de.eiswind.xino.ui.admin.usermanagement.UserProfileView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


open class UserManagementTest : AbstractIntegrationTest() {

    @Before
    fun login() {
        loginPage.login("eiswind@gmail.com", "admin")
    }


    @Test
    fun createUser() {


        dashBoardPage.gotoManageUsers()

        editUsersPage.addUser()
        editUserDetailsPage.enterUserName("thomas@eiswind.de")
        editUserDetailsPage.enterBothUserPasswords("test1234")

        editUserDetailsPage.toggleActive()

        editUserDetailsPage.addPermission(Permission.USER)
//        captureScreenShot("beforesave")
        editUserDetailsPage.save(false)

        val u = userRepository.findByEmail("thomas@eiswind.de")
        assertTrue(u != null)

        if (u != null) {
            assertTrue(u.active)
//            assertTrue(u.permissions.contains(User.Permission.USER))
            assertNotNull(u.creationDate)

            val encoder = BCryptPasswordEncoder()
            assertTrue(encoder.matches("test1234", u.password))
        }


        dashBoardPage.signOut()

        loginPage.login("thomas@eiswind.de", "test1234")
        dashBoardPage.signOut()


    }

    @Test
    fun createUserAlreadyExistsTest() {


        dashBoardPage.gotoManageUsers()

        editUsersPage.addUser()
        editUserDetailsPage.enterUserName("eiswind@gmail.com")
        editUserDetailsPage.enterBothUserPasswords("test1234")
        editUserDetailsPage.toggleActive()
        editUserDetailsPage.addPermission(Permission.USER)
        editUserDetailsPage.save(true)

        val msg = dashBoardPage.confirmErrorMessage()
        assert(msg.contains(EditUserDetailsView.ALREADY_EXISTS))

        dashBoardPage.signOut()


    }

    @Test
    fun createUserPasswordNoMatchTest() {

        dashBoardPage.gotoManageUsers()

        editUsersPage.addUser()
        editUserDetailsPage.enterUserName("thomas@eiswind.de")
        editUserDetailsPage.enterUserPassword("test1234")

        editUserDetailsPage.enterUserPasswordMatch("test1235")
        editUserDetailsPage.toggleActive()
        editUserDetailsPage.addPermission(Permission.USER)
        editUserDetailsPage.save(true)

        val msg = dashBoardPage.confirmErrorMessage()
        assert(msg.contains(PasswordMatchValidatorV8.NO_MATCH))

        dashBoardPage.signOut()


    }


    @Test
    fun deactivateUserTest() {

        createTestUser()

        dashBoardPage.gotoManageUsers()

        editUsersPage.editUser("thomas@eiswind.de")
        editUserDetailsPage.toggleActive()
        editUserDetailsPage.save(false)
        val user = userRepository.findByEmail("thomas@eiswind.de")
        assertTrue(user != null)

        if (user != null) {
            assertFalse(user.active)
        }


        dashBoardPage.signOut()

        loginPage.login("thomas@eiswind.de", "test1234", true) // checks failure

    }

    private fun createTestUser() {
        val user = User()
        user.email = "thomas@eiswind.de"
        user.password = BCryptPasswordEncoder().encode("test1234")
        user.active = true
        userRepository.saveAndFlush(user)
    }

    @Test
    fun changePasswordTest() {
        createTestUser()

        dashBoardPage.gotoEditProfile()

        editUserProfilePage.enterCurrentUserPassword("admin")
        editUserProfilePage.enterNewUserPassword("test5678")
        editUserProfilePage.enterNewUserMatchPassword("test5678")
        editUserProfilePage.save(false)

        val user = userRepository.findByEmail("eiswind@gmail.com")
        assertTrue(user != null)

        if (user != null) {
            val encoder = BCryptPasswordEncoder()
            assertTrue(encoder.matches("test5678", user.password))
        }

        dashBoardPage.signOut()
        loginPage.login("eiswind@gmail.com", "admin", true)  // ok if fails
        loginPage.login("eiswind@gmail.com", "test5678") // this is the new pass
        dashBoardPage.signOut()

    }

    @Test
    fun changePasswordErrorsTest1() {
        createTestUser()

        dashBoardPage.gotoEditProfile()
        editUserProfilePage.enterCurrentUserPassword("demo2")
        editUserProfilePage.save(true)

        val msg = dashBoardPage.confirmErrorMessage()
        assertTrue(msg.contains(UserProfileView.INCORRECT_PASSWORD))
        assertTrue(msg.contains(UserProfileView.PASSWORD_REQUIRED))
        Thread.sleep(100)
        dashBoardPage.signOut()
    }

    @Test
    fun changePasswordErrorsTest2() {
        createTestUser()

        dashBoardPage.gotoEditProfile()
        editUserProfilePage.enterCurrentUserPassword("demo")
        editUserProfilePage.save(true)
        Thread.sleep(100)
        val msg = dashBoardPage.confirmErrorMessage()
        assertTrue(msg.contains(UserProfileView.PASSWORD_REQUIRED))
        Thread.sleep(100)
        dashBoardPage.signOut()
    }

    @Test
    fun changePasswordErrorsTest3() {
        createTestUser()

        dashBoardPage.gotoEditProfile()
        editUserProfilePage.enterCurrentUserPassword("demo")
        editUserProfilePage.enterNewUserPassword("test5678")
        editUserProfilePage.enterNewUserMatchPassword("test5679")
        editUserProfilePage.save(true)
        Thread.sleep(100)

        val msg = dashBoardPage.confirmErrorMessage()
        assertTrue(msg.contains(PasswordMatchValidatorV8.NO_MATCH))

        dashBoardPage.signOut()
    }

    @Test
    fun deleteUserTest() {
        createTestUser()

        dashBoardPage.gotoManageUsers()

        editUsersPage.deleteUser("thomas@eiswind.de")

        val user = userRepository.findByEmail("thomas@eiswind.de")
        assertFalse(user != null)
        dashBoardPage.signOut()
    }
}