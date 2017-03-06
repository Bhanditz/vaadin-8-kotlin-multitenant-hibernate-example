package de.eiswind.xino.datalayer.services

import de.eiswind.xino.datalayer.jooq.Sequences
import de.eiswind.xino.datalayer.jooq.Tables.USER_TENANT
import de.eiswind.xino.datalayer.jooq.Tables.USER_USER
import de.eiswind.xino.datalayer.jooq.tables.interfaces.IUserTenant
import de.eiswind.xino.datalayer.jooq.tables.pojos.UserTenant
import de.eiswind.xino.datalayer.jooq.tables.records.UserTenantRecord
import de.eiswind.xino.datalayer.jooq.tables.records.UserUserRecord
import de.eiswind.xino.spring.jpa.setup.DBSetup
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

/**
 * Created by thomas on 22.09.16.
 */
@Component
open class UserManagementService constructor(@Qualifier("master") private val masterDataSource: DataSource,
                                             private val emailService: EmailService,
                                             private val dbSetup: DBSetup,
                                             private val log: Logger) {


    open fun requestPasswordReset(email: String) {
        val con = masterDataSource.getConnection()

        try {

            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            create.transaction { conf ->
                val userRecord = DSL.using(conf).select().from(USER_USER)
                        .where(USER_USER.EMAIL.eq(email))
                        .fetchOneInto(UserUserRecord::class.java)
                if (userRecord != null) {
                    val user = userRecord

                    val uuid = UUID.randomUUID();
                    user.passwordResetToken = uuid
                    var plusOneDay = LocalDateTime.now().plusDays(1)
                    user.passwordResetTokenValidUntilDate = plusOneDay
                    user.store(USER_USER.PASSWORD_RESET_TOKEN, USER_USER.PASSWORD_RESET_TOKEN_VALID_UNTIL_DATE)
                    log.info("Created password reset Token for " + email + " : " + uuid)
                    emailService.sendMail(email, "Passwort reset request", "/app/login/#!resetPassword/" + uuid)
                }
            }

        } finally {
            con.close()
        }

    }


    open fun checkPasswordToken(token: String): Boolean {
        val con = masterDataSource.getConnection()

        try {

            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            val userRecord = create.select().from(USER_USER)
                    .where(USER_USER.PASSWORD_RESET_TOKEN.eq(UUID.fromString(token)))
                    .fetchOneInto(UserUserRecord::class.java)
            val user = userRecord ?: return false


            if (LocalDateTime.now().isAfter(user.passwordResetTokenValidUntilDate)) {
                return false
            }


        } finally {
            con.close()
        }
        return true
    }

    open fun checkSignupToken(token: String): Boolean {
        val con = masterDataSource.getConnection()

        try {

            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            val tenantRecord = create.select().from(USER_TENANT)
                    .where(USER_TENANT.CREATION_TOKEN.eq(UUID.fromString(token)))
                    .fetchOneInto(UserTenantRecord::class.java)
            val tenant = tenantRecord ?: return false


            if (LocalDateTime.now().isAfter(tenant.creationTokenValidUntilDate)) {
                return false
            }


        } finally {
            con.close()
        }
        return true
    }

    open fun resetPassword(token: String, pass: String) {
        if (checkPasswordToken(token)) {
            val con = masterDataSource.getConnection()
            try {

                val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
                create.transaction { conf ->
                    val userRecord = DSL.using(conf).select().from(USER_USER)
                            .where(USER_USER.PASSWORD_RESET_TOKEN.eq(UUID.fromString(token)))
                            .fetchOneInto(UserUserRecord::class.java)
                    val user = userRecord ?: throw IllegalStateException("Token must exist")
                    user.passwordResetToken = null

                    user.passwordResetTokenValidUntilDate = null
                    user.password = pass
                    user.store(USER_USER.PASSWORD_RESET_TOKEN, USER_USER.PASSWORD_RESET_TOKEN_VALID_UNTIL_DATE, USER_USER.PASSWORD)

                }


            } finally {
                con.close()
            }
        }
    }

    open fun requestAccount(tenant: IUserTenant) {
        val con = masterDataSource.getConnection()

        try {

            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            create.transaction { conf ->
                val dsl = DSL.using(conf)

                val tenantRecord = dsl.newRecord(USER_TENANT)
                tenantRecord.from(tenant);

                tenantRecord.active = false


                val uuid = UUID.randomUUID();
                tenantRecord.id = dsl.nextval(Sequences.SEQ_USER_TENANT)
                tenantRecord.version = 0
                tenantRecord.creationToken = uuid
                var plusOneDay = LocalDateTime.now().plusDays(1)
                tenantRecord.creationTokenValidUntilDate = plusOneDay
                tenantRecord.creationDate = LocalDateTime.now()
                tenantRecord.lastModificationDate = LocalDateTime.now()

                tenantRecord.store()
                log.info("Created account token for " + tenantRecord.email + " : " + uuid)
                emailService.sendMail(tenantRecord.email, "Signup request", "/app/login/#!confirmSignup/" + uuid)

            }

        } finally {
            con.close()
        }

    }


    fun completeTenant(incompleteTenant: IUserTenant): IUserTenant {
        val tenant = UserTenant()
        tenant.from(incompleteTenant)
        val domain = tenant.email.substring(tenant.email.indexOf('@') + 1)
        val tenantName = domain.substring(0, domain.indexOf('.') - 1)
        var counter = 0;
        // TODO check public email providers
        while (!checkTenantName(tenantName + counter) && counter < 100) {
            ++counter
        }
        if (!checkTenantName(tenantName + counter)) {
            throw IllegalStateException("100+ tenants per domain")
        }
        tenant.name = tenantName + counter
        tenant.dbUser = tenant.name
        val rand = Random().nextInt()
        tenant.dbPassword = DBSetup.encode(Integer.toHexString(rand))
        return tenant
    }

    private fun checkTenantName(tenantName: String): Boolean {
        val con = masterDataSource.getConnection()
        try {

            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            val tenantCount = create.select().from(USER_TENANT)
                    .where(USER_TENANT.NAME.eq(tenantName)).count()

            return tenantCount == 0
        } finally {
            con.close()
        }
    }

    fun createTenant(token: String) {
        val tenant = dbSetup.createTenant(UUID.fromString(token))
        log.info("Created account for " + tenant.email + " : " + token)
        emailService.sendMail(tenant.email, "Your account is ready now!", "/app/login/");
    }

    fun deleteAccount() {
        dbSetup.deleteTenant()
    }


}