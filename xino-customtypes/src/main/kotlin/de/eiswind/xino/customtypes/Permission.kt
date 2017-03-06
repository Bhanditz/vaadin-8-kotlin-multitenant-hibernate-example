package de.eiswind.xino.customtypes

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import org.jooq.*
import org.jooq.impl.DSL
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.Types
import java.util.*

class PermissionConverter : Converter<Any, MutableList<Permission>> {

    val OBJECT_MAPPER = ObjectMapper()

    init {
        OBJECT_MAPPER.registerModule(AfterburnerModule())
    }
    override fun from(t: Any?): MutableList<Permission> {

        return if (t == null)
            ArrayList<Permission>()
        else
            OBJECT_MAPPER.readValue(""+ t, PermissionTypeReference())

    }

    override fun to(u:  MutableList<Permission>?): Any? {
        return if (u == null || u.size==0)
            ""
        else
            OBJECT_MAPPER.writeValueAsString(u)

    }

    override fun fromType(): Class<Any> {
        return Any::class.java
    }

    override fun toType(): Class<MutableList<Permission>> {
        return MutableList::class.java as Class<MutableList<Permission>>
    }
}

class PermissionBinding : Binding<Any, MutableList<Permission>> {

    override fun converter(): Converter<Any, MutableList<Permission>> {
        return PermissionConverter()
    }

    @Throws(SQLException::class)
    override fun sql(ctx: BindingSQLContext<MutableList<Permission>>) {

        // This ::json cast is explicitly needed by PostgreSQL:
        ctx.render().visit(DSL.`val`(ctx.convert(converter()).value())).sql("::json")
    }

    @Throws(SQLException::class)
    override fun register(ctx: BindingRegisterContext<MutableList<Permission>>) {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR)
    }

    @Throws(SQLException::class)
    override fun set(ctx: BindingSetStatementContext<MutableList<Permission>>) {
        ctx.statement().setString(
                ctx.index(),
                ctx.convert(converter()).value() as String)
    }

    @Throws(SQLException::class)
    override fun get(ctx: BindingGetResultSetContext<MutableList<Permission>>) {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()))
    }

    @Throws(SQLException::class)
    override fun get(ctx: BindingGetStatementContext<MutableList<Permission>>) {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()))
    }

    // The below methods aren't needed in PostgreSQL:

    @Throws(SQLException::class)
    override fun set(ctx: BindingSetSQLOutputContext<MutableList<Permission>>) {
        throw SQLFeatureNotSupportedException()
    }

    @Throws(SQLException::class)
    override fun get(ctx: BindingGetSQLInputContext<MutableList<Permission>>) {
        throw SQLFeatureNotSupportedException()
    }
}


enum class Permission {
    NONE,
    ADMIN,
    USER
}

class PermissionTypeReference :
        TypeReference<MutableList<Permission>>() {
}