/*
 * AllgemeinController.java 
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
import org.springframework.web.servlet.ModelAndView;

import de.hsbo.ibix.security.IbixScope;
import de.hsbo.ibix.service.UserService;

/**
 * The Class AllgemeinController.
 */
@Controller
public class AllgemeinController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(AllgemeinController.class);

	@Autowired
	UserService userService;

	@Autowired
	IbixScope ibixScope;

	@GetMapping("/")
	public ModelAndView start(Model model) {
//		String username = ibixScope.getUserdetails().getUsername();
//		Optional<User> user = userService.findByUsername(username);

		// TODO:
		return new ModelAndView("redirect:/bearbeitung", model.asMap());
		/*
		 * if (user.isPresent() && user.get().getDatenschutzZugestimmt() != null &&
		 * user.get().getDatenschutzZugestimmt()) { return new
		 * ModelAndView("redirect:/bearbeitung", model.asMap()); }
		 *
		 *
		 *
		 * return new ModelAndView("datenschutz", model.asMap());
		 */
	}

	@GetMapping("/datenschutzbestaetigen")
	public String bestaetigen() {
		String username = ibixScope.getUserdetails().getUsername();
		userService.datenschutzbestaetigen(username);

		return "redirect:/bearbeitung";
	}

	@GetMapping("/datenschutz")
	public ModelAndView datenschutz(Model model) {
		return new ModelAndView("datenschutz", model.asMap());
	}

	@GetMapping("/impressum")
	public ModelAndView impressum(Model model) {
		return new ModelAndView("impressum", model.asMap());
	}
}
