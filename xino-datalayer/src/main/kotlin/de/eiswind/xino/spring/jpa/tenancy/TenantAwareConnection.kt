package de.eiswind.xino.spring.jpa.tenancy

import org.slf4j.LoggerFactory
import java.sql.Connection

/**
 * Created by thomas on 25.09.16.
 */
class TenantAwareConnection(val con: Connection) : Connection by con {

    override fun close() {
        if (log.isTraceEnabled) {
            log.trace("RESET AUTH")
        }
        var stmt = con.prepareStatement("RESET SESSION AUTHORIZATION; " +
                "SET search_path TO public;")
        stmt.execute()
        stmt.close()
        con.close()
    }

    companion object {
        val log = LoggerFactory.getLogger(TenantAwareConnection::class.java)
    }
}