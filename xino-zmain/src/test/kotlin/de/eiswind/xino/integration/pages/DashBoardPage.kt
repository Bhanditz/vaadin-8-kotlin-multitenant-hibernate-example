package de.eiswind.xino.integration.pages

import de.eiswind.xino.ui.admin.usermanagement.EditUsersView
import de.eiswind.xino.ui.admin.usermanagement.UserProfileView
import de.eiswind.xino.ui.errors.ErrorHandler
import de.eiswind.xino.ui.navigation.DashboardMenu
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by thomas on 19.05.16.
 */
class DashBoardPage(override val driver: WebDriver) : AbstractPage(driver) {


    fun gotoManageUsers() {
        clickId("usersMenuItem")
        waitForId(EditUsersView.MANAGE_USERS_LABEL)
    }


    fun signOut() {
        Thread.sleep(100)
        waitForId(DashboardMenu.USER_MENU_BAR)
        driver.findElement(By.id(DashboardMenu.USER_MENU_BAR)).findElement(By.tagName("span")).click()
        Thread.sleep(200)
        val xpath = "//*[contains(text(), 'Sign Out')]"
        waitForXpath(xpath)
        clickXpath(xpath)
    }

    fun gotoEditProfile() {
        driver.findElement(By.id(DashboardMenu.USER_MENU_BAR)).findElement(By.tagName("span")).click()
        val xpath = "//*[contains(text(), 'Edit Profile')]"
        waitForXpathAndClick(xpath)
        waitForId(UserProfileView.EDIT_USER_PROFILE_LABEL)
    }


    fun confirmErrorMessage(): String {
        val wait = WebDriverWait(driver, 1)
        val xpath = "//*[contains(text(), '" + ErrorHandler.INVALID_FORM + "')]"
        waitForXpath(xpath)
        val classname = "v-Notification-description"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(classname)))
        val element = driver.findElement(By.className(classname))
        val text = element.text
        clickXpath(xpath)
        //Thread.sleep(200)
        return text
    }
}
