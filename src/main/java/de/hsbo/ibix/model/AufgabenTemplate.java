/*
 * AufgabenTemplate.java 
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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class AufgabenTemplate.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AufgabenTemplate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	String name;
	Integer schwierigkeitsgrad;
	Boolean standardaufgabe = false;
	Integer standardGeneratorAufgabenId = null;
	Integer standardGeneratorDatenId = null;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	DatenTemplate datentemplate;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	ConfigFunktionen configFunktionen;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	ConfigRestaufgaben configStatistiken;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	ConfigRestaufgaben configPlausi;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	ConfigRestaufgaben configFormatierung;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	Aufgabentyp aufgabentyp;

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	AufgabenTemplate basierendAuf = null;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "aufgabentemplate_tags", joinColumns = @JoinColumn(name = "template_id"))
	@Column(name = "tag")
	Set<String> tags = new HashSet<>();

	public AufgabenTemplate(AufgabenTemplate original) {
		this.name = original.getName();
		this.schwierigkeitsgrad = original.getSchwierigkeitsgrad();
		this.standardaufgabe = original.getStandardaufgabe();
		this.setTagString(original.getTagString());
	}

	@Transient
	public String getTagString() {
		return TagHelper.tagsToString(this.getTags());
	}

	@Transient
	public void setTagString(String tagString) {
		this.setTags(TagHelper.stringToTags(tagString));
	}

	public ConfigFunktionen neueConfigFunktionen() {
		this.configFunktionen = new ConfigFunktionen();
		return this.configFunktionen;
	}

	public ConfigRestaufgaben neueConfigStatistiken() {
		this.configStatistiken = new ConfigRestaufgaben();
		this.configStatistiken.setSubtypen("[2, 3, [4, 5], [7, 8], [9, 10, 11]]");
		String spalten = this.getDatentemplate().getTemplateSpalten().stream()
				.map(ts -> "\"" + ts.getSpalte().getName() + "\"").collect(Collectors.joining(", "));
		this.configStatistiken.setSpalten("[" + spalten + "]");
		this.configStatistiken.setBedingungsspalten("[" + spalten + "]");

		return this.configStatistiken;
	}

	public ConfigRestaufgaben neueConfigPlausi() {
		this.configPlausi = new ConfigRestaufgaben();
		this.configPlausi.setSubtypen("[1, 2, [3, 4, 5], [6, 7]]");
		String spalten = this.getDatentemplate().getTemplateSpalten().stream()
				.map(ts -> "\"" + ts.getSpalte().getName() + "\"").collect(Collectors.joining(", "));
		this.configPlausi.setSpalten("[" + spalten + "]");
		this.configPlausi.setBedingungsspalten("[" + spalten + "]");

		return this.configPlausi;
	}

	public ConfigRestaufgaben neueConfigFormatierung() {
		this.configFormatierung = new ConfigRestaufgaben();
		this.configFormatierung.setSubtypen("[0, 1, 2]");
		String spalten = this.getDatentemplate().getTemplateSpalten().stream()
				.map(ts -> "\"" + ts.getSpalte().getName() + "\"").collect(Collectors.joining(", "));
		this.configFormatierung.setSpalten("[" + spalten + "]");
		this.configFormatierung.setBedingungsspalten("[" + spalten + "]");

		return this.configFormatierung;
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
		return id != null && id.equals(((AufgabenTemplate) obj).id);
	}

	@Override
	public int hashCode() {
		return 2021;
	}
}
