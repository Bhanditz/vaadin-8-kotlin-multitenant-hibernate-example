package de.eiswind.xino.ui.navigation

import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.server.Responsive
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.annotation.SpringViewDisplay
import com.vaadin.ui.CssLayout
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.UI
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.ui.dashboard.DashBoardView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StringUtils

/**

 */
@Theme("mytheme")
@Title("Eiswind Xino 3")
@SpringUI(path = "/")
@SpringViewDisplay
class MainUI constructor(private val navigator: DashboardNavigator,
                         private val dashboardMenu: DashboardMenu) : UI() {

    /**
     * this is injected for testing only at test time
     */
    @Autowired(required = false)
    private val uiRegistry: UIRegistry? = null

    private val content = CssLayout()

    override fun init(request: VaadinRequest) {

        Responsive.makeResponsive(this)
        addStyleName(ValoTheme.UI_WITH_MENU)
        val horizontalLayout = HorizontalLayout()
        horizontalLayout.addComponent(dashboardMenu)
        horizontalLayout.addStyleName("mainview")
        horizontalLayout.setSizeFull()

        content.addStyleName("view-content")
        content.setSizeFull()
        horizontalLayout.addComponent(content)


        horizontalLayout.setExpandRatio(content, 1.0f)
        setContent(horizontalLayout)
        navigator.init(this, content)
        if (StringUtils.isEmpty(navigator.state)) {
            navigator.navigateTo(DashBoardView.NAME)
        }
        if (uiRegistry != null) {
            uiRegistry.registeredUIs.add(this)
            addDetachListener { event -> uiRegistry.registeredUIs.remove(this) }
        }


    }


}
