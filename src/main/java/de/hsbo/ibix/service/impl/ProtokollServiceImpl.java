/*
 * ProtokollServiceImpl.java 
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

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.Bearbeitung;
import de.hsbo.ibix.model.Hinweis;
import de.hsbo.ibix.model.Protokoll;
import de.hsbo.ibix.model.Protokoll.Aktion;
//import de.hsbo.ibix.model.Protokoll.Aktion;
import de.hsbo.ibix.repository.ProtokollRepository;
import de.hsbo.ibix.security.IbixScope;
import de.hsbo.ibix.service.ProtokollService;

/**
 * The Class ProtokollServiceImpl.
 */
@Service
public class ProtokollServiceImpl implements ProtokollService {

	@Autowired
	ProtokollRepository protokollRepository;

	@Autowired
	IbixScope ibixscope;

	@Override
	public void protokolliereNeueAufgabe(Aufgabe aufgabe) {
		Protokoll protokoll = this.neuesProtokollObjekt();
		protokoll.setAktion(Aktion.CREATE);
		protokoll.setAufgabe(aufgabe);
		protokoll.setBearbeitung(aufgabe.getBearbeitung());
		protokollRepository.save(protokoll);
	}

	@Override
	public void protokolliereBewertung(Aufgabe aufgabe, Integer bewertung, String vbaCode, String pythonCode,
			String bewertungsText) {
		Protokoll protokoll = this.neuesProtokollObjekt();

		protokoll.setAktion(Aktion.RATE);
		protokoll.setAufgabe(aufgabe);
		protokoll.setBearbeitung(aufgabe.getBearbeitung());

		vbaCode = vbaCode.replaceAll("^\n+", "").replaceAll("\n+$", "").replaceAll("\n\n\n+", "\n\n");
		pythonCode = pythonCode.replaceAll("^\n+", "").replaceAll("\n+$", "").replaceAll("\n\n\n+", "\n\n");
		bewertungsText = bewertungsText.replaceAll("Alles okay!\n", "")
				.replaceAll("Plausibilitätsverletzungen sind aufgetreten, ein Protokoll wird geschrieben!\n", "")
				.replaceAll("^\n+", "").replaceAll("\n+$", "").replaceAll("\n\n\n+", "\n\n");

		protokoll.setVbaCode(vbaCode);
		protokoll.setPythonCode(pythonCode);
		protokoll.setBewertungsText(bewertungsText);
		protokoll.setBewertung(bewertung);

		protokollRepository.save(protokoll);
	}

	@Override
	public void protokolliereHinweis(Aufgabe aufgabe, Hinweis hinweis) {
		Protokoll protokoll = this.neuesProtokollObjekt();
		protokoll.setAktion(Aktion.HINT);
		// protokoll.setAufgabe(aufgabe);
		// protokoll.setBearbeitung(aufgabe.getBearbeitung());
		protokoll.setHinweis(hinweis);

		protokollRepository.save(protokoll);
	}
	
	@Override
	public void protokolliereLogin() {
		Protokoll protokoll = this.neuesProtokollObjekt();
		protokoll.setAktion(Aktion.LOGIN);

		protokollRepository.save(protokoll);
	}
	
	@Override
	public void protokolliereBonuspunktabruf(String matrikelnummer) {
		Protokoll protokoll = this.neuesProtokollObjekt();
		protokoll.setAktion(Aktion.BONUS_POINT_QUERY);
		protokoll.setBewertungsText(matrikelnummer);

		protokollRepository.save(protokoll);
	}

	private Protokoll neuesProtokollObjekt() {
		Protokoll protokoll = new Protokoll();
		protokoll.setTimestamp(LocalDateTime.now());
		protokoll.setBenutzerHash(ibixscope.getUserdetails().getHashcode());

		return protokoll;
	}

	@Override
	public List<Protokoll> findByFiltertext(String filtertext) {
		Integer id;
		try {
			id = Integer.valueOf(filtertext);
		} catch (Exception e) {
			id = -1;
		}

		return protokollRepository.findFirst100ByFilterstring("%" + filtertext + "%", id);
	}

	@Override
	public Protokoll findById(Integer protokollId) {
		return protokollRepository.findById(protokollId).orElseThrow();
	}

	@Override
	public String ermittleLoesungsdetails(Bearbeitung bearbeitung, Aufgabe aufgabe) {
		Protokoll protokoll = protokollRepository.findFirstByBearbeitungAndAufgabeOrderByTimestampDesc(bearbeitung,
				aufgabe);
		if (protokoll == null) {
			return "";
		}
		String text = protokoll.getBewertungsText();
		int endeText = text.indexOf("Bewertung von Aufgabe");
		if (endeText > 0) {
			text = text.substring(0, endeText);
		}
		text = text.replaceAll("\n", "<br>");

		return text;
	}
}
