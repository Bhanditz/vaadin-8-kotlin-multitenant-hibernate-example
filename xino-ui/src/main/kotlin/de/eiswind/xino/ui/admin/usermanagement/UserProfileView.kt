package de.eiswind.xino.ui.admin.usermanagement


import com.vaadin.data.*
import com.vaadin.data.validator.StringLengthValidator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.VaadinSession
import com.vaadin.spring.access.ViewInstanceAccessControl
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.datalayer.entities.User
import de.eiswind.xino.datalayer.repositories.UserRepository
import de.eiswind.xino.datalayer.services.UserManagementService
import de.eiswind.xino.ui.admin.beans.ProfilePasswordBean
import de.eiswind.xino.ui.dashboard.DashBoardView
import de.eiswind.xino.ui.errors.ErrorHandler
import de.eiswind.xino.ui.icons.GlobalIcons
import de.eiswind.xino.ui.navigation.MainUI
import de.eiswind.xino.ui.vaadin.bind
import org.slf4j.Logger
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.util.StringUtils
import org.vaadin.spring.security.shared.VaadinSharedSecurity

@SpringView(name = UserProfileView.NAME, ui = arrayOf(MainUI::class))
class UserProfileView constructor(private val userRepository: UserRepository,
                                  private val userManagementService: UserManagementService,
                                  private val vaadinSharedSecurity: VaadinSharedSecurity,
                                  private val log: Logger) : VerticalLayout(), View, ViewInstanceAccessControl {

    private val currentPasswordField = PasswordField("Current password")
    private val passwordField = PasswordField("New password")
    private val passwordMatchField = PasswordField("Retype password")

    private lateinit var user: User

    init {


        setSizeFull()
        addStyleName("plain-view")
        val label = Label("Edit User Profile")
        label.id = EDIT_USER_PROFILE_LABEL
        label.addStyleName(ValoTheme.LABEL_H2)
        label.addStyleName(ValoTheme.LABEL_COLORED)

        val form = FormLayout()
        val changePassLabel = Label("Change Password")
        changePassLabel.addStyleName(ValoTheme.LABEL_H3)

        val item = ProfilePasswordBean()

        val binder = Binder<ProfilePasswordBean>(ProfilePasswordBean::class.java)

        binder.forField(currentPasswordField)
                .withValidator(CorrectPasswordValidator())
                .bind(ProfilePasswordBean::currentPassword)
        currentPasswordField.id = CURRENT_PASSWORD_TEXT_FIELD
        form.addComponent(currentPasswordField)

        binder.forField(passwordField)
                .withValidator(StringLengthValidator(PASSWORD_REQUIRED, 1, 100))
                .bind(ProfilePasswordBean::password)
        passwordField.id = PASSWORD_TEXT_FIELD
        form.addComponent(passwordField)


        binder.forField(passwordMatchField)
                .withValidator(PasswordMatchValidatorV8(passwordField))
                .bind(ProfilePasswordBean::passwordMatch)
        passwordMatchField.id = PASSWORD_MATCH_TEXT_FIELD
        passwordField.addValueChangeListener { event ->
            binder.validate()
        }
        form.addComponent(passwordMatchField)


        val saveUserButton = Button("Save Profile")
        saveUserButton.id = SAVE_PROFILE_BUTTON
        saveUserButton.icon = GlobalIcons.SAVE
        saveUserButton.addClickListener { event ->

            try {
                val bean = ProfilePasswordBean()
                binder.writeBean(bean)
                val newPassword = bean.password
                if (!StringUtils.isEmpty(newPassword)) {
                    val passwordEncoder = BCryptPasswordEncoder()
                    user.password = passwordEncoder.encode(newPassword)

                    userRepository.saveAndFlush(user)

                    log.info("Password changed for user " + user.email)
                    ui.navigator.navigateTo(DashBoardView.NAME)
                    Notification.show("Profile has been saved", Notification.Type.TRAY_NOTIFICATION)
                }


            } catch (e: DataIntegrityViolationException) {
                Notification.show("Database integrity violated", e.cause?.message ?: "", Notification.Type.ERROR_MESSAGE)

            } catch (e: ValidationException) {
                ErrorHandler.handleCommitException(e)
            }


        }


        val deleteAccountButton = Button("Delete Account");
        deleteAccountButton.addClickListener { e ->

            val dlg = ConfirmDialog {
                userManagementService.deleteAccount()
                VaadinSession.getCurrent().close()
                vaadinSharedSecurity.logout()
            }
            ui.addWindow(dlg)
        }
        deleteAccountButton.id = DELETE_ACCOUNT_BUTTON
        if (!vaadinSharedSecurity.hasAuthority(Permission.ADMIN.name)) {
            deleteAccountButton.isEnabled = false
        }
        val buttonLayout = HorizontalLayout(saveUserButton)
        buttonLayout.isSpacing = true
        val dangerlabel = Label("Danger Zone!")

        val verticalLayout = VerticalLayout(label, changePassLabel, form, buttonLayout, dangerlabel, deleteAccountButton)
        //        verticalLayout.setMargin(true);
        addComponent(verticalLayout)


    }


    override fun enter(event: ViewChangeEvent) {

        val details = vaadinSharedSecurity.authentication.principal as UserDetails
        val email = details.username
        user = userRepository.findByEmail(email) ?: throw IllegalStateException("User must exist")


    }

    private fun navigateToManager() {
        ui.navigator.navigateTo(EditUsersView.NAME)
    }

    override fun isAccessGranted(ui: UI, beanName: String, view: View): Boolean {
        if (view === this) {
            return vaadinSharedSecurity.hasAuthority(Permission.USER.name)
        }
        return true
    }


    inner class CorrectPasswordValidator : Validator<String> {
        val passwordEncoder = BCryptPasswordEncoder()

        override fun apply(value: String?, context: ValueContext?): ValidationResult {

            if (!passwordEncoder.matches(value, user.password)) {
                return ValidationResult.error(INCORRECT_PASSWORD)
            }
            return ValidationResult.ok()
        }
    }

    class ConfirmDialog(callback: () -> Unit) : Window("Are you sure?") {
        init {
            isModal = true
            val label = Label("This will destroy your account irreversibly")
            val noButton = Button("No")
            val yesButton = Button("Yes, destroy my account")
            yesButton.id = CONFIRM_DELETE_ACCOUNT_BUTTON
            val hl = HorizontalLayout(noButton, yesButton)
            hl.isSpacing = true
            val vl = VerticalLayout(label, hl)
            content = vl
            noButton.addClickListener { e ->
                (parent as UI).removeWindow(this)
            }
            yesButton.addClickListener { e ->
                (parent as UI).removeWindow(this)
                callback.invoke()
            }
        }
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L


        const val NAME = "userProfile"

        val EDIT_USER_PROFILE_LABEL = "editUserProfileLabel"
        val DELETE_ACCOUNT_BUTTON = "deleteAccountButton"
        val CONFIRM_DELETE_ACCOUNT_BUTTON = "confirmDeleteAccountButton"
        val CURRENT_PASSWORD_TEXT_FIELD = "currentPasswordTextField"
        val PASSWORD_TEXT_FIELD = "passwordTextField"
        val PASSWORD_MATCH_TEXT_FIELD = "passwordMatchTextField"
        val SAVE_PROFILE_BUTTON = "saveProfileButton"
        const val INCORRECT_PASSWORD = "Incorrect password"
        const val PASSWORD_REQUIRED = "Password must be given"
    }
}

