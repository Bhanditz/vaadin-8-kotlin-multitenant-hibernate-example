package de.eiswind.xino.ui.login


import com.vaadin.data.*
import com.vaadin.data.validator.StringLengthValidator
import com.vaadin.event.ShortcutAction
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.Responsive
import com.vaadin.shared.ui.MarginInfo
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.datalayer.jooq.tables.interfaces.IUserTenant
import de.eiswind.xino.datalayer.jooq.tables.pojos.UserTenant
import de.eiswind.xino.datalayer.services.UserManagementService
import de.eiswind.xino.spring.security.XinoUserDetailsService
import de.eiswind.xino.ui.admin.usermanagement.EditUserDetailsView
import de.eiswind.xino.ui.admin.usermanagement.PasswordMatchValidatorV8
import de.eiswind.xino.ui.errors.ErrorHandler
import de.eiswind.xino.ui.icons.GlobalIcons
import de.eiswind.xino.ui.vaadin.bind
import org.dozer.DozerBeanMapper
import org.slf4j.Logger
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringView(name = RequestSignupView.NAME, ui = arrayOf(LoginUI::class))
class RequestSignupView constructor(private val userManagementService: UserManagementService,
                                    private val userDetailsService: XinoUserDetailsService,
                                    private val log: Logger,
                                    private val captchaHolder: CaptchaTestValueHolder) : VerticalLayout(), View {



    private val userNameField = TextField("Email")
    private val firstNameField = TextField("First name")
    private val lastNameField = TextField("Last name")
    private val streetField = TextField("Street")
    private val zipcodeField = TextField("Zipcode")
    private val cityField = TextField("City")
    private val phoneField = TextField("Phone")
    private val passwordField = PasswordField("Password")
    private val passwordMatchField = PasswordField("Repeat password")
    private val captcha = CaptchaComponent()

    private val captchaField: TextField = TextField("Enter Captcha")

    private val tenantBean = UserTenant()

    init {
        setSizeFull()

        val loginForm = buildSignupForm()
        addComponent(loginForm)
        setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER)
        tenantBean.email = ""
        tenantBean.firstName = ""
        tenantBean.lastName = ""
        tenantBean.street = ""
        tenantBean.zipcode = ""
        tenantBean.city = ""
        tenantBean.phone = ""
    }


    override fun enter(event: ViewChangeEvent) {


    }

    private fun buildSignupForm(): Component {
        val panel = VerticalLayout()
        panel.setSizeUndefined()
        panel.isSpacing = true
        Responsive.makeResponsive(panel)
        panel.addStyleName("login-panel")

        panel.addComponent(buildLabels())
        val signupForm = buildFields()
        panel.addComponent(signupForm)
//        panel.setComponentAlignment(signupForm, Alignment.MIDDLE_RIGHT)
        return panel
    }

    private fun buildFields(): Component {

        val form = FormLayout()
        form.setSizeUndefined()
        val binder = Binder<RequestSignupBean>(RequestSignupBean::class.java)
        binder.forField(userNameField)
                .withValidator(UserNameUniqueValidatorV8(userDetailsService))
                .bind(RequestSignupBean::email)
        binder.forField(firstNameField).bind(RequestSignupBean::firstName)
        binder.forField(lastNameField).bind(RequestSignupBean::lastName)
        binder.forField(streetField).bind(RequestSignupBean::street)
        binder.forField(zipcodeField).bind(RequestSignupBean::zipcode)
        binder.forField(cityField).bind(RequestSignupBean::city)
        binder.forField(phoneField).bind(RequestSignupBean::phone)
        binder.forField(passwordField)
                .withValidator(StringLengthValidator(EditUserDetailsView.PASSWORD_REQUIRED, 1, 100))
                .bind(RequestSignupBean::password)
        binder.forField(passwordMatchField)
                .withValidator(PasswordMatchValidatorV8(passwordField))
                .bind(RequestSignupBean::passwordMatch)

        binder.forField(captchaField).withValidator(captcha.validator).bind(RequestSignupBean::captcha)
        // for testing only
        captchaHolder.value = captcha.captchaAnswer

        form.addComponent(userNameField)
        form.addComponent(firstNameField)
        form.addComponent(lastNameField)
        form.addComponent(streetField)
        form.addComponent(zipcodeField)
        form.addComponent(cityField)
        form.addComponent(phoneField)
        form.addComponent(passwordField)
        form.addComponent(passwordMatchField)
        form.addComponent(captcha)
        form.addComponent(captchaField)

        with(userNameField) {
            icon = GlobalIcons.USER
            id = USERNAME_TEXT_FIELD
        }

        firstNameField.id = FIRSTNAME_TEXT_FIELD
        lastNameField.id = LASTNAME_TEXT_FIELD

        with(passwordField) {
            icon = GlobalIcons.PASSWORD
            id = PASSWORD_TEXT_FIELD
        }
        passwordMatchField.id = PASSWORD_CONFIRM_TEXT_FIELD
        captchaField.id = CAPCTHA_TEXT_FIELD

        val requestAccountButton = Button("Request account")
        with(requestAccountButton) {
            id = REQUEST_ACCOUNT_BUTTON
            addStyleName(ValoTheme.BUTTON_PRIMARY)
            setClickShortcut(ShortcutAction.KeyCode.ENTER)
            focus()
        }
        val buttonLayout = HorizontalLayout(requestAccountButton)

        buttonLayout.margin = MarginInfo(true, false, false, false)
        form.addComponent(buttonLayout)

        requestAccountButton.addClickListener { event ->
            try {
                val bean = RequestSignupBean()
                binder.writeBean(bean)


                val passwordEncoder = BCryptPasswordEncoder()
                bean.passwordEncoded = passwordEncoder.encode(bean.password)

                val tenant = userManagementService.completeTenant(bean.getTenant())
                userManagementService.requestAccount(tenant)
                ui.navigator.navigateTo(LoginView.NAME)
                Notification.show("Please check your inbox", Notification.Type.ASSISTIVE_NOTIFICATION)
            } catch (e: DataIntegrityViolationException) {
                Notification.show("Database integrity violated", e.cause?.message ?: "", Notification.Type.ERROR_MESSAGE)

            } catch (e: ValidationException) {
                ErrorHandler.handleCommitException(e)
            }
        }
        return form
    }


    private fun buildLabels(): Component {
        val labels = CssLayout()
        labels.addStyleName("labels")


        val title = Label("Signup for an account")
        title.setSizeUndefined()
        title.addStyleName(ValoTheme.LABEL_H3)
        title.addStyleName(ValoTheme.LABEL_LIGHT)

        labels.addComponent(title)
        return labels
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L


        const val NAME = "requestSignup"
        val USERNAME_TEXT_FIELD = "requestUsernameTextField"

        val REQUEST_ACCOUNT_BUTTON = "requestAccountButton"
        val FIRSTNAME_TEXT_FIELD = "firstNameTextField"
        val LASTNAME_TEXT_FIELD = "lastNameTextField"
        val PASSWORD_TEXT_FIELD = "passwordTEstField"
        val PASSWORD_CONFIRM_TEXT_FIELD = "passwordConfirmTesxtField"
        val CAPCTHA_TEXT_FIELD = "captchaTextField"
    }

}

@org.springframework.stereotype.Component
open class CaptchaTestValueHolder {
    open var value: String = ""
}

class UserNameUniqueValidatorV8(val userDetailsService: XinoUserDetailsService) : Validator<String> {
    override fun apply(value: String?, context: ValueContext?): ValidationResult {
        try {
            userDetailsService.loadUserByUsername(value ?: "")
            return ValidationResult.error(EditUserDetailsView.ALREADY_EXISTS)
        } catch (x: UsernameNotFoundException) {
            return ValidationResult.ok()
        }
    }
}

data class RequestSignupBean(var email: String = "",
                             var firstName: String = "",
                             var lastName: String = "",
                             var street: String = "",
                             var zipcode: String = "",
                             var city: String = "",
                             var phone: String = "",
                             var password: String = "",
                             var passwordMatch: String = "",
                             var captcha: String = "",
                             var passwordEncoded: String = "") {


    fun getTenant(): IUserTenant {
        val tenant = UserTenant()
        mapper.map(this, tenant);
        tenant.password = passwordEncoded
        return tenant
    }

    companion object {
        val mapper = DozerBeanMapper()
    }
}


