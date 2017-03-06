package de.eiswind.xino.integration.pages

import de.eiswind.xino.integration.AbstractIntegrationTest
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory

/**
 * Created by thomas on 23.09.16.
 */
open class AbstractPage(open val driver: WebDriver) {

    fun clickId(id: String) {
        driver.findElement(By.id(id)).click()
    }


    fun clickXpath(xpath: String) {
        driver.findElement(By.xpath(xpath)).click()
    }

    fun waitForId(id: String) {
        var repeat = true
        var count = 0
        while (repeat && count < 5) {
            try {
                count++
                repeat = false
                val wait = WebDriverWait(driver, 3)
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)))
            } catch (ex: WebDriverException) {
                if (ex.cause is NullPointerException) {
                    LOG.warn("Caught Exception ", ex)
                    AbstractIntegrationTest.captureScreenShot("Exception")
                    Thread.sleep(100)
                    repeat = true
                } else {
                    throw ex
                }
            }
        }
    }

    fun waitForXpath(xpath: String) {
//        Thread.sleep(100)
        val wait = WebDriverWait(driver, 3)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)))
    }

    fun waitForXpathAndClick(xpath: String) {
        waitForXpath(xpath)
        clickXpath(xpath)
    }

    fun type(id: String, text: String) {
        driver.findElement(By.id(id)).sendKeys(text)
        Thread.sleep(20)
    }

    companion object {
        val LOG = LoggerFactory.getLogger(AbstractPage::class.java)
    }
}