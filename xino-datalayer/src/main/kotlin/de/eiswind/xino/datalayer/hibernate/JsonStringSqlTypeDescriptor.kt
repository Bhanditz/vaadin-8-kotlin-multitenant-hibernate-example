package de.eiswind.xino.datalayer.hibernate

import org.hibernate.type.descriptor.ValueBinder
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.JavaTypeDescriptor
import org.hibernate.type.descriptor.sql.BasicBinder
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.SQLException

class JsonStringSqlTypeDescriptor : AbstractJsonSqlTypeDescriptor() {

    override fun <X> getBinder(
            javaTypeDescriptor: JavaTypeDescriptor<X>): ValueBinder<X> {
        return object : BasicBinder<X>(javaTypeDescriptor, this) {
            @Throws(SQLException::class)
            protected override fun doBind(
                    st: PreparedStatement,
                    value: X,
                    index: Int,
                    options: WrapperOptions) {
                st.setString(index,
                        javaTypeDescriptor.unwrap(value, String::class.java, options))
            }

            @Throws(SQLException::class)
            protected override fun doBind(
                    st: CallableStatement,
                    value: X,
                    name: String,
                    options: WrapperOptions) {
                st.setString(name,
                        javaTypeDescriptor.unwrap(value, String::class.java, options))
            }
        }
    }

    companion object {

        val INSTANCE = JsonStringSqlTypeDescriptor()
    }
}