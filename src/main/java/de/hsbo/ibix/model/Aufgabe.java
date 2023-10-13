/*
 * Aufgabe.java 
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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Aufgabe.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Aufgabe {
	final static Logger log = LoggerFactory.getLogger(Aufgabe.class);
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	Integer nr;
	String bewertungJson;
	Integer generatorAufgabenID;
	Boolean abgeschlossen = false;
	Integer einstufung;
	Integer bonuspunkte;
	Integer prozent;

	@Transient
	Boolean loesungVorhanden = false;

	@ManyToOne
	AufgabenTemplate aufgabenTemplate;

	@ManyToOne
	Bearbeitung bearbeitung;

	public Aufgabe(Aufgabe original) {
		this.nr = original.getNr();
		this.bonuspunkte = original.getBonuspunkte();
		this.prozent = original.getProzent();
		this.bewertungJson = original.getBewertungJson();
		this.generatorAufgabenID = original.getGeneratorAufgabenID();
		this.abgeschlossen = original.getAbgeschlossen();
		this.einstufung = original.getEinstufung();
		this.loesungVorhanden = original.getLoesungVorhanden();
	}

	public String getBewertungstext() {
		StringBuffer text = new StringBuffer();

		if (bewertungJson == null || bewertungJson.isBlank()) {
			return "";
		}
		JSONObject json;
		try {
			json = new JSONObject(bewertungJson);

			Iterator<?> iter = json.keys();
			while (iter.hasNext()) {
				String key = (String) iter.next();

				if (key.equals("0")) {
					if (json.get(key).toString().equals("-1")) {
						return "Programm konnte nicht geladen werden";
					} else if (json.get(key).toString().equals("-2")) {
						return "Das Programm konnte nicht erfolgreich ausgeführt werden";
					} else if (json.get(key).toString().equals("-3")) {
						return "Die Funktion/Prozedur fehlt oder heißt nicht wie in der Aufgabe gefordert";
					} else if (json.get(key).toString().equals("-4")) {
						return "Serverproblem bei der Bewertung der Aufgabe";
					}
				}
				if (json.length() > 1) {
					text.append(" Aufgabe ");
					text.append(key);
					text.append(": ");
				}
				Object o = json.get(key);
				text.append(Math.round(Float.valueOf(o.toString())));
				text.append("% richtig.");
			}
		} catch (JSONException e) {
			log.error(e.getMessage());
			return "";
		}
		return text.toString();
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
		return id != null && id.equals(((Aufgabe) obj).getId());
	}

	@Override
	public int hashCode() {
		return 2001;
	}
}
