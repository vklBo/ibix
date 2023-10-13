/*
 * BearbeitungRepository.java 
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hsbo.ibix.model.Aufgabenblatt;
import de.hsbo.ibix.model.Bearbeitung;

/**
 * The Interface BearbeitungRepository.
 */
public interface BearbeitungRepository extends JpaRepository<Bearbeitung, Integer> {

	List<Bearbeitung> findByBenutzerHash(String userHash);

	List<Bearbeitung> findByBenutzerHashAndAufgabenblatt(String userHash, Aufgabenblatt blatt);

	List<Bearbeitung> findByBenutzerHashAndAufgabenblattOrderByAusgabeDesc(String userHash, Aufgabenblatt blatt);

	Optional<Bearbeitung> findFirstByBenutzerHashAndSpaetesteAbgabeGreaterThan(String userHash, LocalDateTime time);

	Optional<Bearbeitung> findFirstByBenutzerHashAndAufgabenblattAndSpaetesteAbgabeGreaterThan(String userHash,
			Aufgabenblatt aufgabenblatt, LocalDateTime time);

	@Query("select max(b.bonuspunkte) From Bearbeitung b where b.benutzerHash = :benutzerHash and b.aufgabenblatt = :aufgabenblatt and b.ausgabe >= :datum")
	Integer findMaxBewertungByBenutzerHashAndAufgabenblatt(@Param("benutzerHash") String benutzerHash,
			@Param("aufgabenblatt") Aufgabenblatt aufgabenblatt, @Param("datum") LocalDateTime datum);
}
