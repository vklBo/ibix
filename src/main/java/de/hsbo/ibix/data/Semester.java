/*
 * Semester.java 
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
import lombok.Setter;

/**
 * The Class Semester.
 */
@Getter
@Setter
public class Semester {

	/**
	 * The Enum sowi.
	 */
	enum sowi {
		SOMMER, WINTER
	}

	int jahr;
	sowi sommerwinter;

	public Semester(LocalDateTime datetime) {
		int jahrDesDatums = datetime.getYear();
		int monat = datetime.getMonthValue();

		this.jahr = jahrDesDatums;

		if (monat >= 3 && monat <= 8) {
			sommerwinter = sowi.SOMMER;
		} else {
			sommerwinter = sowi.WINTER;
			if (monat <= 2) {
				this.jahr = jahrDesDatums - 1;
			}
		}
	};

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Semester.class) {
			return false;
		}

		return (this.jahr == ((Semester) obj).getJahr()
				&& this.sommerwinter.equals(((Semester) obj).getSommerwinter()));
	}

	/**
	 * Berechnet die Anzahl Semester, die das übergebene Objekt vor dem Objekt
	 * selbst liegt als positive Zahl
	 * 
	 * @param obj
	 * @return Anzahl der Semester, die das übergebene Objekt vor dem Objekt selbst
	 *         liegt
	 */
	public int differenz(Semester obj) {
		int abstand;

		abstand = (this.jahr - obj.getJahr()) * 2;
		if (this.sommerwinter.equals(sowi.WINTER) && obj.getSommerwinter().equals(sowi.SOMMER)) {
			abstand += 1;
		} else if (this.sommerwinter.equals(sowi.SOMMER) && obj.getSommerwinter().equals(sowi.WINTER)) {
			abstand -= 1;
		}

		return abstand;
	}

	/**
	 * @return Datum, zu dem das Semester startet
	 */
	public LocalDateTime getStart() {
		if (this.sommerwinter.equals(sowi.SOMMER)) {
			return (LocalDateTime.of(this.jahr, 3, 1, 0, 0));
		}

		return (LocalDateTime.of(this.jahr, 9, 1, 0, 0));
	}

	/**
	 * @param anzahl
	 * @return Gibt das Semester zurück, dass anzahl Semester vor diesem Semester
	 *         liegt
	 */
	public Semester vorsemester(int anzahl) {
		return (new Semester(this.getStart().minusMonths(anzahl * 6)));
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (sommerwinter.equals(sowi.SOMMER)) {
			result.append("So ");

		} else {
			result.append("Wi ");
		}

		result.append(Integer.toString(jahr).substring(2));

		if (sommerwinter.equals(sowi.WINTER)) {
			result.append("/");
			result.append(Integer.toString(jahr + 1).substring(2));
		}
		return result.toString();
	}
}