package de.eiswind.xino.datalayer.json

import com.vaadin.spring.boot.VaadinAutoConfiguration
import de.eiswind.xino.spring.jpa.PersistenceJPAConfig
import de.eiswind.xino.spring.jpa.tenancy.DataSourceConfigBean
import de.eiswind.xino.spring.jpa.tenancy.TenantProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Created by thomas on 13.09.16.
 */
@EnableJpaRepositories(basePackages = arrayOf("de.eiswind.xino.datalayer.repositories"))
@Import(PersistenceJPAConfig::class)
@EnableTransactionManagement(proxyTargetClass = true)
@SpringBootApplication(exclude = arrayOf(VaadinAutoConfiguration::class))
open class UserPermissionsConfiguration {


    @Bean
    open fun tenantProvider(conf: DataSourceConfigBean): TenantProvider {
        return DemoTenantProvider(conf)
    }

    @Bean
    open fun auditorAware(): AuditorAware<String> {
        return TestSpringSecurityAuditorAware()
    }
}

class DemoTenantProvider constructor(val conf: DataSourceConfigBean) : TenantProvider {
    override fun currentTenant(): String {
        return conf.prefix + "demo"
    }


}

open class TestSpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): String {
        return "test"
    }
}