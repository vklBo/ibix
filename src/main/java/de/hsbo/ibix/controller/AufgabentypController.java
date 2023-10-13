/*
 * AufgabentypController.java 
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

import de.hsbo.ibix.model.AufgabenTemplate;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.service.AufgabenTemplateService;
import de.hsbo.ibix.service.AufgabentypService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class AufgabentypController.
 */
@Controller
public class AufgabentypController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(AufgabentypController.class);

	@Autowired
	AufgabentypService aufgabentypService;

	@Autowired
	AufgabenTemplateService aufgabentemplateService;

	@GetMapping("/aufgabentyp")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) throws FileNotFoundException, IOException, JSONException {

		if (filtertext == null) {
			filtertext = (String) session.getAttribute("typ_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}
		List<Aufgabentyp> aufgabentypen = aufgabentypService.findByNameOrTag(filtertext);

		model.addAttribute("aufgabentypen", aufgabentypen);
		model.addAttribute("activeNavLink", "aufgabentyp");
		model.addAttribute("filtertext", filtertext);
		session.setAttribute("typ_filtertext", filtertext);

		return new ModelAndView("aufgabentyp/index", model.asMap());
	}

	@GetMapping("/aufgabentyp/{aufgabentypId}")
	public ModelAndView bearbeitung(@PathVariable Integer aufgabentypId,
			@RequestParam(required = false) String filtertext, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {

		log.info("Displaying Aufgabenblatt {}", aufgabentypId);

		Aufgabentyp aufgabentyp = aufgabentypService.findById(aufgabentypId);
		if (aufgabentyp == null) {
			return new ModelAndView("redirect:/aufgabenblatt/");
		}

		if (filtertext == null && aufgabentyp != null) {
			filtertext = aufgabentyp.getTagString().trim();
		}

		List<AufgabenTemplate> alleAufgabentemplates = aufgabentemplateService.findByNameOrTag(filtertext);

		model.addAttribute("aufgabentyp", aufgabentyp);
		model.addAttribute("alleAufgabentemplates", alleAufgabentemplates);
		model.addAttribute("activeNavLink", "aufgabentyp");
		model.addAttribute("filtertext", filtertext);

		return new ModelAndView("aufgabentyp/view", model.asMap());
	}

	@PostMapping("/aufgabentyp/{aufgabentypId}")
	public ModelAndView save(@PathVariable Integer aufgabentypId, @ModelAttribute Aufgabentyp aufgabentyp, Model model,
			HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Updating Aufgabentyp {}", aufgabentyp);

		aufgabentyp = aufgabentypService.update(aufgabentyp);

		return new ModelAndView("redirect:/aufgabentyp");

	}

	@GetMapping("/aufgabentyp/new")
	public ModelAndView saveNew(Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Anlegen neuer Aufgabentyp {}");

		Aufgabentyp aufgabentyp = new Aufgabentyp();

		model.addAttribute("aufgabentyp", aufgabentyp);
		model.addAttribute("activeNavLink", "aufgabentyp");

		return new ModelAndView("aufgabentyp/new", model.asMap());
	}

	@PostMapping("/aufgabentyp/new")
	public ModelAndView saveNew(@ModelAttribute Aufgabentyp aufgabentyp, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Speichern neuer Aufgabentyp {}", aufgabentyp);

		aufgabentyp = aufgabentypService.speichern(aufgabentyp);

		return new ModelAndView("redirect:/aufgabentyp/" + aufgabentyp.getId());

	}

	@GetMapping("/aufgabentyp/{aufgabentypId}/entfernen/{aufgabentemplateId}")
	public ModelAndView enferneSpalte(@PathVariable Integer aufgabentypId, @PathVariable Integer aufgabentemplateId,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Enfernen von Aufgabentemplate {} von Aufgabentyp {}", aufgabentypId, aufgabentemplateId);

		aufgabentypService.entferneAufgabenTemplate(aufgabentypId, aufgabentemplateId);

		// return new ModelAndView("redirect:/aufgabentyp/" + aufgabentypId);
		return new ModelAndView("redirect:/aufgabentyp/" + aufgabentypId + "#aufgabentemplates");
	}

	@GetMapping("/aufgabentyp/{aufgabentypId}/zuordnen/{aufgabentemplateId}")
	public ModelAndView ordneSpalteZu(@PathVariable Integer aufgabentypId, @PathVariable int aufgabentemplateId,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Zuordnen von Aufgabentemplate {} zu Aufgabentyp {}", aufgabentypId, aufgabentemplateId);

		aufgabentypService.ordneAufgabenTemplateZu(aufgabentypId, aufgabentemplateId);

		// return new ModelAndView("redirect:/aufgabentyp/" + aufgabentypId);
		return new ModelAndView("redirect:/aufgabentyp/" + aufgabentypId + "#aufgabentemplates");

	}
}
