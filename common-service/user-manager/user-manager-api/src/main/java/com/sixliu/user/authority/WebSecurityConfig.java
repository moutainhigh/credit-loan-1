package com.sixliu.user.authority;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author:MG01867
 * @date:2018年11月12日
 * @email:359852326@qq.com
 * @version:
 * @describe TODO
 */
@Configuration
@EnableOAuth2Sso
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatchers().antMatchers("/login", "/logout", "/oauth/authorize", "/oauth/confirm_access").and()
				.authorizeRequests()
				.anyRequest().authenticated().and().formLogin().loginPage("/html/login.html").failureUrl("/html/login.html?error")
				.permitAll().and().logout().logoutUrl("/logout").invalidateHttpSession(true).clearAuthentication(true);
		http.exceptionHandling().authenticationEntryPoint((req, res, auth) -> {
			res.setHeader("WWW-Authenticate", "Bearer realm=\"webrealm\"");
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, auth.getMessage());
		});
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/html/**","/js/**", "/css/**", "/images/**", "/druid/**", "/favor.ioc");
	}
}
