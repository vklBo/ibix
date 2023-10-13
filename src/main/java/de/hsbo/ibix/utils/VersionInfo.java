/*
 * VersionInfo.java 
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

import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VersionInfo.
 */
public class VersionInfo {

	static Logger log = LoggerFactory.getLogger(VersionInfo.class);
	String name;
	String group;
	String version;

	private VersionInfo(String gid, String aid, String version) {
		this.group = gid;
		this.name = aid;
		this.version = version;
	}

	public String artifactId() {
		return name;
	}

	public String version() {
		return version;
	}

	public String groupId() {
		return group;
	}

	@Override
	public String toString() {
		return groupId() + ":" + artifactId() + ":" + version();
	}

	public static VersionInfo get(String groupId, String name) {
		try {
			Properties p = new Properties();
			URL url = VersionInfo.class.getResource("/META-INF/maven/" + groupId + "/" + name + "/pom.properties");
			if (url != null) {
				log.trace("Reading version from {}", url);
				p.load(url.openStream());

				for (Object k : p.keySet()) {
					log.trace("  {} = {}", k, p.getProperty(k.toString()));
				}

				String version = p.getProperty("version");
				return new VersionInfo(groupId, name, version);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new VersionInfo(groupId, name, "?");
	}
}