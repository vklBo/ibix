/*
 * SecurityConfig.java 
 * 
 * Diese Datei ist Teil des Projekts IBIX.
 * 
 * Copyright 2023, Hochschule Bochum, Prof. Dr. Volker Klingspor, Prof. Dr. Christian Bockermann
 *  
 * Dieses Programm ist freie Software: Sie können es unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation, entweder Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weitergeben und/oder modifizieren.
 *
 * Dieses Programm wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt.
 * Eine Kopie der GNU General Public License finden Sie in der Datei "LICENSE.md" oder unter
 * <https://www.gnu.org/licenses/>.
 * 
 * Das Projekt IBIX wurde durch die "Stiftung Innovation in der Hochschullehre" gefördert.
 */

package de.hsbo.ibix.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The Class SecurityConfig.
 *
 * @author vk SecurityConfig
 * 
 *         Bei Start der Anwendung im Profile "dev" wird die Authentisierung
 *         abgestellt und dem Benutzer Zugriff auf alle Resource gewährt.
 * 
 *         Bei allen anderen Profile wird die Authentisierung aktiviert.
 */

@Configuration
public class SecurityConfig {
	static Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private LdapAuthenticationProvider authenticationProvider;

	@Autowired
	private ConfigurableEnvironment env;

	@Bean
	UserDetailsService userDetailsService() {
		return new IbixUserDetailsService();
	}

	@Bean
	@Profile("!dev")
	SecurityFilterChain web(HttpSecurity http) throws Exception {
		String[] profiles = env.getActiveProfiles();
		log.info("Profile {} aktiv - Mit Authorisierung", String.join(",", profiles));

		http.cors(c -> c.disable())
			.csrf(c -> c.disable())
			.httpBasic(hb -> {})
			.formLogin(formLogin -> formLogin.loginPage("/login").defaultSuccessUrl("/"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/datenschutz", "/impressum", "/css/**",
												"/images/**", "/login", "/error", "/js/**", "/api/**", "/bootstrap/**", "/favicon.ico")
										.permitAll())
			.authorizeHttpRequests((aR) -> aR.requestMatchers(HttpMethod.POST, "/login").permitAll())
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/bearbeitung*/**", "/aufgabe/**").hasAnyRole("USER", "CONFIG", "ADMIN"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/aufgabenblatt/**", "/aufgabentyp/**").hasAnyRole("CONFIG", "ADMIN"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/*template/**", "/spaltendefinition/**", "/config*/**").hasAnyRole("CONFIG", "ADMIN"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/hilfe/**").hasAnyRole("USER", "CONFIG", "ADMIN"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/user/**", "/protokoll/**").hasRole("ADMIN"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("/rest/**").hasAnyRole("REST_API", "ADMIN"))
			.authorizeHttpRequests((aR) -> aR.requestMatchers("", "/").authenticated())
			.authorizeHttpRequests((aR) -> aR.anyRequest().denyAll());
		return http.build();
	}

	@Bean
	@Profile("!dev")
	AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.authenticationProvider(authenticationProvider);
		return authenticationManagerBuilder.build();
	}

	@Bean
	@Profile("dev")
	SecurityFilterChain webDev(HttpSecurity http) throws Exception {
		log.warn("Developing Profile aktiv - Keine Authorisierung!");
		http.cors(c -> c.disable())
			.csrf(c -> c.disable())
			.authorizeHttpRequests((aR) -> aR.anyRequest().permitAll());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		// return NoOpPasswordEncoder.getInstance();
		return null;
	}
}
