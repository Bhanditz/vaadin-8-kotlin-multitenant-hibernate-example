package de.eiswind.xino.integration.pages

import de.eiswind.xino.ui.admin.usermanagement.UserProfileView
import de.eiswind.xino.ui.dashboard.DashBoardView
import org.openqa.selenium.WebDriver

/**
 * Created by thomas on 19.05.16.
 */
class EditUserProfilePage(override val driver: WebDriver) : AbstractPage(driver) {


    fun enterCurrentUserPassword(pass: String) {
        type(UserProfileView.CURRENT_PASSWORD_TEXT_FIELD, pass)

    }


    fun enterNewUserPassword(pass: String) {
        type(UserProfileView.PASSWORD_TEXT_FIELD, pass)

    }

    fun enterNewUserMatchPassword(pass: String) {
        type(UserProfileView.PASSWORD_MATCH_TEXT_FIELD, pass)

    }

    fun deleteAccount() {
        clickId(UserProfileView.DELETE_ACCOUNT_BUTTON)
        waitForId(UserProfileView.CONFIRM_DELETE_ACCOUNT_BUTTON)
        clickId(UserProfileView.CONFIRM_DELETE_ACCOUNT_BUTTON)
    }


    fun save(shouldFail: Boolean) {
        clickId(UserProfileView.SAVE_PROFILE_BUTTON)
        // should navigate to dashboard
        if (!shouldFail) {
            waitForId(DashBoardView.DASHBOARD_LABEL)
        }
    }


}
