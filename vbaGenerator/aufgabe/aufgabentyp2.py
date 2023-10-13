import locale
import random
import logging

import numpy
import json

import helper.allgemeinehelper
from aufgabe.aufgabentyp import Aufgabentyp
from helper.db_helper import DBHelper



class Aufgabentyp2(Aufgabentyp):
    def __init__(self, aufgabe):
        super(Aufgabentyp2, self).__init__(aufgabe)
        self.einzelaufgabenliste = []

    def lies_konfiguration(self) -> dict:
        sql = "Select subtypen, spalten, bedingungsspalten, prozedurname, allgemeiner_text from aufgaben_template, config_restaufgaben where aufgaben_template.id=%s and aufgaben_template.config_statistiken_id=config_restaufgaben.id"
        (subtypen, spalten, bedingungsspalten, prozedurname, allgemeiner_text) = DBHelper.lies_einzelnen_datensatz_als_tupel(sql, (self.aufgabe.aufgabentemplate_id, ))
        config = {"typen": json.loads(subtypen), "spalten": json.loads(spalten), "bedingungsspalten": json.loads(bedingungsspalten)}
        if (prozedurname):
            config["prozedurname"] = prozedurname
        if (allgemeiner_text):
            config["allgemeiner_text"] = allgemeiner_text
        return config

    def erstelle_aufgabe_aus_konfiguration(self, config):
        if "teilaufgabenliste" in config.keys():
            self.erstelle_einzelaufgaben_aus_konfiguration(config)
        else:
            self.erstelleAufgabe(config)
        pass
    
    def erweitereAufgabe(self, config):
        self.config = self.lies_konfiguration()
        self.erstelleAufgabe(config)

    def erstelle_einzelaufgaben_aus_konfiguration(self, config):
        teilaufgabenliste = config["teilaufgabenliste"]
        for teilaufgabe in teilaufgabenliste:
            spalteninfo = self.aufgabe.get_spalte_mit_ordnung(teilaufgabe["spalte"])
            bedingungsspalteninfo = None
            if "bedingungsspalte" in teilaufgabe.keys():
                bedingungsspalteninfo = self.aufgabe.get_spalte_mit_ordnung(teilaufgabe["bedingungsspalte"])
            aufgabentyp = teilaufgabe["aufgabentyp"]
            neueTeilaufgabe = Statistikaufgabe(aufgabentyp, spalteninfo, bedingungsspalteninfo, teilaufgabe)
            self.einzelaufgabenliste.append(neueTeilaufgabe)

    def erstelleAufgabe(self, config):
        self.waehle_spalten_aus(config)
        vorgegebeneAufgabentypen = self.getConfig(config, "typen")
        for aufgabentyp in vorgegebeneAufgabentypen:
            if type(aufgabentyp) is list:
                aufgabentyp = random.choice(aufgabentyp)
            spalte = random.choice(self.zahlenspalten)
            spalteninfo = self.aufgabe.get_spalte_mit_ordnung(spalte)
            bedingungsSpalte = random.choice(self.bedingungsspalten)
            bedingungsspalteninfo = self.aufgabe.get_spalte_mit_ordnung(bedingungsSpalte)
            neueTeilaufgabe = Statistikaufgabe(aufgabentyp, spalteninfo, bedingungsspalteninfo, config)
            self.einzelaufgabenliste.append(neueTeilaufgabe)

    def berechneWerte(self):
        for einzelaufgabe in self.einzelaufgabenliste:
            einzelaufgabe.berechne_wert(self.aufgabe.tabelle)

    def generiere_daten_fuer_darstellung(self) -> dict:
        self.berechneWerte()
        daten = {}
        daten["aufgabe2"] = True

        statistikaufaufgaben = []
        for einzelaufgabe in self.einzelaufgabenliste:
            statistikaufaufgaben.append((einzelaufgabe.get_aufgabentext(),
                                         locale.format_string(einzelaufgabe.get_formatstring(),
                                                              einzelaufgabe.wert)))
        daten["statistikaufaufgaben"] = statistikaufaufgaben
        if "allgemeiner_text" in self.config.keys():
            daten["statistiktext"] = self.config.get("allgemeiner_text");
        daten["statistikprozedurname"] = self.config.get("prozedurname", "statistiken_click");
        daten["statistikaufaufgaben"] = statistikaufaufgaben
        return daten

    def bewerte_loesung(self, sheets, neueDaten=True, vollstaendigeInfos=False):
        prozedurname = self.config.get("prozedurname", "statistiken_click")
        ergebnisTabelle = [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] for i in range(len(self.einzelaufgabenliste)+20)]
        sheets.append(ergebnisTabelle)
        self.berechneWerte()
        fehlerhafteDatensaetze = []
        korrekt = 0
        fehler = 0
        print("Bewertung 'Statistiken':")
        try:
            exec("from helper.vbaimport import " + prozedurname)
        except Exception as err:
            print("FEHLER. Prozedur '{}' nicht gefunden.".format(prozedurname))
            return 0
        try:
            exec(prozedurname + "()")
        except Exception as err:
            print("FEHLER. Programm nicht ausführbar (" + str(err) + ")")
            return 0

        for i, einzelaufgabe in enumerate(self.einzelaufgabenliste):
            zielwert = einzelaufgabe.wert
            ergebnis = sheets[1][i][1]
            try:
                if abs(zielwert - ergebnis) < 0.0001:
                    korrekt += 1
                    print("OK. {}: Wert: {}".format(einzelaufgabe.get_aufgabentext(), ergebnis))
                else:
                    fehler += 1
                    print("FALSCH: {}: Zielwert: {}, Berechnet: {}".format(einzelaufgabe.get_aufgabentext(), zielwert, ergebnis))
                    fehlerhafteDatensaetze.append((einzelaufgabe.get_aufgabentext(), zielwert, ergebnis))
            except Exception as e:
                fehler += 1
                print("FALSCH: {}: Fehler: {}".format(einzelaufgabe.get_aufgabentext, e))
            
        if (vollstaendigeInfos):
            return (fehlerhafteDatensaetze)
        
        if (korrekt + fehler) > 0:
            return (korrekt / (korrekt + fehler) * 100)
        return 0

    def get_templatename(self):
        return "aufgabe2.jinja"

    def to_JSON(self):
        json_list = []
        for einzelaufgabe in self.einzelaufgabenliste:
            json_list.append(einzelaufgabe.to_JSON())
        return {"teilaufgabenliste": json_list}

    def ermittle_vba_code(self):
        konstanten = set()
        variablen = []
        texte = []
        for einzelaufgabe in self.einzelaufgabenliste:
            konstanten.update(einzelaufgabe.get_konstanten())
            variablen.extend(einzelaufgabe.get_variablen())
            texte.append(einzelaufgabe.get_aufgabentext())
        return {"aufgabe2": True, "konstanten_aufgabe2": list(konstanten), "variablen_aufgabe2": variablen,
                "texte_aufgabe2": texte}

    def get_name(self) -> str:
        return ("2")

    def get_aufgabennummer(self) -> int:
        return 2


class Statistikaufgabe():
    def __init__(self, aufgabentyp, spalteninfo, bedingungsspalteninfo, config):
        # Es muss noch unterschieden werden, zwischen den Daten, die zur Definition der Aufgabe erforderlich sind (damit sie aus einer Konfiguration erstellt werden können
        # und Daten, die, wenn die Aufgabe konfiguriert ist, dynamisch erzeugt werden können.
        self.aufgabentyp = aufgabentyp
        self.spalteninfo = spalteninfo
        self.wert = None
        self.bedingung = None
        self.bedingungsspalteninfo = None

        aufgabe = self.get_aufgabentypdefinition()
        if "bedingung" in aufgabe["berechnung"]:
            self.bedingungsspalteninfo = bedingungsspalteninfo
            self.bedingung = self.bedingungsspalteninfo.erstelle_einzelne_bedingung(config)

    def get_aufgabentypdefinition(self):
        return helper.allgemeinehelper.STATISTIKAUFGABENTYPEN[self.aufgabentyp]

    def get_konstanten(self):
        konstanten = set()
        if self.aufgabentyp != 1:
            konstanten.add(helper.allgemeinehelper.gueltiger_name(self.spalteninfo.name.upper() + "_SPALTE"))
        if self.bedingungsspalteninfo:
            konstanten.add(helper.allgemeinehelper.gueltiger_name(self.bedingungsspalteninfo.name.upper() + "_SPALTE"))
        return konstanten

    def get_variablen(self):
        return [helper.allgemeinehelper.gueltiger_name(
            self.get_aufgabentypdefinition()["var"].format(name=self.spalteninfo.name.capitalize(),
                                                           bedingung=self.get_bedingungstext()))]

    def get_bedingungstext(self):
        if self.bedingung is None:
            bedingungstext = ""
        elif type(self.bedingung.wert) == str:
            bedingungstext = self.bedingung.name.capitalize() + self.bedingung.wert.capitalize()
        else:
            bedingungstext = self.bedingung.name.capitalize() + str(self.bedingung.wert)
        return bedingungstext

    def get_berechnung(self):
        aufgabe = self.get_aufgabentypdefinition()
        berechnung = str.format(aufgabe["berechnung"], name=self.spalteninfo.name,
                                bedingung=("" if self.bedingung is None else self.bedingung.get_bedingung()))
        return berechnung

    def get_aufgabentext(self):
        aufgabe = self.get_aufgabentypdefinition()
        return str.format(aufgabe["text"], name=self.spalteninfo.name.capitalize(),
                          bedingung=("" if self.bedingung is None else self.bedingung.get_bedingungstext()))

    def get_formatstring(self):
        aufgabe = self.get_aufgabentypdefinition()
        if "format" in aufgabe.keys():
            return aufgabe["format"]
        return self.spalteninfo.get_format_for_locale()

    def berechne_wert(self, tabelle):
        self.wert = eval(self.get_berechnung(), {}, {"tabelle": tabelle})
        if isinstance(self.wert, numpy.generic):
            self.wert = self.wert.item()

    def to_JSON(self):
        json_dict = {}
        json_dict["aufgabentyp"] = self.aufgabentyp
        json_dict["spalte"] = self.spalteninfo.ordnung
        if self.bedingung is not None:
            json_dict["bedingungsspalte"] = self.bedingungsspalteninfo.ordnung
            json_dict["vergleichsoperator"] = self.bedingung.vergleichsoperator
            json_dict["vergleichswert"] = self.bedingung.wert
        return json_dict
