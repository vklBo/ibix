/*
 * DatenTemplateService.java 
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

import java.util.List;

import de.hsbo.ibix.model.DatenTemplate;

/**
 * The Interface DatenTemplateService.
 */
public interface DatenTemplateService {

	public DatenTemplate copyDatenTemplate(int templateId);

	public void entferneSpalte(int templateSpalteId);

	public List<DatenTemplate> findAll();

	public List<DatenTemplate> findAllByOrderById();

	public DatenTemplate getReferenceById(int id);

	public void ordneSpalteZu(DatenTemplate template, int spaltendefinitionId);

	public void speicherTemplate(DatenTemplate template);

	public DatenTemplate updateTemplate(DatenTemplate template);

	public List<DatenTemplate> findByNameOrTag(String filtertext);
}
