/*
 * SpaltendefinitionController.java 
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gwt.thirdparty.json.JSONException;

import de.hsbo.ibix.model.Spaltendefinition;
import de.hsbo.ibix.service.SpaltendefinitionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class SpaltendefinitionController.
 */
@Controller
public class SpaltendefinitionController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(SpaltendefinitionController.class);

	@Autowired
	SpaltendefinitionService spaltendefinitionService;

	@GetMapping("/spaltendefinition/copy/{spaltendefinitionId}")
	public ModelAndView copy(@PathVariable Integer spaltendefinitionId, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Copy Spaltendefinition {}", spaltendefinitionId);
		Spaltendefinition neueSpaltendefinition;

		neueSpaltendefinition = spaltendefinitionService.copySpaltendefinition(spaltendefinitionId);

		return new ModelAndView("redirect:/spaltendefinition/" + neueSpaltendefinition.getId(), model.asMap());
	}

	@GetMapping("/spaltendefinition")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) {
		if (filtertext == null) {
			filtertext = (String) session.getAttribute("s_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}
		List<Spaltendefinition> spaltendefinitionen = spaltendefinitionService.findByNameOrTag(filtertext);
		log.info("/spaltendefinition -> found {} Spaltendefinitionen for {}", spaltendefinitionen.size(), filtertext);

		model.addAttribute("spaltendefinitionen", spaltendefinitionen);
		model.addAttribute("filtertext", filtertext);
		model.addAttribute("activeNavLink", "spaltendefinition");
		session.setAttribute("s_filtertext", filtertext);

		return new ModelAndView("spaltendefinition/index", model.asMap());
	}

	@PostMapping("/spaltendefinition/{spaltendefinitionId}")
	public ModelAndView save(@PathVariable Integer spaltendefinitionId,
			@ModelAttribute Spaltendefinition spaltendefinition, Model model, HttpServletRequest request) {

		spaltendefinition = spaltendefinitionService.updateSpaltendefinition(spaltendefinition);
		log.info("/Spaltendefinition/save -> gespeichert");
		model.addAttribute("Spaltendefinition", spaltendefinition);

		// return new ModelAndView("/spaltendefinition/view", model.asMap());
		// return new ModelAndView("redirect:/spaltendefinition/" + spaltendefinitionId,
		// model.asMap());
		return new ModelAndView("redirect:/spaltendefinition", model.asMap());

	}

	@PostMapping("/spaltendefinition/")
	public ModelAndView saveNew(@ModelAttribute Spaltendefinition spaltendefinition, Model model,
			HttpServletRequest request) {

		spaltendefinitionService.speicherSpaltendefinition(spaltendefinition);
		log.info("Neue Spaltendefinition gespeichert. ID -> {}", spaltendefinition.getId());

		// return new RedirectView("/spaltendefinition/" + Spaltendefinition.getId());
		// return new ModelAndView("redirect:/spaltendefinition/" +
		// spaltendefinition.getId(), model.asMap());
		return new ModelAndView("redirect:/spaltendefinition?filtertext=" + spaltendefinition.getName(), model.asMap());

	}

	@GetMapping("/spaltendefinition/{spaltendefinitionId}")
	public ModelAndView view(@PathVariable Integer spaltendefinitionId, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {

		log.info("Displaying Spaltendefinition {}", spaltendefinitionId);
		Spaltendefinition spaltendefinition;
		try {
			spaltendefinition = spaltendefinitionService.getReferenceById(spaltendefinitionId);
		} catch (Exception e) {
			log.info("Spaltendefinition {} not found", spaltendefinitionId);
			spaltendefinition = null;
		}

		if (spaltendefinition == null) {
			spaltendefinition = new Spaltendefinition();
			spaltendefinition.setId(spaltendefinitionId);
			// return new ModelAndView("redirect:/spaltendefinition/");
		}

		model.addAttribute("spaltendefinition", spaltendefinition);
		model.addAttribute("activeNavLink", "spaltendefinition");

		return new ModelAndView("spaltendefinition/view", model.asMap());
	}

	@GetMapping("/spaltendefinition/new")
	public ModelAndView view(Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("New Spaltendefinition {}");
		Spaltendefinition spaltendefinition;
		spaltendefinition = new Spaltendefinition();
		model.addAttribute("spaltendefinition", spaltendefinition);
		model.addAttribute("activeNavLink", "spaltendefinition");

		return new ModelAndView("spaltendefinition/view", model.asMap());
	}
}
