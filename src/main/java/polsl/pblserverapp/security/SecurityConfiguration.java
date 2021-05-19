package polsl.pblserverapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import polsl.pblserverapp.services.UserDetailsServiceImplementation;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
    private final LoggingAccessDeniedHandler accessDeniedHandler;

    public SecurityConfiguration(LoggingAccessDeniedHandler loggingAccessDeniedHandler)
    {
        this.accessDeniedHandler = loggingAccessDeniedHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.headers().frameOptions().disable();
        http.csrf().disable();
        http
                .authorizeRequests()
                .antMatchers(
                        "/",
                        "/js/**",
                        "/css/**",
                        "/images/**",
                        "/webjars/**").permitAll()
                .antMatchers("/logged/**").hasAnyRole("USER","ADMIN")
                .antMatchers("/register/**").permitAll()
                .antMatchers("/signin").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/signin")
                .defaultSuccessUrl("/logged")
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/signin")
                .permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler);
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService()
    {
        return new UserDetailsServiceImplementation();
    }
}
