package de.eiswind.xino.integration.selenium

import com.icegreen.greenmail.util.DummySSLSocketFactory
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import de.eiswind.xino.integration.AbstractIntegrationTest
import de.eiswind.xino.ui.login.CaptchaTestValueHolder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.security.Security
import kotlin.test.assertEquals
import kotlin.test.assertTrue


open class SignUpTest : AbstractIntegrationTest() {

    lateinit var greenMail: GreenMail

    @Autowired
    lateinit var captchaHolder: CaptchaTestValueHolder

    @Before
    fun setup() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory::class.java.getName())
        val config = ServerSetupTest.SMTPS
        config.setServerStartupTimeout(10000)
        greenMail = GreenMail(config)
        greenMail.start()
        greenMail.setUser("thomas@eiswind.de", "Hemoglob#74@df")
        greenMail.setUser("eiswind@eiswind.de", "test")
    }

    @After
    fun tearDown() {
        greenMail.stop()
    }


    @Test
    fun signUpNewAccount() {

        loginPage.requestSignupPart1("eiswind@eiswind.de")
        // the captcha is generated on pageload
        loginPage.requestSignupPart2(captchaHolder.value)
        captureScreenShot("aftersignup")
        Thread.sleep(1000)
        assertEquals(1, greenMail.receivedMessages.size)

        val msg = greenMail.receivedMessages[0]
        val body = GreenMailUtil.getBody(msg)
        println(body)
        assertTrue(body.indexOf(MARKER) > 0)
        val uuid = body.substring(body.indexOf(MARKER) + MARKER.length)
        loginPage.confirmSignup(uuid)

        greenMail.reset()
        Thread.sleep(5000)
        assertEquals(1, greenMail.receivedMessages.size)

        loginPage.login("eiswind@eiswind.de", "test1234")
        dashBoardPage.gotoEditProfile()
        editUserProfilePage.deleteAccount()
        //we would have to wait connection idle time until the schema gets really dropped
//        Thread.sleep(10000)
    }


    companion object {
        const val MARKER = "confirmSignup/"
    }
}