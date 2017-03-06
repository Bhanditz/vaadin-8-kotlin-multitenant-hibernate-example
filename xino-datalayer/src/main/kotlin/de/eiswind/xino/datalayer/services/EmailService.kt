package de.eiswind.xino.datalayer.services

import com.sun.mail.smtp.SMTPTransport
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException
import org.slf4j.Logger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Created by thomas on 22.09.16.
 */

@Component
@ConfigurationProperties(prefix = "eiswind.mail")
open class EmailService constructor(private val log: Logger) {

    var host: String? = null;
    var user: String? = null;
    var password: String? = null;
    var port: String? = null;
    var auth: String? = null;
    var socketFactoryClass: String? = null;
    var socketFactoryPort: String? = null;
    var starttlsEnable: String = ""


    fun sendMail(to: String, subject: String, body: String) {

        var mailProperties = createProperties()

        val session = Session.getInstance(mailProperties, null)
        val msg = MimeMessage(session)
        try {
            msg.setFrom(InternetAddress("thomas@eiswind.de"))
            msg.setRecipient(Message.RecipientType.TO, InternetAddress(to))
            msg.setSubject(subject)
            msg.setText(body)
            msg.setSentDate(Date())

            val t = session.getTransport("smtp") as SMTPTransport
            t.connect(mailProperties.get("mail.smtp.host") as String,
                    mailProperties.getProperty("mail.smtp.user"),
                    mailProperties.getProperty("mail.smtp.password"))

            t.sendMessage(msg, msg.getAllRecipients())
        } catch (x: MessagingException) {
            log.error("Send mail failed", x)

        } catch (x: RuntimeException) {
            log.error("Send mail failed", x)
        }
    }

    open fun createProperties(): Properties {
        val props = Properties()
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", user)
        props.put("mail.smtp.password", password)
        props.put("mail.smtp.port", port)
        props.put("mail.smtp.auth", auth)
        props.put("mail.smtp.socketFactory.port", socketFactoryPort)
        props.put("mail.smtp.socketFactory.class", socketFactoryClass)
        props.put("starttls.enable", starttlsEnable)
        return props
    }


}