/*
 * AufgabentypServiceImpl.java 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.AufgabenTemplate;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.repository.AufgabenTemplateRepository;
import de.hsbo.ibix.repository.AufgabentypRepository;
import de.hsbo.ibix.service.AufgabentypService;

/**
 * The Class AufgabentypServiceImpl.
 */
@Service
public class AufgabentypServiceImpl implements AufgabentypService {
	final static Logger log = LoggerFactory.getLogger(AufgabentypServiceImpl.class);

	@Autowired
	AufgabentypRepository aufgabentypRepo;

	@Autowired
	AufgabenTemplateRepository aufgabenTemplateRepo;

	@Override
	public List<Aufgabentyp> findAll() {
		return aufgabentypRepo.findAll();
	}

	@Override
	public Aufgabentyp findById(Integer id) {
		return aufgabentypRepo.getReferenceById(id);
	}

	@Override
	public Aufgabentyp update(Aufgabentyp aufgabentyp) {
		Aufgabentyp original = aufgabentypRepo.getReferenceById(aufgabentyp.getId());
		original.setName(aufgabentyp.getName());
		original.setLernziel(aufgabentyp.getLernziel());
		original.setProgrammiersprache(aufgabentyp.getProgrammiersprache());
		original.setTagString(aufgabentyp.getTagString());

		aufgabentypRepo.save(original);
		return original;
	}

	@Override
	public void entferneAufgabenTemplate(Integer aufgabentypId, Integer aufgabentemplateId) {
		// Aufgabentyp aufgabentyp = aufgabentypRepo.getReferenceById(aufgabentypId);
		AufgabenTemplate aufgabenTemplate = aufgabenTemplateRepo.getReferenceById(aufgabentemplateId);

		aufgabenTemplate.setAufgabentyp(null);
		aufgabenTemplateRepo.save(aufgabenTemplate);
	}

	@Override
	public void ordneAufgabenTemplateZu(Integer aufgabentypId, int aufgabentemplateId) {
		Aufgabentyp aufgabentyp = aufgabentypRepo.getReferenceById(aufgabentypId);
		AufgabenTemplate aufgabenTemplate = aufgabenTemplateRepo.getReferenceById(aufgabentemplateId);

		aufgabenTemplate.setAufgabentyp(aufgabentyp);
		aufgabenTemplateRepo.save(aufgabenTemplate);

	}

	@Override
	public Aufgabentyp speichern(Aufgabentyp aufgabentyp) {
		aufgabentypRepo.save(aufgabentyp);
		return aufgabentyp;
	}

	@Override
	public List<Aufgabentyp> findByNameOrTag(String filtertext) {
		return aufgabentypRepo.findByNameContainingOrTagsContainingOrderById(filtertext, filtertext);
	}
}