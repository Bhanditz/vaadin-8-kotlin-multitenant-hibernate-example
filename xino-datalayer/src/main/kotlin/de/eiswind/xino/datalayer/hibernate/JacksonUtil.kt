package de.eiswind.xino.datalayer.hibernate

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import java.io.IOException
import java.util.*

object JacksonUtil {

    val OBJECT_MAPPER = ObjectMapper()

    init {
        OBJECT_MAPPER.registerModule(AfterburnerModule())
    }

    fun <T> fromNode(node: JsonNode, clazz: Class<T>): T {
        try {
            return OBJECT_MAPPER.treeToValue(node, clazz)
        } catch (e: IOException) {
            throw IllegalArgumentException("The given string value: "
                    + node + " cannot be transformed to Json object", e)
        }

    }

    fun <T> fromNode(node: JsonNode, reference: TypeReference<T>): T {
        try {
            return OBJECT_MAPPER.convertValue(node, reference)
        } catch (e: IOException) {
            throw IllegalArgumentException("The given string value: "
                    + node + " cannot be transformed to Json object", e)
        }
    }

    fun toNode(value: Any): JsonNode {
        try {
            return OBJECT_MAPPER.valueToTree<JsonNode>(value);
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("The given Json object value: "
                    + value + " cannot be transformed to a String", e)
        }

    }

    fun <T> fromString(string: String, clazz: Class<T>): T {
        try {
            return OBJECT_MAPPER.readValue(string, clazz)
        } catch (e: IOException) {
            throw IllegalArgumentException("The given string value: "
                    + string + " cannot be transformed to Json object", e)
        }

    }

    fun <T> fromString(string: String, reference: TypeReference<T>): T {
        try {
            return OBJECT_MAPPER.readValue(string, reference)
        } catch (e: IOException) {
            throw IllegalArgumentException("The given string value: "
                    + string + " cannot be transformed to Json object", e)
        }
    }

    fun toString(value: Any): String {
        try {
            return OBJECT_MAPPER.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("The given Json object value: "
                    + value + " cannot be transformed to a String", e)
        }

    }

    fun toJsonNode(value: String): JsonNode {
        try {
            return OBJECT_MAPPER.readTree(value)
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        }

    }

    fun <T : Any> clone(value: T, ref: TypeReference<T>): T {
        return fromNode(toNode(value), ref)
    }

    fun <T : Any> clone(value: T): T {
        return when (value) {
            is ArrayList<*> -> {
                val newList = ArrayList<Any?>()
                for (elem in value) {
                    newList.add(fromNode(
                            toNode(elem), elem.javaClass))
                }
                newList as T
            }
            else ->
                fromNode(toNode(value), value.javaClass)
        }
    }
}