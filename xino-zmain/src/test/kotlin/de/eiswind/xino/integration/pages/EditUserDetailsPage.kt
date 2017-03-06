package de.eiswind.xino.integration.pages

import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.ui.admin.usermanagement.EditUserDetailsView
import de.eiswind.xino.ui.admin.usermanagement.EditUsersView
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

/**
 * Created by thomas on 19.05.16.
 */
class EditUserDetailsPage(override val driver: WebDriver) : AbstractPage(driver) {

    fun enterUserName(email: String) {
        driver.findElement(By.id(EditUserDetailsView.USER_NAME_TEXT_FIELD)).sendKeys(email)
    }

    fun enterBothUserPasswords(pass: String) {
        enterUserPassword(pass)
        enterUserPasswordMatch(pass)
    }

    fun enterUserPassword(pass: String) {
        type(EditUserDetailsView.PASSWORD_TEXT_FIELD, pass)

    }

    fun enterUserPasswordMatch(pass: String) {
        type(EditUserDetailsView.PASSWORD_MATCH_TEXT_FIELD, pass)

    }

    fun toggleActive() {
        driver.findElement(By.id(EditUserDetailsView.ACTIVE_CHECKBOX)).findElement(By.tagName("input")).click()
    }

    fun addPermission(permission: Permission) {
        driver.findElement(By.id(EditUserDetailsView.PERMISSION_SELECT)).findElement(By.xpath("//*[contains(text(), '" + permission.toString() + "')]")).click()
        driver.findElement(By.id(EditUserDetailsView.PERMISSION_SELECT)).findElement(By.xpath("//*[contains(text(), '>>')]/parent::*")).click()

    }

    fun save(shouldFail: Boolean) {
        Thread.sleep(50)
        clickId(EditUserDetailsView.SAVE_USER_BUTTON)

        if (!shouldFail) {
            // should navigate to manage users
            waitForId(EditUsersView.MANAGE_USERS_LABEL)
        }
    }
}
