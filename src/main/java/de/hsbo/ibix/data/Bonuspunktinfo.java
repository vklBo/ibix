/*
 * Bonuspunktinfo.java 
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

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class Bonuspunktinfo.
 */
@Getter
@Setter
@NoArgsConstructor
public class Bonuspunktinfo {
	int punkte;
	int aufgabenblattNr;
	int bearbeitungId;
	Semester semester;
	LocalDateTime timestamp;

	public Bonuspunktinfo(Integer aufgabenblattNr, LocalDateTime abgabe, Integer bonuspunkte, Integer  bearbeitungId) {
		super();
		this.punkte = bonuspunkte;
		this.timestamp = abgabe;
		this.semester = new Semester(timestamp);
		this.aufgabenblattNr = aufgabenblattNr;
		this.bearbeitungId = bearbeitungId;
	}
	
	@Override
	public String toString() {
		return semester.toString() + ": " + punkte + "(" + aufgabenblattNr + ", " + bearbeitungId + ")";
	}
}