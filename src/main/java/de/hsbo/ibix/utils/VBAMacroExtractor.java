/*
 * VBAMacroExtractor.java 
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VBAMacroExtractor.
 */
public class VBAMacroExtractor {

	final static Logger log = LoggerFactory.getLogger(VBAMacroExtractor.class);

	public static void main(String[] args) throws Exception {

		Map<String, Charset> o = Charset.availableCharsets();

		for (Map.Entry<String, Charset> entry : o.entrySet()) {
			System.out.println(entry.getKey() + entry.getValue());
		}

		File input = new File("/Users/vk/Downloads/Mappe1.xlsm");
		System.out.println(new VBAMacroExtractor().extract(input));
		input = new File("/Users/vk/Downloads/Mappe2.xlsm");
		new VBAMacroExtractor().extract(input);
		input = new File("/Users/vk/Downloads/Mappe3.xlsm");
		new VBAMacroExtractor().extract(input);
		input = new File("/Users/vk/Upload/HASHCODE/loesung_110.xlsm");
		new VBAMacroExtractor().extract(input);
		input = new File("/Users/vk/Upload/HASHCODE/aufgabe_110.xlsm");
		new VBAMacroExtractor().extract(input);
		input = new File("/Users/vk/Downloads/aufgabe3_3.xlsm");
		new VBAMacroExtractor().extract(input);

		System.out.println(new VBAMacroExtractor().extract(input));
	}

	/**
	 * Extracts the VBA modules from a macro-enabled office file and returns it as
	 * String
	 *
	 * @param input the macro-enabled office file.
	 * @since 3.15-beta2
	 */
	public String extract(File input) throws IOException {
		if (!input.exists()) {
			throw new FileNotFoundException(input.toString());
		}

		log.info("Filename: {}", input.getAbsolutePath());

		final Map<String, String> macros;
		StringBuffer gesamterCode = new StringBuffer();

		try (VBAMacroReader reader = new VBAMacroReader(input)) {
			macros = reader.readMacros();
		}

		for (Entry<String, String> entry : macros.entrySet()) {
			String moduleName = entry.getKey();
			String moduleCode = entry.getValue();

			gesamterCode.append(String.format("' Module: %s\n", moduleName));
			gesamterCode.append(moduleCode);
			gesamterCode.append("\n");
		}

		return gesamterCode.toString();
	}
}
