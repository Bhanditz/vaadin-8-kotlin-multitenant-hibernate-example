package de.eiswind.xino.ui.navigation

import com.vaadin.server.Resource
import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.ui.admin.usermanagement.EditUsersView
import de.eiswind.xino.ui.dashboard.DashBoardView
import de.eiswind.xino.ui.icons.GlobalIcons

enum class DashboardViewType constructor(val viewName: String,
                                                 val label: String, val icon: Resource,
                                                 val requiredRole: Permission) {

    DASHBOARD(DashBoardView.NAME, "Dashboard", GlobalIcons.HOME, Permission.NONE),
    USERS(EditUsersView.NAME, "Users", GlobalIcons.USER, Permission.ADMIN);


}
