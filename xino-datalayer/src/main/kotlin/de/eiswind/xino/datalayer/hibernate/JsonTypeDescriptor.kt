package de.eiswind.xino.datalayer.hibernate

import com.fasterxml.jackson.core.type.TypeReference
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor
import org.hibernate.type.descriptor.java.MutableMutabilityPlan
import org.hibernate.usertype.DynamicParameterizedType
import java.util.*

class JsonTypeDescriptor : AbstractTypeDescriptor<Any>(Any::class.java, object : MutableMutabilityPlan<Any>() {
    override fun deepCopyNotNull(value: Any): Any {
        return JacksonUtil.clone(value)
    }
}), DynamicParameterizedType {

    private lateinit var jsonObjectClass: Class<*>
    private var jsonTypeReference: TypeReference<*>? = null

    override fun setParameterValues(parameters: Properties) {
        jsonObjectClass = (parameters.get(
                DynamicParameterizedType.PARAMETER_TYPE)

                as DynamicParameterizedType.ParameterType)
                .getReturnedClass()
        val jsonTypeParam = parameters.get("json.typereference") as String?
        if (jsonTypeParam != null) {
            jsonTypeReference = Class.forName(jsonTypeParam)
                    .getConstructor().newInstance() as TypeReference<*>
        }
    }

    override fun areEqual(one: Any?, another: Any?): Boolean {
        if (one === another) {
            return true
        }
        if (one == null || another == null) {
            return false
        }
        return JacksonUtil.toJsonNode(JacksonUtil.toString(one)).equals(
                JacksonUtil.toJsonNode(JacksonUtil.toString(another)))
    }

    override fun toString(value: Any): String {
        return JacksonUtil.toString(value)
    }

    override fun fromString(string: String): Any {
        if (jsonTypeReference == null) {
            return JacksonUtil.fromString(string, jsonObjectClass)
        } else {
            return JacksonUtil.fromString(string,
                    jsonTypeReference as TypeReference<*>)
        }
    }


    override fun <X> unwrap(value: Any?, type: Class<X>, options: WrapperOptions): X? {
        if (value == null) {
            return null
        }
        if (String::class.java.isAssignableFrom(type)) {
            return toString(value) as X
        }
        if (Any::class.java.isAssignableFrom(type)) {
            return JacksonUtil.toJsonNode(toString(value)) as X
        }
        throw unknownUnwrap(type)
    }

    override fun <X> wrap(value: X?, options: WrapperOptions): Any? {
        if (value == null) {
            return null
        }
        return fromString(value.toString())
    }

}

