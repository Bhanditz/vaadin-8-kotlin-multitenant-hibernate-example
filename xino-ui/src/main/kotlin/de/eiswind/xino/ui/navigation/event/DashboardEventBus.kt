package de.eiswind.xino.ui.navigation.event

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.SubscriberExceptionContext
import com.google.common.eventbus.SubscriberExceptionHandler
import com.vaadin.spring.annotation.UIScope
import org.springframework.stereotype.Component

import java.io.Serializable

/**
 * A simple wrapper for Guava event bus. Defines static convenience methods for
 * relevant actions.
 */
@Component
@UIScope
class DashboardEventBus : SubscriberExceptionHandler, Serializable {

    @Transient private val eventBus = EventBus(this)

    fun post(event: Any) {
        eventBus.post(event)
    }

    fun register(`object`: Any) {
        eventBus.register(`object`)
    }

    fun unregister(`object`: Any) {
        eventBus.unregister(`object`)
    }

    override fun handleException(exception: Throwable,
                                 context: SubscriberExceptionContext) {
        exception.printStackTrace()
    }
}
