# ibix
Individualized computer science exercises

Project at Bochum University of Applied Sciences on the generation of learning speed-dependent exercises in computer science

Funded by "Stiftung Innovation in der Hochschullehre"

## Requirements

- Java (JRE 17)
- Python3
- MySql Database

## Adaptation to own environment

The credential check at login and the retrieval of user data from the Ldap bases on the environment at the University of Bochum. The corresponding source code must be adapted to your own environment.

Classes:
- de.hsbo.ibix.security.LdapAuthenticationProvider
- de.hsbo.ibix.service.impl.LdapUserService

## Installation
 
- Create a Database (Engine InnoDb)
- Add two tables:

```
	CREATE TABLE `konkrete_aufgabe` (
	  `id` int NOT NULL AUTO_INCREMENT,
	  `aufgabentemplate_id` int NOT NULL,
	  `config` json NOT NULL,
	  `konkretedaten_id` int DEFAULT NULL,
	  PRIMARY KEY (`id`)
	);
	
	CREATE TABLE `konkrete_daten` (
	  `id` int NOT NULL AUTO_INCREMENT,
	  `tabellendaten` json DEFAULT NULL,
	  `datentemplate_id` int DEFAULT NULL,
	  `struktur` json DEFAULT NULL,
	  PRIMARY KEY (`id`)
	);
```

- Add a view:

```
	CREATE  VIEW `spaltendefinitionvollstaendig`
	AS SELECT
	   `template_spalte`.`template_id` AS `datentemplate_id`,
	   `template_spalte`.`id` AS `id`,
	   `template_spalte`.`ordnung` AS `ordnung`,
	   `template_spalte`.`spaltennr` AS `spaltennr`,
	   `spaltendefinition`.`titel` AS `titel`,
	   `spaltendefinition`.`name` AS `name`,
	   `spaltendefinition`.`typ` AS `typ`,
	   `spaltendefinition`.`auswahl` AS `auswahl`,
	   `spaltendefinition`.`inhalt` AS `inhalt`,
	   `spaltendefinition`.`intervalle` AS `intervalle`,
	   `spaltendefinition`.`config` AS `config`
	FROM (`template_spalte` join `spaltendefinition`) where (`template_spalte`.`spalte_id` = `spaltendefinition`.`id`);
```

- Configure Java-Application

```
File: src/main/resourcesapplication-prod.properties
```

- Configure Python-Application

```
File: vbaGenerator/helper/db_helper.py
```

- Start Python-Application

In directory "vbaGenerator":

```
python3 main.py
```

- Start Java-Application

In den root directory:

```
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```


