/*
 * HinweisServiceImpl.java 
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

import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.Hinweis;
import de.hsbo.ibix.repository.AufgabeRepository;
import de.hsbo.ibix.repository.HinweisRepository;
import de.hsbo.ibix.security.IbixScope;
import de.hsbo.ibix.service.HinweisService;
import de.hsbo.ibix.service.ProtokollService;

/**
 * The Class HinweisServiceImpl.
 */
@Service
public class HinweisServiceImpl implements HinweisService {
	final static Logger log = LoggerFactory.getLogger(HinweisServiceImpl.class);

	@Autowired
	HinweisRepository hinweisRepo;

	@Autowired
	AufgabeRepository aufgabeRepo;

	@Autowired
	ProtokollService protokollService;

	@Autowired
	IbixScope ibixscope;

	@Override
	public List<Hinweis> findAll() {
		return hinweisRepo.findAll();
	}

	@Override
	public Hinweis getReferenceById(int id) {
		Hinweis hinweis = hinweisRepo.getReferenceById(id);
		protokollService.protokolliereHinweis(null, hinweis);
		return hinweis;
	}

	@Override
	public Hinweis findHinweiseFuerAufgabe(Integer aufgabeId) {
		Aufgabe aufgabe = aufgabeRepo.getReferenceById(aufgabeId);

		// Achtung, dieses Hinweis-Objekt wird nicht gespeichert, es dient nur als
		// Einstiegspunkte für die Anzeige der Hilfsoptionen
		Hinweis einstiegshinweis = hinweisRepo.getReferenceById(0);
		protokollService.protokolliereHinweis(null, einstiegshinweis);
		einstiegshinweis.getWeitereHinweise().addAll(hinweisRepo.findByAufgabenTemplate(aufgabe.getAufgabenTemplate()));
		return einstiegshinweis;
	}
}
