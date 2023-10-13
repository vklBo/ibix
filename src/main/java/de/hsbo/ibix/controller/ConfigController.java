/*
 * ConfigController.java 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.gwt.thirdparty.json.JSONException;

import de.hsbo.ibix.model.AufgabenTemplate;
import de.hsbo.ibix.model.ConfigFunktionen;
import de.hsbo.ibix.model.ConfigRestaufgaben;
import de.hsbo.ibix.service.AufgabenTemplateService;
import de.hsbo.ibix.service.SpaltendefinitionService;
import de.hsbo.ibix.service.TemplateSpalteService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The Class ConfigController.
 */
@Controller
public class ConfigController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(ConfigController.class);

	@Autowired
	AufgabenTemplateService templateService;

	@Autowired
	SpaltendefinitionService configFunktionenService;

	@Autowired
	TemplateSpalteService templateSpalteService;

	@GetMapping("/configFunktionen/{templateId}")
	public ModelAndView viewConfigFunktionen(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) {
		log.info("View Konfiguration Funktionen für Template {}", templateId);
		ConfigFunktionen config;
		AufgabenTemplate template = templateService.getReferenceById(templateId);

		config = template.getConfigFunktionen();
		if (config == null) {
			config = template.neueConfigFunktionen();
		}
		model.addAttribute("configFunktionen", config);
		model.addAttribute("templateId", templateId);
		model.addAttribute("activeNavLink", "aufgabentemplate");

		return new ModelAndView("aufgabentemplate/configfunktionen", model.asMap());
	}

	@PostMapping("/configFunktionen/{templateId}")
	public ModelAndView saveConfigFunktionen(@PathVariable Integer templateId,
			@ModelAttribute ConfigFunktionen configFunktionen, Model model, HttpServletRequest request) {
		log.info("Post Konfiguration Funktionen für Template {}", templateId);
		AufgabenTemplate template;

		template = templateService.getReferenceById(templateId);
		templateService.saveConfigFunktionen(template, configFunktionen);

		return new ModelAndView("redirect:/configFunktionen/" + templateId, model.asMap());
	}

	@GetMapping("/configFunktionen/delete/{templateId}")
	public ModelAndView deleteConfigFunktionen(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) {
		log.info("Lösche Konfiguration Funktionen für Template {}", templateId);
		templateService.loescheConfigFunktionen(templateId);

		return new ModelAndView("redirect:/aufgabentemplate/" + templateId + "#teilaufgaben", model.asMap());
	}

	@GetMapping("/configStatistiken/{templateId}")
	public ModelAndView viewConfigStatistiken(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) {
		log.info("View Konfiguration Statistiken für Template {}", templateId);
		ConfigRestaufgaben config;
		AufgabenTemplate template = templateService.getReferenceById(templateId);

		config = template.getConfigStatistiken();
		if (config == null) {
			config = template.neueConfigStatistiken();
		}
		model.addAttribute("configStatistiken", config);
		model.addAttribute("templateId", templateId);
		model.addAttribute("activeNavLink", "aufgabentemplate");

		return new ModelAndView("aufgabentemplate/configstatistiken", model.asMap());
	}

	@PostMapping("/configStatistiken/{templateId}")
	public ModelAndView saveConfigStatistiken(@PathVariable Integer templateId,
			@ModelAttribute ConfigRestaufgaben config, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException, JSONException {
		log.info("Post Konfiguration Statistiken für Template {}", templateId);
		AufgabenTemplate template;

		template = templateService.getReferenceById(templateId);
		templateService.saveConfigStatistiken(template, config);

		return new ModelAndView("redirect:/configStatistiken/" + templateId, model.asMap());
	}

	@GetMapping("/configStatistiken/delete/{templateId}")
	public ModelAndView deleteConfigStatistiken(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) {
		log.info("Lösche Konfiguration Statistiken für Template {}", templateId);
		templateService.loescheConfigStatistiken(templateId);

		return new ModelAndView("redirect:/aufgabentemplate/" + templateId + "#teilaufgaben", model.asMap());
	}

	@GetMapping("/configPlausi/{templateId}")
	public ModelAndView viewConfigPlausi(@PathVariable Integer templateId, Model model, HttpServletRequest request) {
		log.info("View Konfiguration Plausi für Template {}", templateId);
		ConfigRestaufgaben config;
		AufgabenTemplate template = templateService.getReferenceById(templateId);

		config = template.getConfigPlausi();
		if (config == null) {
			config = template.neueConfigPlausi();
		}
		model.addAttribute("configPlausi", config);
		model.addAttribute("templateId", templateId);
		model.addAttribute("activeNavLink", "aufgabentemplate");

		return new ModelAndView("aufgabentemplate/configplausi", model.asMap());
	}

	@PostMapping("/configPlausi/{templateId}")
	public ModelAndView saveConfigPlausi(@PathVariable Integer templateId, @ModelAttribute ConfigRestaufgaben config,
			Model model, HttpServletRequest request) {
		log.info("Post Konfiguration Plausi für Template {}", templateId);
		AufgabenTemplate template;

		template = templateService.getReferenceById(templateId);
		templateService.saveConfigPlausi(template, config);

		return new ModelAndView("redirect:/configPlausi/" + templateId, model.asMap());
	}

	@GetMapping("/configPlausi/delete/{templateId}")
	public ModelAndView deleteConfigSPlausi(@PathVariable Integer templateId, Model model, HttpServletRequest request) {
		log.info("Lösche Konfiguration Plausi für Template {}", templateId);
		templateService.loescheConfigPlausi(templateId);

		return new ModelAndView("redirect:/aufgabentemplate/" + templateId + "#teilaufgaben", model.asMap());
	}

	@GetMapping("/configFormatierung/{templateId}")
	public ModelAndView viewConfigFormatierung(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) {
		log.info("View Konfiguration Formatierung für Template {}", templateId);
		ConfigRestaufgaben config;
		AufgabenTemplate template = templateService.getReferenceById(templateId);

		config = template.getConfigFormatierung();
		if (config == null) {
			config = template.neueConfigFormatierung();
		}
		model.addAttribute("configFormatierung", config);
		model.addAttribute("templateId", templateId);
		model.addAttribute("activeNavLink", "aufgabentemplate");

		return new ModelAndView("aufgabentemplate/configformatierung", model.asMap());
	}

	@PostMapping("/configFormatierung/{templateId}")
	public ModelAndView saveConfigFormatierung(@PathVariable Integer templateId,
			@ModelAttribute ConfigRestaufgaben config, Model model, HttpServletRequest request) {
		log.info("Post Konfiguration Formatierung für Template {}", templateId);
		AufgabenTemplate template;

		template = templateService.getReferenceById(templateId);
		templateService.saveConfigFormatierung(template, config);

		return new ModelAndView("redirect:/configFormatierung/" + templateId, model.asMap());
	}

	@GetMapping("/configFormatierung/delete/{templateId}")
	public ModelAndView deleteConfigSFormatierung(@PathVariable Integer templateId, Model model,
			HttpServletRequest request) {
		log.info("Lösche Konfiguration Formatierung für Template {}", templateId);
		templateService.loescheConfigFormatierung(templateId);

		return new ModelAndView("redirect:/aufgabentemplate/" + templateId + "#teilaufgaben", model.asMap());
	}
}
