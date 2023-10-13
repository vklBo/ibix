/*
 * DatenTemplateController.java 
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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.google.gwt.thirdparty.json.JSONException;

import de.hsbo.ibix.model.DatenTemplate;
import de.hsbo.ibix.model.Spaltendefinition;
import de.hsbo.ibix.model.TemplateSpalte;
import de.hsbo.ibix.service.DatenTemplateService;
import de.hsbo.ibix.service.GeneratorService;
import de.hsbo.ibix.service.SpaltendefinitionService;
import de.hsbo.ibix.service.TemplateSpalteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class DatenTemplateController.
 */
@Controller
public class DatenTemplateController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(DatenTemplateController.class);

	@Autowired
	DatenTemplateService datenTemplateService;

	@Autowired
	SpaltendefinitionService spaltendefinitionService;

	@Autowired
	TemplateSpalteService templateSpalteService;

	@Autowired
	GeneratorService generatorService;

	@GetMapping("/datentemplate")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) {
		SecurityContext context = SecurityContextHolder.getContext();
		context.getAuthentication().getDetails();
		if (filtertext == null) {
			filtertext = (String) session.getAttribute("t_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}
		List<DatenTemplate> templates = datenTemplateService.findByNameOrTag(filtertext);
		log.info("/datentemplate -> found {} templates for {}", templates.size(), filtertext);

		model.addAttribute("templates", templates);
		model.addAttribute("filtertext", filtertext);
		session.setAttribute("t_filtertext", filtertext);
		model.addAttribute("activeNavLink", "datentemplate");

		return new ModelAndView("datentemplate/index", model.asMap());
	}

	@GetMapping("/datentemplate/{templateId}")
	public ModelAndView view(@PathVariable Integer templateId, @RequestParam(required = false) String filtertext,
			Model model, HttpServletRequest request, HttpSession session)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Displaying datentemplate {}", templateId);
		DatenTemplate template;
		List<Spaltendefinition> alleSpalten;
		try {
			template = datenTemplateService.getReferenceById(templateId);
		} catch (Exception e) {
			log.info("datentemplate {} not found", templateId);
			template = null;
		}

		if (filtertext == null && template != null) {
			filtertext = "#allgemein " + template.getTagString().trim();
		}

		if (template == null) {
			template = new DatenTemplate();
			template.setId(templateId);
			// return new ModelAndView("redirect:/template/");
		}
		// alleSpalten = spaltendefinintionService.findAllByOrderByIdDesc();
		alleSpalten = spaltendefinitionService.findByNameOrTag(filtertext);
		String datenHtmlCode = generatorService.loadTabelleHTML(templateId, true);

		model.addAttribute("template", template);
		model.addAttribute("alleSpalten", alleSpalten);
		model.addAttribute("datenHtmlCode", datenHtmlCode);
		model.addAttribute("filtertext", filtertext);

		return new ModelAndView("datentemplate/view", model.asMap());
	}

	@PostMapping("/datentemplate/{templateId}")
	public ModelAndView save(@PathVariable Integer templateId, @ModelAttribute DatenTemplate template, Model model,
			HttpServletRequest request, HttpSession session) {
		log.info("/datentemplate/update von DatenTemplate {}", templateId);

		template = datenTemplateService.updateTemplate(template);

		return new ModelAndView("redirect:/datentemplate/" + template.getId(), model.asMap());
	}

	@GetMapping("/datentemplate/copy/{templateId}")
	public ModelAndView copy(@PathVariable Integer templateId, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Copy template {}", templateId);
		DatenTemplate neuesTemplate;

		neuesTemplate = datenTemplateService.copyDatenTemplate(templateId);

		return new ModelAndView("redirect:/datentemplate/" + neuesTemplate.getId(), model.asMap());
	}

	@GetMapping("/datentemplate/{templateId}/entfernen/{templateSpalteId}")
	public ModelAndView enferneSpalte(@PathVariable Integer templateId, @PathVariable Integer templateSpalteId,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Enfernen von Spalte zu  template {}", templateId);

		datenTemplateService.entferneSpalte(templateSpalteId);

		return new ModelAndView("redirect:/datentemplate/" + templateId, model.asMap());
	}

	@GetMapping("/datentemplate/{templateId}/zuordnen/{spalteId}")
	public ModelAndView ordneSpalteZu(@PathVariable Integer templateId, @PathVariable int spalteId, Model model,
			HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Zuordnung von Spalte zu  template {}", templateId);
		DatenTemplate template;

		template = datenTemplateService.getReferenceById(templateId);
		datenTemplateService.ordneSpalteZu(template, spalteId);

		return new ModelAndView("redirect:/datentemplate/" + template.getId(), model.asMap());
	}

	@GetMapping("/datentemplate/{templateId}/bearbeiten/{templateSpalteId}")
	public ModelAndView zeigeSpaltenzuordnung(@PathVariable Integer templateId, @PathVariable int templateSpalteId,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Bearbeien der Spaltenzuordnung zu templateSpalteID {}", templateSpalteId);

		TemplateSpalte templateSpalte = templateSpalteService.getReferenceById(templateSpalteId);
		model.addAttribute("templateSpalte", templateSpalte);
		model.addAttribute("templateId", templateId);

		return new ModelAndView("templatespalte/view", model.asMap());
	}

	@PostMapping("/datentemplate/{templateId}/bearbeiten/{templateSpalteId}")
	public ModelAndView updateSpaltenzuordnung(@PathVariable Integer templateId, @PathVariable int templateSpalteId,
			@ModelAttribute TemplateSpalte templateSpalte, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Bearbeien der Spaltenzuordnung zu templateSpalteID {}", templateSpalteId);

		templateSpalte = templateSpalteService.update(templateSpalte);

		model.addAttribute("templateSpalte", templateSpalte);
		model.addAttribute("templateId", templateId);

		return new ModelAndView("templatespalte/view", model.asMap());
	}

	@GetMapping("/datentemplate/new")
	public ModelAndView view(Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("New template {}");
		DatenTemplate template;
		List<Spaltendefinition> alleSpalten;
		template = new DatenTemplate();
		alleSpalten = spaltendefinitionService.findAll();

		model.addAttribute("template", template);
		model.addAttribute("alleSpalten", alleSpalten);

		return new ModelAndView("datentemplate/new", model.asMap());
	}

	@PostMapping("/datentemplate/new")
	public ModelAndView saveNewTemplate(@ModelAttribute DatenTemplate template, Model model, HttpServletRequest request,
			HttpSession session) {
		log.info("POST /datentemplate von neuem DatenTemplate {}");

		datenTemplateService.speicherTemplate(template);

		return new ModelAndView("redirect:/datentemplate?filtertext=" + template.getName());
	}

	@Override
	@ExceptionHandler({ RuntimeException.class })
	public ModelAndView x_handleException(Exception e, WebRequest webRequest, Model model) {
		log.error("Fehler: {}", e.getMessage());
		log.error("ContextPath -> {}", webRequest.getContextPath());
		log.error("Description -> {}", webRequest.getDescription(true));

		Iterator<String> it = webRequest.getHeaderNames();
		while (it.hasNext()) {
			String header = it.next();
			log.error("{} -> {}", header, webRequest.getHeaderValues(header));
		}

		model.addAttribute("url", "/datentemplate");
		return new ModelAndView("fehler", model.asMap());
	}
}
