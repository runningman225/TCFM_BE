package com.future.tcfm.config;

import com.future.tcfm.config.security.*;
import com.future.tcfm.model.JwtUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Collections;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationProvider authenticationProvider;

    @Autowired
    private JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(authenticationProvider));
    }


    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilter() {
        JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(new JwtSuccessHandler());
        filter.setAuthenticationFailureHandler(new JwtFailureHandler());
        return filter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    public static JwtUserDetails getCurrentUser(){
        return (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        //        http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())
        http.cors().and().csrf().disable()//disable cors
//        .and().csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.PUT,"/api/expense/**").hasAnyAuthority("SUPER_ADMIN","GROUP_ADMIN")
                .antMatchers(HttpMethod.PUT,"/api/payment/**").hasAnyAuthority("SUPER_ADMIN","GROUP_ADMIN")
                .antMatchers(HttpMethod.PUT,"/api/user/managementUser/**","/api/group","/api/group/**").hasAnyAuthority("SUPER_ADMIN")
                .antMatchers(HttpMethod.POST,"/api/user","/api/group").hasAnyAuthority("SUPER_ADMIN")
                .antMatchers(HttpMethod.DELETE,"/api/user/**","/api/group/**").hasAnyAuthority("SUPER_ADMIN")
                .antMatchers(HttpMethod.GET,"/api/scheduler/**").hasAnyAuthority("SUPER_ADMIN")
                .and()
                .exceptionHandling().authenticationEntryPoint(entryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers().cacheControl();
    }
}
