package md.utm.universty.configure;

import lombok.AllArgsConstructor;
import md.utm.universty.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
    private final PasswordEncoder bCryptPasswordEncoder;
    private final UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.
                authorizeRequests()
                .antMatchers("/accounts/login").anonymous()
                .antMatchers("/accounts/register/confirm").anonymous()
                .antMatchers("/accounts/password/reset/**").anonymous()
                .anyRequest()
                .authenticated()
                .and().csrf().disable()
                .formLogin()
                .loginPage("/accounts/login")
                .failureUrl("/accounts/login?error=true")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .passwordParameter("password")
                .and().rememberMe()
                .key("rem-me-key")
                .rememberMeParameter("remember")
                .rememberMeCookieName("rememberLogin")
                .tokenValiditySeconds(86400)
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/accounts/logout"))
                .logoutSuccessUrl("/accounts/login?logout=true").and().exceptionHandling();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        final CustomAuthenticationProvider authenticationProvider = new CustomAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**", "/static/**", "/assets/**", "/demo/**");
    }
}
