/*
 * IbixPermissionEvaluator.java 
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

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.Bearbeitung;

/**
 * The Class IbixPermissionEvaluator.
 */
@Component
public class IbixPermissionEvaluator implements PermissionEvaluator {
	final static Logger log = LoggerFactory.getLogger(IbixPermissionEvaluator.class);

	@Autowired
	IbixScope ibixscope;

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		String userhash = ibixscope.getUserdetails().getHashcode();
		boolean userIsAdmin;
		Aufgabe aufgabe = null;
		Bearbeitung bearbeitung = null;

		userIsAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));

		if (targetDomainObject.getClass().getSimpleName().startsWith("Aufgabe")) {
			aufgabe = (Aufgabe) targetDomainObject;
			bearbeitung = aufgabe.getBearbeitung();
		}
		if (targetDomainObject.getClass().getSimpleName().startsWith("Bearbeitung")) {
			bearbeitung = (Bearbeitung) targetDomainObject;
		}

		if (bearbeitung != null && !bearbeitung.getBenutzerHash().equals(userhash)) {
			if (userIsAdmin) {
				log.warn("Zugriff auf Bearbeitung {} von Benutzer {} durch ADMIN {}", bearbeitung.getId(),
						bearbeitung.getBenutzerHash(), userhash);
				return true;
			}
			return false;
		}

		return true;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		return false;
	}
}
