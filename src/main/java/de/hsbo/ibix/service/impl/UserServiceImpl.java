/*
 * UserServiceImpl.java 
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

package de.hsbo.ibix.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.User;
import de.hsbo.ibix.repository.UserRepository;
import de.hsbo.ibix.service.LdapUserService;
import de.hsbo.ibix.service.UserService;

/**
 * The Class UserServiceImpl.
 */
@Service
public class UserServiceImpl implements UserService {
	final static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	UserRepository userRepo;

	@Autowired
	LdapUserService ldapUserService;

	@Override
	public List<User> findAll() {
		return userRepo.findAll();
	}

	@Override
	public List<User> findByFilterstring(String filtertext) {
		if (filtertext == "") {
			return userRepo.findAll(Sort.by(Sort.Direction.DESC, "lastlogin"));
		}
			
		return userRepo.findByFilterstring(filtertext);
	}

	@Override
	public User getReferenceById(int id) {
		return userRepo.getReferenceById(id);
	}

	@Override
	public User findAndUpdateUserById(int id) {
		User user = userRepo.getReferenceById(id);

		if (user != null && (user.getMatrikelnummer() != null || user.getMatrikelnummer().isBlank())) {
			ldapUserService.vervollstaendigeUserDaten(user);
			userRepo.save(user);
		}

		return user;
	}

	@Override
	public User updateUser(User user) {
		User original = userRepo.getReferenceById(user.getId());
		original.setSchreibverlaengerung(user.getSchreibverlaengerung());
		userRepo.save(original);
		return original;
	}

	@Override
	public Optional<User> findByUsername(String username) {
		Optional<User> user = userRepo.findUserByUsernameIgnoreCase(username);

		return user;
	}

	@Override
	public Optional<User> findByMatrikelnummer(String matrikelnummer) {
		Optional<User> user = userRepo.findUserByMatrikelnummerIgnoreCase(matrikelnummer);

		return user;
	}

	@Override
	public void datenschutzbestaetigen(String username) {
		Optional<User> userOpt = this.findByUsername(username);

		if (userOpt.isEmpty()) {
			throw new RuntimeException("datenschutzbestaetigen für unbekannten User " + username);
		}

		User user = userOpt.get();

		userOpt.get().setDatenschutzZugestimmt(true);
		userRepo.save(user);
	}

	@Override
	public void vervollstaendigeUserdaten() {
		Collection<User> userListe = userRepo.findByMatrikelnummerIsNull();

		ldapUserService.vervollstaendigeUserDatenFuerListe(userListe);
	}

}
