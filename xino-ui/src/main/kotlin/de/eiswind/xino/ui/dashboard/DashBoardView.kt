package de.eiswind.xino.ui.dashboard

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.Responsive
import com.vaadin.spring.access.ViewInstanceAccessControl
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme
import de.eiswind.xino.ui.navigation.MainUI
import org.vaadin.spring.security.shared.VaadinSharedSecurity


@SpringView(name = DashBoardView.NAME, ui = arrayOf(MainUI::class))
class DashBoardView constructor(private val vaadinSharedSecurity: VaadinSharedSecurity) : VerticalLayout(), View, ViewInstanceAccessControl {

    private val label: Label
    private val verticalLayout: VerticalLayout


    init {
        setSizeFull()

        label = Label("Dashboard")
        label.id = DASHBOARD_LABEL
        label.addStyleName(ValoTheme.LABEL_H2)
        label.addStyleName(ValoTheme.LABEL_COLORED)

        addStyleName("dashboard-view")
        Responsive.makeResponsive(this)
        verticalLayout = VerticalLayout(label)
        addComponent(verticalLayout)
    }


    override fun enter(event: ViewChangeEvent) {
        label.value = "Welcome " + vaadinSharedSecurity.authentication.name
//        if (vaadinSharedSecurity.hasAuthority(User.Permission.APPROVER.authority)) {
//            val bySpotHistoryStatus = campaignRepository.findBySpotHistoryStatus(Spot.Status.REQUEST_FOR_APPROVAL)
//            if (bySpotHistoryStatus.size > 0) {
//                approvalLayout.isVisible = true
//                campaignBeanItemContainer.removeAllItems()
//                campaignBeanItemContainer.addAll(bySpotHistoryStatus)
//            }
//        }
    }

    override fun isAccessGranted(ui: UI, beanName: String, view: View): Boolean {
        if (view === this) {
            return vaadinSharedSecurity.isAuthenticated
        }
        return true
    }

    companion object {

        private val serialVersionUID = -4430276235082912377L

        //    private static final Logger LOGGER = LoggerFactory.getLogger(DashBoardView.class);

        const val NAME = "dashboard"
        val DASHBOARD_LABEL = "dashboardLabel"
        val PREVIEW_BUTTON = "previewButton"
        val CAMPAIGN_GRID = "campaignGrid"
    }
}