package de.eiswind.xino.ui.admin.usermanagement

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.datalayer.services.UserManagementService
import de.eiswind.xino.ui.icons.GlobalIcons
import de.eiswind.xino.ui.login.LoginUI
import de.eiswind.xino.ui.login.LoginView
import org.slf4j.Logger

@SpringView(name = ConfirmSignupView.NAME, ui = arrayOf(LoginUI::class))
class ConfirmSignupView constructor(private val userManagementService: UserManagementService,
                                    private val log: Logger) : VerticalLayout(), View {


    private lateinit var token: String

    init {
        setSizeFull()
        addStyleName("plain-view")
        val label = Label("Create Account")
        label.id = CREATE_ACCOUNT_LABEL
        label.addStyleName(ValoTheme.LABEL_H2)
        label.addStyleName(ValoTheme.LABEL_COLORED)


        val createAccounButton = Button("Create my account now!")
        createAccounButton.id = CREATE_ACCOUNT_BUTTON
        createAccounButton.icon = GlobalIcons.USER
        createAccounButton.addClickListener { event ->


            userManagementService.createTenant(token)

            ui.navigator.navigateTo(LoginView.NAME)
            Notification.show("You will receive an email shortly when your account is ready!", Notification.Type.TRAY_NOTIFICATION)


        }


        val buttonLayout = HorizontalLayout(createAccounButton)
        buttonLayout.isSpacing = true
        val verticalLayout = VerticalLayout(label, buttonLayout)
        //        verticalLayout.setMargin(true);
        addComponent(verticalLayout)


    }


    override fun enter(event: ViewChangeEvent) {
        if (event.parameters != null) {
            // split at "/", add each part as a label
            val msgs = event.parameters.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (msgs.size == 1) {
                token = msgs[0]
                if (!userManagementService.checkSignupToken(token)) {
                    navigateToLogin()
                    Notification.show("Invalid Token", Notification.Type.TRAY_NOTIFICATION)
                }
            } else {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        ui.navigator.navigateTo(LoginView.NAME)
    }


    companion object {

        private val serialVersionUID = -4430276235082912377L


        const val NAME = "confirmSignup"
        val CREATE_ACCOUNT_LABEL = "createAccountLabel"
        val CREATE_ACCOUNT_BUTTON = "createAccountButton"

    }
}