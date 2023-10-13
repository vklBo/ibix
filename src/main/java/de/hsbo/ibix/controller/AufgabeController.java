/*
 * AufgabeController.java 
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gwt.thirdparty.json.JSONException;

import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.service.AufgabeService;
import de.hsbo.ibix.service.BearbeitungService;
import de.hsbo.ibix.service.FileService;
import de.hsbo.ibix.utils.RegExUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The Class AufgabeController.
 */
@Controller
@RolesAllowed({ "ROLE_ADMIN" })
public class AufgabeController extends IbixController {

	public final static Logger log = LoggerFactory.getLogger(AufgabeController.class);

	@Autowired
	AufgabeService aufgabeService;

	@Autowired
	BearbeitungService bearbeitungService;

	@Autowired
	FileService fileService;

	@GetMapping("/aufgabe/{aufgabeId}")
	public ModelAndView aufgabeAnzeigen(@PathVariable Integer aufgabeId, @RequestParam(required = false) String message,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException, JSONException {
		log.info("Displaying Aufgabe {}", aufgabeId);

		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);
		aufgabe.setLoesungVorhanden(fileService.existiertLoesung(aufgabe.getBearbeitung().getId()));

		model.addAttribute("aufgabe", aufgabe);
		if (message != null && !message.isBlank()) {
			model.addAttribute("message", message);
		}
		model.addAttribute("aufgabeHtmlCode", fileService.loadHtml(aufgabe.getGeneratorAufgabenID()));
		model.addAttribute("activeNavLink", "bearbeitung");

		return new ModelAndView("aufgabe/view", model.asMap());
	}

	@GetMapping(path = "/aufgabe/pdf/{aufgabeId}", produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public FileSystemResource downloadPdf(@PathVariable Integer aufgabeId, HttpServletResponse response)
			throws Exception {
		log.info("Download PDF für Aufgabe {}", aufgabeId);

		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);

		File pdfFile = fileService.loadPDF(aufgabe.getGeneratorAufgabenID());

		response.setContentType(MediaType.APPLICATION_PDF_VALUE);
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s.pdf",
				RegExUtils.pruefeDateiNamen(aufgabe.getAufgabenTemplate().getName())));
		return new FileSystemResource(pdfFile);
	}

	@GetMapping(path = "/aufgabe/excel/{aufgabeId}")
	@ResponseBody
	public FileSystemResource downloadExcel(@PathVariable Integer aufgabeId, HttpServletResponse response)
			throws IOException {
		log.info("Download Excel für Aufgabe {}", aufgabeId);
		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);

		File excelFile = fileService.loadExcel(aufgabe.getGeneratorAufgabenID());

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s.xlsx",
				RegExUtils.pruefeDateiNamen(aufgabe.getAufgabenTemplate().getName())));
		return new FileSystemResource(excelFile);
	}

	@PostMapping("/aufgabe/upload/{aufgabeId}")
	public String uploadLoesung(@PathVariable Integer aufgabeId, @RequestParam MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {
		log.info("File Upload für Aufgabe {}", aufgabeId);

		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);

		String nachricht = bearbeitungService.speichereLoesung(aufgabe.getBearbeitung(), file);

		redirectAttributes.addFlashAttribute("message", nachricht);

		// TODO: bearbeitungService.bewerteLoesung(bearbeitung);

		return "redirect:/aufgabe/" + aufgabeId;
	}

	@GetMapping(path = "/aufgabe/loesungsdatei/{aufgabeId}", produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public FileSystemResource downloadLoesung(@PathVariable Integer aufgabeId, HttpServletResponse response)
			throws Exception {
		log.info("Download Lösungsdatei für Aufgabe {}", aufgabeId);

		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);
		File loesungFile = fileService.loadLoesung(aufgabe.getBearbeitung().getId());

		response.setContentType("MediaTypapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s.xlsm",
				RegExUtils.pruefeDateiNamen(aufgabe.getBearbeitung().getAufgabenblatt().getName())));
		return new FileSystemResource(loesungFile);
	}

	@GetMapping("/aufgabe/bewerte/{aufgabeId}")
	public ModelAndView bewerteLoesung(@PathVariable Integer aufgabeId, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException {

		log.info("Bewerte Aufgabe {}", aufgabeId);

		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);

		aufgabeService.bewerteLoesung(aufgabe);

		log.info("Fertig mit Bewertung");

		return new ModelAndView("redirect:/aufgabe/" + aufgabeId);
	}

	@GetMapping("/aufgabe/abschliessen/{aufgabeId}/{einstufung}")
	public ModelAndView schliesseAufgabeAb(@PathVariable Integer aufgabeId, @PathVariable Integer einstufung,
			Model model, HttpServletRequest request) throws FileNotFoundException, IOException {

		Aufgabe aufgabe = aufgabeService.findById(aufgabeId);

		return new ModelAndView("redirect:/bearbeitung/" + aufgabe.getBearbeitung().getId());
	}
}
