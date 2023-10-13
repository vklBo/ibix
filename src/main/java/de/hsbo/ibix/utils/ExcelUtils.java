/*
 * ExcelUtils.java 
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * The Class ExcelUtils.
 */
public class ExcelUtils {

	public static void copySheet(Sheet src, Sheet dst) {

		int rowStart = src.getFirstRowNum();
		int rowEnd = src.getLastRowNum();

		for (int row = rowStart; row <= rowEnd; row++) {

			Row r = src.getRow(row);
			Row d = dst.createRow(row);

			for (int col = r.getFirstCellNum(); col <= r.getLastCellNum(); col++) {

				Cell cell = r.getCell(col);
				if (cell != null) {
					Cell target = d.createCell(col);
					copyCell(cell, target);
				}
			}
		}
	}

	private static void copyCell(Cell src, Cell dst) {
		if (src.getCellType() == CellType.FORMULA) {
			dst.setCellFormula(src.getCellFormula());
			return;
		}

		if (src.getCellStyle() != null) {
//			dst.setCellStyle(src.getCellStyle());
			dst.getCellStyle().cloneStyleFrom(src.getCellStyle());
		}

		if (src.getCellType() == CellType.NUMERIC) {
			dst.setCellValue(src.getNumericCellValue());
		} else if (src.getStringCellValue() != null) {
			dst.setCellValue(src.getStringCellValue());
		}
	}
}
