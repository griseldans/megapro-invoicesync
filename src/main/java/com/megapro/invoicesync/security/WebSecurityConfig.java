package com.megapro.invoicesync.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http 
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/assets/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/taxes")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/invoice/add-approver")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/tax")).hasAnyAuthority("Finance Staff")
                .requestMatchers(new AntPathRequestMatcher("/create-account")).hasAnyAuthority("Admin")
                .requestMatchers(new AntPathRequestMatcher("/create-invoice")).hasAnyAuthority("Non-Finance Staff")
                .requestMatchers(new AntPathRequestMatcher("/invoice/{id}")).hasAnyAuthority(
                    "Non-Finance Staff", "Non-Finance Manager",
                    "Finance Staff", "Finance Manager",
                    "Finance Director")
                .requestMatchers(new AntPathRequestMatcher("/approval-flows")).hasAnyAuthority("Admin")
                .requestMatchers(new AntPathRequestMatcher("/add-approval-flow")).hasAnyAuthority("Admin")
                .requestMatchers(new AntPathRequestMatcher("/invoices")).hasAnyAuthority("Finance Director", "Finance Staff", "Finance Manager", "Non-Finance Manager")
                .requestMatchers(new AntPathRequestMatcher("/approve-invoice")).hasAnyAuthority("Finance Director", "Finance Staff", "Finance Manager", "Non-Finance Manager")
                .anyRequest().authenticated())
            
            .formLogin((form) -> form
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
            )
            .logout((logout) -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login"))
                .exceptionHandling((exceptionHandling) -> 
                exceptionHandling.accessDeniedPage("/access-denied"))
            ;

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder());
    }
}
