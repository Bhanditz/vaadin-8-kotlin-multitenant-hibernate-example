package de.eiswind.xino.integration

import de.eiswind.xino.ui.navigation.UIRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by thomas on 14.07.15.
 */
@Configuration
open class ContextExtensions {

    @Bean
    open fun uiRegistry(): UIRegistry {
        return UIRegistry()
    }


}
