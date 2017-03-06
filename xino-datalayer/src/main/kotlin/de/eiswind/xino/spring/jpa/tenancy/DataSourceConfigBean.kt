package de.eiswind.xino.spring.jpa.tenancy


import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


/**
 * Created by thomas on 12.09.16.
 */

@Component
@ConfigurationProperties(prefix = "eiswind.masterds")
open class DataSourceConfigBean {
    var driverClassName = ""
    var username = ""
    var password = ""
    var url = ""
    var prefix = ""
    var proxy = false

}