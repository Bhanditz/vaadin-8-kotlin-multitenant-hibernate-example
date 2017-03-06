package de.eiswind.xino.ui.login

import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.annotation.SpringViewDisplay
import com.vaadin.ui.UI


@SpringUI(path = "/login")
@SpringViewDisplay
@Title("Eiswind Xino 3")
@Theme("mytheme")
class LoginUI constructor() : UI() {

    override fun init(request: VaadinRequest) {
//        val navigator = Navigator(this, this)
//        navigator.addProvider(viewProvider)
//        navigator.navigateTo(navigator.state)
    }

    companion object {
        private val serialVersionUID = 5310014981075920878L
    }

}