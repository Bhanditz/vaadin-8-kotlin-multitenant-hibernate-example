package de.eiswind.xino.spring.jpa

import de.eiswind.xino.datalayer.services.ServiceMarker
import de.eiswind.xino.spring.jpa.tenancy.HibernateTenantResolver
import de.eiswind.xino.spring.jpa.tenancy.TenantAwareEntityManagerFactoryBean
import de.eiswind.xino.spring.jpa.tenancy.TenantConnectionProvider
import de.eiswind.xino.spring.jpa.tenancy.TenantProvider
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory
import org.hibernate.cfg.AvailableSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.*
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
@ComponentScan(basePackageClasses = arrayOf(PersistenceJPAConfig::class, ServiceMarker::class))
open class PersistenceJPAConfig {

    @Autowired
    private lateinit var dataSource: DataSource

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    open fun logger(ip: InjectionPoint): Logger {
        return LoggerFactory.getLogger(ip.member.declaringClass)
    }

    @Bean
    open fun postgresAdapter(): JpaVendorAdapter {
        val adapter = HibernateJpaVendorAdapter()
        adapter.setDatabase(Database.POSTGRESQL)
        adapter.jpaDialect.setPrepareConnection(false)
        return adapter
    }






    private fun jpaProperties(): Properties {
        val jpaProperties = Properties()
        jpaProperties.setProperty("hibernate.show_sql", "false")
        jpaProperties.setProperty(AvailableSettings.STATEMENT_FETCH_SIZE, "20")
        jpaProperties.setProperty(AvailableSettings.BATCH_VERSIONED_DATA, "true")
        jpaProperties.setProperty(AvailableSettings.STATEMENT_BATCH_SIZE, "20")
        jpaProperties.setProperty(AvailableSettings.ORDER_UPDATES, "true")
        jpaProperties.setProperty(AvailableSettings.ORDER_INSERTS, "true")

        jpaProperties.setProperty(AvailableSettings.MULTI_TENANT, "SCHEMA")
        jpaProperties.setProperty(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, HibernateTenantResolver::class.java.name)
        jpaProperties.setProperty(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, TenantConnectionProvider::class.java.name)
        jpaProperties.setProperty(AvailableSettings.CACHE_REGION_FACTORY, SingletonEhCacheRegionFactory::class.java.name)
        jpaProperties.setProperty(AvailableSettings.USE_SECOND_LEVEL_CACHE, "true")
        jpaProperties.setProperty(AvailableSettings.USE_QUERY_CACHE, "true")
        jpaProperties.setProperty(AvailableSettings.JDBC_TIME_ZONE,"UTC")
        return jpaProperties
    }

    @Bean @DependsOn("dbsetup")
    open fun entityManagerFactory(adapter: JpaVendorAdapter, tenantProvider: TenantProvider): LocalContainerEntityManagerFactoryBean {
        val em = TenantAwareEntityManagerFactoryBean(tenantProvider)
        em.jpaVendorAdapter = adapter
//        em.dataSource = dataSource
        em.setJpaProperties(jpaProperties())
        return em
    }


    @Bean
    open fun transactionManager(emf: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = emf
        return transactionManager
    }

    @Bean
    open fun exceptionTranslation(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }

}