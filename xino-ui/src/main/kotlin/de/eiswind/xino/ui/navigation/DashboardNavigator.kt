package de.eiswind.xino.ui.navigation


import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.UIScope
import com.vaadin.spring.navigator.SpringNavigator
import de.eiswind.xino.ui.navigation.event.DashboardEvent
import de.eiswind.xino.ui.navigation.event.DashboardEventBus
import org.springframework.stereotype.Component

@SuppressWarnings("serial")
@UIScope
@Component
class DashboardNavigator(private val dashboardEventBus: DashboardEventBus) : SpringNavigator() {


    init {
        initViewChangeListener()
    }


    private fun initViewChangeListener() {
        addViewChangeListener(object : ViewChangeListener {

            override fun beforeViewChange(event: ViewChangeListener.ViewChangeEvent): Boolean {
                // Since there's no conditions in switching between the views
                // we can always return true.
                return true
            }

            override fun afterViewChange(event: ViewChangeListener.ViewChangeEvent) {

                for (viewType in DashboardViewType.values()) {
                    if (viewType.viewName == event.viewName) {
                        dashboardEventBus.post(DashboardEvent.PostViewChangeEvent(viewType))
                        break
                    }
                }



                //                if (tracker != null) {
                //                    // The view change is submitted as a pageview for GA tracker
                //                    tracker.trackPageview("/dashboard/" + event.getViewName());
                //                }
            }
        })
    }


}
