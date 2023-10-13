/*
 * Formatierung.java 
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

package de.hsbo.ibix.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class Formatierung.
 */
@Getter
@Setter
@NoArgsConstructor
public class Formatierung {
	public boolean zahl = false;
	public boolean text = false;
	public int stellen = 2;

	public Formatierung(String formatstring) {
		Pattern pattern = Pattern.compile("\\{:s", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(formatstring);
		if (matcher.find()) {
			text = true;
		}

		pattern = Pattern.compile("\\{:(\\d?)\\.(\\d)+f", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(formatstring);
		if (matcher.find()) {
			zahl = true;
			stellen = Integer.parseInt(matcher.group(2));
		}
	}

	@Override
	public String toString() {
		if (text) {
			return ("{\"format\": \"{:s}\"}");
		}
		if (zahl) {
			return String.format("{\"format\": \"{:.%1df}\"}", stellen);
		}
		return "";
	}

}