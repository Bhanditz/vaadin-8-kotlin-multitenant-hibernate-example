package de.eiswind.xino.spring.security.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy
import org.vaadin.spring.http.HttpService
import org.vaadin.spring.security.annotation.EnableVaadinSharedSecurity
import org.vaadin.spring.security.config.VaadinSharedSecurityConfiguration
import org.vaadin.spring.security.config.VaadinSharedSecurityConfiguration.VAADIN_LOGOUT_HANDLER_BEAN
import org.vaadin.spring.security.shared.*
import org.vaadin.spring.security.web.VaadinRedirectStrategy

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
@EnableVaadinSharedSecurity
open class SecurityConfiguration : WebSecurityConfigurerAdapter() {


    // TODO Spring-Boot-Actuator


    @Autowired
    private lateinit var userDetailsService: UserDetailsService


    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     *
     * Configure the redirectSaveHandler bean as a VaadinAuthenticationSuccessHandler
     */

    @Bean(name = arrayOf("authenticationManager"))
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean(name = arrayOf(VAADIN_LOGOUT_HANDLER_BEAN))
    open fun vaadinLogoutHandler(vaadinRedirectStrategy: VaadinRedirectStrategy): VaadinLogoutHandler {
        val logoutHandler = VaadinRedirectLogoutHandler(vaadinRedirectStrategy)
        logoutHandler.logoutUrl = "/app/logout"
        return logoutHandler
    }

    // TODO Disable SpringSecurityFilterChain DefaultFilters (/css, /jsm /images)
    @Throws(Exception::class)
    override fun configure(web: WebSecurity?) {
        web!!.ignoring().antMatchers("/VAADIN/**")
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.userDetailsService<UserDetailsService>(userDetailsService).passwordEncoder(BCryptPasswordEncoder())
    }


    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable() // Use Vaadin's built-in CSRF protection instead
        http.authorizeRequests()
                .antMatchers("/app/login/**").anonymous()
                .antMatchers("/app/APP/connector/**").anonymous()
                .antMatchers("/app/UIDL/**").permitAll()
                .antMatchers("/app/HEARTBEAT/**").permitAll()
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
        http.httpBasic().disable()
        http.formLogin().disable()
        http.headers().frameOptions().sameOrigin()
        // Remember to add the VaadinSessionClosingLogoutHandler
        http.logout().addLogoutHandler(VaadinSessionClosingLogoutHandler()).logoutUrl("/app/logout").logoutSuccessUrl("/app/login?logout").permitAll()
        http.exceptionHandling().authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/app/login/#/"))
        // Instruct Spring Security to use the same RememberMeServices as Vaadin4Spring. Also remember the key.
        http.rememberMe().rememberMeServices(rememberMeServices()).key("myAppKey")
        // Instruct Spring Security to use the same authentication strategy as Vaadin4Spring
        http.sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy())
    }

    @Bean
    open fun rememberMeServices(): RememberMeServices {
        return TokenBasedRememberMeServices("myAppKey", userDetailsService())
    }

    /**
     * The [SessionAuthenticationStrategy] must be available as a Spring bean for Vaadin4Spring.
     */
    @Bean
    open fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return SessionFixationProtectionStrategy()
    }

    @Bean(name = arrayOf(VaadinSharedSecurityConfiguration.VAADIN_AUTHENTICATION_SUCCESS_HANDLER_BEAN))
    open fun vaadinAuthenticationSuccessHandler(httpService: HttpService,
                                                vaadinRedirectStrategy: VaadinRedirectStrategy): VaadinAuthenticationSuccessHandler {
        return VaadinUrlAuthenticationSuccessHandler(httpService, vaadinRedirectStrategy, "/app/")
    }

    //    public static void main(String[] args) {
    //        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    //        System.out.println(enc.encode("admin"));
    //    }

    // admin: $2a$10$Uk.WWeOWxxPuKy4WpefIGeZ6uu.qEhja/d3Idjs7s5yqTGocktyKe

}