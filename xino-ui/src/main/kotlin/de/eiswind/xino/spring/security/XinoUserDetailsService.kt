package de.eiswind.xino.spring.security

import de.eiswind.xino.datalayer.jooq.Tables.USER_USER
import de.eiswind.xino.datalayer.jooq.tables.records.UserUserRecord
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*
import javax.sql.DataSource

/**
 * Created by thomas on 16.05.16.
 */
@Component
class XinoUserDetailsService
@Autowired
constructor(@Qualifier("master") private val ds: DataSource) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {


        val con = ds.getConnection()

        try {

            val create = DSL.using(con, SQLDialect.POSTGRES_9_5)
            val userRecord = create.select().from(USER_USER)
                    .where(USER_USER.EMAIL.eq(email))
                    .and(USER_USER.ACTIVE.eq(true))
                    .fetchOneInto(UserUserRecord::class.java)
            val user = userRecord ?: throw UsernameNotFoundException("Login incorrect")


            val userDetails = object : TenantUserDetails {
                override fun getTenant(): String {
                    return user.tenant
                }

                override fun getAuthorities(): Collection<GrantedAuthority> {
                    val result = ArrayList<GrantedAuthority>()
                    val permissions = user.permissions
                    for (permission in permissions) {
                        result.add(SimpleGrantedAuthority(permission.name))
                    }

                    return result;

                }

                override fun getPassword(): String {
                    return user.password ?: ""
                }

                override fun getUsername(): String {
                    return user.email ?: ""
                }

                override fun isAccountNonExpired(): Boolean {
                    return true
                }

                override fun isAccountNonLocked(): Boolean {
                    return true
                }

                override fun isCredentialsNonExpired(): Boolean {
                    return true
                }

                override fun isEnabled(): Boolean {
                    return user.active
                }
            }
            return userDetails
        } finally {
            con.close()
        }

    }
}

interface TenantUserDetails : UserDetails {

    fun getTenant(): String
}
