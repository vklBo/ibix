/*
 * UserRepository.java 
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

package de.hsbo.ibix.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hsbo.ibix.model.User;

/**
 * The Interface UserRepository.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findUserByUsernameIgnoreCase(String username);
	Optional<User> findUserByHashcode(String hashcode);
	Collection<User> findByMatrikelnummerIsNull();
	Optional<User> findUserByMatrikelnummerIgnoreCase(String matrikelnummer);

	@Query("SELECT u FROM User u  where u.username like %:filterstring% or u.hashcode like %:filterstring% or u.vorname like %:filterstring% or u.nachname like %:filterstring% or u.matrikelnummer like %:filterstring% order by u.lastlogin desc")
	List<User> findByFilterstring(@Param("filterstring") String filterstring);
}
