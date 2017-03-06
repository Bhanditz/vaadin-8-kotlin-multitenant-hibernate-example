package de.eiswind.xino.integration

import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.openqa.selenium.WebDriver

/**
 * Created by thomas on 09.02.17.
 */
class ScreenShotOnFailure(private val driver: WebDriver) : MethodRule {

    override fun apply(statement: Statement, frameworkMethod: FrameworkMethod, o: Any): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    statement.evaluate()
                } catch (t: Throwable) {
                    // exception will be thrown only when a test fails.
                    captureScreenShot(frameworkMethod.name)
                    // rethrow to allow the failure to be reported by JUnit
                    throw t
                }

            }


            fun captureScreenShot(fileName: String) {
                AbstractIntegrationTest.captureScreenShot(fileName)
            }
        }
    }
}