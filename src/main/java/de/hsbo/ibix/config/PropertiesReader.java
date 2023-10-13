/*
 * PropertiesReader.java 
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class PropertiesReader.
 */
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "ibix")
public class PropertiesReader {

	private String generatorUri;
	private String excelPath;

	private String pythonPath;

	private String generatorPath;
	private String bewertungPythonFileName;

	private String ldapBase;
	private String ldapUrl;
	private String ldapUsername;
	private String ldapPassword;

	public String getGeneratorPath() {
		if (!generatorPath.endsWith("/")) {
			generatorPath = generatorPath.concat("/");
		}
		return generatorPath;
	}
}
