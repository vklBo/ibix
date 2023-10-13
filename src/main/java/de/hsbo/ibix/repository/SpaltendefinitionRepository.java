/*
 * SpaltendefinitionRepository.java 
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
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hsbo.ibix.model.Spaltendefinition;

/**
 * The Interface SpaltendefinitionRepository.
 */
public interface SpaltendefinitionRepository extends JpaRepository<Spaltendefinition, Integer> {
	public List<Spaltendefinition> findAllByOrderById();
	public List<Spaltendefinition> findAllByOrderByIdDesc();
	public List<Spaltendefinition> findByNameContainingOrderById(String name);
	public List<Spaltendefinition> findByNameContainingOrTagsContainingOrderById(String key1, String key2);
	
	@Query("SELECT distinct s FROM Spaltendefinition s left join s.tags t where  s.name like :filterstring or t in :tags order by s.id")
	public List<Spaltendefinition> findByNameOrTag(@Param("filterstring") String filterstring, @Param("tags") Set<String> tags);

	@Override
	public Spaltendefinition getReferenceById(Integer id);

	@Query("SELECT distinct s FROM Spaltendefinition s left join s.tags t where t = '#allgemein' or t in :tags order by s.id desc")
	public List<Spaltendefinition> findByTags(@Param("tags") Set<String> tags);
}
