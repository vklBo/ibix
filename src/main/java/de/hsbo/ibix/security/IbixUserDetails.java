/*
 * IbixUserDetails.java 
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

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class IbixUserDetails.
 */
@Getter
@Setter
public class IbixUserDetails implements UserDetails {
	private static final long serialVersionUID = -8780472358299608417L;

	private String username;
	private String password;

	private String hashcode;
	private String matrikelnummer;
	private String vorname;
	private String nachname;
	private String email;

	private Collection<GrantedAuthority> authorities;

	public IbixUserDetails(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public boolean isAdmin() {
		if (username.equals("anonymousUser")) { // Testbetrieb
			return true;
		}

		if (this.getAuthorities() != null) {
			if (this.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				return true;
			}
		}
		return false;
	}

	public boolean isConfig() {
		if (username.equals("anonymousUser")) { // Testbetrieb
			return true;
		}

		if (this.getAuthorities() != null) {
			if (this.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
					|| this.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONFIG"))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
