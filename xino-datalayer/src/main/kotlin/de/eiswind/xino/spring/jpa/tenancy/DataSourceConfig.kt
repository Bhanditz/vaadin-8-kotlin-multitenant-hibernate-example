package de.eiswind.xino.spring.jpa.tenancy


import net.ttddyy.dsproxy.listener.SLF4JLogLevel
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource


/**
 * Created by thomas on 12.09.16.
 */
@Configuration

open class DataSourceConfig {

    @Autowired
    lateinit var config: DataSourceConfigBean

    @Bean
    @Qualifier("master")
    @Profile("default")
    open fun masterDataSource(): DataSource {
        return DataSourceBuilder.create()
                .driverClassName(config.driverClassName)
                .username(config.username)
                .password(config.password)
                .url(config.url).build()
    }


    @Bean
    @Qualifier("submaster")
    @Profile("test")
    open fun masterDataSourceProxy(): DataSource {
        return DataSourceBuilder.create()
                .driverClassName(config.driverClassName)
                .username(config.username)
                .password(config.password)
                .url(config.url).build()
    }

    @Bean("masterDataSource")
    @Profile("test")
    @Qualifier("master")
    open fun dataSource(@Qualifier("submaster") dataSourceNative: DataSource): DataSource {
        return ProxyDataSourceBuilder
                .create(dataSourceNative)
                .logQueryBySlf4j(SLF4JLogLevel.INFO)
                .build()
    }

}