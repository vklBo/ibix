/*
 * Hinweis.java 
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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Hinweis.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Hinweis {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	String titel;
	@Lob
	@Column(length = 10000)
	String problem;
	@Lob
	@Column(length = 10000)
	String loesung;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentHinweis")
	List<Hinweis> weitereHinweise = new ArrayList<>();

	@ManyToOne
	private Hinweis parentHinweis;

	@ManyToOne
	AufgabenTemplate aufgabenTemplate;

	public Hinweis(Hinweis original) {
		this.titel = original.getTitel();
		this.problem = original.getProblem();
		this.loesung = original.getLoesung();
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
		return id != null && id.equals(((Hinweis) obj).getId());
	}

	@Override
	public int hashCode() {
		return 2001;
	}
}
