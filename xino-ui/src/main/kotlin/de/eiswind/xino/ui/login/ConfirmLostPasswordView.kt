package de.eiswind.xino.ui.admin.usermanagement


import com.vaadin.data.*
import com.vaadin.data.validator.StringLengthValidator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.datalayer.services.UserManagementService
import de.eiswind.xino.ui.errors.ErrorHandler
import de.eiswind.xino.ui.icons.GlobalIcons
import de.eiswind.xino.ui.login.LoginUI
import de.eiswind.xino.ui.login.LoginView
import de.eiswind.xino.ui.vaadin.bind
import org.slf4j.Logger
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.util.StringUtils

@SpringView(name = ConfirmLostPasswordView.NAME, ui = arrayOf(LoginUI::class))
class ConfirmLostPasswordView constructor(private val userManagementService: UserManagementService,
                                          private val log: Logger) : VerticalLayout(), View {

    private val passwordField = PasswordField("Password")
    private val passwordMatchField = PasswordField("Retype Password")

    private lateinit var token: String



    init {
        setSizeFull()
        addStyleName("plain-view")
        val label = Label("Reset Passwort")
        label.id = RESET_PASSWORT_LABEL
        label.addStyleName(ValoTheme.LABEL_H2)
        label.addStyleName(ValoTheme.LABEL_COLORED)

        val form = FormLayout()

        val passwordBean = LostPasswordBean()

        val binder = Binder<LostPasswordBean>(LostPasswordBean::class.java)


        binder.forField(passwordField).withValidator(StringLengthValidator(PASSWORD_REQUIRED, 1, 100))
                .bind(LostPasswordBean::password)

        passwordField.id = PASSWORD_TEXT_FIELD
        passwordField.addValueChangeListener { event ->
            binder.validate()
        }
        form.addComponent(passwordField)

        binder.forField(passwordMatchField)
                .withValidator(PasswordMatchValidatorV8(passwordField))
                .bind(LostPasswordBean::passwordMatch)
        passwordMatchField.id = PASSWORD_MATCH_TEXT_FIELD

        form.addComponent(passwordMatchField)


        val saveUserButton = Button("Save Password")
        saveUserButton.id = SAVE_PASSWORD_BUTTON
        saveUserButton.icon = GlobalIcons.SAVE
        saveUserButton.addClickListener { event ->
            try {

                binder.writeBean(passwordBean)

                if (!StringUtils.isEmpty(passwordBean.password)) {
                    val passwordEncoder = BCryptPasswordEncoder()
                    userManagementService.resetPassword(token, passwordEncoder.encode(passwordBean.password))
                }
                ui.navigator.navigateTo(LoginView.NAME)
                Notification.show("Password has been saved", Notification.Type.TRAY_NOTIFICATION)
            } catch (e: DataIntegrityViolationException) {
                Notification.show("Database integrity violated", e.cause?.message ?: "", Notification.Type.ERROR_MESSAGE)

            } catch (e: ValidationException) {
                ErrorHandler.handleCommitException(e)
            }


        }


        val buttonLayout = HorizontalLayout(saveUserButton)
        buttonLayout.isSpacing = true
        val verticalLayout = VerticalLayout(label, form, buttonLayout)
        //        verticalLayout.setMargin(true);
        addComponent(verticalLayout)


    }


    override fun enter(event: ViewChangeEvent) {
        if (event.parameters != null) {
            // split at "/", add each part as a label
            val msgs = event.parameters.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (msgs.size == 1) {
                token = msgs[0]
                if (!userManagementService.checkPasswordToken(token)) {
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


    data class LostPasswordBean(var password: String = "", var passwordMatch: String = "")




    companion object {

        private val serialVersionUID = -4430276235082912377L

        const val NAME = "resetPassword"
        val PASSWORD = "password"
        val PASSWORD_MATCH = "passwordMatch"
        val RESET_PASSWORT_LABEL = "resetPasswordLabel"
        val PASSWORD_TEXT_FIELD = "passwordResetTextField"
        val PASSWORD_MATCH_TEXT_FIELD = "passwordResetMatchTextField"
        val SAVE_PASSWORD_BUTTON = "savePasswordButton"
        val PASSWORD_REQUIRED = "Password must be given"
    }
}

class PasswordMatchValidatorV8 constructor(private val src: PasswordField) : Validator<String> {

    override fun apply(value: String, ctx: ValueContext): ValidationResult {
        val pw: String = src.value
        if (pw != value) {
            return ValidationResult.error(NO_MATCH)
        }
        return ValidationResult.ok()
    }

    companion object {
        const val NO_MATCH = "Passwords don't match"
    }
}