/*
 * AufgabenblattController.java 
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

import de.hsbo.ibix.model.Aufgabenblatt;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.service.AufgabenblattService;
import de.hsbo.ibix.service.AufgabentypService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class AufgabenblattController.
 */
@Controller
public class AufgabenblattController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(AufgabenblattController.class);

	@Autowired
	AufgabenblattService aufgabenblattService;

	@Autowired
	AufgabentypService aufgabentypService;

	@GetMapping("/aufgabenblatt")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) {
		if (filtertext == null) {
			filtertext = (String) session.getAttribute("blatt_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}

		List<Aufgabenblatt> aufgabenblaetter = aufgabenblattService.findByNameOrTag(filtertext);
		log.info("/aufgabenblatt -> found {} Aufgabenblaetter", aufgabenblaetter.size());

		model.addAttribute("aufgabenblaetter", aufgabenblaetter);
		model.addAttribute("activeNavLink", "aufgabenblatt");
		model.addAttribute("filtertext", filtertext);
		session.setAttribute("blatt_filtertext", filtertext);

		return new ModelAndView("aufgabenblatt/index", model.asMap());

	}

	@GetMapping("/aufgabenblatt/{aufgabenblattId}")
	public ModelAndView view(@PathVariable Integer aufgabenblattId, @RequestParam(required = false) String filtertext,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {

		log.info("Displaying Aufgabenblatt {}", aufgabenblattId);

		Aufgabenblatt aufgabenblatt = aufgabenblattService.findById(aufgabenblattId);
		if (aufgabenblatt == null) {
			return new ModelAndView("redirect:/aufgabenblatt");
		}

		if (filtertext == null && aufgabenblatt != null) {
			filtertext = aufgabenblatt.getTagString().trim();
		}

		List<Aufgabentyp> alleAufgabentypen = aufgabentypService.findByNameOrTag(filtertext);

		model.addAttribute("aufgabenblatt", aufgabenblatt);
		model.addAttribute("alleAufgabentypen", alleAufgabentypen);
		model.addAttribute("activeNavLink", "aufgabenblatt");
		model.addAttribute("filtertext", filtertext);

		return new ModelAndView("aufgabenblatt/view", model.asMap());
	}

	@PostMapping("/aufgabenblatt/{aufgabenblattId}")
	public ModelAndView save(@PathVariable Integer aufgabenblattId, @ModelAttribute Aufgabenblatt aufgabenblatt,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Updating Aufgabenblatt {}", aufgabenblattId);

		aufgabenblatt = aufgabenblattService.update(aufgabenblatt);

		return new ModelAndView("redirect:/aufgabenblatt");

	}

	@GetMapping("/aufgabenblatt/new")
	public ModelAndView neuesBlatt(Model model, HttpServletRequest request) {
		Aufgabenblatt aufgabenblatt = new Aufgabenblatt();

		model.addAttribute("aufgabenblatt", aufgabenblatt);
		model.addAttribute("activeNavLink", "aufgabenblatt");

		return new ModelAndView("aufgabenblatt/new", model.asMap());
	}

	@PostMapping("/aufgabenblatt/new")
	public ModelAndView neuesBlattSpeichern(@ModelAttribute Aufgabenblatt aufgabenblatt, Model model,
			HttpServletRequest request) {
		aufgabenblatt = aufgabenblattService.speicher(aufgabenblatt);

		return new ModelAndView("redirect:/aufgabenblatt/" + aufgabenblatt.getId());
	}

	@GetMapping("/aufgabenblatt/{aufgabenblattId}/entfernen/{aufgabentypId}")
	public ModelAndView enferneSpalte(@PathVariable Integer aufgabenblattId, @PathVariable Integer aufgabentypId,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Enfernen von Aufgabentyp {} von Aufgabenblatt {}", aufgabentypId, aufgabenblattId);

		aufgabenblattService.entferneAufgabentyp(aufgabenblattId, aufgabentypId);

		// return new ModelAndView("redirect:/aufgabenblatt/" + aufgabenblattId);
		return new ModelAndView("redirect:/aufgabenblatt/" + aufgabenblattId + "#aufgabentypen");

	}

	@GetMapping("/aufgabenblatt/{aufgabenblattId}/zuordnen/{aufgabentypId}")
	public ModelAndView ordneSpalteZu(@PathVariable Integer aufgabenblattId, @PathVariable int aufgabentypId,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Zuordnen von Aufgabentyp {} zu Aufgabenblatt {}", aufgabentypId, aufgabenblattId);

		aufgabenblattService.ordneAufgabentypZu(aufgabenblattId, aufgabentypId);

		// return new ModelAndView("redirect:/aufgabenblatt/" + aufgabenblattId);
		return new ModelAndView("redirect:/aufgabenblatt/" + aufgabenblattId + "#aufgabentypen");

	}
}
