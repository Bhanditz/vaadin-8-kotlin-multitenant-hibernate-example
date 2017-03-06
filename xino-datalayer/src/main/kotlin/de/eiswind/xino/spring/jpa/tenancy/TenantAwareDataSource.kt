package de.eiswind.xino.spring.jpa.tenancy

import de.eiswind.xino.datalayer.jooq.Tables.USER_TENANT
import de.eiswind.xino.datalayer.jooq.tables.records.UserTenantRecord
import de.eiswind.xino.spring.jpa.setup.DBSetup
import net.ttddyy.dsproxy.listener.SLF4JLogLevel
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.AbstractDataSource
import org.springframework.stereotype.Component
import java.sql.Connection
import java.util.*
import javax.sql.DataSource

@Component
@Primary
class TenantAwareDataSource constructor(private val config: DataSourceConfigBean,
                                        private val tenantProvider: TenantProvider,
                                        private val log: Logger,
                                        @Qualifier("master") private val masterDS: DataSource) : AbstractDataSource() {


    val cache = HashMap<String, DataSource>()


    override fun getConnection(): Connection {
        val con = dataSource.connection
        return con
    }

    override fun getConnection(username: String?, password: String?): Connection {
        val con = dataSource.getConnection(username, password)
        return con
    }

    private val dataSource: DataSource
        get() {
            val ds = cache.computeIfAbsent(tenantProvider.currentTenant(), { key ->

                log.info("Creating tenant datasource");
                val con = masterDS.getConnection();
                con.autoCommit = false
                try {


                    val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
                    val tenantRecord = create.select().from(USER_TENANT)
                            .where(USER_TENANT.ACTIVE.eq(true))
                            .and(USER_TENANT.NAME.eq(tenantProvider.currentTenant()))
                            .fetchOneInto(UserTenantRecord::class.java)
                    val innerDS = DataSourceBuilder.create()
                            .driverClassName(config.driverClassName)
                            .username(tenantRecord.dbUser)
                            .password(DBSetup.decode(tenantRecord.dbPassword))
                            .url(config.url).build()

                    val tomDS = innerDS as org.apache.tomcat.jdbc.pool.DataSource
                    tomDS.minIdle = 0  // release all connections when we do not need them
                    tomDS.maxIdle = 0  // as we may have many concurrent pools
                    tomDS.initialSize = 1
                    tomDS.defaultAutoCommit = false
                    tomDS.minEvictableIdleTimeMillis = 60000
                    if (config.proxy) {
                        ProxyDataSourceBuilder
                                .create(innerDS)
                                .logQueryBySlf4j(SLF4JLogLevel.INFO)
                                .build()
                    } else {
                        innerDS
                    }
                } finally {
                    con.close()
                }
            })

            return ds;
        }

    fun close() {
        val ds = dataSource
        when (ds) {
            is net.ttddyy.dsproxy.support.ProxyDataSource -> {
                (ContextProvider.context.getBean("masterDataSourceProxy") as org.apache.tomcat.jdbc.pool.DataSource).close(true)
            }

            is org.apache.tomcat.jdbc.pool.DataSource -> ds.close(true)
            else -> throw IllegalStateException(ds.javaClass.name)
        }

    }

}
