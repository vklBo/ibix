/*
 * Intervall.java 
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

import de.hsbo.ibix.model.Spaltendefinition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class Intervall.
 */
@Getter
@Setter
@NoArgsConstructor
public class Intervall {
	public boolean untenOffen;
	public boolean obenOffen;
	public float untergrenze;
	public float obergrenze;
	public int stellen;

	public Intervall(boolean untenOffen, boolean obenOffen, float untergrenze, float obergrenze, int stellen) {
		super();
		this.untenOffen = untenOffen;
		this.obenOffen = obenOffen;
		this.untergrenze = untergrenze;
		this.obergrenze = obergrenze;
		this.stellen = stellen;
	}

	public Intervall(String intervalltext) {
		Pattern pattern = Pattern.compile(Spaltendefinition.intervallpattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(intervalltext);

		if (matcher.find()) {
			untenOffen = (matcher.group(1) == "(");
			untergrenze = Float.parseFloat(matcher.group(2));
			obergrenze = Float.parseFloat(matcher.group(3));
			obenOffen = (matcher.group(4) == ")");
			stellen = Integer.parseInt(matcher.group(5));
		} else {
			new RuntimeException("Intervall kann nicht geparsed werden.");
		}
	}

	@Override
	public String toString() {
		StringBuffer erg = new StringBuffer();
		erg.append(untenOffen ? "(" : "[");
		erg.append(untergrenze);
		erg.append(", ");
		erg.append(obergrenze);
		erg.append(obenOffen ? ")" : "]");
		erg.append(", ");
		erg.append(stellen);
		return erg.toString();
	}
}