/*
 * ConfigRestaufgaben.java 
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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class ConfigRestaufgaben.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConfigRestaufgaben {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	String prozedurname;
	String allgemeinerText;
	String subtypen;
	String spalten;
	String bedingungsspalten;

	public ConfigRestaufgaben(ConfigRestaufgaben original) {
		this.prozedurname = original.getProzedurname();
		this.allgemeinerText = original.getAllgemeinerText();
		this.subtypen = original.getSubtypen();
		this.spalten = original.getSpalten();
		this.bedingungsspalten = original.getBedingungsspalten();
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
		return id != null && id.equals(((ConfigRestaufgaben) obj).id);
	}

	@Override
	public int hashCode() {
		return 2022;
	}
}
