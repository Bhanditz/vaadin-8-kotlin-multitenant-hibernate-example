package de.eiswind.xino.spring.jpa.tenancy

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Created by thomas on 13.09.16.
 */
@Component
open class ContextProvider : ApplicationContextAware {
    override fun setApplicationContext(p0: ApplicationContext?) {
        context = p0 ?: throw IllegalStateException();
    }


    companion object {
        lateinit var context: ApplicationContext;

    }

}