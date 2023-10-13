/*
 * AufgabenTemplateServiceImpl.java 
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

package de.hsbo.ibix.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.exception.IbixException;
import de.hsbo.ibix.exception.UserNotificationException;
import de.hsbo.ibix.model.Aufgabe;
import de.hsbo.ibix.model.AufgabenTemplate;
import de.hsbo.ibix.model.Aufgabentyp;
import de.hsbo.ibix.model.ConfigFunktionen;
import de.hsbo.ibix.model.ConfigRestaufgaben;
import de.hsbo.ibix.model.DatenTemplate;
import de.hsbo.ibix.repository.AufgabeRepository;
import de.hsbo.ibix.repository.AufgabenTemplateRepository;
import de.hsbo.ibix.repository.BearbeitungRepository;
import de.hsbo.ibix.repository.ConfigFunktionenRepository;
import de.hsbo.ibix.repository.ConfigRestaufgabenRepository;
import de.hsbo.ibix.repository.DatenTemplateRepository;
import de.hsbo.ibix.security.IbixScope;
import de.hsbo.ibix.service.AufgabenTemplateService;
import de.hsbo.ibix.service.GeneratorService;

/**
 * The Class AufgabenTemplateServiceImpl.
 */
@Service
public class AufgabenTemplateServiceImpl implements AufgabenTemplateService {
	final static Logger log = LoggerFactory.getLogger(AufgabenTemplateServiceImpl.class);

	@Autowired
	GeneratorService generatorService;

	@Autowired
	AufgabenTemplateRepository templateRepo;

	@Autowired
	DatenTemplateRepository datenTemplateRepo;

	@Autowired
	ConfigFunktionenRepository configFunktionenRepo;
	@Autowired
	ConfigRestaufgabenRepository configRestaufgabenRepo;

	@Autowired
	BearbeitungRepository bearbeitungRepo;

	@Autowired
	AufgabeRepository aufgabeRepo;

	@Autowired
	IbixScope ibixscope;

	@Override
	public AufgabenTemplate copyAufgabenTemplate(int templateId) {
		AufgabenTemplate original = templateRepo.getReferenceById(templateId);
		AufgabenTemplate neu = new AufgabenTemplate(original);
		neu.setDatentemplate(original.getDatentemplate());
		neu.setName(neu.getName() + " Kopie");
		if (original.getConfigFunktionen() != null) {
			neu.setConfigFunktionen(new ConfigFunktionen(original.getConfigFunktionen()));
		}
		if (original.getConfigStatistiken() != null) {
			neu.setConfigStatistiken(new ConfigRestaufgaben(original.getConfigStatistiken()));
		}
		if (original.getConfigPlausi() != null) {
			neu.setConfigPlausi(new ConfigRestaufgaben(original.getConfigPlausi()));
		}
		if (original.getConfigFormatierung() != null) {
			neu.setConfigFormatierung(new ConfigRestaufgaben(original.getConfigFormatierung()));
		}

		templateRepo.save(neu);
		return neu;
	}

	@Override
	public List<AufgabenTemplate> findAll() {
		return templateRepo.findAll();
	}

	@Override
	public List<AufgabenTemplate> findAllByOrderById() {
		return templateRepo.findAllByOrderById();
	}

	@Override
	public AufgabenTemplate getReferenceById(int id) {
		return templateRepo.getReferenceById(id);
	}

	@Override
	public void speicherTemplate(AufgabenTemplate template) {
		templateRepo.save(template);
	}

	@Override
	public AufgabenTemplate updateTemplate(AufgabenTemplate template) {
		AufgabenTemplate original = templateRepo.getReferenceById(template.getId());
		// Nur Daten speichern, die im Formular auch geändert werden können
		original.setName(template.getName());
		original.setTagString(template.getTagString());
		original.setSchwierigkeitsgrad(template.getSchwierigkeitsgrad());
		original.setStandardaufgabe(template.getStandardaufgabe());
		original.setStandardGeneratorAufgabenId(template.getStandardGeneratorAufgabenId());
		original.setStandardGeneratorDatenId(template.getStandardGeneratorDatenId());

		templateRepo.save(original);

		return original;
	}

	@Override
	public List<AufgabenTemplate> findByNameOrTag(String filtertext) {
		return templateRepo.findByNameContainingOrTagsContainingOrderById(filtertext, filtertext);
	}

	@Override
	public void ordneDatentemplateZu(AufgabenTemplate template, int datentemplateID) {
		DatenTemplate datentemplate = datenTemplateRepo.getReferenceById(datentemplateID);

		template.setDatentemplate(datentemplate);
		templateRepo.save(template);
	}

	@Override
	public void saveConfigFunktionen(AufgabenTemplate template, ConfigFunktionen config) {
		if (template.getConfigFunktionen() == null) {
			template.setConfigFunktionen(new ConfigFunktionen(config));
		} else {
			template.getConfigFunktionen().setAufgabentext(config.getAufgabentext());
			template.getConfigFunktionen().setFunktionsname(config.getFunktionsname());
		}
		templateRepo.save(template);
	}

	@Override
	public void saveConfigPlausi(AufgabenTemplate template, ConfigRestaufgaben config) {
		if (template.getConfigPlausi() == null) {
			template.setConfigPlausi(new ConfigRestaufgaben(config));
		} else {
			template.getConfigPlausi().setProzedurname(config.getProzedurname());
			template.getConfigPlausi().setAllgemeinerText(config.getAllgemeinerText());
			template.getConfigPlausi().setSubtypen(config.getSubtypen());
			template.getConfigPlausi().setSpalten(config.getSpalten());
			template.getConfigPlausi().setBedingungsspalten(config.getBedingungsspalten());
		}
		templateRepo.save(template);
	}

	@Override
	public void saveConfigFormatierung(AufgabenTemplate template, ConfigRestaufgaben config) {
		if (template.getConfigFormatierung() == null) {
			template.setConfigFormatierung(new ConfigRestaufgaben(config));
		} else {
			template.getConfigFormatierung().setProzedurname(config.getProzedurname());
			template.getConfigFormatierung().setAllgemeinerText(config.getAllgemeinerText());
			template.getConfigFormatierung().setSubtypen(config.getSubtypen());
			template.getConfigFormatierung().setSpalten(config.getSpalten());
			template.getConfigFormatierung().setBedingungsspalten(config.getBedingungsspalten());
		}
		templateRepo.save(template);
	}

	@Override
	public void saveConfigStatistiken(AufgabenTemplate template, ConfigRestaufgaben config) {
		if (template.getConfigStatistiken() == null) {
			template.setConfigStatistiken(new ConfigRestaufgaben(config));
		} else {
			template.getConfigStatistiken().setProzedurname(config.getProzedurname());
			template.getConfigStatistiken().setAllgemeinerText(config.getAllgemeinerText());
			template.getConfigStatistiken().setSubtypen(config.getSubtypen());
			template.getConfigStatistiken().setSpalten(config.getSpalten());
			template.getConfigStatistiken().setBedingungsspalten(config.getBedingungsspalten());
		}
		templateRepo.save(template);
	}

	@Override
	public void loescheConfigFunktionen(Integer templateId) {
		AufgabenTemplate template = templateRepo.getReferenceById(templateId);
		ConfigFunktionen config = template.getConfigFunktionen();
		template.setConfigFunktionen(null);
		configFunktionenRepo.delete(config);
		templateRepo.save(template);

	}

	@Override
	public void loescheConfigStatistiken(Integer templateId) {
		AufgabenTemplate template = templateRepo.getReferenceById(templateId);
		ConfigRestaufgaben config = template.getConfigStatistiken();
		template.setConfigStatistiken(null);
		configRestaufgabenRepo.delete(config);
		templateRepo.save(template);
	}

	@Override
	public void loescheConfigPlausi(Integer templateId) {
		AufgabenTemplate template = templateRepo.getReferenceById(templateId);
		ConfigRestaufgaben config = template.getConfigPlausi();
		template.setConfigPlausi(null);
		configRestaufgabenRepo.delete(config);
		templateRepo.save(template);
	}

	@Override
	public void loescheConfigFormatierung(Integer templateId) {
		AufgabenTemplate template = templateRepo.getReferenceById(templateId);
		ConfigRestaufgaben config = template.getConfigFormatierung();
		template.setConfigFormatierung(null);
		configRestaufgabenRepo.delete(config);
		templateRepo.save(template);
	}

	@Override
	// Template auswählen:
	// gibt es ein Default?
	// wurde das default noch nicht verwendet (dann existiert noch keine Aufgabe zu
	// dem Typ) -> default nehmen
	//
	// Bisher verwendete Templates suchen
	// Gibt es ein Aufgabentemplate, das dasselbe Datentemplate nutzt, wie eine
	// Vorgängeraufgabe (gespeichert in config)
	// Noch nicht verwendete Tempaltes ermitteln
	// Zufällig ein Template auswählen
	public AufgabenTemplate wähleTemplateAus(Aufgabentyp aufgabentyp, HashMap<String, Integer> config) {
		List<AufgabenTemplate> templatesDesAufgabentyps = aufgabentyp.getAufgabenTemplates();
		List<Aufgabe> vorhandeneAufgaben;
		List<Integer> vorhandeneAufgabenTemplateIds = new ArrayList<>();
		List<AufgabenTemplate> potentielleAufgabenTemplates = new ArrayList<>();

		Optional<AufgabenTemplate> defaulttemplate = templatesDesAufgabentyps.stream()
				.filter(t -> t.getStandardaufgabe() != null && t.getStandardaufgabe()).findFirst();
		String userHash = ibixscope.getUserdetails().getHashcode();

		vorhandeneAufgaben = aufgabeRepo.findAufgabenFuerBenutzerUndAufgabentyp(userHash, aufgabentyp.getId());

		Integer vorherigeAufgabeTemplateID = config.get("vorherigeAufgabeTemplateID");

		// Falls noch keine Aufgaben des Typs vom Benutzer bearbeitet wurden, nimm
		// (sofern vorhanden) die Standardaufgabe
		if (defaulttemplate.isPresent() && vorhandeneAufgaben.isEmpty()) {
			if (vorherigeAufgabeTemplateID != null && defaulttemplate.get().getBasierendAuf() != null
					&& !vorherigeAufgabeTemplateID.equals(defaulttemplate.get().getBasierendAuf().getId())) {
				throw new IbixException("Aufgabe falsch konfiguriert: Standardardtemplate von "
						+ defaulttemplate.get().getId() + " muss auf " + defaulttemplate.get().getBasierendAuf().getId()
						+ "basieren, und nicht auf " + vorherigeAufgabeTemplateID);
			}

			log.debug("Standardaufgabe: Template {}", defaulttemplate.get().getId());
			config.put("Standardaufgabe", 1);
			return defaulttemplate.get();
		}

		config.put("Standardaufgabe", 0);

		Iterator<Aufgabe> vorhandeneAufgabenIterator = vorhandeneAufgaben.iterator();
		Integer datentemplateID = config.get("datenTemplateID");

		// Sammel die IDs der schon für den Benutzer und Aufgabentyp vorhandenen
		// Templates
		while (vorhandeneAufgabenIterator.hasNext()) {
			vorhandeneAufgabenTemplateIds.add(vorhandeneAufgabenIterator.next().getAufgabenTemplate().getId());
		}

		// Suche die Aufgabentemplate zusammen, die noch nicht bearbeitet wurden
		Iterator<AufgabenTemplate> templatesDesAufgabentypsIterator = templatesDesAufgabentyps.iterator();
		while (templatesDesAufgabentypsIterator.hasNext()) {
			AufgabenTemplate template = templatesDesAufgabentypsIterator.next();
			// Falls das Template dasselbe Datentemplate verwendet, wie die vorherige
			// Aufgabe, dann nimm dieses
			log.debug("Prüfe Template {} auf dasselbe Datentemplate {} (Template.Datentemplate_id: {})",
					template.getId(), datentemplateID, template.getDatentemplate().getId());

			if (datentemplateID != null && template.getDatentemplate().getId() == datentemplateID) {
				log.debug("Verwende Template {} mit demselben Datentemplate {}", template.getId(), datentemplateID);
				return template;
			}
			// Falls das Template noch nicht durch den Benutzer bearbeitet wurde, füg' es
			// den potentiellen Templates hinzu
			if (!vorhandeneAufgabenTemplateIds.contains(template.getId())) {
				potentielleAufgabenTemplates.add(template);
			}
		}

		// Falls keine neuen Templates zur Verfügung stehen, wieder auf die gesamte
		// Liste zurückgreifen
		if (potentielleAufgabenTemplates.isEmpty()) {
			potentielleAufgabenTemplates = templatesDesAufgabentyps;
		}

		if (potentielleAufgabenTemplates.isEmpty()) {
			throw new UserNotificationException("Zu dem Aufgabentyp sind keine Templates definiert.");
		}
		
		int index = new Random().nextInt(potentielleAufgabenTemplates.size());
		AufgabenTemplate gewaehltesTemplate = potentielleAufgabenTemplates.get(index);
		log.debug("Verwende Template {} mit Datentemplate {}", gewaehltesTemplate.getId(),
				gewaehltesTemplate.getDatentemplate().getId());
		return gewaehltesTemplate;
	}

	@Override
	public Aufgabe erstelleAufgabe(AufgabenTemplate template, HashMap<String, Integer> config) {
		// das folgende geht sicher auch eleganter :-)
		StringBuffer typen = new StringBuffer();
		Integer generatorDatenID;
		Integer generatorAufgabenID;
		Aufgabe aufgabe;

		if (template.getConfigFunktionen() != null) {
			typen.append("1");
		}
		if (template.getConfigStatistiken() != null) {
			typen.append("2");
		}
		if (template.getConfigPlausi() != null) {
			typen.append("3");
		}
		if (template.getConfigFormatierung() != null) {
			typen.append("4");
		}

		if (template.getStandardaufgabe() != null && template.getStandardaufgabe()
				&& config.getOrDefault("Standardaufgabe", 0) == 1
				&& template.getStandardGeneratorAufgabenId() != null) {
			generatorDatenID = template.getStandardGeneratorDatenId();
			generatorAufgabenID = template.getStandardGeneratorAufgabenId();
			log.debug("Verwende Standardaufgabe zu Template {}: GeneratorAufgabenid {} - GeneratorDatenID {}",
					template.getId(), generatorAufgabenID, generatorDatenID);
		} else {
			generatorDatenID = config.get("generatorDatenID");
			if (generatorDatenID == null && template.getDatentemplate() == null) {
				throw new UserNotificationException("Für das Aufgabentemplate ist kein Datentemplate definiert.");
			}
			
			if (generatorDatenID == null || template.getDatentemplate().getId() != config.get("datenTemplateID")) {
				generatorDatenID = generatorService.neueDatentabelle(template.getDatentemplate().getId());
				log.debug("Neue Daten zu Template {} erzugt:  GeneratorDatenID {}", template.getId(), generatorDatenID);
			}

			if (template.getBasierendAuf() != null) {
				Integer vorgaengerGeneratorAufgabenID = config.get("generatorAufgabenID");
				log.debug("Vor: Erzeuge neue Aufgabe zu Template {} aus VorgängerID {}", template.getId(),
						vorgaengerGeneratorAufgabenID);
				generatorAufgabenID = generatorService.neueAufgabeBasierendAufVorgaengerTemplate(template.getId(),
						vorgaengerGeneratorAufgabenID, typen.toString());
				log.debug("Nach:Neue Aufgabe zu Template {} aus VorgängerID {}: GeneratorAufgabenID {}",
						template.getId(), vorgaengerGeneratorAufgabenID, generatorAufgabenID);
			} else {
				log.debug("Vor: Erzeuge neue Aufgabe zu Template {}:  DatenID {}", template.getId(), generatorDatenID);
				generatorAufgabenID = generatorService.neueAufgabe(template.getId(), generatorDatenID,
						typen.toString());
				log.debug("Nach: Neue Aufgabe zu Template {} mit DatenID {}: GeneratorAufgabenID {}", template.getId(),
						generatorDatenID, generatorAufgabenID);
			}

			// ggf. das Template updaten
			if (template.getStandardaufgabe() != null && template.getStandardaufgabe()) {
				template.setStandardGeneratorDatenId(generatorDatenID);
				template.setStandardGeneratorAufgabenId(generatorAufgabenID);
				templateRepo.save(template);
			}
		}

		config.put("generatorDatenID", generatorDatenID);
		config.put("generatorAufgabenID", generatorAufgabenID);
		config.put("vorherigeAufgabeTemplateID", template.getId());
		config.put("datenTemplateID", template.getDatentemplate().getId());

		log.debug("Konfiguration nach Erzeugung der Aufgabe: {}", config);

		aufgabe = new Aufgabe();
		aufgabe.setAufgabenTemplate(template);
		aufgabe.setGeneratorAufgabenID(generatorAufgabenID);

		return aufgabe;
	}

	@Override
	public List<AufgabenTemplate> findRelevanteVorgaengerTemplates(AufgabenTemplate template) {
		if (template.getDatentemplate() != null) {
			return templateRepo.findAllByDatentemplate(template.getDatentemplate());
		}

		return templateRepo.findByNameContainingOrTagsContainingOrderById(template.getTagString(), template.getTagString());
	}

	@Override
	public void ordneVorgaengertemplateZu(AufgabenTemplate template, Integer vorgaengertemplateID) {
		AufgabenTemplate vorgaengertemplate = null;
		if (vorgaengertemplateID != null) {
			vorgaengertemplate = templateRepo.getReferenceById(vorgaengertemplateID);
		}

		template.setBasierendAuf(vorgaengertemplate);
		if (vorgaengertemplate != null) {
			template.setDatentemplate(vorgaengertemplate.getDatentemplate());
		}
		templateRepo.save(template);
	}
}
