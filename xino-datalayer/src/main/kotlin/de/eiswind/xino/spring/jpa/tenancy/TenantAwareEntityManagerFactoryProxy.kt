package de.eiswind.xino.spring.jpa.tenancy

import org.hibernate.Metamodel
import org.hibernate.Session
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.jpa.HibernateEntityManagerFactory
import javax.persistence.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.metamodel.EntityType


/**
 * Created by thomas on 14.09.16.
 */
open class TenantAwareEntityManagerFactoryProxy constructor(var delegate: HibernateEntityManagerFactory, val tenantProvider: TenantProvider) : HibernateEntityManagerFactory {
    override fun getSessionFactory(): SessionFactoryImplementor {
        return delegate.sessionFactory
    }


    override fun getEntityTypeByName(p0: String?): EntityType<*> {
        return delegate.getEntityTypeByName(p0)
    }

    override fun <T : Any?> findEntityGraphsByType(entityClass: Class<T>?): MutableList<EntityGraph<in T>>? {
        return findEntityGraphsByType(entityClass)
    }


    override fun getCriteriaBuilder(): CriteriaBuilder {
        return delegate.criteriaBuilder
    }

    override fun getMetamodel(): Metamodel {
        return delegate.metamodel
    }

    override fun getProperties(): MutableMap<String, Any>? {
        return delegate.properties
    }

    override fun <T : Any?> unwrap(cls: Class<T>?): T {
        return delegate.unwrap(cls)
    }

    override fun createEntityManager(): EntityManager {
        var em = delegate.createEntityManager()
        enableFilter(em)
        return em;
    }


    override fun createEntityManager(map: MutableMap<Any?, Any?>?): EntityManager {
        var em = delegate.createEntityManager(map)
        enableFilter(em)
        return em;
    }

    override fun createEntityManager(synchronizationType: SynchronizationType?): EntityManager {
        var em = delegate.createEntityManager(synchronizationType)
        enableFilter(em)
        return em;
    }

    override fun createEntityManager(synchronizationType: SynchronizationType?, map: MutableMap<Any?, Any?>?): EntityManager {
        var em = delegate.createEntityManager(synchronizationType, map)
        enableFilter(em)
        return em;
    }

    override fun addNamedQuery(name: String?, query: Query?) {
        delegate.addNamedQuery(name, query)
    }

    override fun isOpen(): Boolean {
        return delegate.isOpen
    }

    override fun getCache(): Cache {
        return delegate.cache
    }

    override fun <T : Any?> addNamedEntityGraph(graphName: String?, entityGraph: EntityGraph<T>?) {
        delegate.addNamedEntityGraph(graphName, entityGraph)
    }

    override fun getPersistenceUnitUtil(): PersistenceUnitUtil {
        return delegate.persistenceUnitUtil
    }

    override fun close() {
        delegate.close()
    }

    private fun enableFilter(em: EntityManager) {

        var session = em.delegate as Session
        session.enableFilter("tenantFilter").setParameter("tenant", tenantProvider.currentTenant());
    }

}