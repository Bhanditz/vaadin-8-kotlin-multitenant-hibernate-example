package de.eiswind.xino.main

import de.eiswind.xino.spring.jpa.PersistenceJPAConfig
import de.eiswind.xino.spring.security.SecurityMarker
import de.eiswind.xino.spring.security.config.LoggedInTenantProvider
import de.eiswind.xino.ui.UIMarker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan


import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement


@EntityScan(basePackages = arrayOf("de.eiswind.xino.datalayer.entities"))
@EnableJpaRepositories(basePackages = arrayOf("de.eiswind.xino.datalayer.repositories"))
@Import(PersistenceJPAConfig::class)
@ComponentScan(basePackageClasses = arrayOf(SecurityMarker::class, UIMarker::class))
@EnableTransactionManagement(proxyTargetClass = true)
@SpringBootApplication
open class VaadinApplication {

    @Autowired
    private lateinit var tenantProvider: LoggedInTenantProvider

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(VaadinApplication::class.java, *args)
        }
    }
}

