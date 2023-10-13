/*
 * ProtokollRepository.java 
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
import de.hsbo.ibix.model.Bearbeitung;
import de.hsbo.ibix.model.Protokoll;

/**
 * The Interface ProtokollRepository.
 */
public interface ProtokollRepository extends JpaRepository<Protokoll, Integer> {
	public Optional<Protokoll> findById(Integer id);
	public Protokoll findFirstByBearbeitungAndAufgabeOrderByTimestampDesc(Bearbeitung bearbeitung, Aufgabe aufgabe);

	@Query("SELECT p FROM Protokoll p join User u on (u.hashcode = p.benutzerHash) where p.benutzerHash like :filterstring or p.vbaCode like :filterstring  or u.username like :filterstring or p.bearbeitung.id = :id or p.aufgabe.id = :id order by p.timestamp desc limit 100")
	List<Protokoll> findFirst100ByFilterstring(@Param("filterstring") String filterstring, @Param("id") Integer id);
}
