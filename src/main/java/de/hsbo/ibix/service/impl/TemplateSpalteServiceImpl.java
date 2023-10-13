/*
 * TemplateSpalteServiceImpl.java 
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

import de.hsbo.ibix.model.TemplateSpalte;
import de.hsbo.ibix.repository.TemplateSpalteRepository;
import de.hsbo.ibix.service.TemplateSpalteService;

/**
 * The Class TemplateSpalteServiceImpl.
 */
@Service
public class TemplateSpalteServiceImpl implements TemplateSpalteService {
	final static Logger log = LoggerFactory.getLogger(TemplateSpalteServiceImpl.class);

	@Autowired
	TemplateSpalteRepository templateSpalteRepo;

	@Override
	public List<TemplateSpalte> findAll() {
		return templateSpalteRepo.findAll();
	}

	@Override
	public TemplateSpalte getReferenceById(int id) {
		return templateSpalteRepo.getReferenceById(id);
	}

	@Override
	public TemplateSpalte update(TemplateSpalte templateSpalte) {
		TemplateSpalte original = templateSpalteRepo.getReferenceById(templateSpalte.getId());
		// Nur Daten speichern, die im Formular auch geändert werden können
		original.setOrdnung(templateSpalte.getOrdnung());
		original.setSpaltennr(templateSpalte.getSpaltennr());

		templateSpalteRepo.save(original);
		return original;
	}

}
