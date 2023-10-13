/*
 * ConfigRestaufgabenServiceImpl.java 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.model.ConfigRestaufgaben;
import de.hsbo.ibix.repository.ConfigRestaufgabenRepository;
import de.hsbo.ibix.service.ConfigRestaufgabenService;

/**
 * The Class ConfigRestaufgabenServiceImpl.
 */
@Service
public class ConfigRestaufgabenServiceImpl implements ConfigRestaufgabenService {
	final static Logger log = LoggerFactory.getLogger(ConfigRestaufgabenServiceImpl.class);

	@Autowired
	ConfigRestaufgabenRepository configRepo;

	@Override
	public ConfigRestaufgaben getReferenceById(int id) {
		return configRepo.getReferenceById(id);
	}

	@Override
	public void speicher(ConfigRestaufgaben config) {
		configRepo.save(config);
	}

	@Override
	public ConfigRestaufgaben update(ConfigRestaufgaben config) {
		configRepo.save(config);

		return config;
	}

}
