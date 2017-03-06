package de.eiswind.xino.integration.selenium

import com.icegreen.greenmail.util.DummySSLSocketFactory
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import de.eiswind.xino.integration.AbstractIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.Security
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


open class PasswordLostTest : AbstractIntegrationTest() {

    lateinit var greenMail: GreenMail

    @Before
    fun setup() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory::class.java.getName())
        val config = ServerSetupTest.SMTPS
        config.setServerStartupTimeout(10000)
        greenMail = GreenMail(config)
        greenMail.start()
        greenMail.setUser("thomas@eiswind.de", "Hemoglob#74@df")
        greenMail.setUser("eiswind@gmail.com", "test")
    }

    @After
    fun tearDown() {
        greenMail.stop()
    }


    @Test
    fun resetPasswordPositive() {

        loginPage.passwordLost("eiswind@gmail.com")

        assertEquals(1, greenMail.receivedMessages.size)

        val msg = greenMail.receivedMessages[0]
        val body = GreenMailUtil.getBody(msg)
        println(body)
        assertTrue(body.indexOf(MARKER) > 0)
        val uuid = body.substring(body.indexOf(MARKER) + MARKER.length)
        loginPage.resetPassword(uuid, "test1234")

        loginPage.login("eiswind@gmail.com", "test1234")
        dashBoardPage.signOut()

        // we expect that we cannot do this twice

        loginPage.resetPasswordFailure(uuid)
    }

    @Test
    fun resetPasswordNegative() {
        val uuid = UUID.randomUUID()
        loginPage.resetPasswordFailure(uuid.toString())

    }

    companion object {
        const val MARKER = "resetPassword/"
    }
}