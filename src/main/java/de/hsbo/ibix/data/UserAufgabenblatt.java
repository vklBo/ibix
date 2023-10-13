/*
 * UserAufgabenblatt.java 
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

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

import de.hsbo.ibix.model.Aufgabenblatt;
import de.hsbo.ibix.model.Bearbeitung;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class UserAufgabenblatt.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserAufgabenblatt {
	Aufgabenblatt aufgabenblatt;
	List<Bearbeitung> bearbeitungsliste;
	Integer prozent;
	Integer bonuspunkte;
	Bonuspunktinfo bonuspunktinfo;

	public UserAufgabenblatt(Aufgabenblatt aufgabenblatt, List<Bearbeitung> bearbeitungsliste) {
		super();
		this.aufgabenblatt = aufgabenblatt;
		this.bearbeitungsliste = bearbeitungsliste;
	}

	public Integer getProzent() {
		if (this.prozent == null) {
			OptionalInt max = bearbeitungsliste.stream().filter(b -> (b.isAktuell() && b.getProzent() != null))
					.mapToInt(b -> b.getProzent()).max();
			if (max.isPresent()) {
				this.prozent = max.getAsInt();
			}
		}
		return this.prozent;
	}

	public Integer getBonuspunkte() {
		if (this.bonuspunkte == null) {
			OptionalInt max = bearbeitungsliste.stream().filter(b -> (b.isAktuell() && b.getBonuspunkte() != null))
					.mapToInt(b -> b.getBonuspunkte()).max();
			if (max.isPresent()) {
				this.bonuspunkte = max.getAsInt();
			}
		}
		return this.bonuspunkte;
	}
	
	public Bonuspunktinfo getBonuspunktinfo() {
		if (this.bonuspunktinfo == null) {
			Bearbeitung tmpMax = null;
			
			Iterator<Bearbeitung> iter = bearbeitungsliste.iterator();
			
			while (iter.hasNext()){
				Bearbeitung bearbeitung = iter.next();
				if (bearbeitung.isAktuell() && bearbeitung.getBonuspunkte() != null) {
					if (tmpMax == null || tmpMax.getBonuspunkte() < bearbeitung.getBonuspunkte()) {
						tmpMax = bearbeitung;
					}
				}
			}
			
			//Aufgabenblattnr, Typ?, Semester, Punkte (Link auf Bearbeitung?)
			if (tmpMax != null) {
				this.bonuspunktinfo = new Bonuspunktinfo(tmpMax.getAufgabenblatt().getNr(), tmpMax.getAbgabe(), tmpMax.getBonuspunkte(), tmpMax.getId());
		
			}
		}
		return this.bonuspunktinfo;
	}
}