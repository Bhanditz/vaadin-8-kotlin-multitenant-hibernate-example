package de.eiswind.xino.spring.security

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
open class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): String {

        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || !authentication.isAuthenticated) {
            return ""
        }
        return (authentication.principal as TenantUserDetails).username
    }
}