/*
 * Bearbeitung.java 
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

package de.hsbo.ibix.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.hsbo.ibix.data.Semester;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class Bearbeitung.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bearbeitung {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;
	LocalDateTime ausgabe;
	LocalDateTime abgabe;
	LocalDateTime spaetesteAbgabe;
	Integer prozent;
	Integer bonuspunkte;
	String benutzerHash;

	@Transient
	Boolean loesungVorhanden = false;

	@ManyToOne
	Aufgabenblatt aufgabenblatt;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "bearbeitung", orphanRemoval = true, fetch = FetchType.EAGER)
	List<Aufgabe> aufgaben = new ArrayList<>();

	public Bearbeitung(Bearbeitung original) {
		this.ausgabe = original.getAusgabe();
		this.abgabe = original.getAbgabe();
		this.spaetesteAbgabe = original.getSpaetesteAbgabe();
		this.bonuspunkte = original.getBonuspunkte();
		this.prozent = original.getProzent();
		this.benutzerHash = original.getBenutzerHash();
	}

	public void addAufgabe(Aufgabe aufgabe) {
		this.aufgaben.add(aufgabe);
		aufgabe.setBearbeitung(this);
	}

	public void removeAufgabe(Aufgabe aufgabe) {
		aufgabe.setBearbeitung(null);
		this.aufgaben.remove(aufgabe);
	}

	@Transient
	public boolean isVerspaetet() {
		boolean verspaetet = false;
		if (getSpaetesteAbgabe() != null) {
			if (getAbgabe() != null) {
				verspaetet = getAbgabe().isAfter(getSpaetesteAbgabe());
			} else {
				verspaetet = getSpaetesteAbgabe().isBefore(LocalDateTime.now());
			}
		}
		return verspaetet;
	}

	@Transient
	public boolean isAbgelaufen() {
		boolean abgelaufen = false;
		if (getSpaetesteAbgabe() != null) {
			abgelaufen = getSpaetesteAbgabe().isBefore(LocalDateTime.now());
		}

		return abgelaufen;
	}

	@Transient
	public boolean isAktuell() {
		if (this.getAusgabe() == null) {
			return false;
		}
		Semester aktuellesSemester = new Semester(LocalDateTime.now());
		Semester bearbeitungssemester = new Semester(this.getAusgabe());
		int differenz = aktuellesSemester.differenz(bearbeitungssemester);
		return (differenz < 2);
	}

	public Integer getBonuspunkte() {
		if (this.bonuspunkte != null && this.isAktuell() && this.aufgabenblatt.getKlausur()) {
			return this.bonuspunkte;
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return id != null && id.equals(((Bearbeitung) obj).id);
	}

	@Override
	public int hashCode() {
		return 2021;
	}
}
