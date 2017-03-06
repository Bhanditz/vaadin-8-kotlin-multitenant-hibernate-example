package de.eiswind.xino.integration.pages

import com.machinepublishers.jbrowserdriver.JBrowserDriver
import de.eiswind.xino.ui.admin.usermanagement.ConfirmLostPasswordView
import de.eiswind.xino.ui.admin.usermanagement.ConfirmSignupView
import de.eiswind.xino.ui.login.LoginView
import de.eiswind.xino.ui.login.RequestLostPasswordView
import de.eiswind.xino.ui.login.RequestSignupView

/**
 * Created by thomas on 19.05.16.
 */
class LoginPage(override val driver: JBrowserDriver, private val port: Int) : AbstractPage(driver) {

    @JvmOverloads fun login(user: String, pass: String, shouldFail: Boolean = false) {


        driver.get("http://localhost:" + port + "/app")
        Thread.sleep(200)
        waitForId(LoginView.USERNAME_TEXT_FIELD)

        type(LoginView.USERNAME_TEXT_FIELD, user)
        type(LoginView.PASSWORD_TEXT_FIELD, pass)
        clickId(LoginView.SIGN_IN_BUTTON)

        if (!shouldFail) {
            waitForId("dashboardMenuItem")
        } else {
            clickFailed()
        }

    }

    fun logout() {
        driver.get("http://localhost:$port/login?logout")
    }

    fun clickFailed() {
        val xpath = "//*[contains(text(), 'Login failed')]"
        waitForXpathAndClick(xpath)
    }

    fun passwordLost(email: String) {
        driver.get("http://localhost:" + port)
        waitForId(LoginView.USERNAME_TEXT_FIELD)
        clickId(LoginView.LOST_PASSWORD_BUTTON)
        waitForId(RequestLostPasswordView.USERNAME_TEXT_FIELD)
        type(RequestLostPasswordView.USERNAME_TEXT_FIELD, email)
        clickId(RequestLostPasswordView.RESET_PASSWORD_BUTTON)
        waitForId(LoginView.USERNAME_TEXT_FIELD)
    }

    fun resetPassword(uuid: String, pass: String) {
        driver.get("http://localhost:" + port + "/app/login/#!resetPassword/" + uuid)
        waitForId(ConfirmLostPasswordView.PASSWORD_TEXT_FIELD)
        type(ConfirmLostPasswordView.PASSWORD_TEXT_FIELD, pass)
        Thread.sleep(100)
        type(ConfirmLostPasswordView.PASSWORD_MATCH_TEXT_FIELD, pass)
        Thread.sleep(100)
        clickId(ConfirmLostPasswordView.SAVE_PASSWORD_BUTTON)
        waitForId(LoginView.USERNAME_TEXT_FIELD)
    }

    fun resetPasswordFailure(uuid: String) {
        driver.get("http://localhost:" + port + "/app/login/#!resetPassword/" + uuid)
        waitForId(LoginView.USERNAME_TEXT_FIELD)
    }

    fun requestSignupPart1(email: String) {
        driver.get("http://localhost:" + port)
        waitForId(LoginView.USERNAME_TEXT_FIELD)
        clickId(LoginView.SIGN_UP_BUTTON)

        waitForId(RequestSignupView.USERNAME_TEXT_FIELD)
        type(RequestSignupView.USERNAME_TEXT_FIELD, email)
        type(RequestSignupView.FIRSTNAME_TEXT_FIELD, "firstname")
        type(RequestSignupView.LASTNAME_TEXT_FIELD, "lastname")
        type(RequestSignupView.PASSWORD_TEXT_FIELD, "test1234")
        type(RequestSignupView.PASSWORD_CONFIRM_TEXT_FIELD, "test1234")


    }

    fun requestSignupPart2(captcha: String) {
        type(RequestSignupView.CAPCTHA_TEXT_FIELD, captcha)
        clickId(RequestSignupView.REQUEST_ACCOUNT_BUTTON)
    }

    fun confirmSignup(uuid: String) {
        driver.get("http://localhost:" + port + "/app/login/#!confirmSignup/" + uuid)
        waitForId(ConfirmSignupView.CREATE_ACCOUNT_BUTTON)
        clickId(ConfirmSignupView.CREATE_ACCOUNT_BUTTON)
    }
}
