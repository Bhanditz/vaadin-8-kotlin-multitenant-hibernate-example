package de.eiswind.xino.datalayer.hibernate

import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.usertype.DynamicParameterizedType

import java.util.Properties

class JsonStringType : AbstractSingleColumnStandardBasicType<Any>(JsonStringSqlTypeDescriptor.INSTANCE, JsonTypeDescriptor()), DynamicParameterizedType {

    override fun getName(): String {
        return "json"
    }

    override fun registerUnderJavaType(): Boolean {
        return true
    }

    override fun setParameterValues(parameters: Properties) {
        (javaTypeDescriptor as JsonTypeDescriptor).setParameterValues(parameters)
    }
}