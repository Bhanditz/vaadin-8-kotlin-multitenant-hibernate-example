package de.eiswind.xino.ui.admin.usermanagement


import com.vaadin.data.provider.ListDataProvider
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.spring.access.ViewInstanceAccessControl
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.*
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.datalayer.entities.User
import de.eiswind.xino.datalayer.repositories.UserRepository
import de.eiswind.xino.ui.helpers.UrlHelper
import de.eiswind.xino.ui.icons.GlobalIcons
import de.eiswind.xino.ui.navigation.MainUI
import org.slf4j.Logger
import org.vaadin.spring.security.shared.VaadinSharedSecurity
import java.util.*

@SpringView(name = EditUsersView.NAME, ui = arrayOf(MainUI::class))
class EditUsersView constructor(private val userRepository: UserRepository,
                                private val vaadinSharedSecurity: VaadinSharedSecurity, private val log: Logger) : VerticalLayout(), View, ViewInstanceAccessControl {


    private val grid: Grid<User>
    private val deleteUserButton: Button
    private val editUserButton: Button
    private lateinit var users: MutableList<User>
    private lateinit var dataProvider: ListDataProvider<User>

    init {
        setSizeFull()
        addStyleName("plain-view")
        val label = Label("Manage Users")
        label.id = MANAGE_USERS_LABEL
        label.addStyleName(ValoTheme.LABEL_H2)
        label.addStyleName(ValoTheme.LABEL_COLORED)



        grid = Grid()
        grid.id = USER_GRID
        grid.addColumn{ u -> u.email }.setCaption("Username")
//        val activeColumn = grid.getColumn(User::active.name)
//        activeColumn.setRenderer(ImageRenderer(), CheckBoxConverter())

        val addUserButton = Button("Add User")
        addUserButton.id = ADD_USER_BUTTON
        addUserButton.icon = GlobalIcons.ADD
        addUserButton.addClickListener { event ->
            ui.navigator.navigateTo(EditUserDetailsView.NAME + "/new")

        }
        deleteUserButton = Button("Delete User")
        deleteUserButton.icon = GlobalIcons.DELETE
        deleteUserButton.id = DELETE_USER_BUTTON
        deleteUserButton.addClickListener { event ->
            val userRecord = grid.asSingleSelect().value
            if (userRecord.email == vaadinSharedSecurity.authentication.name) {
                Notification.show("You may not delete the current user.", Notification.Type.ASSISTIVE_NOTIFICATION)
            } else {
                log.info("Deleted user " + userRecord.email)
                userRepository.delete(userRecord)
                users.remove(userRecord)
                dataProvider.refreshAll()
                Notification.show("User has been deleted", Notification.Type.TRAY_NOTIFICATION)
            }
        }


        editUserButton = Button("Edit User")
        editUserButton.icon = GlobalIcons.EDIT
        editUserButton.id = EDIT_USER_BUTTON
        editUserButton.addClickListener { event ->
            val userRecord = grid.asSingleSelect().value
            if (userRecord.id == null) {
                throw IllegalStateException("Unsaved user (null id) in edit handler")
            }
            ui.navigator.navigateTo(EditUserDetailsView.NAME + "/" + UrlHelper.encode(userRecord.id!!.toString()))
        }
        grid.addSelectionListener { event -> updateButtonEnabled() }

        updateButtonEnabled()

        val buttonLayout = HorizontalLayout(addUserButton, editUserButton, deleteUserButton)
        buttonLayout.isSpacing = true
        val verticalLayout = VerticalLayout(label, grid, buttonLayout)
        //        verticalLayout.setMargin(true);
        addComponent(verticalLayout)


    }

    private fun updateButtonEnabled() {
        val userRecord = grid.asSingleSelect().value
        if (userRecord != null) {
            deleteUserButton.isEnabled = userRecord.email != vaadinSharedSecurity.authentication.name
            editUserButton.isEnabled = true
        } else {
            deleteUserButton.isEnabled = false
            editUserButton.isEnabled = false
        }
    }


    override fun enter(event: ViewChangeEvent) {
        users = ArrayList(userRepository.findAll())
        dataProvider = ListDataProvider(users)
        grid.dataProvider = dataProvider
    }

    override fun isAccessGranted(ui: UI, beanName: String, view: View): Boolean {
        if (view === this) {
            return vaadinSharedSecurity.hasAuthority(Permission.ADMIN.name)
        }
        return true
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L
        const val NAME = "users"
        val ADD_USER_BUTTON = "addUserButton"
        val MANAGE_USERS_LABEL = "manageUsersLabel"
        val EDIT_USER_BUTTON = "editUserButton"
        val DELETE_USER_BUTTON = "deleteUserButton"
        val USER_GRID = "userGrid"
    }
}