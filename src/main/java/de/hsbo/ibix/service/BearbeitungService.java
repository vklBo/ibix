/*
 * BearbeitungService.java 
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

package de.hsbo.ibix.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import de.hsbo.ibix.data.UserAufgabenblatt;
import de.hsbo.ibix.model.Aufgabenblatt;
import de.hsbo.ibix.model.Bearbeitung;

/**
 * The Interface BearbeitungService.
 */
public interface BearbeitungService {
	public List<Bearbeitung> findAllForUser();

	public Bearbeitung findById(Integer bearbeitungId);

	public void bewerteLoesung(Bearbeitung bearbeitung) throws IOException;

	public String speichereLoesung(Bearbeitung bearbeitung, MultipartFile file) throws IOException;

	public HashMap<Integer, List<Bearbeitung>> findeBearbeitungenFuerAufgabenblaetter(
			List<Aufgabenblatt> aufgabenblaetter);

	Bearbeitung erstelleBearbeitung(Integer aufgabenblattId);

	HashMap<String, String> findeBesteBewertungenFuerMatrikelnummer(String matrikelnummer);

	public HashMap<Integer, String> ermittleLoesungsdetails(Bearbeitung bearbeitung);

	public List<UserAufgabenblatt> ermittleUserAufgabeblaetter();
}