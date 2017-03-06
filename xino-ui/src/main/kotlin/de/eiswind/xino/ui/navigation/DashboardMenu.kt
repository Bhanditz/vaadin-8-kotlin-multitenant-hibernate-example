package de.eiswind.xino.ui.navigation

import com.google.common.eventbus.Subscribe
import com.vaadin.server.FontAwesome
import com.vaadin.server.ThemeResource
import com.vaadin.server.VaadinSession
import com.vaadin.shared.ui.label.ContentMode
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.*
import com.vaadin.ui.MenuBar.MenuItem
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.ui.admin.usermanagement.UserProfileView
import de.eiswind.xino.ui.navigation.event.DashboardEvent
import de.eiswind.xino.ui.navigation.event.DashboardEventBus
import org.vaadin.spring.security.shared.VaadinSharedSecurity
import javax.annotation.PostConstruct

/**
 * A responsive menu component providing user information and the controls for
 * primary navigation between the views.
 */
@SuppressWarnings("serial", "unchecked")
@org.springframework.stereotype.Component
@UIScope
class DashboardMenu constructor(private val eventBus: DashboardEventBus,
                                private val vaadinSharedSecurity: VaadinSharedSecurity) : CustomComponent() {

    //    private Label notificationsBadge;
    //    private Label reportsBadge;
    private lateinit var settingsItem: MenuItem


    init {
        primaryStyleName = "valo-menu"
        id = ID
        setSizeUndefined()

        // There's only one DashboardMenu per UI so this doesn't need to be
        // unregistered from the UI-scoped DashboardEventBus.


    }

    @PostConstruct
    fun init() {
        eventBus.register(this)
        compositionRoot = buildContent()
    }

    private fun buildContent(): Component {
        val menuContent = CssLayout()
        menuContent.addStyleName("sidebar")
        menuContent.addStyleName(ValoTheme.MENU_PART)
        menuContent.addStyleName("no-vertical-drag-hints")
        menuContent.addStyleName("no-horizontal-drag-hints")
        menuContent.setWidth(null)
        menuContent.setHeight("100%")

        menuContent.addComponent(buildTitle())
        menuContent.addComponent(buildUserMenu())
        menuContent.addComponent(buildToggleButton())
        menuContent.addComponent(buildMenuItems())

        return menuContent
    }

    private fun buildTitle(): Component {
        val logo = Label("Eiswind <strong>Xino 3</strong>", ContentMode.HTML)
        logo.setSizeUndefined()
        val logoWrapper = HorizontalLayout(logo)
        logoWrapper.setComponentAlignment(logo, Alignment.MIDDLE_CENTER)
        logoWrapper.addStyleName("valo-menu-title")
        return logoWrapper
    }


    private fun buildUserMenu(): Component {
        val settings = MenuBar()
        settings.addStyleName("user-menu")
        settings.id = USER_MENU_BAR

        settingsItem = settings.addItem("", ThemeResource(
                "img/profile-pic-300px.jpg"), null)

        settingsItem.text = vaadinSharedSecurity.authentication.name
        val profileItem = settingsItem.addItem("Edit Profile") { selectedItem -> UI.getCurrent().navigator.navigateTo(UserProfileView.NAME) }
        settingsItem.addItem("Preferences") { selectedItem ->
            Notification.show("Preferences", Notification.Type.HUMANIZED_MESSAGE)

        }
        settingsItem.addSeparator()
        settingsItem.addItem("Sign Out") { selectedItem ->
            VaadinSession.getCurrent().close()
            vaadinSharedSecurity.logout()
        }

        return settings
    }

    private fun buildToggleButton(): Component {
        val valoMenuToggleButton = Button("Menu") { event ->
            if (compositionRoot.styleName.contains(STYLE_VISIBLE)) {
                compositionRoot.removeStyleName(STYLE_VISIBLE)
            } else {
                compositionRoot.addStyleName(STYLE_VISIBLE)
            }

        }
        valoMenuToggleButton.icon = FontAwesome.LIST
        valoMenuToggleButton.addStyleName("valo-menu-toggle")
        valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS)
        valoMenuToggleButton.addStyleName(ValoTheme.BUTTON_SMALL)
        return valoMenuToggleButton
    }

    private fun buildMenuItems(): Component {
        val menuItemsLayout = CssLayout()
        menuItemsLayout.addStyleName("valo-menuitems")
        //        menuItemsLayout.setHeight(100.0f, Unit.PERCENTAGE);

        for (view in DashboardViewType.values()) {
            val requiredRole = view.requiredRole

            if (Permission.NONE.equals(requiredRole) || vaadinSharedSecurity.hasAuthority(requiredRole.name)) {
                val menuItemComponent = ValoMenuItemButton(view)
                menuItemComponent.id = view.viewName + "MenuItem"
                menuItemsLayout.addComponent(menuItemComponent)
            }

        }
        return menuItemsLayout

    }

    //    private Component buildBadgeWrapper(final Component menuItemButton,
    //                                        final Component badgeLabel) {
    //        CssLayout dashboardWrapper = new CssLayout(menuItemButton);
    //        dashboardWrapper.addStyleName("badgewrapper");
    //        dashboardWrapper.addStyleName(ValoTheme.MENU_ITEM);
    //        badgeLabel.addStyleName(ValoTheme.MENU_BADGE);
    //        badgeLabel.setWidthUndefined();
    //        badgeLabel.setVisible(false);
    //        dashboardWrapper.addComponent(badgeLabel);
    //        return dashboardWrapper;
    //    }


    @Subscribe
    fun postViewChange(event: DashboardEvent.PostViewChangeEvent) {
        // After a successful view change the menu can be hidden in mobile view.
        compositionRoot.removeStyleName(STYLE_VISIBLE)
    }


    inner class ValoMenuItemButton(private val view: DashboardViewType) : Button() {

        init {
            primaryStyleName = "valo-menu-item"
            icon = view.icon
            caption = view.label

            addClickListener { event ->
                UI.getCurrent().navigator.navigateTo(view.viewName)

            }
            eventBus.register(this)
        }

        @Subscribe
        fun postViewChange(event: DashboardEvent.PostViewChangeEvent) {
            removeStyleName(STYLE_SELECTED)
            if (event.view == view) {
                addStyleName(STYLE_SELECTED)
            }
        }

    }

    companion object {

        val ID = "dashboard-menu"
        //    public static final String REPORTS_BADGE_ID = "dashboard-menu-reports-badge";
        //    public static final String NOTIFICATIONS_BADGE_ID = "dashboard-menu-notifications-badge";
        private val STYLE_VISIBLE = "valo-menu-visible"
        val USER_MENU_BAR = "userMenuBar"
        val STYLE_SELECTED = "selected"
        var SETTINGS_MENU_ITEM = ""
    }
}
