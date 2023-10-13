/*
 * IbixScope.java 
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

package de.hsbo.ibix.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * The Class IbixScope.
 */
@Component
@SessionScope
public class IbixScope {
	final static Logger log = LoggerFactory.getLogger(IbixScope.class);

	private IbixUserDetails userdetails;

	@Autowired
	IbixUserDetailsService userdetailsservice;

	public IbixUserDetails getUserdetails() {
		if (userdetails == null) {
			userdetails = userdetailsservice
					.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		}

		return userdetails;
	}

	public void setUserdetails(IbixUserDetails userdetails) {
		log.info("Context: userdetails gesetzt {}", userdetails);
		this.userdetails = userdetails;
	}

}