/*
 * PdfCompiler.java 
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

package de.hsbo.ibix.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PdfCompiler.
 */
public class PdfCompiler {

	static Logger log = LoggerFactory.getLogger(PdfCompiler.class);
	static String PDFLATEX = locatePdfLatex(); // "/bin/cat"; // "/usr/local/bin/pdflatex";

	public static byte[] compileToPDF(String tex) throws Exception {

		String uuid = UUID.randomUUID().toString();

		File dir = new File("/tmp/pdf_compiler_" + uuid); // File.createTempFile("__pdf_compiler_", "");
//		dir.delete();
		dir.mkdirs();

		if (!dir.isDirectory()) {
			log.error("FAILED TO CREATE DIRECTORY: {}", dir);
		}

		/*
		 * String res = "/static/images/hsbo_white.pdf"; InputStream is =
		 * PdfCompiler.class.getResourceAsStream(res); if (is != null) { File lf = new
		 * File(dir.getAbsolutePath() + File.separator + "hsbo_white.pdf");
		 * log.debug("Copying resource {} to {}", res, lf); copy(is, lf); }
		 */

		log.debug("WD: {}", dir);
		log.debug("Using pdflatex located at {}", PDFLATEX);

		File texFile = new File(dir.getAbsolutePath() + File.separator + "input.tex");
		FileWriter fw = new FileWriter(texFile);
		fw.write(tex);
		fw.close();

		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(dir);
		pb.command(PDFLATEX, "-interaction=nonstopmode", texFile.getName());

		Process process = pb.start();
		int rc = process.waitFor();

		log.debug("Return code: {}", rc);

		File pdfFile = new File(dir.getAbsolutePath() + File.separator + "input.pdf");
		log.debug("PDF output should be {}", pdfFile);
		if (!pdfFile.isFile()) {
			throw new Exception("Failed to compile PDF document!");
		} else {
			log.debug("PDF file {} exists and is of size {} bytes", pdfFile, pdfFile.length());
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileInputStream fin = new FileInputStream(pdfFile);

		byte[] buf = new byte[4096];
		int read = fin.read(buf);
		while (read > 0) {
			baos.write(buf, 0, read);
			read = fin.read(buf);
		}

		fin.close();

		baos.flush();
		baos.close();
		return baos.toByteArray();
	}

	public static void copy(InputStream in, File out) throws IOException {
		if (!out.getParentFile().isDirectory()) {
			out.getParentFile().mkdirs();
		}

		byte[] buf = new byte[16384];
		FileOutputStream fos = new FileOutputStream(out);

		int read = in.read(buf);
		while (read > 0) {
			fos.write(buf, 0, read);
			read = in.read(buf);
		}

		fos.close();
	}

	public static String locatePdfLatex() {
		List<String> locs = new ArrayList<>();
		locs.add("/Library/TeX/texbin/pdflatex");
		locs.add("/usr/local/bin/pdflatex");
		locs.add("/usr/bin/pdflatex");

		for (String loc : locs) {
			File file = new File(loc);
			if (file.isFile() && file.canExecute()) {
				return loc;
			}
		}

		return "/usr/local/bin/pdflatex";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
