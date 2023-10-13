/*
 * Aufgabentyp.java 
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
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Aufgabentyp.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Aufgabentyp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	String name;
	String lernziel;
	Integer schwierigkeit;
	Boolean standard; // Kennzeichnet, dass die Aufgabe im Normaltempo gelöst werden sollte
	Integer punkte;
	String programmiersprache; // Dauerhaft besser: beliebige zusätzliche Eigenschaften

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "aufgabentyp")
	List<AufgabenTemplate> aufgabenTemplates = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "aufgabentyp_tags", joinColumns = @JoinColumn(name = "typ_id"))
	@Column(name = "tag")
	Set<String> tags = new HashSet<>();

	public Aufgabentyp(Aufgabentyp original) {
		this.name = original.getName();
		this.lernziel = original.getLernziel();
		this.schwierigkeit = original.getSchwierigkeit();
		this.standard = original.getStandard();
		this.punkte = original.getPunkte();
		this.programmiersprache = original.getProgrammiersprache();
	}

	public void addAufgabenTemplate(AufgabenTemplate template) {
		this.aufgabenTemplates.add(template);
		template.setAufgabentyp(this);
	}

	public void removeAufgabenTemplate(AufgabenTemplate template) {
		template.setAufgabentyp(null);
		this.aufgabenTemplates.remove(template);
	}

	@Transient
	public String getTagString() {
		return TagHelper.tagsToString(this.getTags());
	}

	@Transient
	public void setTagString(String tagString) {
		this.setTags(TagHelper.stringToTags(tagString));
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
		return id != null && id.equals(((Aufgabentyp) obj).id);
	}

	@Override
	public int hashCode() {
		return 2021;
	}
}
