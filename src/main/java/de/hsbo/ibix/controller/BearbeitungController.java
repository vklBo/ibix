/*
 * BearbeitungController.java 
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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

import de.hsbo.ibix.data.UserAufgabenblatt;
import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.Bearbeitung;
import de.hsbo.ibix.service.AufgabenblattService;
import de.hsbo.ibix.service.BearbeitungService;
import de.hsbo.ibix.service.FileService;
import de.hsbo.ibix.utils.ExcelUtils;
import de.hsbo.ibix.utils.RegExUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The Class BearbeitungController.
 */
@Controller
public class BearbeitungController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(BearbeitungController.class);

	@Autowired
	BearbeitungService bearbeitungService;

	@Autowired
	AufgabenblattService aufgabenblattService;

	@Autowired
	FileService fileService;

	@GetMapping("/bearbeitung")
	public ModelAndView index(Model model, HttpServletRequest request) {
		List<UserAufgabenblatt> userAufgabenblaetter = bearbeitungService.ermittleUserAufgabeblaetter();

		model.addAttribute("aufgabenblaetter", userAufgabenblaetter);
		model.addAttribute("activeNavLink", "bearbeitung");

		return new ModelAndView("bearbeitung/index", model.asMap());
	}

	@GetMapping("/bearbeitung/{bearbeitungId}")
	public ModelAndView bearbeitungAnzeigen(@PathVariable Integer bearbeitungId,
			@RequestParam(required = false) String message, Model model, HttpServletRequest request)
			throws IOException {
		Bearbeitung bearbeitung = bearbeitungService.findById(bearbeitungId);
		log.info("/bearbeitung mit ID {} found", bearbeitung.getId());

		bearbeitung.setLoesungVorhanden(fileService.existiertLoesung(bearbeitung.getId()));

		HashMap<Integer, String> details = bearbeitungService.ermittleLoesungsdetails(bearbeitung);

		model.addAttribute("bearbeitung", bearbeitung);
		model.addAttribute("details", details);
		model.addAttribute("activeNavLink", "bearbeitung");

		return new ModelAndView("bearbeitung/view", model.asMap());
	}

	@GetMapping("/bearbeitung/{bearbeitungId}/LoesungsVorlage.xlsm")
	@ResponseBody
	public ByteArrayResource bearbeitungLoesungsvorlage(@PathVariable Integer bearbeitungId,
			@RequestParam(required = false) String message, Model model, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		log.info("Erzeuge LoesungsVorlage.xlsm...");
		Bearbeitung bearbeitung = bearbeitungService.findById(bearbeitungId);
		log.info("/bearbeitung mit ID {} found", bearbeitung.getId());

		Workbook wb = WorkbookFactory
				.create(BearbeitungController.class.getResourceAsStream("/empty-LoesungsVorlage.xlsm"));
		int idx = 0;
		for (Aufgabe aufgabe : bearbeitung.getAufgaben()) {
			Sheet sheet = null;

			if (idx < wb.getNumberOfSheets()) {
				sheet = wb.getSheetAt(idx);
				wb.setSheetName(idx, "Aufgabe " + aufgabe.getNr());
			} else {
				sheet = wb.createSheet("Aufgabe " + aufgabe.getNr());
			}

			File excelFile = fileService.loadExcel(aufgabe.getGeneratorAufgabenID());
			Workbook src = WorkbookFactory.create(excelFile);
			ExcelUtils.copySheet(src.getSheetAt(0), sheet);
			src.close();
			idx++;
		}

		String fileName = "Loesungsvorlage-Aufgabenblatt-" + bearbeitung.getAufgabenblatt().getNr() + ".xlsm";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		wb.close();

		model.addAttribute("bearbeitung", bearbeitung);
		model.addAttribute("activeNavLink", "bearbeitung");

		response.setContentType("MediaTypapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s.xlsm", fileName));
		return new ByteArrayResource(baos.toByteArray());
	}

	@PostMapping("/bearbeitungOeffnen")
	public String bearbeitungAnzeigen(@RequestParam Integer bearbeitungId) {
		return "redirect:/bearbeitung/" + bearbeitungId;
	}

	@GetMapping("/bearbeitung/new/{aufgabenblattId}")
	public String bearbeitungAnlegen(@PathVariable Integer aufgabenblattId, Model model, HttpServletRequest request)
			throws IOException {
		Bearbeitung bearbeitung = bearbeitungService.erstelleBearbeitung(aufgabenblattId);

		if (bearbeitung == null) {
			return "redirect:/verspaetung";
		}

		log.info("/bearbeitung mit ID {} erstellt for Student {}", bearbeitung.getId());

		return "redirect:/bearbeitung/" + bearbeitung.getId(); // bearbeitungId;
	}

	@PostMapping("/bearbeitung/upload/{bearbeitungId}")
	public String uploadLoesung(@PathVariable Integer bearbeitungId, @RequestParam MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {
		log.info("File Upload für Bearbeitung {}", bearbeitungId);

		Bearbeitung bearbeitung = bearbeitungService.findById(bearbeitungId);
		String nachricht = bearbeitungService.speichereLoesung(bearbeitung, file);

		redirectAttributes.addFlashAttribute("message", nachricht);

		if (nachricht.endsWith("erfolgreich hochgeladen")) {
			bearbeitungService.bewerteLoesung(bearbeitung);
		}

		return "redirect:/bearbeitung/" + bearbeitung.getId(); // bearbeitungId;
	}

	@GetMapping(path = "/bearbeitung/loesungsdatei/{bearbeitungId}", produces = MediaType.APPLICATION_PDF_VALUE)
	@ResponseBody
	public FileSystemResource downloadLoesung(@PathVariable Integer bearbeitungId, HttpServletResponse response)
			throws Exception {
		log.info("Download Lösungsdatei für Bearbeitung {}", bearbeitungId);

		Bearbeitung bearbeitung = bearbeitungService.findById(bearbeitungId);
		File loesungFile = fileService.loadLoesung(bearbeitungId);

		response.setContentType("MediaTypapplication/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s.xlsm",
				RegExUtils.pruefeDateiNamen(bearbeitung.getAufgabenblatt().getName())));
		return new FileSystemResource(loesungFile);
	}

	@GetMapping("/bearbeitung/bewerte/{bearbeitungId}")
	public String bewerteLoesung(@PathVariable Integer bearbeitungId, Model model, HttpServletRequest request)
			throws FileNotFoundException, IOException {

		log.info("Bewerte Bearbeitung {}", bearbeitungId);

		Bearbeitung bearbeitung = bearbeitungService.findById(bearbeitungId);
		bearbeitungService.bewerteLoesung(bearbeitung);

		log.info("Fertig mit Bewertung");

		return "redirect:/bearbeitung/" + bearbeitung.getId(); // bearbeitungId;

	}
}
