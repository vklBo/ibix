/*
 * LoggerInterceptor.java 
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

package de.hsbo.ibix.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import de.hsbo.ibix.security.IbixScope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The Class LoggerInterceptor.
 */
@Component
public class LoggerInterceptor implements HandlerInterceptor {
	private static Logger log = LoggerFactory.getLogger(LoggerInterceptor.class);

	@Autowired
	IbixScope ibixscope;

	@Override
	public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
			throws Exception {
		HttpServletRequest req = (HttpServletRequest) request;
		// HttpServletResponse res = (HttpServletResponse) response;
		String uri = req.getRequestURI();
		if (!(uri.contains("/css") || uri.contains("/bootstrap") || uri.contains("/images"))) {
			if (ibixscope != null && ibixscope.getUserdetails() != null) {
				log.info("User {}: Request  {} : {}", ibixscope.getUserdetails().getHashcode(), req.getMethod(), uri);
			} else {
				log.info("Request  {} : {} ohne Userdetails", req.getMethod(), uri);
			}
		}
		return true;
	}
}
