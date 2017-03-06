package de.eiswind.xino.spring.jpa.tenancy

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import java.sql.Connection

/**
 * Created by thomas on 14.09.16.
 */
class TenantConnectionProvider : MultiTenantConnectionProvider {


    private val dataSource: TenantAwareDataSource
        get() {
            return ContextProvider.context.getBean(TenantAwareDataSource::class.java)
        }


    override fun supportsAggressiveRelease(): Boolean {
        return true
    }

    override fun getConnection(tenantIdentifier: String?): Connection {
        return dataSource.getConnection()
    }

    override fun <T : Any?> unwrap(unwrapType: Class<T>?): T {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isUnwrappableAs(unwrapType: Class<*>?): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAnyConnection(): Connection {
        return dataSource.getConnection()
    }

    override fun releaseAnyConnection(connection: Connection?) {
        connection?.close()
    }

    override fun releaseConnection(tenantIdentifier: String?, connection: Connection?) {
        connection?.close();
    }

}