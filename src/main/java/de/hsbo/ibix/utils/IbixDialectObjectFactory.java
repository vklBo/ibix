/*
 * IbixDialectObjectFactory.java 
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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

/**
 * A factory for creating IbixDialectObject objects.
 */
public class IbixDialectObjectFactory implements IExpressionObjectFactory {

	static Logger log = LoggerFactory.getLogger(IbixDialectObjectFactory.class);
	final Map<String, Class<?>> objectClasses = new TreeMap<>();

	public IbixDialectObjectFactory() {
		log.debug("Creating new AnalyticsDialectObjectFactory...");
//		objectClasses.put("bytes", ByteSizeFormat.class);
		objectClasses.put("app", Version.class);
//		objectClasses.put("permissions", Permissions.class);
	}

	@Override
	public Object buildObject(IExpressionContext ctx, String name) {
		log.trace("Building object for name '{}' in context: {}", name, ctx);

		try {
			Class<?> clazz = objectClasses.get(name);
			log.trace("Using class '{}' to create object...", clazz);
			if (clazz != null) {
				Object obj = clazz.getDeclaredConstructor().newInstance();
				return obj;
			}
		} catch (Exception e) {
			log.error("Failed to build object for name '{}': {}", name, e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Set<String> getAllExpressionObjectNames() {
		return objectClasses.keySet();
	}

	@Override
	public boolean isCacheable(String arg0) {
		return true;
	}

}