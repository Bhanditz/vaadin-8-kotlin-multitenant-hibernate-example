package de.eiswind.xino.ui.login

import com.vaadin.event.ShortcutAction.KeyCode
import com.vaadin.icons.VaadinIcons
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.Responsive
import com.vaadin.server.ThemeResource
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.ui.icons.GlobalIcons
import org.slf4j.Logger
import org.springframework.security.core.AuthenticationException
import org.vaadin.spring.security.shared.VaadinSharedSecurity

@SpringView(name = LoginView.NAME, ui = arrayOf(LoginUI::class))
class LoginView constructor(private val security: VaadinSharedSecurity,
                            private val log: Logger) : VerticalLayout(), View {

    private var username = TextField("Username")

    private var password = PasswordField("Password")

    private val rememberMe = CheckBox("Remember me")

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
            icon = VaadinIcons.USER
            id = USERNAME_TEXT_FIELD
        }
        with(password) {
            icon = GlobalIcons.PASSWORD
            id = PASSWORD_TEXT_FIELD
        }

        rememberMe.id = REMEMBERME_CHECKBOX

        val signin = Button("Sign In")
        with(signin) {
            id = SIGN_IN_BUTTON
            addStyleName(ValoTheme.BUTTON_PRIMARY)
            setClickShortcut(KeyCode.ENTER)
            focus()
        }

        val lostPassWord = Button("Lost password?")
        with(lostPassWord) {
            id = LOST_PASSWORD_BUTTON
            addStyleName(ValoTheme.BUTTON_LINK)
        }

        val signup = Button("Sign up for an account")
        with(signup) {
            id = SIGN_UP_BUTTON
            addStyleName(ValoTheme.BUTTON_LINK)
        }
        val horizontalLayout = HorizontalLayout(signin, lostPassWord, signup)
        horizontalLayout.isSpacing = true
        horizontalLayout.setComponentAlignment(signin, Alignment.BOTTOM_LEFT)
        fields.addComponents(username, password, rememberMe, horizontalLayout)

        signin.addClickListener { event ->

            try {

                security.login(username.value, password.value, rememberMe.value)

            } catch (e: AuthenticationException) {
                Notification.show("Login failed", Notification.Type.ERROR_MESSAGE)
            } catch (e: Exception) {
                log.error("Authentication error", e)
            }
        }

        lostPassWord.addClickListener { event ->
            ui.navigator.navigateTo(RequestLostPasswordView.NAME)
        }

        signup.addClickListener { event ->
            ui.navigator.navigateTo(RequestSignupView.NAME)
        }

        return fields
    }

    private fun buildLabels(): Component {
        val labels = CssLayout()
        labels.addStyleName("labels")

        val welcome = Label("")
        labels.icon = ThemeResource("img/eiswind.svg.png")
        welcome.setSizeUndefined()
        welcome.addStyleName(ValoTheme.LABEL_H4)
        welcome.addStyleName(ValoTheme.LABEL_COLORED)
        labels.addComponent(welcome)

        val title = Label("Xino3 Anwendung ohne Funktion")
        title.setSizeUndefined()
        title.addStyleName(ValoTheme.LABEL_H3)
        title.addStyleName(ValoTheme.LABEL_LIGHT)

        labels.addComponent(title)
        return labels
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L


        const val NAME = ""
        val USERNAME_TEXT_FIELD = "usernameTextField"
        val REMEMBERME_CHECKBOX = "rememberMeCheckBox"
        val PASSWORD_TEXT_FIELD = "passwordTextField"
        val SIGN_IN_BUTTON = "signInButton"
        val SIGN_UP_BUTTON = "signUpButton"
        val LOST_PASSWORD_BUTTON = "lostPasswordButton"
    }

}