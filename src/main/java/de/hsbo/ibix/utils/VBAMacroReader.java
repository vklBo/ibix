/*
 * VBAMacroReader.java 
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

import static org.apache.poi.util.StringUtil.endsWithIgnoreCase;

/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */

import static org.apache.poi.util.StringUtil.startsWithIgnoreCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.RLEDecompressingInputStream;

/**
 * Original: org.apache.poi.poifs.macros.VBAMacroReader Codepage-Probleme für
 * Mac-Codepage gefixed
 *
 * Finds all VBA Macros in an office file (OLE2/POIFS and OOXML/OPC), and
 * returns them.
 *
 * @since 3.15-beta2
 */
public class VBAMacroReader implements Closeable {

	/**
	 * The Class Module.
	 */
	protected static class Module {
		Integer offset;
		byte[] buf;

		void read(InputStream in) throws IOException {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
			out.close();
			buf = out.toByteArray();
		}
	}

	/**
	 * The Class ModuleMap.
	 */
	protected static class ModuleMap extends HashMap<String, Module> {
		private static final long serialVersionUID = 1L;
		Charset charset = Charset.forName("Cp1252"); // default charset
	}

	protected static final String VBA_PROJECT_OOXML = "vbaProject.bin";

	protected static final String VBA_PROJECT_POIFS = "VBA";

	// Constants from MS-OVBA:
	// https://msdn.microsoft.com/en-us/library/office/cc313094(v=office.12).aspx
	private static final int EOF = -1;
	private static final int VERSION_INDEPENDENT_TERMINATOR = 0x0010;

	protected static final int VERSION_DEPENDENT_TERMINATOR = 0x002B;

	private static final int PROJECTVERSION = 0x0009;

	private static final int PROJECTCODEPAGE = 0x0003;

	private static final int STREAMNAME = 0x001A;
	private static final int MODULEOFFSET = 0x0031;

	protected static final int MODULETYPE_PROCEDURAL = 0x0021;

	protected static final int MODULETYPE_DOCUMENT_CLASS_OR_DESIGNER = 0x0022;

	protected static final int PROJECTLCID = 0x0002;

	private static void readModule(DocumentInputStream dis, String name, ModuleMap modules) throws IOException {
		Module module = modules.get(name);
		// TODO Refactor this to fetch dir then do the rest
		if (module == null) {
			// no DIR stream with offsets yet, so store the compressed bytes for later
			module = new Module();
			modules.put(name, module);
			module.read(dis);
		} else {
			if (module.offset == null) {
				// This should not happen. bug 59858
				throw new IOException("Module offset for '" + name + "' was never read.");
			}
			// we know the offset already, so decompress immediately on-the-fly
			long skippedBytes = dis.skip(module.offset);
			if (skippedBytes != module.offset) {
				throw new IOException(
						"tried to skip " + module.offset + " bytes, but actually skipped " + skippedBytes + " bytes");
			}
			InputStream stream = new RLEDecompressingInputStream(dis);
			module.read(stream);
			stream.close();
		}

	}

	/**
	 * reads module from DIR node in input stream and adds it to the modules map for
	 * decompression later on the second pass through this function, the module will
	 * be decompressed
	 *
	 * Side-effects: adds a new module to the module map or sets the buf field on
	 * the module to the decompressed stream contents (the VBA code for one module)
	 *
	 * @param in         the run-length encoded input stream to read from
	 * @param streamName the stream name of the module
	 * @param modules    a map to store the modules
	 * @throws IOException
	 */
	private static void readModule(RLEDecompressingInputStream in, String streamName, ModuleMap modules)
			throws IOException {
		int moduleOffset = in.readInt();
		Module module = modules.get(streamName);
		if (module == null) {
			// First time we've seen the module. Add it to the ModuleMap and decompress it
			// later
			module = new Module();
			module.offset = moduleOffset;
			modules.put(streamName, module);
			// Would adding module.read(in) here be correct?
		} else {
			// Decompress a previously found module and store the decompressed result into
			// module.buf
			InputStream stream = new RLEDecompressingInputStream(
					new ByteArrayInputStream(module.buf, moduleOffset, module.buf.length - moduleOffset));
			module.read(stream);
			stream.close();
		}
	}

	/**
	 * Read <code>length</code> bytes of MBCS (multi-byte character set) characters
	 * from the stream
	 *
	 * @param stream  the inputstream to read from
	 * @param length  number of bytes to read from stream
	 * @param charset the character set encoding of the bytes in the stream
	 * @return a java String in the supplied character set
	 * @throws IOException
	 */
	private static String readString(InputStream stream, int length, Charset charset) throws IOException {
		byte[] buffer = new byte[length];
		int count = stream.read(buffer);
		return new String(buffer, 0, count, charset);
	}

	/**
	 * Skips <code>n</code> bytes in an input stream, throwing IOException if the
	 * number of bytes skipped is different than requested.
	 *
	 * @throws IOException
	 */
	private static void trySkip(InputStream in, long n) throws IOException {
		long skippedBytes = in.skip(n);
		if (skippedBytes != n) {
			if (skippedBytes < 0) {
				throw new IOException("Tried skipping " + n + " bytes, but no bytes were skipped. "
						+ "The end of the stream has been reached or the stream is closed.");
			} else {
				throw new IOException("Tried skipping " + n + " bytes, but only " + skippedBytes
						+ " bytes were skipped. " + "This should never happen.");
			}
		}
	}

	private POIFSFileSystem fs;

	public VBAMacroReader(File file) throws IOException {
		try {
			this.fs = new POIFSFileSystem(file);
		} catch (OfficeXmlFileException e) {
			openOOXML(new FileInputStream(file));
		}
	}

	public VBAMacroReader(InputStream rstream) throws IOException {
		PushbackInputStream stream = new PushbackInputStream(rstream, 8);
//		byte[] header8 = IOUtils.peekFirst8Bytes(stream);

		if (FileMagic.valueOf(stream) == FileMagic.OLE2) {
			fs = new POIFSFileSystem(stream);
		} else {
			openOOXML(stream);
		}
	}

	public VBAMacroReader(POIFSFileSystem fs) {
		this.fs = fs;
	}

	@Override
	public void close() throws IOException {
		fs.close();
		fs = null;
	}

	/**
	 * Recursively traverses directory structure rooted at <code>dir</code>. For
	 * each macro module that is found, the module's name and code are added to
	 * <code>modules</code>.
	 *
	 * @param dir
	 * @param modules
	 * @throws IOException
	 * @since 3.15-beta2
	 */
	protected void findMacros(DirectoryNode dir, ModuleMap modules) throws IOException {
		if (VBA_PROJECT_POIFS.equalsIgnoreCase(dir.getName())) {
			// VBA project directory, process
			readMacros(dir, modules);
		} else {
			// Check children
			for (Entry child : dir) {
				if (child instanceof DirectoryNode) {
					findMacros((DirectoryNode) child, modules);
				}
			}
		}
	}

	private void openOOXML(InputStream zipFile) throws IOException {
		ZipInputStream zis = new ZipInputStream(zipFile);
		ZipEntry zipEntry;
		while ((zipEntry = zis.getNextEntry()) != null) {
			if (endsWithIgnoreCase(zipEntry.getName(), VBA_PROJECT_OOXML)) {
				try {
					// Make a NPOIFS from the contents, and close the stream
					this.fs = new POIFSFileSystem(zis);
					return;
				} catch (IOException e) {
					// Tidy up
					zis.close();

					// Pass on
					throw e;
				}
			}
		}
		zis.close();
		throw new IllegalArgumentException("No VBA project found");
	}

	/**
	 * Reads all macros from all modules of the opened office file.
	 *
	 * @return All the macros and their contents
	 *
	 * @since 3.15-beta2
	 */
	public Map<String, String> readMacros() throws IOException {
		final ModuleMap modules = new ModuleMap();
		findMacros(fs.getRoot(), modules);

		Map<String, String> moduleSources = new HashMap<>();
		for (Map.Entry<String, Module> entry : modules.entrySet()) {
			Module module = entry.getValue();
			if (module.buf != null && module.buf.length > 0) { // Skip empty modules
				moduleSources.put(entry.getKey(), new String(module.buf, modules.charset));
			}
		}
		return moduleSources;
	}

	/**
	 * Reads VBA Project modules from a VBA Project directory located at
	 * <code>macroDir</code> into <code>modules</code>.
	 *
	 * @since 3.15-beta2
	 */
	protected void readMacros(DirectoryNode macroDir, ModuleMap modules) throws IOException {
		for (Entry entry : macroDir) {
			if (!(entry instanceof DocumentNode)) {
				continue;
			}

			String name = entry.getName();
			DocumentNode document = (DocumentNode) entry;
			try (DocumentInputStream dis = new DocumentInputStream(document)) {
				if ("dir".equalsIgnoreCase(name)) {
					// process DIR
					RLEDecompressingInputStream in = new RLEDecompressingInputStream(dis);
					String streamName = null;
					int recordId = 0;
					try {
						while (true) {
							recordId = in.readShort();
							if (EOF == recordId || VERSION_INDEPENDENT_TERMINATOR == recordId) {
								break;
							}
							int recordLength = in.readInt();
							switch (recordId) {
							case PROJECTVERSION:
								trySkip(in, 6);
								break;
							case PROJECTCODEPAGE:
								int codepage = in.readShort();
								String codepagestr = "Cp" + codepage;
								if (codepage == 10000) {
									codepagestr = "MacRoman";
								}
								System.out.println("Codepage: " + codepage);

								try {
									modules.charset = Charset.forName(codepagestr);
								} catch (UnsupportedCharsetException e) {
									modules.charset = Charset.forName("Cp1252");
								}
								break;
							case STREAMNAME:
								streamName = readString(in, recordLength, modules.charset);
								break;
							case MODULEOFFSET:
								readModule(in, streamName, modules);
								break;
							default:
								trySkip(in, recordLength);
								break;
							}
						}
					} catch (final IOException e) {
						throw new IOException("Error occurred while reading macros at section id " + recordId + " ("
								+ HexDump.shortToHex(recordId) + ")", e);
					} finally {
						in.close();
					}
				} else if (!startsWithIgnoreCase(name, "__SRP") && !startsWithIgnoreCase(name, "_VBA_PROJECT")) {
					// process module, skip __SRP and _VBA_PROJECT since these do not contain macros
					readModule(dis, name, modules);
				}
			}
		}
	}
}
