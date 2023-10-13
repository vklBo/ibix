/*
 * IbixController.java 
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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import de.hsbo.ibix.exception.UserNotificationException;

/**
 * The Class IbixController.
 */
public abstract class IbixController {

	final static Logger log = LoggerFactory.getLogger(IbixController.class);

	@ExceptionHandler({ Exception.class })
	public ModelAndView x_handleException(Exception e, WebRequest webRequest, Model model) {
		log.error("Fehler: {}", e.getMessage());
		log.error("ContextPath -> {}", webRequest.getContextPath());
		log.error("Description -> {}", webRequest.getDescription(true));

		Iterator<String> it = webRequest.getHeaderNames();
		while (it.hasNext()) {
			String header = it.next();
			log.error("{} -> {}", header, webRequest.getHeaderValues(header));
		}

		if (e instanceof UserNotificationException une) {
			model.addAttribute("msg", une.getMessage());

			if (une.getUrl() != null) {
				model.addAttribute("ziel", une.getUrl());
			}
		}
		return new ModelAndView("fehler", model.asMap());
	}
}
