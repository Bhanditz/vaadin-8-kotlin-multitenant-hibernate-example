package de.eiswind.xino.datalayer.hibernate

import org.hibernate.type.descriptor.ValueExtractor
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.JavaTypeDescriptor
import org.hibernate.type.descriptor.sql.BasicExtractor
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor

import java.sql.CallableStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

abstract class AbstractJsonSqlTypeDescriptor : SqlTypeDescriptor {

    override fun getSqlType(): Int {
        return Types.OTHER
    }

    override fun canBeRemapped(): Boolean {
        return true
    }

    override fun <X> getExtractor(
            javaTypeDescriptor: JavaTypeDescriptor<X>): ValueExtractor<X> {
        return object : BasicExtractor<X>(javaTypeDescriptor, this) {
            @Throws(SQLException::class)
            override fun doExtract(
                    rs: ResultSet,
                    name: String,
                    options: WrapperOptions): X {
                return javaTypeDescriptor.wrap(
                        rs.getObject(name), options)
            }

            @Throws(SQLException::class)
            override fun doExtract(
                    statement: CallableStatement,
                    index: Int,
                    options: WrapperOptions): X {
                return javaTypeDescriptor.wrap(
                        statement.getObject(index), options)
            }

            @Throws(SQLException::class)
            override fun doExtract(
                    statement: CallableStatement,
                    name: String,
                    options: WrapperOptions): X {
                return javaTypeDescriptor.wrap(
                        statement.getObject(name), options)
            }
        }
    }

}