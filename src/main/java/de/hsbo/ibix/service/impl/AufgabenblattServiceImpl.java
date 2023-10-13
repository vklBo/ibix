/*
 * AufgabenblattServiceImpl.java 
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

import de.hsbo.ibix.model.Aufgabenblatt;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.repository.AufgabenblattRepository;
import de.hsbo.ibix.repository.AufgabentypRepository;
import de.hsbo.ibix.repository.BearbeitungRepository;
import de.hsbo.ibix.service.AufgabenblattService;

/**
 * The Class AufgabenblattServiceImpl.
 */
@Service
public class AufgabenblattServiceImpl implements AufgabenblattService {
	final static Logger log = LoggerFactory.getLogger(AufgabenblattServiceImpl.class);

	@Autowired
	AufgabenblattRepository aufgabenblattRepo;

	@Autowired
	AufgabentypRepository aufgabentypRepo;

	@Autowired
	BearbeitungRepository bearbeitungRepo;

	@Override
	public List<Aufgabenblatt> findAktuellGueltige() {
		return aufgabenblattRepo.findByInaktivFalse();
	}

	@Override
	public List<Aufgabenblatt> findAll() {
		return aufgabenblattRepo.findAll();
	}

	@Override
	public Aufgabenblatt findById(Integer id) {
		return aufgabenblattRepo.getReferenceById(id);
	}

	@Override
	public Aufgabenblatt update(Aufgabenblatt aufgabenblatt) {
		Aufgabenblatt original = aufgabenblattRepo.getReferenceById(aufgabenblatt.getId());
		original.setNr(aufgabenblatt.getNr());
		original.setName(aufgabenblatt.getName());
		original.setZusatztext(aufgabenblatt.getZusatztext());
		original.setStart(aufgabenblatt.getStart());
		original.setEnde(aufgabenblatt.getEnde());
		original.setDauer(aufgabenblatt.getDauer());
		original.setKlausur(aufgabenblatt.getKlausur());
		original.setInaktiv(aufgabenblatt.getInaktiv());
		original.setTagString(aufgabenblatt.getTagString());

		aufgabenblattRepo.save(original);
		return original;
	}

	@Override
	public Aufgabenblatt speicher(Aufgabenblatt aufgabenblatt) {
		aufgabenblattRepo.save(aufgabenblatt);
		return aufgabenblatt;
	}

	@Override
	public void entferneAufgabentyp(Integer aufgabenblattId, Integer aufgabentypId) {
		Aufgabenblatt aufgabenblatt = aufgabenblattRepo.getReferenceById(aufgabenblattId);

		// Das Aufgabentyp-Objekt aus der Datenbank zu suchen und per remove() zu
		// entfernen hat nicht funktioniert, die Typen sind unterschiedliche
		aufgabenblatt.getAufgabentypen().removeIf(typ -> (typ.getId() == aufgabentypId));

		aufgabenblattRepo.save(aufgabenblatt);
	}

	@Override
	public void ordneAufgabentypZu(Integer aufgabenblattId, int aufgabentypId) {
		Aufgabenblatt aufgabenblatt = aufgabenblattRepo.getReferenceById(aufgabenblattId);
		Aufgabentyp aufgabentyp = aufgabentypRepo.getReferenceById(aufgabentypId);
		aufgabenblatt.getAufgabentypen().add(aufgabentyp);
		aufgabenblattRepo.save(aufgabenblatt);
	}

	@Override
	public List<Aufgabenblatt> findByNameOrTag(String filtertext) {
		return aufgabenblattRepo.findByNameContainingOrTagsContainingOrderByNr(filtertext, filtertext);
	}
}