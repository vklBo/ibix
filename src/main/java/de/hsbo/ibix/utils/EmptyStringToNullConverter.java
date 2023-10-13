/*
 * EmptyStringToNullConverter.java 
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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * The Class EmptyStringToNullConverter.
 */
@Converter(autoApply = true)
public class EmptyStringToNullConverter implements AttributeConverter<String, String> {

	@Override
	public String convertToDatabaseColumn(String string) {
		// Use defaultIfEmpty to preserve Strings consisting only of whitespaces
		return ((string == null || string.trim() == "") ? null : string);
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		// If you want to keep it null otherwise transform to empty String
		return ((dbData == null || dbData.trim() == "") ? "" : dbData);
	}
}