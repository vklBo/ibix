/*
 * AufgabeServiceImpl.java 
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

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.repository.AufgabeRepository;
import de.hsbo.ibix.service.AufgabeService;
import de.hsbo.ibix.service.FileService;
import de.hsbo.ibix.service.GeneratorService;
import de.hsbo.ibix.service.ProtokollService;

/**
 * The Class AufgabeServiceImpl.
 */
@Service
public class AufgabeServiceImpl implements AufgabeService {
	final static Logger log = LoggerFactory.getLogger(AufgabeServiceImpl.class);

	@Autowired
	AufgabeRepository aufgabeRepo;

	@Autowired
	FileService fileService;

	@Autowired
	GeneratorService generatorService;

	@Autowired
	ProtokollService protokollService;

	@Override
	@PostAuthorize("hasPermission(returnObject, 'ROLE_USER')")
	public Aufgabe findById(Integer id) {
		return aufgabeRepo.getReferenceById(id);
	}

	@Override
	@PreAuthorize("hasPermission(#aufgabe, 'ROLE_USER')")
	public void bewerteLoesung(Aufgabe aufgabe) throws IOException {
		String vbaFilename = fileService.getVbaFilename(aufgabe.getBearbeitung().getId());

		String vbaCode = fileService.loadVbaCode(vbaFilename);
		String pythonCode = generatorService.loadPythonCode(vbaFilename);

		Boolean pruefung = aufgabe.getBearbeitung().getAufgabenblatt().getKlausur();
		String responseString = generatorService.bewerteLoesung(aufgabe.getGeneratorAufgabenID(), vbaFilename,
				pruefung);

		String jsonString = responseString.substring(responseString.lastIndexOf('\n') + 1);

		aufgabe.setProzent(this.extrahiereProzent(jsonString));

		if (pruefung) {
			aufgabe.setBonuspunkte(this.berechneBewertung(jsonString));
		} else {
			aufgabe.setBonuspunkte(null);
		}
		aufgabe.setBewertungJson(jsonString);

		log.info("Bewertung von Aufgabe {} (Generatorid: {}) -> {}", aufgabe.getId(), aufgabe.getGeneratorAufgabenID(),
				jsonString);
		protokollService.protokolliereBewertung(aufgabe, aufgabe.getProzent(), vbaCode, pythonCode, responseString);
		aufgabeRepo.save(aufgabe);
	}

	private Integer extrahiereProzent(String jsonString) {
		int prozente = 0;
		int anzahl = 0;

		if (jsonString == null || jsonString.isBlank()) {
			return 0;
		}
		JSONObject json;
		try {
			json = new JSONObject(jsonString);
			Iterator<?> iter = json.keys();
			while (iter.hasNext()) {
				anzahl += 1;
				String key = (String) iter.next();

				Object o = json.get(key);
				long wert = Math.round(Double.valueOf(o.toString()));
				if (wert >= 0) {
					prozente += wert;
				}
			}
			if (anzahl != 0) {
				prozente = prozente / anzahl;
				return prozente;
			}
			return 0;
		} catch (JSONException e) {
			log.error(e.toString());
		}
		return 0;
	}

	@Override
	@PreAuthorize("hasPermission(#aufgabe, 'ROLE_USER')")
	public void schliesseAufgabe(Aufgabe aufgabe, Integer einstufung) {
		aufgabe.setAbgeschlossen(true);
		aufgabe.setEinstufung(einstufung);
		aufgabeRepo.save(aufgabe);
	}

	private Integer berechneBewertung(String jsonString) {
		int prozente = 0;
		int anzahl = 0;

		if (jsonString == null || jsonString.isBlank()) {
			return 0;
		}
		JSONObject json;
		try {
			json = new JSONObject(jsonString);
			Iterator<?> iter = json.keys();
			while (iter.hasNext()) {
				anzahl += 1;
				String key = (String) iter.next();

				Object o = json.get(key);
				prozente += (Math.round(Double.valueOf(o.toString())));
			}
			if (anzahl != 0) {
				prozente = prozente / anzahl;
				if (prozente == 100) {
					return 4;
				} else if (prozente >= 75) {
					return 3;
				} else if (prozente >= 50) {
					return 2;
				} else if (prozente >= 25) {
					return 1;
				}
				return 0;
			}

		} catch (JSONException e) {
			log.error(e.toString());
		}

		return 0;
	}
}