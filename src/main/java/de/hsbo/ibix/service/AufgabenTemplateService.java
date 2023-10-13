/*
 * AufgabenTemplateService.java 
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

import java.util.HashMap;
import java.util.List;

import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.AufgabenTemplate;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.model.ConfigFunktionen;
import de.hsbo.ibix.model.ConfigRestaufgaben;

/**
 * The Interface AufgabenTemplateService.
 */
public interface AufgabenTemplateService {

	public AufgabenTemplate copyAufgabenTemplate(int templateId);

	public List<AufgabenTemplate> findAll();

	public List<AufgabenTemplate> findAllByOrderById();

	public AufgabenTemplate getReferenceById(int id);

	public void saveConfigFunktionen(AufgabenTemplate template, ConfigFunktionen config);

	public void speicherTemplate(AufgabenTemplate template);

	public AufgabenTemplate updateTemplate(AufgabenTemplate template);

	public List<AufgabenTemplate> findByNameOrTag(String filtertext);

	public void saveConfigStatistiken(AufgabenTemplate template, ConfigRestaufgaben config);

	public void ordneDatentemplateZu(AufgabenTemplate template, int datentemplateID);

	public void loescheConfigFunktionen(Integer templateId);

	public void loescheConfigStatistiken(Integer templateId);

	public void loescheConfigPlausi(Integer templateId);

	public void loescheConfigFormatierung(Integer templateId);

	void saveConfigPlausi(AufgabenTemplate template, ConfigRestaufgaben config);

	void saveConfigFormatierung(AufgabenTemplate template, ConfigRestaufgaben config);

	public AufgabenTemplate wähleTemplateAus(Aufgabentyp aufgabentyp, HashMap<String, Integer> config);

	Aufgabe erstelleAufgabe(AufgabenTemplate template, HashMap<String, Integer> config);

	public List<AufgabenTemplate> findRelevanteVorgaengerTemplates(AufgabenTemplate template);

	public void ordneVorgaengertemplateZu(AufgabenTemplate template, Integer vorgaengertemplateID);
}
