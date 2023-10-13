/*
 * FileServiceImpl.java 
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.hsbo.ibix.config.PropertiesReader;
import de.hsbo.ibix.exception.StorageException;
import de.hsbo.ibix.exception.StorageFileNotFoundException;
import de.hsbo.ibix.exception.UserNotificationException;
import de.hsbo.ibix.security.IbixScope;
import de.hsbo.ibix.service.FileService;
import de.hsbo.ibix.service.GeneratorService;
import de.hsbo.ibix.utils.VBAMacroExtractor;

/**
 * The Class FileServiceImpl.
 */
@Service
public class FileServiceImpl implements FileService {

	// TODO: Komplett überarbeiten

	@Autowired
	PropertiesReader propertiesReader;

	@Autowired
	GeneratorService generatorService;

	@Autowired
	IbixScope ibixscope;

	protected Path benutzerPfad(String filename) {
		String userhash = ibixscope.getUserdetails().getHashcode();
		Path pfad = Path.of(propertiesReader.getExcelPath()).resolve(userhash);

		if (filename != null) {
			return pfad.resolve(filename);
		}

		return pfad;
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(Path.of(propertiesReader.getExcelPath()));
		} catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public File loadExcel(Integer generatorId) throws IOException {
		pruefeVerzeichnis();
		String filename = String.format("aufgabe_%d.xlsx", generatorId);

		if (!pruefeDatei(filename)) {
			byte[] excelContent = generatorService.loadExcel(generatorId);
			Path filepath = benutzerPfad(filename);

			try (FileOutputStream fos = new FileOutputStream(filepath.toFile());) {
				fos.write(excelContent);
			}
		}

		Path filepath = benutzerPfad(filename);

		if (!pruefeDatei(filename)) {
			throw new StorageFileNotFoundException(filename);
		}

		return filepath.toFile();
	}

	@Override
	public String loadHtml(Integer generatorId) throws FileNotFoundException, IOException {
		pruefeVerzeichnis();
		String filename = String.format("aufgabe_%d.html", generatorId);
		Path filepath = benutzerPfad(filename);
		String htmlContent = null;

		if (!pruefeDatei(filename)) {
			htmlContent = generatorService.loadHTML(generatorId);

			try (FileOutputStream fos = new FileOutputStream(filepath.toFile());) {
				fos.write(htmlContent.getBytes());
			}

		} else {
			try (FileInputStream fis = new FileInputStream(filepath.toFile())) {
				htmlContent = new String(fis.readAllBytes());
			}
		}
		return htmlContent;
	}

	@Override
	public File loadLoesung(Integer bearbeitungID) throws IOException {
		pruefeVerzeichnis();
		String filename = String.format("loesung_%d.xlsm", bearbeitungID);

		if (!pruefeDatei(filename)) {
			throw new StorageFileNotFoundException(filename);
		}

		Path filepath = benutzerPfad(filename);
		return filepath.toFile();
	}

	@Override
	public Boolean existiertLoesung(Integer bearbeitungID) throws IOException {
		pruefeVerzeichnis();
		String filename = String.format("loesung_%d.xlsm", bearbeitungID);

		return (pruefeDatei(filename));
	}

	@Override
	public File loadPDF(Integer generatorAufgabenID) throws Exception {
		pruefeVerzeichnis();
		String filename = String.format("aufgabe_%d.pdf", generatorAufgabenID);

		if (!pruefeDatei(filename)) {
			byte[] pdfContent = generatorService.loadPdf(generatorAufgabenID);
			Path filepath = benutzerPfad(filename);

			try (FileOutputStream fos = new FileOutputStream(filepath.toFile());) {
				fos.write(pdfContent);
			}

		}

		Path filepath = benutzerPfad(filename);

		if (!pruefeDatei(filename)) {
			throw new StorageFileNotFoundException(filename);
		}
		return filepath.toFile();
	}

	@Override
	public String getVbaFilename(Integer generatorAufgabenID) throws IOException {
		pruefeVerzeichnis();
		String filename = String.format("vbacode_%d.txt", generatorAufgabenID);
		if (!pruefeDatei(filename)) {
			throw new StorageFileNotFoundException(filename);
		}

		Path filepath = benutzerPfad(filename);
		return filepath.toString();
	}

	@Override
	public String loadVbaCode(String filename) throws IOException {
		pruefeVerzeichnis();
		String vbaCode;

		if (!pruefeDatei(filename)) {
			throw new StorageFileNotFoundException(filename);
		}

		Path filepath = benutzerPfad(filename);
		vbaCode = Files.readString(filepath);

		return vbaCode;
	}

	protected boolean pruefeDatei(String filename) throws MalformedURLException {
		Path pfad = benutzerPfad(filename);
		Resource resource;

		resource = new UrlResource(pfad.toUri());
		if (resource.exists() || resource.isReadable()) {
			return true;
		}

		return false;
	}

	protected void pruefeVerzeichnis() throws IOException {
		Path pfad = benutzerPfad(null);
		Resource resource;

		resource = new UrlResource(pfad.toUri());
		if (!resource.exists()) {
			Files.createDirectories(pfad);
		}
	}

	@Override
	public void speicherLoesung(Integer bearbeitungID, MultipartFile file) {
		if (file.isEmpty()) {
			throw new UserNotificationException("Hochgeladene Datei ist leer.");
		}
		String filename = String.format("loesung_%d.xlsm", bearbeitungID);
		Path destinationFile = benutzerPfad(filename);

		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new StorageException("Hochgeladene Datei kann nicht geschrieben werden.", e);
		}

		String vbaCode = null;
		try {
			vbaCode = new VBAMacroExtractor().extract(destinationFile.toFile());
		} catch (IOException e) {
			// throw new UserNotificationException("VBA-Code aus hochgeladener Datei kann
			// nicht extrahiert werden.", e);
			throw new UserNotificationException("VBA-Code aus hochgeladener Datei kann nicht extrahiert werden.", e);
		}

		String vbafilename = String.format("vbacode_%d.txt", bearbeitungID);
		Path vbaCodeFile = benutzerPfad(vbafilename);
		try (FileOutputStream fos = new FileOutputStream(vbaCodeFile.toString())) {
			fos.write(vbaCode.getBytes());
		} catch (IOException e) {
			throw new StorageException("Datei mit VBA-Code kann nicht geschrieben werden.", e);
		}
	}
}
