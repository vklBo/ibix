/*
 * LdapUserServiceImpl.java 
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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsbo.ibix.config.PropertiesReader;
import de.hsbo.ibix.model.User;
import de.hsbo.ibix.repository.UserRepository;
import de.hsbo.ibix.service.LdapUserService;

/**
 * The Class LdapUserServiceImpl.
 */
@Service
public class LdapUserServiceImpl implements LdapUserService {
	static Logger log = LoggerFactory.getLogger(LdapUserServiceImpl.class);

	@Autowired
	private PropertiesReader propertiesReader;

	@Autowired
	private UserRepository userRepo;

	private ExecutorService executor;
	Hashtable<String, Object> env = new Hashtable<String, Object>();

	public LdapUserServiceImpl() {
		executor = Executors.newFixedThreadPool(5);
		env = new Hashtable<String, Object>(11);

		String ldapSearchBase;
		String ldapAdServer;
		String ldapUsername;
		String ldapPassword;
		String userDN;

		ldapSearchBase = propertiesReader.getLdapBase();
		ldapAdServer = propertiesReader.getLdapUrl() + ldapSearchBase;
		ldapUsername = propertiesReader.getLdapUsername();
		ldapPassword = propertiesReader.getLdapPassword();

		userDN = "uid=" + ldapUsername + ", ou=People, o=isp";
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, userDN);
		env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapAdServer);
		// Enable connection pooling
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		env.put("com.sun.jndi.ldap.connect.timeout", "5000");
	}

	public LdapContext getCtx() throws NamingException, Exception {
		LdapContext ctx;

		ctx = new InitialLdapContext(env, null);

		log.info("LDAP context: {}", ctx);
		return ctx;
	}

	@Override
	public void vervollstaendigeUserDaten(User user) {
		executor.execute(new LdapUserDetailLadenExecutor(user));
	}

	@Override
	public void vervollstaendigeUserDatenFuerListe(Collection<User> userliste) {
		Iterator<User> iter = userliste.iterator();

		while (iter.hasNext())
			vervollstaendigeUserDaten(iter.next());
	}

	/**
	 * The Class LdapUserDetailLadenExecutor.
	 */
	private class LdapUserDetailLadenExecutor implements Runnable {
		User user;

		public LdapUserDetailLadenExecutor(User user) {
			super();
			this.user = user;
		}

		@Override
		public void run() {
			LdapContext ctx = null;
			NamingEnumeration<SearchResult> results = null;
			String username = user.getUsername().toLowerCase();

			if (username.equals("anonymoususer")) {
				return;
			}
			try {
				String searchFilter = "(uid=" + username + ")";
				log.info("Searching users with filter:\t{}", searchFilter);

				SearchControls searchControls = new SearchControls();
				searchControls.setCountLimit(0);
				searchControls.setDerefLinkFlag(true);
				searchControls.setReturningObjFlag(true);
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

				ctx = getCtx();
				results = ctx.search("", searchFilter, searchControls);

				while (results.hasMoreElements()) {
					SearchResult searchResult = results.nextElement();
					if (searchResult != null) {
						log.debug(searchResult.toString());
						Attributes attr = searchResult.getAttributes();
						if (attr.get("uid") != null && attr.get("uid").get().equals(username)) {
							user.setVorname(getAttribute(attr, "givenName"));
							user.setNachname(getAttribute(attr, "sn"));
							user.setEmail(getAttribute(attr, "mail"));
							user.setMatrikelnummer(getAttribute(attr, "fhboStudentNumber"));
							userRepo.save(user);
						}
					}
				}
			} catch (NameNotFoundException e) {
				log.info("User {} in Ldap nicht gefunden", username);
			} catch (javax.naming.CommunicationException e) {
				log.error(e.getMessage());
			} catch (javax.naming.NamingException e) {
				log.error(e.getMessage());
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					results.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			try {
				ctx.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

		private String getAttribute(Attributes atts, String name) throws NamingException {
			String ergebnis = null;
			Attribute att = atts.get(name);
			if (att != null) {
				if (att.size() > 0) {
					ergebnis = (String) att.get(0);
				}
			}
			return ergebnis;
		}
	}
}