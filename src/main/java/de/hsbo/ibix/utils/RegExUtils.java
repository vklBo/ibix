/*
 * RegExUtils.java 
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

package de.hsbo.ibix.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RegExUtils.
 */
public class RegExUtils {

	public final static Logger log = LoggerFactory.getLogger(RegExUtils.class);

	public static void main(String[] args) {
		System.out.println(RegExUtils.getTagsFromString("#aössdf asdf asdf #js_fs #jdskf"));
	}

	public static Set<String> getTagsFromString(String text) {
		Set<String> tags = new HashSet<>();

		if (text == null) {
			return tags;
		}

		Pattern pattern = Pattern.compile("#[A-Z0-9ÜÄÖa-züäöß_]*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);

		while (matcher.find()) {
			tags.add(matcher.group());
		}
		return tags;
	}

	public static String pruefeDateiNamen(String dateiname) {
		// Liste ungültiger zeichen
		String ungueltigeZeichen = "[^a-zA-Z0-9_]";
		dateiname = dateiname.replaceAll(ungueltigeZeichen, "");
		log.debug("Erzeugter Dateiname: {}", dateiname);
		return dateiname;
	}
}
