/*
 * AufgabeRepository.java 
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

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hsbo.ibix.model.Aufgabe;

/**
 * The Interface AufgabeRepository.
 */
public interface AufgabeRepository extends JpaRepository<Aufgabe, Integer> {

	@Query("SELECT a FROM Aufgabe a where a.aufgabenTemplate.aufgabentyp.id = :aufgabentypId and a.bearbeitung.id = :bearbeitungId and not a.abgeschlossen")
	Optional<Aufgabe> findByBearbeitungIdAndTemplateId(@Param("bearbeitungId") Integer bearbeitungId,
			@Param("aufgabentypId") Integer aufgabentypId);

	@Query("SELECT a FROM Aufgabe a where a.bearbeitung.benutzerHash = :userHash and a.aufgabenTemplate.aufgabentyp.id = :aufgabentypId")
	List<Aufgabe> findAufgabenFuerBenutzerUndAufgabentyp(@Param("userHash") String userHash,
			@Param("aufgabentypId") Integer aufgabentypId);
}
