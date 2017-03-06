package de.eiswind.xino.datalayer.hibernate

import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.type.descriptor.ValueBinder
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.JavaTypeDescriptor
import org.hibernate.type.descriptor.sql.BasicBinder

import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.SQLException

class JsonBinarySqlTypeDescriptor : AbstractJsonSqlTypeDescriptor() {

    override fun <X : Any> getBinder(
            javaTypeDescriptor: JavaTypeDescriptor<X>): ValueBinder<X> {

        return object : BasicBinder<X>(javaTypeDescriptor, this) {
            @Throws(SQLException::class)
            override fun doBind(
                    st: PreparedStatement,
                    value: X,
                    index: Int,
                    options: WrapperOptions) {
                st.setObject(index,
                        javaTypeDescriptor.unwrap<X>(
                                value,
                                (JsonNode::class.java as Class<X>),
                                options),
                        getSqlType())
            }

            @Throws(SQLException::class)
            override fun doBind(
                    st: CallableStatement,
                    value: X,
                    name: String,
                    options: WrapperOptions) {
                st.setObject(name,
                        javaTypeDescriptor.unwrap<X>(
                                value,
                                (JsonNode::class.java as Class<X>),
                                options),
                        getSqlType())
            }
        }
    }

    companion object {

        val INSTANCE = JsonBinarySqlTypeDescriptor()
    }
}