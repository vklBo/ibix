/*
 * IbixUserDetailsService.java 
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.User;
import de.hsbo.ibix.repository.UserRepository;
import de.hsbo.ibix.service.LdapUserService;

/**
 * The Class IbixUserDetailsService.
 */
@Service
public class IbixUserDetailsService implements UserDetailsService {
	static Logger log = LoggerFactory.getLogger(IbixUserDetailsService.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LdapUserService ldapUserService;

	@Override
	public IbixUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		username = username.toLowerCase();

		Optional<User> optUser = userRepository.findUserByUsernameIgnoreCase(username);
		User user;

		if (optUser.isEmpty()) {
			user = new User();
			user.setUsername(username);
			user.setHashcode(UUID.randomUUID().toString());
		} else {
			user = optUser.get();
			user.setUsername(user.getUsername().toLowerCase());
			if (user.getHashcode() == null || user.getHashcode().isBlank()) {
				user.setHashcode(UUID.randomUUID().toString());
			}
		}

		user.setLastlogin(LocalDateTime.now());

		if (user.getMatrikelnummer() == null || user.getMatrikelnummer().isBlank()) {
			ldapUserService.vervollstaendigeUserDaten(user);
			log.debug("Userdaten vervollständigt: {}", user.getUsername());
		}

		userRepository.save(user);

		IbixUserDetails userdetails = new IbixUserDetails(username, "");
		userdetails.setHashcode(user.getHashcode());
		return userdetails;
	}
}