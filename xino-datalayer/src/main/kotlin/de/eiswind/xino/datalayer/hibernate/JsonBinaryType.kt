package de.eiswind.xino.datalayer.hibernate

import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.usertype.DynamicParameterizedType
import java.util.*

class JsonBinaryType : AbstractSingleColumnStandardBasicType<Any>(JsonBinarySqlTypeDescriptor.INSTANCE, JsonTypeDescriptor()), DynamicParameterizedType {

    override fun getName(): String {
        return "jsonb"
    }

    override fun setParameterValues(parameters: Properties) {
        (getJavaTypeDescriptor() as JsonTypeDescriptor).setParameterValues(parameters)
    }

}