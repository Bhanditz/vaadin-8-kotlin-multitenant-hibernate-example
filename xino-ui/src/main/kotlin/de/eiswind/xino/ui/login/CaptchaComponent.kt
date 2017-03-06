package de.eiswind.xino.ui.login


import com.vaadin.data.ValidationResult
import com.vaadin.data.Validator
import com.vaadin.data.ValueContext
import com.vaadin.server.StreamResource
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Image

import nl.captcha.Captcha
import nl.captcha.gimpy.BlockGimpyRenderer
import nl.captcha.text.producer.DefaultTextProducer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * Created by thomas on 08.10.16.
 */
class CaptchaComponent : HorizontalLayout() {

    val captcha = Captcha.Builder(300, 40).addText(DefaultTextProducer()).gimp(BlockGimpyRenderer()).build()

    val captchaAnswer: String
        get() {
            return captcha.answer
        }

    val validator: Validator<String>
        get() {
            return CaptchaValidator()
        }

    init {

        val resource = StreamResource(CaptchaImage(), "captcha.png")
        addComponent(Image("", resource))

    }

    inner class CaptchaImage : StreamResource.StreamSource {
        override fun getStream(): InputStream {
            val stream = ByteArrayOutputStream()
            ImageIO.write(captcha.image, "png", stream);

            return ByteArrayInputStream(stream.toByteArray())
        }
    }

    inner class CaptchaValidator : Validator<String> {
        override fun apply(value: String?, context: ValueContext?): ValidationResult {

            if (captchaAnswer == value) {
                return ValidationResult.ok()
            }
            return ValidationResult.error("Invalid Captcha")
        }


    }


}


