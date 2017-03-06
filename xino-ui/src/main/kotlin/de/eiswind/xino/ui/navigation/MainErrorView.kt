package de.eiswind.xino.ui.navigation

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import com.vaadin.ui.VerticalLayout
import de.eiswind.xino.ui.dashboard.DashBoardView


class MainErrorView : VerticalLayout(), View {

    init {
        setSizeFull()
        val label = Label("The page you requested is not available.")
        val button = Button("Back to main page")
        button.addClickListener { event -> ui.navigator.navigateTo(DashBoardView.NAME) }
        addComponent(label)
        addComponent(button)
    }


    override fun enter(event: ViewChangeEvent) {

    }

    companion object {

        private val serialVersionUID = -4430276235082912377L
    }
}//    private static final Logger LOGGER = LoggerFactory.getLogger(MainErrorView.class);
//    public static final String NAME = "error";