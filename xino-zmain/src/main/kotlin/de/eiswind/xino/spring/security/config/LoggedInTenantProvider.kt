package de.eiswind.xino.spring.security.config

import de.eiswind.xino.spring.jpa.tenancy.TenantProvider
import de.eiswind.xino.spring.security.TenantUserDetails
import org.slf4j.Logger
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Created by thomas on 13.09.16.
 */
@Component
class LoggedInTenantProvider constructor(val log: Logger) : TenantProvider {


    private var startup = true;

    override fun currentTenant(): String {
        val ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            val auth = ctx.getAuthentication();

            if (auth != null && auth.isAuthenticated) {
                val details = auth.principal as TenantUserDetails
                return details.getTenant()
            }

        }
        if (startup) {
            // we must allow hibernate to start up without a tenant connection
            return "access_denied"
        }
        throw AccessDeniedException("Not authenticated")
    }

    @EventListener
    fun contextStarted(event: ContextRefreshedEvent) {
        startup = false
        log.info("Startup complete, disallowing non-authenticated entity managers")
    }


}