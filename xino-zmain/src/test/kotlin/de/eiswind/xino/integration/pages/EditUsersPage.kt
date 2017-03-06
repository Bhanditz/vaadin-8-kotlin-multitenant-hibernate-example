package de.eiswind.xino.integration.pages

import de.eiswind.xino.integration.AbstractIntegrationTest
import de.eiswind.xino.ui.admin.usermanagement.EditUserDetailsView
import de.eiswind.xino.ui.admin.usermanagement.EditUsersView
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver

/**
 * Created by thomas on 19.05.16.
 */
class EditUsersPage(override val driver: WebDriver) : AbstractPage(driver) {

    fun addUser() {
        clickId(EditUsersView.ADD_USER_BUTTON)
        waitForId(EditUserDetailsView.EDIT_USER_DETAILS_LABEL)
    }

    fun editUser(email: String) {
        selectUser(email)
        clickId(EditUsersView.EDIT_USER_BUTTON)
        waitForId(EditUserDetailsView.EDIT_USER_DETAILS_LABEL)
    }

    fun deleteUser(email: String) {
        selectUser(email)
        AbstractIntegrationTest.captureScreenShot("BeforeDeleteUser")
        clickId(EditUsersView.DELETE_USER_BUTTON)
        AbstractIntegrationTest.captureScreenShot("AfterDeleteUser")
    }

    fun selectUser(email: String) {
        driver.findElement(By.id(EditUsersView.USER_GRID)).findElement(By.xpath("//*[contains(text(), '$email')]")).click()
    }

}
