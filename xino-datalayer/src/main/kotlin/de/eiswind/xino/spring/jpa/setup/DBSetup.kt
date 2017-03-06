package de.eiswind.xino.spring.jpa.setup

import de.eiswind.xino.datalayer.jooq.Tables.USER_TENANT
import de.eiswind.xino.datalayer.jooq.Tables.USER_USER
import de.eiswind.xino.datalayer.jooq.tables.interfaces.IUserTenant
import de.eiswind.xino.datalayer.jooq.tables.records.UserTenantRecord
import de.eiswind.xino.spring.jpa.tenancy.DataSourceConfigBean
import de.eiswind.xino.spring.jpa.tenancy.TenantAwareDataSource
import de.eiswind.xino.spring.jpa.tenancy.TenantProvider
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.DependsOn
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.sql.Connection
import java.util.*
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.sql.DataSource


/**
 * Created by thomas on 13.09.16.
 */
@Component("dbsetup") @DependsOn("liquibase")
open class DBSetup constructor(@Qualifier("master") val ds: DataSource,
                               val tenantDS: TenantAwareDataSource,
                               val config: DataSourceConfigBean,
                               val tenantProvider: TenantProvider,
                               val log: Logger) {


    @PostConstruct
    open fun postStartTasks() {
        log.info("Updating tenant schemata");
        val con = ds.getConnection();
        con.autoCommit = false
        try {


            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            val tenantRecords = create.select().from(USER_TENANT)
                    .where(USER_TENANT.ACTIVE.eq(true))
                    .fetchInto(UserTenantRecord::class.java)

            for (tenantRecord in tenantRecords) {
                createTenant(con, tenantRecord)

            }
        } finally {
            con.close()
        }


    }

    fun createTenant(con: Connection?, tenantRecord: UserTenantRecord) {
        log.info("Updating tenant schema for " + tenantRecord.name);


        val password = decode(tenantRecord.dbPassword)
        val user = tenantRecord.dbUser

        val prefix = config.prefix
        val url = config.url
        val db = url.substring(url.lastIndexOf("/") + 1)
        val userName = prefix + user

        if (con != null) {
            val stmt = con.createStatement()
            val rs = stmt.executeQuery("SELECT * FROM   pg_catalog.pg_user WHERE  usename = '" + userName + "'")

            if (!rs.next()) {

                stmt.execute("CREATE ROLE " + userName + " WITH NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT LOGIN ENCRYPTED PASSWORD '" + password + "'")
                stmt.execute("CREATE SCHEMA IF NOT EXISTS " + userName + " AUTHORIZATION " + userName)
                stmt.execute("ALTER ROLE " + userName + " SET search_path TO " + userName)
                stmt.execute("REVOKE CREATE ON SCHEMA " + userName + " FROM " + userName + " CASCADE")
            }
            stmt.execute("SET search_path TO " + userName)
            con.commit()
            stmt.close()

            val dbCon = JdbcConnection(con);
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbCon);
            val liquibase = Liquibase("changelog/tenant/changelog-tenant.xml",
                    ClassLoaderResourceAccessor(), database);


            liquibase.setChangeLogParameter("db.user", prefix + user);
            liquibase.setChangeLogParameter("db.name", db);
            liquibase.setChangeLogParameter("db.schema", prefix + user);
            liquibase.setChangeLogParameter("db.password", password);
            ;
            liquibase.update("tenant");

            val stmt2 = con.createStatement()
            stmt2.execute("SET search_path TO public")
            con.commit()
            stmt2.close()

        }

    }

    fun createTenant(token: UUID): IUserTenant {
        log.info("Creating new tenant schema");
        val con = ds.getConnection();
        try {


            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            val tenantRecord = create.select().from(USER_TENANT)
                    .where(USER_TENANT.CREATION_TOKEN.eq(token))
                    .fetchOneInto(UserTenantRecord::class.java)

            if (tenantRecord == null) {
                throw IllegalStateException("Invalid token")
            }
            createTenant(con, tenantRecord)
            return tenantRecord
        } finally {
            con.close()
        }
    }

    fun deleteTenant() {
        log.info("Deactivating tenant")
        val con = ds.getConnection();
        val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
        val tenant = tenantProvider.currentTenant()
        if (con != null) {
            try {
                create.deleteFrom(USER_TENANT).where(USER_TENANT.NAME.eq(tenant)).execute()
                create.update(USER_USER).set(USER_USER.ACTIVE, false).where(USER_USER.TENANT.eq(tenant)).execute()
                con.commit()
            } finally {
                con.close()
            }
        }

        val executor = Executors.newFixedThreadPool(1)
        val auth = SecurityContextHolder.getContext().authentication
        executor.submit {
            SecurityContextHolder.getContext().authentication = auth
            log.info("Dropping tenant schema")

            tenantDS.close()
            val con2 = ds.getConnection();

            if (con2 != null) {
                try {

                    val stmt = con2.createStatement()
                    stmt.executeUpdate("DROP SCHEMA " + tenant + " CASCADE")
                    stmt.executeUpdate("DROP ROLE " + tenant)
                    stmt.close()
                } catch (x: Throwable) {
                    log.error("dropping schema fail!", x)
                } finally {
                    con2.close()
                }
            }
            log.info("Done Dropping tenant schema")
        }


    }


    companion object {

        val key = "qcmjEmfq7abM5M3n" // todo externalize

        fun decode(pass: String): String {
            val encrypted = Base64.getDecoder().decode(pass)
            val keyBýtes = key.toByteArray()
            val c2: Cipher

            c2 = Cipher.getInstance("AES")

            val k2 = SecretKeySpec(keyBýtes, "AES")
            c2.init(Cipher.DECRYPT_MODE, k2)
            val data = c2.doFinal(encrypted)
            return String(data)
        }

        fun encode(pass: String): String {

            val keyBýtes = key.toByteArray()
            val c2: Cipher

            c2 = Cipher.getInstance("AES")

            val k2 = SecretKeySpec(keyBýtes, "AES")
            c2.init(Cipher.ENCRYPT_MODE, k2)
            val data = c2.doFinal(pass.toByteArray())
            return String(Base64.getEncoder().encode(data))
        }
    }


}



