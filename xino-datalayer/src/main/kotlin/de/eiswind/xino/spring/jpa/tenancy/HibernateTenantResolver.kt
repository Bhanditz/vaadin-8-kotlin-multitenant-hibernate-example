package de.eiswind.xino.spring.jpa.tenancy

import org.hibernate.context.spi.CurrentTenantIdentifierResolver

/**
 * Created by thomas on 14.09.16.
 */
class HibernateTenantResolver : CurrentTenantIdentifierResolver {


    private val tenantProvider: TenantProvider
        get () {
            return ContextProvider.context.getBean(TenantProvider::class.java)
        }


    override fun resolveCurrentTenantIdentifier(): String {
        return tenantProvider.currentTenant()
    }

    override fun validateExistingCurrentSessions(): Boolean {
        return true
    }

}