/*
 * User.java 
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

import de.hsbo.ibix.data.Semester;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class User.
 */
@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	String username;
	String hashcode;

	String vorname;
	String nachname;
	String email;
	String matrikelnummer;
	Boolean datenschutzZugestimmt;
	LocalDateTime lastlogin;
	LocalDateTime bonuspunktabruf;
	Integer schreibverlaengerung = 0;

	public void setSchreibverlaengerung(Integer prozent) {
		if (prozent == null) {
			this.schreibverlaengerung = 0;
		} else {
			this.schreibverlaengerung = prozent;
		}
	}

	@Transient
	public String getSemesterLogin() {
		Semester sem = new Semester(this.getLastlogin());
		return sem.toString();
	}
}
