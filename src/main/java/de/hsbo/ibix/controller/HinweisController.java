/*
 * HinweisController.java 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import de.hsbo.ibix.model.Hinweis;
import de.hsbo.ibix.service.HinweisService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The Class HinweisController.
 */
@Controller
public class HinweisController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(HinweisController.class);

	@Autowired
	HinweisService hinweisService;

	@GetMapping("/hilfe/aufgabe/{aufgabeId}")
	public ModelAndView viewForAufgabe(@PathVariable Integer aufgabeId, Model model, HttpServletRequest request) {

		Hinweis hinweis;
		hinweis = hinweisService.findHinweiseFuerAufgabe(aufgabeId);

		model.addAttribute("hinweis", hinweis);
		model.addAttribute("activeNavLink", "aufgabe");

		return new ModelAndView("hilfe/view", model.asMap());
	}

	@GetMapping("/hilfe/{hinweisId}")
	public ModelAndView view(@PathVariable Integer hinweisId, Model model, HttpServletRequest request) {

		Hinweis hinweis;
		hinweis = hinweisService.getReferenceById(hinweisId);

		model.addAttribute("hinweis", hinweis);
		model.addAttribute("activeNavLink", "aufgabe");

		return new ModelAndView("hilfe/view", model.asMap());
	}
}
