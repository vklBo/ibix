/*
 * Aufgabenblatt.java 
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Aufgabenblatt.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Aufgabenblatt {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	String name;
	Integer nr;
	String zusatztext;
	Boolean inaktiv = false;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime start;

	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime ende;

	Integer dauer; // in Minuten
	Boolean klausur;

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@OrderColumn(name = "ordnung")
	@JoinTable(name = "aufgabenblatt_aufgabentyp", joinColumns = @JoinColumn(name = "aufgabenblatt_id"), inverseJoinColumns = @JoinColumn(name = "aufgabentyp_id"))
	List<Aufgabentyp> aufgabentypen = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "aufgabenblatt_tags", joinColumns = @JoinColumn(name = "blatt_id"))
	@Column(name = "tag")
	Set<String> tags = new HashSet<>();

	public Aufgabenblatt(Aufgabenblatt original) {
		this.nr = original.getNr();
		this.name = original.getName();
		this.zusatztext = original.getZusatztext();
		this.start = original.getStart();
		this.ende = original.getEnde();
		this.dauer = original.getDauer();
		this.klausur = original.getKlausur();
	}

	@Transient
	public boolean isGestartet() {
		if (this.start == null) {
			return true;
		}
		return !LocalDateTime.now().isBefore(this.start);
	}

	@Transient
	public boolean isBeendet() {
		if (this.ende == null) {
			return false;
		}
		return LocalDateTime.now().isAfter(this.ende);
	}

	@Transient
	public boolean isAktuell() {
		return this.isGestartet() && !this.isBeendet();
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
		return id != null && id.equals(((Aufgabenblatt) obj).getId());
	}

	@Override
	public int hashCode() {
		return 2002;
	}
}
