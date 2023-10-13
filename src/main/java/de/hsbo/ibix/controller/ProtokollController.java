/*
 * ProtokollController.java 
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.hsbo.ibix.model.Protokoll;
import de.hsbo.ibix.service.ProtokollService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class ProtokollController.
 */
@Controller
public class ProtokollController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(ProtokollController.class);

	@Autowired
	ProtokollService protokollService;

	@GetMapping("/protokoll")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) {
		if (filtertext == null) {
			filtertext = (String) session.getAttribute("protokoll_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}

		List<Protokoll> protokolldaten = protokollService.findByFiltertext(filtertext);

		model.addAttribute("protokolldaten", protokolldaten);
		model.addAttribute("activeNavLink", "protokoll");
		model.addAttribute("filtertext", filtertext);
		session.setAttribute("protokoll_filtertext", filtertext);

		return new ModelAndView("protokoll/index", model.asMap());

	}

	@GetMapping("/protokoll/{protokollId}")
	public ModelAndView view(@PathVariable Integer protokollId, Model model, HttpServletRequest request) {
		Protokoll protokoll = protokollService.findById(protokollId);

		model.addAttribute("protokoll", protokoll);
		model.addAttribute("activeNavLink", "protokoll");

		return new ModelAndView("protokoll/view", model.asMap());
	}
}
