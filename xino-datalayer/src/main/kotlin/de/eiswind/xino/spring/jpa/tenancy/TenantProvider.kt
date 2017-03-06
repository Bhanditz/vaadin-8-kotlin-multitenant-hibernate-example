package de.eiswind.xino.spring.jpa.tenancy

/**
 * Created by thomas on 13.09.16.
 */
interface TenantProvider {
    fun currentTenant(): String


}