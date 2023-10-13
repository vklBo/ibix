/*
 * SpaltendefinitionServiceImpl.java 
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

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.Spaltendefinition;
import de.hsbo.ibix.repository.SpaltendefinitionRepository;
import de.hsbo.ibix.service.SpaltendefinitionService;
import de.hsbo.ibix.utils.TagHelper;

/**
 * The Class SpaltendefinitionServiceImpl.
 */
@Service
public class SpaltendefinitionServiceImpl implements SpaltendefinitionService {
	final static Logger log = LoggerFactory.getLogger(SpaltendefinitionServiceImpl.class);

	@Autowired
	SpaltendefinitionRepository spaltendefinitionRepo;

	@Override
	public List<Spaltendefinition> findAll() {
		return spaltendefinitionRepo.findAll();
	}

	@Override
	public List<Spaltendefinition> findAllByOrderById() {
		return spaltendefinitionRepo.findAllByOrderById();
	}

	@Override
	public List<Spaltendefinition> findByNameOrTag(String filtertext) {
		Set<String> tags = TagHelper.stringToTags(filtertext);
		return spaltendefinitionRepo.findByNameOrTag("%" + filtertext + "%", tags);
	}

	@Override
	public List<Spaltendefinition> findByTags(Set<String> tags) {
		return spaltendefinitionRepo.findByTags(tags);
	}

	@Override
	public List<Spaltendefinition> findAllByOrderByIdDesc() {
		return spaltendefinitionRepo.findAllByOrderByIdDesc();
	}

	@Override
	public List<Spaltendefinition> findByNameContaining(String filtertext) {
		return spaltendefinitionRepo.findByNameContainingOrderById(filtertext);

	}

	@Override
	public Spaltendefinition getReferenceById(int id) {
		return spaltendefinitionRepo.getReferenceById(id);
	}

	@Override
	public void speicherSpaltendefinition(Spaltendefinition template) {
		spaltendefinitionRepo.save(template);
	}

	@Override
	public Spaltendefinition updateSpaltendefinition(Spaltendefinition spaltendefinition) {
		Spaltendefinition original = spaltendefinitionRepo.getReferenceById(spaltendefinition.getId());

		original.setAuswahl(spaltendefinition.getAuswahl());
		original.setFormat(spaltendefinition.getFormat());
		original.setInhalt(spaltendefinition.getInhalt());
		original.setIntervalle(spaltendefinition.getIntervalle());
		original.setName(spaltendefinition.getName());
		original.setTagString(spaltendefinition.getTagString());
		original.setTitel(spaltendefinition.getTitel());
		original.setTyp(spaltendefinition.getTyp());
		original.setConfig(spaltendefinition.getFormat().toString());

		spaltendefinitionRepo.save(original);

		return original;

	}

	@Override
	public Spaltendefinition copySpaltendefinition(int spaltendefinitionId) {
		Spaltendefinition original = this.getReferenceById(spaltendefinitionId);
		Spaltendefinition neu = new Spaltendefinition(original);
		neu.setTitel(neu.getTitel() + " Kopie");
		neu.setTagString(original.getTagString());

		spaltendefinitionRepo.save(neu);

		return neu;
	}

}
