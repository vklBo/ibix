/*
 * Spaltendefinition.java 
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hsbo.ibix.data.Formatierung;
import de.hsbo.ibix.utils.TagHelper;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Spaltendefinition.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Spaltendefinition {

	public static String intervallpattern = "([\\[\\(])(-?\\d+\\.?\\d+), *(-?\\d+\\.?\\d+)([\\]\\)]), *(-?\\d*)";
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;
	String titel;
	String name;
	String typ;
	String auswahl;
	String inhalt;
	String intervalle;
	String config;

	// TODO: Muss die Rückrichtung gespeichert werden?
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "spalte_id")
	List<TemplateSpalte> templateSpalten = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "spaltendefinition_tags", joinColumns = @JoinColumn(name = "spaltendefinition_id"))
	@Column(name = "tag")
	Set<String> tags = new HashSet<>();

	@Transient
	Formatierung format;

	public Spaltendefinition(Spaltendefinition original) {
		this.titel = original.getTitel();
		this.name = original.getName();
		this.typ = original.getTyp();
		this.auswahl = original.getAuswahl();
		this.inhalt = original.getInhalt();
		this.intervalle = original.getIntervalle();
		this.config = original.getConfig();
		this.format = original.getFormat();
	}

	@Transient
	public String getTagString() {
		return TagHelper.tagsToString(this.getTags());
	}

	@Transient
	public void setTagString(String tagString) {
		this.setTags(TagHelper.stringToTags(tagString));
	}

	@PostLoad
	public void postLoadFunction() {
		format = new Formatierung(this.config);
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
		return id != null && id.equals(((Spaltendefinition) obj).id);
	}

	@Override
	public int hashCode() {
		return 2021;
	}

}
