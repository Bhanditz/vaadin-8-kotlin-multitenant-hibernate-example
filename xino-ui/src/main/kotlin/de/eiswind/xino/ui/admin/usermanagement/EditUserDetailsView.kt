package de.eiswind.xino.ui.admin.usermanagement


import com.vaadin.data.*
import com.vaadin.data.validator.StringLengthValidator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.spring.access.ViewInstanceAccessControl
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.datalayer.entities.User
import de.eiswind.xino.datalayer.repositories.UserRepository
import de.eiswind.xino.spring.security.XinoUserDetailsService
import de.eiswind.xino.ui.admin.beans.PasswordBean
import de.eiswind.xino.ui.errors.ErrorHandler
import de.eiswind.xino.ui.icons.GlobalIcons
import de.eiswind.xino.ui.navigation.MainUI
import de.eiswind.xino.ui.vaadin.bind
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.util.StringUtils
import org.vaadin.spring.security.shared.VaadinSharedSecurity
import java.util.*

@SpringView(name = EditUserDetailsView.NAME, ui = arrayOf(MainUI::class))
class EditUserDetailsView constructor(private val userRepository: UserRepository,
                                      private val userDetailsService: XinoUserDetailsService,
                                      private val vaadinSharedSecurity: VaadinSharedSecurity,
                                      private val log: Logger) : VerticalLayout(), View, ViewInstanceAccessControl {

    private val passwordField = PasswordField("Password")
    private val passwordMatchField = PasswordField("Retype password")
    private val userBeanBinder = BeanBinder<User>(User::class.java)
    private val passwordBean = PasswordBean()
    private val passBeanBinder = BeanBinder<PasswordBean>(PasswordBean::class.java)

    private val userNameField = TextField("Email")
    private lateinit var user: User
    //    private val permissionBeanItemContainer: BeanItemContainer<Permission>
    private val permissionSelect = TwinColSelect<Permission>()


    init {
        passBeanBinder.bean = passwordBean
        setSizeFull()
        addStyleName("plain-view")
        val label = Label("Edit User Details")
        label.id = EDIT_USER_DETAILS_LABEL
        label.addStyleName(ValoTheme.LABEL_H2)
        label.addStyleName(ValoTheme.LABEL_COLORED)

        val form = FormLayout()



        userNameField.id = USER_NAME_TEXT_FIELD
        form.addComponent(userNameField)

        val activeCheckbox = CheckBox("Active")
        userBeanBinder.forField(activeCheckbox).bind(User::active)
        activeCheckbox.id = ACTIVE_CHECKBOX
        form.addComponent(activeCheckbox)


        permissionSelect.setItems(Permission.values().asList())

        with(permissionSelect) {
            id = PERMISSION_SELECT
            leftColumnCaption = "Available Permissions"
            rightColumnCaption = "Selected Permissions"
            itemCaptionGenerator = ItemCaptionGenerator { p -> p.toString() }

        }

        form.addComponent(permissionSelect)




        passBeanBinder.forField(passwordField)
                .withValidator(StringLengthValidator(PASSWORD_REQUIRED, 1, 100))
                .bind(PasswordBean::password)


        passwordField.id = PASSWORD_TEXT_FIELD
        form.addComponent(passwordField)

        passBeanBinder.forField(passwordMatchField)
                .withValidator(PasswordMatchValidatorV8(passwordField))
                .bind(PasswordBean::passwordMatch)
        passwordMatchField.id = PASSWORD_MATCH_TEXT_FIELD
        passwordField.addValueChangeListener { event ->
            passBeanBinder.validate()
        }
        form.addComponent(passwordMatchField)


        val saveUserButton = Button("Save User")
        saveUserButton.id = SAVE_USER_BUTTON
        saveUserButton.icon = GlobalIcons.SAVE
        saveUserButton.addClickListener { event ->
            try {

                userBeanBinder.writeBean(user)

                if (passwordField.isVisible) {
                    passBeanBinder.writeBean(passwordBean)

                    if (!StringUtils.isEmpty(passwordField.value)) {
                        val passwordEncoder = BCryptPasswordEncoder()
                        user.password = passwordEncoder.encode(passwordField.value)
                    }
                }
                val permissions = ArrayList(permissionSelect.value as MutableSet<Permission>)
                user.permissions = permissions
                userRepository.save(user)
                ui.navigator.navigateTo(EditUsersView.NAME)
                Notification.show("User has been saved", Notification.Type.TRAY_NOTIFICATION)
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

    private fun bind() {


    }

    override fun enter(event: ViewChangeEvent) {
        if (event.parameters != null) {
            // split at "/", add each part as a label
            val msgs = event.parameters.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (msgs.size == 1) {
                if ("new" == msgs[0]) {
                    user = User()
                    user.email = ""
                    user.active = false
                    userBeanBinder.forField(userNameField)
                            .withValidator(UserNameUniqueValidator(userDetailsService))
                            .bind(User::email)
                    userBeanBinder.readBean(user)
                } else {
                    try {
                        val id = msgs[0].toLong()
                        val userFromDb = userRepository.findOne(id)
                        // TODO select groups
                        if (userFromDb != null) {
                            user = userFromDb
                            passwordField.isVisible = false

                            passwordMatchField.isVisible = false

                            userBeanBinder.forField(userNameField)
                                    .bind(User::email)
                            userBeanBinder.readBean(user)
                            permissionSelect.value = HashSet(user.permissions)

                            userNameField.setEnabled(false)
                        } else {
                            log.warn("Unknown user id " + id)
                            navigateToManager()
                        }

                    } catch (x: NumberFormatException) {
                        navigateToManager()
                    }

                }
            } else {
                navigateToManager()
            }
        }
    }

    private fun navigateToManager() {
        ui.navigator.navigateTo(EditUsersView.NAME)
    }

    override fun isAccessGranted(ui: UI, beanName: String, view: View): Boolean {
        if (view === this) {
            return vaadinSharedSecurity.hasAuthority(Permission.ADMIN.name)
        }
        return true
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L
        private val LOG = LoggerFactory.getLogger(EditUserDetailsView::class.java)

        const val NAME = "userDetails"

        val EDIT_USER_DETAILS_LABEL = "editUserDetailsLabel"
        val USER_NAME_TEXT_FIELD = "userNameTextField"
        val PASSWORD_TEXT_FIELD = "passwordTextField"
        val PASSWORD_MATCH_TEXT_FIELD = "passwordMatchTextField"
        val SAVE_USER_BUTTON = "saveUserButton"
        val ACTIVE_CHECKBOX = "activeCheckbox"
        val PERMISSION_SELECT = "permissionSelect"
        val ALREADY_EXISTS = "Email already exists"
        val PASSWORD_REQUIRED = "Password must be given"
    }
}

class UserNameUniqueValidator(val userDetailsService: XinoUserDetailsService) : Validator<String> {
    override fun apply(value: String?, context: ValueContext?): ValidationResult {
        try {
            userDetailsService.loadUserByUsername(value ?: "")
            return ValidationResult.error(EditUserDetailsView.ALREADY_EXISTS)
        } catch (x: UsernameNotFoundException) {
            return ValidationResult.ok();
        }
    }

}


