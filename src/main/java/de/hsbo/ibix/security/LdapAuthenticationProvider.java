/*
 * LdapAuthenticationProvider.java 
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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.hsbo.ibix.service.ProtokollService;
import stream.util.URLUtilities;

/**
 * The Class LdapAuthenticationProvider.
 */
@Component
public class LdapAuthenticationProvider implements AuthenticationProvider {
	final static Logger log = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

	@Autowired
	private IbixUserDetailsService userDetailsService;
	
	@Autowired
	private ProtokollService protokollService;

	@Autowired
	IbixScope ibixscope;

	@Override
	public Authentication authenticate(Authentication authentication) {
		String username = authentication.getName().toLowerCase();
		String password = authentication.getCredentials().toString();
		Collection<GrantedAuthority> authorities = new ArrayList<>();

		try {
			final URL url = new URL("http://urlToAuthService");
			final Charset cs = Charset.forName("US-ASCII");

			URLConnection con = url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);

			String data = "username=" + URLEncoder.encode(username, cs) + "&password="
					+ URLEncoder.encode(password, cs);

			PrintStream p = new PrintStream(con.getOutputStream());
			p.print(data);
			p.flush();
			p.close();

			String json = URLUtilities.readResponse(con.getInputStream());
			log.info("Authentication:\n{}", json);

			Gson gson = new GsonBuilder().create();
			MyAuthentication auth = gson.fromJson(json, MyAuthentication.class);

			IbixUserDetails userdetails = userDetailsService.loadUserByUsername(username);

			if (auth != null) {
				for (String role : auth.getRoles()) {
					if (role.startsWith("ibix_")) {
						String group = role.substring(5);
						log.info("Adding group '{}' for user '{}'", group, username);
						authorities.add(new SimpleGrantedAuthority("ROLE_" + group.toUpperCase()));
					}
				}
				if (authorities.isEmpty()) {
					log.info("Adding defaultgroup '{}' for user '{}'", "ROLE_USER", username);
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				}
			}

			userdetails.setAuthorities(authorities);
			ibixscope.setUserdetails(userdetails);
			protokollService.protokolliereLogin();

			Authentication authToken = new UsernamePasswordAuthenticationToken(username, password, authorities);
			return authToken;
		} catch (UnknownHostException e) {
			throw new AuthenticationServiceException("Authentifizierungsserver nicht erreichbar.");
		} catch (IOException e) {
			if (e.getMessage().contains("code: 403")) {
				throw new BadCredentialsException("Benutzername oder Password sind falsch");
			}
			throw new AuthenticationServiceException(e.getMessage(), e);

		} catch (Exception e) {
			log.error("Failed to authenticate: {}", e.getMessage());
			e.printStackTrace();
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

	@Override
	public boolean supports(Class<?> authenticationType) {
		return authenticationType.equals(UsernamePasswordAuthenticationToken.class);
	}

	/**
	 * The Class MyAuthentication.
	 */
	public static class MyAuthentication {
		String username;
		List<String> roles;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public List<String> getRoles() {
			return roles;
		}

		public void setRoles(List<String> roles) {
			this.roles = roles;
		}
	}
}
