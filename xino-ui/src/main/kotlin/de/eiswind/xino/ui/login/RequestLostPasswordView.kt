package de.eiswind.xino.ui.login

import com.vaadin.event.ShortcutAction.KeyCode
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.Responsive
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.datalayer.services.UserManagementService
import de.eiswind.xino.ui.icons.GlobalIcons
import org.slf4j.Logger

@SpringView(name = RequestLostPasswordView.NAME, ui = arrayOf(LoginUI::class))
class RequestLostPasswordView constructor(private val userManagementService: UserManagementService,
                                          private val log: Logger) : VerticalLayout(), View {

    private var username: TextField = TextField("Username")


    init {
        setSizeFull()

        val loginForm = buildLoginForm()
        addComponent(loginForm)
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER)

    }


    override fun enter(event: ViewChangeEvent) {

        //        loginTokenService.checkRememberMeCookie().ifPresent(token -> {
        //            try {
        //                security.login(token);
        //            } catch (Exception e) {
        //                LOGGER.error("This should not happen with remeber me tokens", e);
        //            }
        //        });
        //
        //        tenant.focus();
    }

    private fun buildLoginForm(): Component {
        val loginPanel = VerticalLayout()
        loginPanel.setSizeUndefined()
        loginPanel.isSpacing = true
        Responsive.makeResponsive(loginPanel)
        loginPanel.addStyleName("login-panel")

        loginPanel.addComponent(buildLabels())
        loginPanel.addComponent(buildFields())

        return loginPanel
    }

    private fun buildFields(): Component {
        val fields = VerticalLayout()
        fields.isSpacing = true
        fields.addStyleName("fields")


        with(username) {
            icon = GlobalIcons.USER
            id = USERNAME_TEXT_FIELD
        }


        val resetPass = Button("Reset Password")
        with(resetPass) {
            id = RESET_PASSWORD_BUTTON
            addStyleName(ValoTheme.BUTTON_PRIMARY)
            setClickShortcut(KeyCode.ENTER)
            focus()
        }



        fields.addComponents(username, resetPass)
        fields.setComponentAlignment(resetPass, Alignment.BOTTOM_LEFT)

        resetPass.addClickListener { event ->
            userManagementService.requestPasswordReset(username.value)
            ui.navigator.navigateTo(LoginView.NAME)
            Notification.show("Password reset link has been sent, if we know about this user. Please check your inbox!", Notification.Type.TRAY_NOTIFICATION)
        }


        return fields
    }

    private fun buildLabels(): Component {
        val labels = CssLayout()
        labels.addStyleName("labels")


        val title = Label("Request password reset")
        title.setSizeUndefined()
        title.addStyleName(ValoTheme.LABEL_H3)
        title.addStyleName(ValoTheme.LABEL_LIGHT)

        labels.addComponent(title)
        return labels
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L


        const val NAME = "lostPassword"
        val USERNAME_TEXT_FIELD = "LostPassUsernameTextField"

        val RESET_PASSWORD_BUTTON = "resetPasswordButton"

    }

}