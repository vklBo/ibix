/*
 * GeneratorService.java 
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

/**
 * The Interface GeneratorService.
 */
public interface GeneratorService {
	byte[] loadExcel(Integer aufgabenID);

	public String loadHTML(Integer aufgabenID);

	public String loadPythonCode(String filename);

	byte[] loadPdf(Integer aufgabenID) throws Exception;

	public Integer neueAufgabe(int templateID, int generatorDatenID, String aufgabentyp);

	public String bewerteLoesung(Integer generatorID, String vbaCode, Boolean pruefung);

	public Integer neueDatentabelle(Integer id);

	public Integer showTabelle(Integer datenTemplateID, Integer alleSpalten);

	String loadTabelleHTML(Integer templateId, boolean alleSpalten);

	public Integer neueAufgabeBasierendAufVorgaengerTemplate(Integer templateID, Integer vorgaengerTemplateID,
			String aufgabentyp);
}
