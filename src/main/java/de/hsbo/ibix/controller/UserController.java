/*
 * UserController.java 
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import de.hsbo.ibix.model.User;
import de.hsbo.ibix.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * The Class UserController.
 */
@Controller
public class UserController extends IbixController {

	final static Logger log = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;

	@GetMapping("/user")
	public ModelAndView index(@RequestParam(required = false) String filtertext, Model model,
			HttpServletRequest request, HttpSession session) {
		if (filtertext == null) {
			filtertext = (String) session.getAttribute("u_filtertext");
			if (filtertext == null) {
				filtertext = "";
			}
		} else {
			filtertext = filtertext.trim();
		}
		List<User> userlist = userService.findByFilterstring(filtertext);
		log.info("/user -> found {} User for {}", userlist.size(), filtertext);

		model.addAttribute("userlist", userlist);
		model.addAttribute("filtertext", filtertext);
		model.addAttribute("activeNavLink", "user");
		session.setAttribute("u_filtertext", filtertext);

		return new ModelAndView("user/index", model.asMap());
	}

	@GetMapping("/user/{userId}")
	public ModelAndView view(@PathVariable Integer userId, Model model, HttpServletRequest request) {

		log.info("Displaying User {}", userId);
		User user;
		user = userService.findAndUpdateUserById(userId);

		model.addAttribute("user", user);
		model.addAttribute("activeNavLink", "user");

		return new ModelAndView("user/view", model.asMap());
	}

	@PostMapping("/user/{userId}")
	public ModelAndView save(@PathVariable Integer userId, @ModelAttribute User user, Model model,
			HttpServletRequest request) {

		user = userService.updateUser(user);
		log.info("/user/ {} -> gespeichert", user.getUsername());
		// model.addAttribute("user", user);

		// return new ModelAndView("user/view", model.asMap());
		return new ModelAndView("redirect:/user?filtertext=" + user.getHashcode());

	}

	@GetMapping("/user/loadDetails")
	public ModelAndView loadDetail(Model model, HttpServletRequest request) {
		userService.vervollstaendigeUserdaten();

		model.addAttribute("activeNavLink", "user");

		return new ModelAndView("redirect:/user");
	}
}
