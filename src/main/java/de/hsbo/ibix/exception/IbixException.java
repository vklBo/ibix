/*
 * IbixException.java 
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

package de.hsbo.ibix.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class IbixException.
 */
@Getter
@Setter
public class IbixException extends RuntimeException {
	String url;

	private static final long serialVersionUID = 1L;

	public IbixException(String message) {
		super(message);
		this.url = null;
	}

	public IbixException(String message, String url) {
		super(message);
		this.url = url;
	}

	public IbixException(String message, Throwable cause) {
		super(message, cause);
	}

	public IbixException(String message, Throwable cause, String url) {
		super(message, cause);
		this.url = url;
	}
}
