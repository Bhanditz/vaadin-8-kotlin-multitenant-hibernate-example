package de.eiswind.xino.datalayer.entities

import de.eiswind.xino.customtypes.Permission
import org.hibernate.annotations.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.Parameter
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.Length
import java.util.*
import javax.persistence.*
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "user_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
open class User : BaseTenantEntity() {

    open var id: Long? = null
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hilo_user")
        @GenericGenerator(
                name = "hilo_user",
                strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                parameters = arrayOf(
                        Parameter(name = "sequence_name", value = "seq_user_user"),
                        Parameter(name = "initial_value", value = "1"),
                        Parameter(name = "increment_size", value = "5"),
                        Parameter(name = "optimizer", value = "hilo")
                ))
        get

    open var active = true
        @Column(name = "active", nullable = false) get


    open var email: String = ""
        @Column(name = "email", nullable = false, unique = true)
        @Email
        @NaturalId
        @Length(max = 255) get

    open var password: String = ""
        @Length(max = 255) get

    open var lastName: String = ""
        @Column(name = "last_name")
        @Length(max = 255) get

    open var firstName: String = ""
        @Column(name = "first_name")
        @Length(max = 255) get

    open var permissions: MutableList<Permission> = ArrayList<Permission>()
        @Column(name = "permissions")
        @Type(type = "jsonb",
                parameters = arrayOf(Parameter(name = "json.typereference",
                        value = "de.eiswind.xino.customtypes.PermissionTypeReference"))) get

    override fun toString(): String {
        return this.email
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false
        if (version != other.version) return false
        return true
    }

    override fun hashCode(): Int {
        return 31
    }

}




