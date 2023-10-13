/*
 * BearbeitungServiceImpl.java 
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.hsbo.ibix.data.Semester;
import de.hsbo.ibix.data.UserAufgabenblatt;
import de.hsbo.ibix.exception.UserNotificationException;
import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.AufgabenTemplate;
import de.hsbo.ibix.model.Aufgabenblatt;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.model.Bearbeitung;
import de.hsbo.ibix.model.User;
import de.hsbo.ibix.repository.AufgabeRepository;
import de.hsbo.ibix.repository.AufgabenblattRepository;
import de.hsbo.ibix.repository.AufgabentypRepository;
import de.hsbo.ibix.repository.BearbeitungRepository;
import de.hsbo.ibix.repository.UserRepository;
import de.hsbo.ibix.security.IbixScope;
import de.hsbo.ibix.security.IbixUserDetails;
import de.hsbo.ibix.service.AufgabeService;
import de.hsbo.ibix.service.AufgabenTemplateService;
import de.hsbo.ibix.service.AufgabenblattService;
import de.hsbo.ibix.service.BearbeitungService;
import de.hsbo.ibix.service.FileService;
import de.hsbo.ibix.service.GeneratorService;
import de.hsbo.ibix.service.ProtokollService;
import de.hsbo.ibix.service.UserService;

/**
 * The Class BearbeitungServiceImpl.
 */
@Service
public class BearbeitungServiceImpl implements BearbeitungService {

	final static Logger log = LoggerFactory.getLogger(BearbeitungServiceImpl.class);

	@Autowired
	BearbeitungRepository bearbeitungRepo;

	@Autowired
	AufgabenblattRepository aufgabenblattRepo;

	@Autowired
	AufgabeRepository aufgabeRepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
	GeneratorService generatorService;

	@Autowired
	AufgabeService aufgabeService;

	@Autowired
	AufgabenblattService aufgabenblattService;

	@Autowired
	AufgabenTemplateService templateService;

	@Autowired
	AufgabentypRepository aufgabentypRepo;

	@Autowired
	IbixScope ibixscope;

	@Autowired
	FileService fileService;

	@Autowired
	ProtokollService protokollService;

	@Autowired
	UserService userService;

	@Override
	@PostAuthorize("hasPermission(returnObject, 'ROLE_USER')")
	public Bearbeitung findById(Integer bearbeitungId) {
		return bearbeitungRepo.getReferenceById(bearbeitungId);
	}

	@Override
	public List<Bearbeitung> findAllForUser() {
		String userHash = ibixscope.getUserdetails().getHashcode();

		return bearbeitungRepo.findByBenutzerHash(userHash);
	}

	@Override
	public HashMap<Integer, List<Bearbeitung>> findeBearbeitungenFuerAufgabenblaetter(
			List<Aufgabenblatt> aufgabenblaetter) {
		HashMap<Integer, List<Bearbeitung>> bearbeitungsHashmap = new HashMap<>();
		String userHash = ibixscope.getUserdetails().getHashcode();

		Iterator<Aufgabenblatt> iter = aufgabenblaetter.iterator();

		while (iter.hasNext()) {
			Aufgabenblatt aufgabenblatt = iter.next();
			List<Bearbeitung> bearbeitungen;
			bearbeitungen = bearbeitungRepo.findByBenutzerHashAndAufgabenblattOrderByAusgabeDesc(userHash,
					aufgabenblatt);
			bearbeitungsHashmap.put(aufgabenblatt.getId(), bearbeitungen);
		}
		return bearbeitungsHashmap;
	}

	@Override
	public HashMap<String, String> findeBesteBewertungenFuerMatrikelnummer(String matrikelnummer) {
		HashMap<String, String> bewertungsHashmap = new HashMap<>();
		bewertungsHashmap.put("Matrikelnummer", matrikelnummer);

		Optional<User> optUser = userService.findByMatrikelnummer(matrikelnummer);
		if (optUser.isEmpty()) {
			bewertungsHashmap.put("not found", "not found");
			return bewertungsHashmap;
		}

		Semester aktuell = new Semester(LocalDateTime.now());
		LocalDateTime abDatum = aktuell.vorsemester(1).getStart();

		User user = optUser.get();
		String userHash = user.getHashcode();
		bewertungsHashmap.put("Matrikelnummer", matrikelnummer);
		bewertungsHashmap.put("Vorname", user.getVorname());
		bewertungsHashmap.put("Nachname", user.getNachname());
		bewertungsHashmap.put("Email", user.getEmail());

		List<Aufgabenblatt> aufgabenblaetter = aufgabenblattService.findAktuellGueltige();
		Iterator<Aufgabenblatt> iter = aufgabenblaetter.iterator();

		while (iter.hasNext()) {
			Aufgabenblatt aufgabenblatt = iter.next();
			if (aufgabenblatt.getKlausur()) {
				Integer maxbewertung = bearbeitungRepo.findMaxBewertungByBenutzerHashAndAufgabenblatt(userHash,
						aufgabenblatt, abDatum);
				if (maxbewertung != null) {
					String aufgabe = "";
					int aufgabenblattid = aufgabenblatt.getId();
					if (aufgabenblattid == 5)
						aufgabe = "1";
					else if (aufgabenblattid == 10)
						aufgabe = "2";
					else if (aufgabenblattid == 14)
						aufgabe = "4";
					else if (aufgabenblattid == 18)
						aufgabe = "3";
					else
						aufgabe = "unbekannt";

					bewertungsHashmap.put(aufgabe, maxbewertung.toString());
				}
			}
		}

		user.setBonuspunktabruf(LocalDateTime.now());
		userRepo.save(user);
		return bewertungsHashmap;
	}

	@Override
	@PostAuthorize("hasPermission(returnObject, 'ROLE_USER')")
	public Bearbeitung erstelleBearbeitung(Integer aufgabenblattId) {
		Aufgabenblatt aufgabenblatt = aufgabenblattRepo.getReferenceById(aufgabenblattId);

		LocalDateTime jetzt = LocalDateTime.now();

		Optional<Bearbeitung> bearbeitungOpt = bearbeitungRepo
				.findFirstByBenutzerHashAndAufgabenblattAndSpaetesteAbgabeGreaterThan(
						ibixscope.getUserdetails().getHashcode(), aufgabenblatt, jetzt);

		if (bearbeitungOpt.isPresent()) {
			throw new UserNotificationException("Neuerstellung vor Ende der Bearbeitung der vorhergehenden Blatts.",
					"/bearbeitung");
		}

		if ((aufgabenblatt.getStart() != null && jetzt.isBefore(aufgabenblatt.getStart()))
				|| (aufgabenblatt.getEnde() != null && jetzt.isAfter(aufgabenblatt.getEnde()))) {
			throw new RuntimeException("Anfrage zur Anlage einer Bearbeitung außerhalb des  Bearbeitungszeitraums.");
		}

		Bearbeitung bearbeitung = erstelleBearbeitungMitAufgaben(aufgabenblatt);
		return bearbeitung;
	}

	private Bearbeitung erstelleBearbeitungMitAufgaben(Aufgabenblatt aufgabenblatt) {
		int nr = 1;
		String userHash = ibixscope.getUserdetails().getHashcode();
		Optional<User> user = userService.findByUsername(ibixscope.getUserdetails().getUsername());
		Bearbeitung bearbeitung;

		bearbeitung = new Bearbeitung();
		bearbeitung.setBenutzerHash(userHash);
		bearbeitung.setAufgabenblatt(aufgabenblatt);
		bearbeitung.setAusgabe(LocalDateTime.now());
		bearbeitungRepo.save(bearbeitung);

		Aufgabe aufgabe;
		Aufgabentyp aufgabentyp;
		HashMap<String, Integer> config = new HashMap<>();

		Iterator<Aufgabentyp> iter = aufgabenblatt.getAufgabentypen().iterator();
		while (iter.hasNext()) {
			log.debug("Konfiguration für neue Aufgabe: {}", config);
			aufgabentyp = iter.next();
			// die TemplateAuswahl setzt als Seiteneffekt die config, mit der ggf. das
			// nächste Template ausgewählt wird.
			AufgabenTemplate template = templateService.wähleTemplateAus(aufgabentyp, config);
			aufgabe = templateService.erstelleAufgabe(template, config);
			aufgabe.setNr(nr);
			++nr;
			bearbeitung.addAufgabe(aufgabe);
			aufgabeRepo.save(aufgabe);
			protokollService.protokolliereNeueAufgabe(aufgabe);
		}

		if (bearbeitung.getAufgabenblatt().getDauer() != null && bearbeitung.getAufgabenblatt().getDauer() > 0) {
			Integer verlaengerung = user.get().getSchreibverlaengerung();
			double bearbeitungsdauer = bearbeitung.getAufgabenblatt().getDauer();
			bearbeitungsdauer = Math.ceil(bearbeitungsdauer * (1.0 + (double) verlaengerung / 100.0));
			bearbeitung.setSpaetesteAbgabe(bearbeitung.getAusgabe().plusMinutes((int) bearbeitungsdauer));
		}
		bearbeitungRepo.save(bearbeitung);
		return bearbeitung;
	}

	@Override
	@PreAuthorize("hasPermission(#bearbeitung, 'ROLE_USER')")
	public void bewerteLoesung(Bearbeitung bearbeitung) throws IOException {
		int punkte = 0;
		int prozent = 0;
		int anzahl = 0;
		Iterator<Aufgabe> iter = bearbeitung.getAufgaben().iterator();

		while (iter.hasNext()) {
			Aufgabe aufgabe = iter.next();
			aufgabeService.bewerteLoesung(aufgabe);
			if (aufgabe.getBonuspunkte() != null) {
				punkte += aufgabe.getBonuspunkte();
			}
			if (aufgabe.getProzent() != null) {
				anzahl++;
				prozent += aufgabe.getProzent();
			}
		}
		log.info("User {}: Bewertung von Aufgabenblatt Nr. {} (BearbeitungID {}): {} Prozent",
				ibixscope.getUserdetails().getHashcode(), bearbeitung.getAufgabenblatt().getNr(), bearbeitung.getId(),
				prozent);
		if (bearbeitung.getAufgabenblatt().getKlausur()) {
			bearbeitung.setBonuspunkte(punkte);
		}
		if (anzahl > 0) {
			bearbeitung.setProzent((int) Math.ceil(prozent / anzahl));
		}
		bearbeitungRepo.save(bearbeitung);
	}

	@Override
	@PreAuthorize("hasPermission(#bearbeitung, 'ROLE_USER')")
	public String speichereLoesung(Bearbeitung bearbeitung, MultipartFile file) throws IOException {
		this.pruefeUser(bearbeitung);

		LocalDateTime abgabezeit = LocalDateTime.now();
		String ausgabetext = "Lösungsdatei '" + file.getOriginalFilename() + "' erfolgreich hochgeladen";

		if (file.getSize() == 0) {
			return "Keine Datei hochgeladen.";
		}

		if (bearbeitung.getSpaetesteAbgabe() != null && bearbeitung.getSpaetesteAbgabe().isBefore(abgabezeit)) {
			if (fileService.existiertLoesung(bearbeitung.getId())) {
				return "Abgabefrist abgelaufen. Da bereits eine Lösungsdatei vorliegt, wurde die Abgabe nicht mehr angegenommen.";
			} else {
				ausgabetext = "Abgabefrist abgelaufen. Die Abgabe wurde trotzdem entgegengenommen, wird aber nicht bewertet.";
			}
		}

		fileService.speicherLoesung(bearbeitung.getId(), file);
		bearbeitung.setAbgabe(abgabezeit);

		bearbeitungRepo.save(bearbeitung);

		return ausgabetext;
	}

	private void pruefeUser(Bearbeitung bearbeitung) {
		String userhash = ibixscope.getUserdetails().getHashcode();
		if (!bearbeitung.getBenutzerHash().equals(userhash)) {
			// throw new RessourceAccessException("Zugriff auf Daten eines anderen Users.");
			log.warn("pruefeUser: Bearbeitung {} durch User {} verwendet,", bearbeitung.getId(), userhash);
		}
	}

	@Override
	public HashMap<Integer, String> ermittleLoesungsdetails(Bearbeitung bearbeitung) {
		HashMap<Integer, String> details = new HashMap<>();
		Iterator<Aufgabe> iter = bearbeitung.getAufgaben().iterator();

		while (iter.hasNext()) {
			Aufgabe aufgabe = iter.next();
			String loesungsdetails = protokollService.ermittleLoesungsdetails(bearbeitung, aufgabe);
			details.put(aufgabe.getId(), loesungsdetails);
		}
		return details;
	}

	@Override
	public List<UserAufgabenblatt> ermittleUserAufgabeblaetter() {
		List<UserAufgabenblatt> userAufgabenblaetter = new ArrayList<UserAufgabenblatt>();
		List<Aufgabenblatt> aufgabenblaetter;

		IbixUserDetails userdetails = ibixscope.getUserdetails();
		String userHash = userdetails.getHashcode();
		
		if (userdetails.isConfig() || userdetails.isAdmin()) {
			aufgabenblaetter = aufgabenblattService.findAll();
		}
		else {
			aufgabenblaetter = aufgabenblattService.findAktuellGueltige();
		}
		
		Iterator<Aufgabenblatt> iter = aufgabenblaetter.iterator();
		while (iter.hasNext()) {
			Aufgabenblatt aufgabenblatt = iter.next();
			List<Bearbeitung> bearbeitungen;
			bearbeitungen = bearbeitungRepo.findByBenutzerHashAndAufgabenblattOrderByAusgabeDesc(userHash,
					aufgabenblatt);
			userAufgabenblaetter.add(new UserAufgabenblatt(aufgabenblatt, bearbeitungen));
		}

		return userAufgabenblaetter;
	}

}
