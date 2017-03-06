package de.eiswind.xino.spring.jpa.tenancy

import org.hibernate.jpa.HibernateEntityManagerFactory
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import javax.persistence.EntityManagerFactory

/**
 * Created by thomas on 14.09.16.
 */
open class TenantAwareEntityManagerFactoryBean constructor(val tenantProvider: TenantProvider) : LocalContainerEntityManagerFactoryBean() {
    override fun createNativeEntityManagerFactory(): EntityManagerFactory {
        return TenantAwareEntityManagerFactoryProxy(super.createNativeEntityManagerFactory() as HibernateEntityManagerFactory, tenantProvider)
    }
}