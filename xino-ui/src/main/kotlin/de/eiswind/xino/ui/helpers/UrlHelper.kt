package de.eiswind.xino.ui.helpers

import org.slf4j.LoggerFactory
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Created by thomas on 25.05.15.
 */
object UrlHelper {

    private val LOGGER = LoggerFactory.getLogger(UrlHelper::class.java)

    fun encode(s: String): String {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name())
        } catch (e: UnsupportedEncodingException) {
            LOGGER.error("This should never happen", e)
            throw RuntimeException("URL", e)
        }

    }
}
