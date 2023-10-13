/*
 * IbixRestController.java 
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

package de.hsbo.ibix.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.hsbo.ibix.service.BearbeitungService;
import de.hsbo.ibix.service.ProtokollService;

/**
 * The Class IbixRestController.
 */
@RestController
public class IbixRestController {

	final static Logger log = LoggerFactory.getLogger(IbixRestController.class);

	@Autowired
	private BearbeitungService bearbeitungService;
	
	@Autowired
	private ProtokollService protokollService;
	
	@GetMapping("/rest/bonuspunkte/{matrikelnummer}")
	public HashMap<String, String> bonusPunkte(@PathVariable String matrikelnummer) {
		HashMap<String, String> ergebnis = bearbeitungService.findeBesteBewertungenFuerMatrikelnummer(matrikelnummer);
		protokollService.protokolliereBonuspunktabruf(matrikelnummer);
		
		return ergebnis;
	}

}
