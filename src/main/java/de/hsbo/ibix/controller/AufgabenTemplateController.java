/*
 * AufgabenTemplateController.java 
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
import de.hsbo.ibix.model.DatenTemplate;
import de.hsbo.ibix.service.AufgabenTemplateService;
import de.hsbo.ibix.service.DatenTemplateService;
import de.hsbo.ibix.service.GeneratorService;
import de.hsbo.ibix.service.SpaltendefinitionService;
import de.hsbo.ibix.service.TemplateSpalteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class AufgabenTemplateController.
 */
@Controller
public class AufgabenTemplateController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(AufgabenTemplateController.class);

	@Autowired
	AufgabenTemplateService aufgabenTemplateService;

	@Autowired
	DatenTemplateService datenTemplateService;

	@Autowired
	SpaltendefinitionService spaltendefinitionService;

	@Autowired
	TemplateSpalteService templateSpalteService;

	@Autowired
	GeneratorService generatorService;

	@GetMapping("/aufgabentemplate")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) {
		if (filtertext == null) {
			filtertext = (String) session.getAttribute("at_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}
		List<AufgabenTemplate> templates = aufgabenTemplateService.findByNameOrTag(filtertext);
		log.info("/aufgabentemplate -> found {} templates for {}", templates.size(), filtertext);

		model.addAttribute("templates", templates);
		model.addAttribute("activeNavLink", "aufgabentemplate");
		model.addAttribute("filtertext", filtertext);
		session.setAttribute("at_filtertext", filtertext);

		return new ModelAndView("aufgabentemplate/index", model.asMap());
	}

	@GetMapping("/aufgabentemplate/{templateId}")
	public ModelAndView view(@PathVariable Integer templateId, @RequestParam(required = false) String filtertext,
			@RequestParam(required = false) String filtertextVorgaenger, Model model, HttpServletRequest request,
			HttpSession session) throws FileNotFoundException, IOException, JSONException {
		log.info("Displaying aufgabentemplate {}", templateId);
		AufgabenTemplate template;
		try {
			template = aufgabenTemplateService.getReferenceById(templateId);
		} catch (Exception e) {
			log.info("aufgabentemplate {} not found", templateId);
			template = null;
		}

		if (filtertext == null && template != null) {
			filtertext = (String) session.getAttribute("at_dt_filtertext");
			if (filtertext == null) {
				filtertext = template.getTagString().trim();
			}
		}
		if (filtertextVorgaenger == null && template != null) {
			filtertextVorgaenger = (String) session.getAttribute("at_vg_filtertext");
			if (filtertextVorgaenger == null) {
				filtertextVorgaenger = template.getTagString().trim();
			}
		}

		if (template == null) {
			template = new AufgabenTemplate();
			template.setId(templateId);
			// return new ModelAndView("redirect:/template/");
		}

		List<DatenTemplate> alleDatentemplates;
		alleDatentemplates = datenTemplateService.findByNameOrTag(filtertext);

		List<AufgabenTemplate> relevanteVorgaengerTemplates;
		if (filtertextVorgaenger != null) {
			relevanteVorgaengerTemplates = aufgabenTemplateService.findByNameOrTag(filtertextVorgaenger);
		} else {
			relevanteVorgaengerTemplates = aufgabenTemplateService.findRelevanteVorgaengerTemplates(template);
		}

		String datenHtmlCode = "";
		if (template.getDatentemplate() != null) {
			datenHtmlCode = generatorService.loadTabelleHTML(template.getDatentemplate().getId(), false);
		}

		model.addAttribute("template", template);
		model.addAttribute("filtertext", filtertext);
		model.addAttribute("filtertextVorgaenger", filtertextVorgaenger);
		model.addAttribute("datenHtmlCode", datenHtmlCode);
		model.addAttribute("datentemplates", alleDatentemplates);
		model.addAttribute("relevanteVorgaengerTemplates", relevanteVorgaengerTemplates);
		model.addAttribute("activeNavLink", "aufgabentemplate");
		
		session.setAttribute("at_dt_filtertext", filtertext);
		session.setAttribute("at_vg_filtertext", filtertextVorgaenger);


		return new ModelAndView("aufgabentemplate/view", model.asMap());
	}

	@PostMapping("/aufgabentemplate/{templateId}")
	public ModelAndView save(@PathVariable Integer templateId, @ModelAttribute AufgabenTemplate template, Model model,
			HttpServletRequest request, HttpSession session) {
		log.info("/aufgabentemplate/update von aufgabentemplate {}", templateId);

		aufgabenTemplateService.updateTemplate(template);

		return new ModelAndView("redirect:/aufgabentemplate/" + template.getId());

	}

	@GetMapping("/aufgabentemplate/copy/{templateId}")
	public ModelAndView copy(@PathVariable Integer templateId, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Copy template {}", templateId);
		AufgabenTemplate neuesTemplate;

		neuesTemplate = aufgabenTemplateService.copyAufgabenTemplate(templateId);

		return new ModelAndView("redirect:/aufgabentemplate/" + neuesTemplate.getId(), model.asMap());
	}

	@GetMapping("/aufgabentemplate/new")
	public ModelAndView view(Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("New template {}");
		AufgabenTemplate template;
		template = new AufgabenTemplate();

		model.addAttribute("template", template);
		model.addAttribute("activeNavLink", "aufgabentemplate");

		return new ModelAndView("aufgabentemplate/new", model.asMap());
	}

	@GetMapping("/aufgabentemplate/{templateId}/zuordnen/{datentemplateID}")
	public ModelAndView ordneDatentemplateZu(@PathVariable Integer templateId, @PathVariable int datentemplateID,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Zuordnung von Datentemplate zu  template {}", templateId);
		AufgabenTemplate template;

		template = aufgabenTemplateService.getReferenceById(templateId);
		aufgabenTemplateService.ordneDatentemplateZu(template, datentemplateID);

		// return new ModelAndView("redirect:/aufgabentemplate/" + template.getId(),
		// model.asMap());
		return new ModelAndView("redirect:/aufgabentemplate/" + template.getId() + "#datentemplate", model.asMap());
	}

	@GetMapping("/aufgabentemplate/{templateId}/zuordnenVorgaenger/{vorgaengertemplateID}")
	public ModelAndView ordneVorgaengerTemplateZu(@PathVariable Integer templateId,
			@PathVariable int vorgaengertemplateID, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Zuordnung von Datentemplate zu  template {}", templateId);
		AufgabenTemplate template;

		template = aufgabenTemplateService.getReferenceById(templateId);
		aufgabenTemplateService.ordneVorgaengertemplateZu(template, vorgaengertemplateID);

		// return new ModelAndView("redirect:/aufgabentemplate/" + template.getId(),
		// model.asMap());
		return new ModelAndView("redirect:/aufgabentemplate/" + template.getId() + "#datentemplate", model.asMap());
	}

	@GetMapping("/aufgabentemplate/{templateId}/vorgaengerLoeschen")
	public ModelAndView loescheVorgaengerTemplate(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Zuordnung von Datentemplate zu  template {}", templateId);
		AufgabenTemplate template;

		template = aufgabenTemplateService.getReferenceById(templateId);
		aufgabenTemplateService.ordneVorgaengertemplateZu(template, null);

		// return new ModelAndView("redirect:/aufgabentemplate/" + template.getId(),
		// model.asMap());
		return new ModelAndView("redirect:/aufgabentemplate/" + template.getId() + "#datentemplate", model.asMap());
	}

	@PostMapping("/aufgabentemplate/new")
	public ModelAndView saveNewTemplate(@ModelAttribute AufgabenTemplate template, Model model,
			HttpServletRequest request, HttpSession session) {
		log.info("POST /aufgabentemplate von neuem aufgabentemplate {}");

		aufgabenTemplateService.speicherTemplate(template);
		return new ModelAndView("redirect:/aufgabentemplate?filtertext=" + template.getName());

	}
}
