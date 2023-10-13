/*
 * DatenTemplateServiceImpl.java 
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

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.DatenTemplate;
import de.hsbo.ibix.model.Spaltendefinition;
import de.hsbo.ibix.model.TemplateSpalte;
import de.hsbo.ibix.repository.DatenTemplateRepository;
import de.hsbo.ibix.repository.SpaltendefinitionRepository;
import de.hsbo.ibix.repository.TemplateSpalteRepository;
import de.hsbo.ibix.service.DatenTemplateService;

/**
 * The Class DatenTemplateServiceImpl.
 */
@Service
public class DatenTemplateServiceImpl implements DatenTemplateService {
	final static Logger log = LoggerFactory.getLogger(DatenTemplateServiceImpl.class);

	@Autowired
	DatenTemplateRepository templateRepo;

	@Autowired
	SpaltendefinitionRepository spaltendefinitionRepo;

	@Autowired
	TemplateSpalteRepository templateSpalteRepo;

	@Override
	public DatenTemplate copyDatenTemplate(int templateId) {
		DatenTemplate original = templateRepo.getReferenceById(templateId);
		DatenTemplate neu = new DatenTemplate(original);
		neu.setName(neu.getName() + " Kopie");

		Iterator<TemplateSpalte> iter = original.getTemplateSpalten().iterator();
		while (iter.hasNext()) {
			TemplateSpalte ts = iter.next();
			TemplateSpalte neueSpalte = new TemplateSpalte(ts);
			neueSpalte.setSpalte(ts.getSpalte());
			neueSpalte.setTemplate(neu);
			neu.getTemplateSpalten().add(neueSpalte);
		}
		templateRepo.save(neu);
		return neu;
	}

	@Override
	public void entferneSpalte(int templateSpalteId) {
		TemplateSpalte templateSpalte = templateSpalteRepo.getReferenceById(templateSpalteId);
		Spaltendefinition spalte;
		DatenTemplate template;

		spalte = templateSpalte.getSpalte();
		template = templateSpalte.getTemplate();

		spalte.getTemplateSpalten().remove(templateSpalte);
		template.getTemplateSpalten().remove(templateSpalte);

		templateSpalte.setSpalte(null);
		templateSpalte.setTemplate(null);
		templateSpalteRepo.delete(templateSpalte);
	}

	@Override
	public List<DatenTemplate> findAll() {
		return templateRepo.findAll();
	}

	@Override
	public List<DatenTemplate> findAllByOrderById() {
		return templateRepo.findAllByOrderById();
	}

	@Override
	public DatenTemplate getReferenceById(int id) {
		return templateRepo.getReferenceById(id);
	}

	@Override
	public void ordneSpalteZu(DatenTemplate template, int spaltendefinitionId) {
		int ordnung = 0;
		int spaltennr = 0;

		TemplateSpalte templateSpalte;
		Spaltendefinition spalte = spaltendefinitionRepo.getReferenceById(spaltendefinitionId);

		Iterator<TemplateSpalte> iter = template.getTemplateSpalten().iterator();

		while (iter.hasNext()) {
			templateSpalte = iter.next();
			if (templateSpalte.getSpalte().getId() == spaltendefinitionId) {
				// Spalte schon vorhanden, nichts zu tun
				return;
			}
			ordnung = Math.max(ordnung, templateSpalte.getOrdnung());
			if (templateSpalte.getSpaltennr() != null) {
				spaltennr = Math.max(spaltennr, templateSpalte.getSpaltennr());
			}
		}

		templateSpalte = new TemplateSpalte();
		templateSpalte.setOrdnung(ordnung + 1);
		templateSpalte.setSpaltennr(spaltennr + 1);
		templateSpalte.setSpalte(spalte);
		templateSpalte.setTemplate(template);

		spalte.getTemplateSpalten().add(templateSpalte);
		template.getTemplateSpalten().add(templateSpalte);

		templateSpalteRepo.save(templateSpalte);
		templateRepo.save(template);
		spaltendefinitionRepo.save(spalte);
	}

	@Override
	public void speicherTemplate(DatenTemplate template) {
		templateRepo.save(template);
	}

	@Override
	public DatenTemplate updateTemplate(DatenTemplate template) {
		DatenTemplate original = templateRepo.getReferenceById(template.getId());
		// Nur Daten speichern, die im Formular auch geändert werden können
		original.setName(template.getName());
		original.setTagString(template.getTagString());

		templateRepo.save(original);

		return original;
	}

	@Override
	public List<DatenTemplate> findByNameOrTag(String filtertext) {
		return templateRepo.findByNameContainingOrTagsContainingOrderById(filtertext, filtertext);
	}
}
