/*
 * GeneratorServiceImpl.java 
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import de.hsbo.ibix.config.PropertiesReader;
import de.hsbo.ibix.exception.IbixException;
import de.hsbo.ibix.service.GeneratorService;
import de.hsbo.ibix.utils.PdfCompiler;

//TODO: eventuell den Client durch Apache HttpClient ersetzen
// https://www.baeldung.com/httpclient-guide

/**
 * The Class GeneratorServiceImpl.
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {
	final static Logger log = LoggerFactory.getLogger(BearbeitungServiceImpl.class);

	@Autowired
	PropertiesReader propertiesReader;

	@Override
	public Integer neueAufgabe(int templateID, int generatorDatenID, String aufgabentyp) {
		log.info("Erzeugen einer neuen Aufgabe zur ID -> {} vom Typ {}", templateID, aufgabentyp);

		String baseUri = propertiesReader.getGeneratorUri();
		WebClient client = WebClient.create(baseUri);
		String uristr = String.format("create/%d/%d/%s", templateID, generatorDatenID, aufgabentyp);
		String responseString = client.get().uri(uristr).retrieve().bodyToMono(String.class).block();

		Integer konkreteAufgabenID = Integer.decode(responseString);
		log.info("ID für neue Aufgabe -> {}", konkreteAufgabenID);
		return konkreteAufgabenID;
	}

	@Override
	public Integer neueAufgabeBasierendAufVorgaengerTemplate(Integer templateID, Integer vorgaengerTemplateID,
			String aufgabentyp) {
		log.info("Erzeugen einer neuen Aufgabe zur ID -> {} basierend auf {}", templateID, vorgaengerTemplateID);

		String baseUri = propertiesReader.getGeneratorUri();
		WebClient client = WebClient.create(baseUri);
		String uristr = String.format("create_and_save_from_previous/%d/%d/%s", templateID, vorgaengerTemplateID,
				aufgabentyp);
		String responseString = client.get().uri(uristr).retrieve().bodyToMono(String.class).block();

		Integer konkreteAufgabenID = Integer.decode(responseString);
		log.info("ID für neue Aufgabe -> {}", konkreteAufgabenID);
		return konkreteAufgabenID;
	}

	@Override
	public String loadHTML(Integer aufgabenID) {
		log.info("Laden des HTML-Codes für Aufgabe -> {}", aufgabenID);
		String responseString = new String(callGenerator("read/%d", aufgabenID));
		return responseString;
	}

	@Override
	public byte[] loadPdf(Integer aufgabenID) throws Exception {
		String texContent = getTexContent(aufgabenID);
		byte[] pdfContent;
		pdfContent = PdfCompiler.compileToPDF(texContent);
		return pdfContent;

	}

	protected String getTexContent(Integer aufgabenID) {
		log.info("Generieren der PDF-Datei für Aufgabe -> {}", aufgabenID);
		String responseString = new String(callGenerator("tex/%d", aufgabenID));
		return responseString;
	}

	@Override
	public byte[] loadExcel(Integer aufgabenID) {
		log.info("Laden des Excel-Codes für Aufgabe -> {}", aufgabenID);
		byte[] response = callGenerator("loadexcel/%d", aufgabenID);
		return response;

	}

	@Override
	public Integer neueDatentabelle(Integer datentemplateid) {
		log.info("Erzeugen einer neuen Datentabelle zur ID -> {}", datentemplateid);

		String baseUri = propertiesReader.getGeneratorUri();
		WebClient client = WebClient.create(baseUri);
		String uristr = String.format("createtabelle/%d", datentemplateid);
		String responseString = client.get().uri(uristr).retrieve().bodyToMono(String.class).block();

		Integer konkreteDatenID = Integer.decode(responseString);
		log.info("ID für neue Datentabelle -> {}", konkreteDatenID);
		return konkreteDatenID;
	}

	@Override
	public Integer showTabelle(Integer datenTemplateID, Integer alleSpalten) {
		log.info("Laden der Tabelle zur DatenTemplateID -> {}", datenTemplateID);

		String baseUri = propertiesReader.getGeneratorUri();
		WebClient client = WebClient.create(baseUri);
		String uristr = String.format("tabelle/%d/%d", datenTemplateID, alleSpalten);
		String responseString = client.get().uri(uristr).retrieve().bodyToMono(String.class).block();

		Integer konkreteDatenID = Integer.decode(responseString);
		log.info("ID für neue Datentabelle -> {}", konkreteDatenID);
		return konkreteDatenID;
	}

//	@Override
//	public String bewerteLoesung(Integer generatorID, String vbaCode, Boolean pruefung) {
//		return this.ermittleBewertungAusPythonProcess(generatorID, vbaCode, pruefung);
//		String baseUri = propertiesReader.getGeneratorUri();
//		WebClient client = WebClient.create(baseUri);
//
//		MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
//		bodyValues.add("vbacode", vbaCode);
//
//		String uristr = String.format("bewertevba/%d", generatorID);
//		if (pruefung) {
//			uristr = uristr + "/1";
//		}
//		String responseString = client.post().uri(uristr).body(BodyInserters.fromFormData(bodyValues)).retrieve()
//				.bodyToMono(String.class).block();
//
//		log.info("Bewertung für Aufgabe -> {}", responseString);
//		return responseString;
//	}

	@Override
	public String bewerteLoesung(Integer generatorID, String vbaFileName, Boolean pruefung) {
		String responseString;
		int exitCode;

		List<String> params = new ArrayList<>();
		params.add(propertiesReader.getPythonPath());
		params.add(propertiesReader.getGeneratorPath() + propertiesReader.getBewertungPythonFileName());
		params.add(vbaFileName);
		params.add(String.valueOf(generatorID));
		params.add(pruefung ? "1" : "0");

		ProcessBuilder processBuilder = new ProcessBuilder(params);
		processBuilder.directory(new File(propertiesReader.getGeneratorPath()));

		processBuilder.redirectErrorStream(true);

		Process process;
		try {
			process = processBuilder.start();

			InputStream inputStream = process.getInputStream();

			responseString = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
					.collect(Collectors.joining("\n"));

			exitCode = process.waitFor();
		} catch (Exception e) {
			log.error("Bewertung kann nicht gestartet werden", e);
			log.error(e.getMessage());
			throw new IbixException(String.format("Probleme bei Bewertung von Aufgabe %d", generatorID), e);
		}

		if (exitCode < 0) {
			throw new IbixException(String.format("Bewertung von Aufgaben %d mit Exit Code %d", generatorID, exitCode));
		}

		return responseString;
	}

	private byte[] callGenerator(String call, Integer aufgabenID) {
		String baseUri = propertiesReader.getGeneratorUri();
		WebClient client = WebClient.create(baseUri);
		String uristr = String.format(call, aufgabenID);
		byte[] response = client.get().uri(uristr).retrieve().bodyToMono(byte[].class).block();
		return response;
	}

	@Override
	public String loadTabelleHTML(Integer datenTemplateId, boolean alleSpalten) {
		String responseString = "";
		log.info("Laden des HTML-Codes für Tabelle -> {}", datenTemplateId);
		String call = alleSpalten ? "tabelle/%d/1" : "tabelle/%d/0";
		try {
			responseString = new String(callGenerator(call, datenTemplateId));
		} catch (Exception e) {
			e.printStackTrace();
			responseString = e.getMessage();
		}
		return responseString;
	}

	@Override
	public String loadPythonCode(String filename) {
		String responseString = "";
		log.trace("Laden des Python-Codes für {}", filename);

		String baseUri = propertiesReader.getGeneratorUri();
		WebClient client = WebClient.create(baseUri);

		try {
			MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();
			bodyValues.add("filename", filename);

			String uristr = "pythoncode";
			responseString = client.post().uri(uristr).body(BodyInserters.fromFormData(bodyValues)).retrieve()
					.bodyToMono(String.class).block();
		} catch (Exception e) {
			log.error(e.getMessage());
			responseString = e.getMessage();
		}

		log.trace("Laden des Python-Codes Ergebnis: {}", responseString);

		return responseString;
	}
}
