package de.eiswind.xino.integration


import com.machinepublishers.jbrowserdriver.JBrowserDriver
import com.machinepublishers.jbrowserdriver.Settings
import com.machinepublishers.jbrowserdriver.Timezone
import com.machinepublishers.jbrowserdriver.UserAgent
import de.eiswind.xino.customtypes.Permission
import de.eiswind.xino.datalayer.entities.User
import de.eiswind.xino.datalayer.repositories.UserRepository
import de.eiswind.xino.integration.pages.*
import de.eiswind.xino.main.VaadinApplication
import org.apache.commons.io.FileUtils
import org.junit.*
import org.junit.runner.RunWith
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.springframework.test.context.transaction.TransactionalTestExecutionListener
import org.springframework.test.context.web.ServletTestExecutionListener
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Level


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(VaadinApplication::class, ContextExtensions::class), webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test", "greenmail")
@WithUserDetails(value = "demo@eiswind.de")
@TestExecutionListeners(listeners = arrayOf(ServletTestExecutionListener::class, DirtiesContextBeforeModesTestExecutionListener::class,
        DependencyInjectionTestExecutionListener::class, DirtiesContextTestExecutionListener::class,
        TransactionalTestExecutionListener::class, SqlScriptsTestExecutionListener::class,
        WithSecurityContextTestExecutionListener::class))
abstract class AbstractIntegrationTest {

    @Rule @JvmField
    val failure = ScreenShotOnFailure(driver);

    @Value("\${local.server.port}")
    protected var port: Int = 0


    @Autowired
    protected lateinit var userRepository: UserRepository

    protected lateinit open var loginPage: LoginPage
    protected lateinit open var dashBoardPage: DashBoardPage
    protected lateinit open var editUsersPage: EditUsersPage
    protected lateinit open var editUserDetailsPage: EditUserDetailsPage
    protected lateinit open var editUserProfilePage: EditUserProfilePage


    @Before
    @Throws(ParseException::class, IOException::class)
    open fun init() {
        createAdminUser()
        loginPage = LoginPage(driver, port)
        dashBoardPage = DashBoardPage(driver)
        editUsersPage = EditUsersPage(driver)
        editUserDetailsPage = EditUserDetailsPage(driver)
        editUserProfilePage = EditUserProfilePage(driver)

    }


    private fun createAdminUser() {
        val users = userRepository.findAll();
        for (user in users) {
            if (!(user.email == "demo@eiswind.de")) {
                userRepository.delete(user)
            }
        }
        val user = User()
        user.email = "eiswind@gmail.com"
        user.password = BCryptPasswordEncoder().encode("admin")
        user.active = true
        user.permissions.add(Permission.ADMIN)
        user.permissions.add(Permission.USER)
        userRepository.save(user)
    }

    @After
    fun delete() {

        val users = userRepository.findAll();
        val userList = ArrayList<User>(users)
        userList.removeIf { user -> user.email == "demo@eiswind.de" }
        userRepository.deleteInBatch(userList)
    }


    companion object {
        fun captureScreenShot(name: String) {
            var fileName = name
            val scrFile = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
            fileName += "_" + UUID.randomUUID().toString()
            val builddir = "target/screenshots"
            File(builddir).mkdirs()

            val targetFile = File(builddir + "/$fileName.png")
            FileUtils.copyFile(scrFile, targetFile)
        }

        init {
            if (System.getProperty("webdriver.firefox.bin") == null) {
                System.setProperty("webdriver.firefox.bin", "/opt/firefox/firefox")
            }

        }


        lateinit var driver: JBrowserDriver

        @BeforeClass @JvmStatic
        fun launchBrowser() {
//            val profile = FirefoxProfile()
//            profile.setPreference("intl.accept_languages", "EN-us")
//            driver = FirefoxDriver(profile)
            driver = JBrowserDriver(Settings.builder()
                    .timezone(Timezone.EUROPE_BERLIN)
                    .userAgent(UserAgent.CHROME)
                    .loggerLevel(Level.SEVERE)
                    .ajaxWait(250)
                    .build())
            driver.manage().window().maximize()
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
        }

        @AfterClass @JvmStatic
        fun closeBrowser() {
            driver.quit()

        }
    }

}