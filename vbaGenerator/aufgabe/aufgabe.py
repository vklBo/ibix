import json
import locale
import pickle
import base64
import logging

import numpy
import pandas
from jinja2 import Environment, select_autoescape, FileSystemLoader

import helper.allgemeinehelper
import helper.vbaimport
from aufgabe.aufgabentypfactory import aufgabentyp_from_konfiguration
from aufgabe.spaltenbeschreibung import Spaltenbeschreibung
from helper.db_helper import DBHelper
from helper.jinja import MyFileSystemLoader

class Aufgabe:
    ANZAHL_PRUEFZEILEN = 200

    def __init__(self):
        self.aufgabentemplate_id = 0
        self.datentemplate_id = 0
        self.daten_id = None
        self.spaltenliste = []
        self.anzahl_zeilen = 8
        self.teilaufgabenliste = []

    @classmethod
    def from_konfiguration(cls, konkretedaten_id, config: dict) -> None:
        aufgabentemplate_id = config["aufgabentemplate_id"]
        anzahl_zeilen = config["anzahl_zeilen"]
        return Aufgabe.from_aufgabentemplate_id(aufgabentemplate_id, konkretedaten_id, [], config, anzahl_zeilen)

    @classmethod
    def daten_from_datentemplate_id(cls, datentemplate_id: int, anzahl_zeilen: int = 8):
        aufgabe = Aufgabe()
        aufgabe.datentemplate_id = datentemplate_id
        aufgabe.anzahl_zeilen = anzahl_zeilen
        aufgabe.erstelle_struktur()
        aufgabe.fuelle_tabelle(anzahl_zeilen)
        aufgabe.setzeMinMax()
        return aufgabe
    
    @classmethod
    def daten_from_daten_id(cls, daten_id: int):
        aufgabe = Aufgabe()
        aufgabe.daten_id = daten_id
        struktur = aufgabe.lies_daten_aus_db(daten_id)
        aufgabe.erstelle_struktur(struktur)
        aufgabe.setzeMinMax()
        return aufgabe
    
    @classmethod
    def aufgabe_from_aufgabentemplate(cls, aufgabentemplate_id: int, daten_id: int, teilaufgaben: list, config = {}):
        if not (aufgabentemplate_id or daten_id):
            raise Exception("Aufgabe ohne Template oder Daten erstellen ist nicht möglich")
        aufgabe = Aufgabe().daten_from_daten_id(daten_id)
        aufgabe.aufgabentemplate_id = aufgabentemplate_id
        for key in teilaufgaben:
            aufgabe.teilaufgabenliste.append(
                aufgabentyp_from_konfiguration(aufgabe, key, config[key] if key in config.keys() else {}))
        return aufgabe
    
    @classmethod
    def aufgabe_from_aufgabentemplate_based_on_previous_aufgabe(cls, aufgabentemplate_id: int, previous_id: int, teilaufgaben: list, config = {}):
        if not (aufgabentemplate_id or previous_id):
            raise Exception("Aufgabe ohne Template und vorige Aufgabe erstellen ist nicht möglich")
        aufgabe = Aufgabe().aufgabe_from_db(previous_id)
        aufgabe.aufgabentemplate_id = aufgabentemplate_id
        teilaufgaben_dict = {teilaufgabe.get_aufgabennummer(): teilaufgabe for teilaufgabe in aufgabe.teilaufgabenliste}
        for key in teilaufgaben:
            if key in teilaufgaben_dict.keys():
                #Weitere Einzelaufgabe an Teilaufgaben anhängen
                teilaufgaben_dict[key].erweitereAufgabe(config[key] if key in config.keys() else {})
            else:
                #Neue Teilaufgabe erstellen
                aufgabe.teilaufgabenliste.append(
                    aufgabentyp_from_konfiguration(aufgabe, key, config[key] if key in config.keys() else {}))
        return aufgabe

    @classmethod
    def aufgabe_from_db(cls, konkreteaufgabe_id: int) -> None:
        sql = "Select aufgabentemplate_id, config, konkretedaten_id from konkrete_aufgabe where id = %s"
        (aufgabentemplate_id, config, konkretedaten_id) = DBHelper.lies_einzelnen_datensatz_als_tupel(sql, (konkreteaufgabe_id,))
        config = json.loads(config)
        aufgabe = Aufgabe().aufgabe_from_aufgabentemplate(aufgabentemplate_id, konkretedaten_id, config.keys(), config)
        return aufgabe
    
    def lies_daten_aus_db(self, daten_id):
        sql = "select tabellendaten, datentemplate_id, struktur from konkrete_daten where id = %s"
        (tabelle_als_json, datentemplate_id, config) = DBHelper.lies_einzelnen_datensatz_als_tupel(sql, (daten_id,))
        self.tabelle = pandas.DataFrame(json.loads(tabelle_als_json))
        self.datentemplate_id = datentemplate_id
        return json.loads(config)

    def save_daten_to_db(self):
        #sql = "INSERT INTO konkrete_daten(datentemplate_id, tabellendaten, struktur) VALUES (%s, %s, %s) returning id"
        sql = "INSERT INTO konkrete_daten(datentemplate_id, tabellendaten, struktur) VALUES (%s, %s, %s)"
        tabelle_als_json = self.tabelle.to_json()
        config = json.dumps(self.struktur_to_JSON())
        id = DBHelper.speicher_datensatz(sql, (self.datentemplate_id, tabelle_als_json, config, ))
        logging.info("neue Daten mit id: %d gespeichert.", id)
        return id
    
    def save_aufgabe_to_db(self):
        #sql = "INSERT INTO konkrete_aufgabe(aufgabentemplate_id, config, konkretedaten_id) VALUES (%s, %s, %s) returning id"
        sql = "INSERT INTO konkrete_aufgabe(aufgabentemplate_id, config, konkretedaten_id) VALUES (%s, %s, %s)"
        jsondict = self.teilaufgaben_to_JSON()
        jsonstring = json.dumps(jsondict)
        id = DBHelper.speicher_datensatz(sql, (self.aufgabentemplate_id, jsonstring, self.daten_id))
        logging.info("neue Aufgabe mit id: %d gespeichert.", id)
        return id

    def lies_aufgabenname(self):
        #sql = "select aufgabenname from aufgabe where id = %s"
        sql = "select name from aufgaben_template where id = %s"
        (name,) = DBHelper.lies_einzelnen_datensatz_als_tupel(sql, (self.aufgabentemplate_id,))
        return name

    def lies_aufgabenstruktur(self):
        sql = 'SELECT * from spaltendefinitionvollstaendig WHERE datentemplate_id=%s order by ordnung'
        spaltendefinitionen = DBHelper.lies_datensaetze_als_liste(sql, (self.datentemplate_id,))
        self.spaltenliste = [Spaltenbeschreibung.from_beschreibungstupel(beschreibungstupel)
                             for beschreibungstupel in spaltendefinitionen]

    def intialisiere_aufgabe(self, config: dict = {}) -> None:
        for spalte in self.spaltenliste:
            spaltenconfig = config.get(spalte.name, {})
            spalte.initialisiere(self.spaltenliste, spaltenconfig)

    def fuelle_tabelle(self, anzahlzeilen):
        self.tabelle = None
        for i in range(anzahlzeilen):
            werte_der_zeile = {}
            neue_werte = None
            for spalte in self.spaltenliste:
                neue_werte = spalte.generiere_wert(werte_der_zeile)
                werte_der_zeile.update(neue_werte)
            if self.tabelle is None:
                self.tabelle = pandas.DataFrame(columns=werte_der_zeile.keys())
            self.tabelle.loc[i] = werte_der_zeile.values()
        self.setzeMinMax()

    def erstelle_struktur(self, config: dict = {}):
        self.lies_aufgabenstruktur()
        self.intialisiere_aufgabe(config)

    def setzeMinMax(self):
        for spalte in self.spaltenliste:
            if spalte.typ in ['berechnet', 'bestimmt']:
                spalte.setzeMinMax(self.tabelle[spalte.name].min(), self.tabelle[spalte.name].max())

    def struktur_to_JSON(self):
        json_dict = {}
#        json_dict["datentemplate_id"] = self.datentemplate_id
#        json_dict["anzahl_zeilen"] = self.anzahl_zeilen
        for spalte in self.spaltenliste:
            spaltenconfig = spalte.to_JSON()
            if bool(spaltenconfig):  # Nur hinzufügen, wenn gefüllt
                json_dict[spalte.name] = spaltenconfig
        return json_dict
    
    def teilaufgaben_to_JSON(self):
        json_dict = {}
 #       json_dict["aufgabentemplate_id"] = self.aufgabentemplate_id
 #       json_dict["datentemplate_id"] = self.datentemplate_id
 #       json_dict["anzahl_zeilen"] = self.anzahl_zeilen
        for teilaufgabe in self.teilaufgabenliste:
            json_dict[teilaufgabe.get_name()] = teilaufgabe.to_JSON()
        return json_dict

    def get_spalten_mit_spaltennummern_sortiert(self):
        gefundeneSpalten = [spalte for spalte in self.spaltenliste if spalte.spaltennr is not None]
        return sorted(gefundeneSpalten, key=lambda spalte: spalte.spaltennr)

    def get_spalte_mit_ordnung(self, ordnung):
        for spalte in self.spaltenliste:
            if spalte.ordnung == ordnung:
                return spalte

    def erstelle_dokument(self, html=True, klausur=False):
        templates = "templates"
        if not html:
            templates = ["templates_tex", "templates"]

        daten = self.generiere_daten_fuer_darstellung()
        daten["klausur"] = klausur

        for teilaufgabe in self.teilaufgabenliste:
            daten.update(teilaufgabe.generiere_daten_fuer_darstellung())

        env = Environment(loader=MyFileSystemLoader(templates, html=html), autoescape=select_autoescape(),
                          trim_blocks=True,
                          lstrip_blocks=True)
        template = env.get_template("base.jinja")
        doctext = template.render(daten)
        return doctext
    
    def erstelle_tabelle(self, alleSpalten = False):
        templates = "templates"

        daten = self.generiere_daten_fuer_darstellung(alleSpalten = alleSpalten)
        daten["nurTabelle"] = True

        env = Environment(loader=MyFileSystemLoader(templates, html=True), autoescape=select_autoescape(),
                          trim_blocks=True,
                          lstrip_blocks=True)
        template = env.get_template("base.jinja")
        doctext = template.render(daten)
        return doctext

    def erstelle_vbacode(self):
        templates = ["templates_vbacode", "templates"]

        daten = {}

        for teilaufgabe in self.teilaufgabenliste:
            daten.update(teilaufgabe.ermittle_vba_code())

        env = Environment(loader=FileSystemLoader(templates), autoescape=select_autoescape(),
                          trim_blocks=True,
                          keep_trailing_newline=True,
                          lstrip_blocks=True)
        template = env.get_template("base.jinja")
        doctext = template.render(daten)
        return doctext

    def generiere_tabellenkopie_as_zweidim_liste(self, tabelle=None, formatiert=True, alleSpalten=False, leereZusatzzeile=False):
        spaltenliste = []
        spaltennamen = []
        formate = []
        spaltentitel = []

        if not alleSpalten:
            spalten = [spalte for spalte in self.spaltenliste if spalte.spaltennr is not None]
            spalten = sorted(spalten, key=lambda spalte: spalte.spaltennr)
        else:
            spalten = self.spaltenliste # alle Saplten
            spalten = sorted(spalten, key=lambda spalte: spalte.ordnung)
            
        for spalte in spalten:
            spaltenliste.append(spalte.spaltennr)
            spaltennamen.append(spalte.name)
            spaltentitel.append(spalte.titel)
            formate.append(spalte.get_format_for_locale())

        if tabelle is not None:
            tabellenkopie = tabelle[spaltennamen].copy()
        else:
            tabellenkopie = self.tabelle[spaltennamen].copy()
        flatTabelle = []
        for i in range(len(tabellenkopie)):
            zeile = []
            for j, value in enumerate(tabellenkopie.iloc[i].tolist()):
                if formatiert:
                    if "f" in formate[j]:
                        if not helper.allgemeinehelper.is_number(value):
                            zeile.append(value)
                        else:
                            zeile.append(locale.format_string(formate[j], value))
                    else:
                        zeile.append(locale.format_string(formate[j], value))
                else:
                    zeile.append(value)
            if leereZusatzzeile:     # Für die Bewertung müssen ggf. mehr Zeilen und wie hier auch Spalten angelegt werden.
                zeile.append("")
            flatTabelle.append(zeile)
        if leereZusatzzeile:
            zeile = [""]
            for j, value in enumerate(tabellenkopie.iloc[0].tolist()):
                zeile.append("")
            flatTabelle.append(zeile)
        return (flatTabelle, spaltentitel)

    def generiere_daten_fuer_darstellung(self, alleSpalten = False) -> dict:
        daten = {}
        if self.aufgabentemplate_id:
            daten["aufgabennummer"] = str(self.aufgabentemplate_id)
            daten["aufgabenname"] = self.lies_aufgabenname()
        (tabellenkopie, spaltentitel) = self.generiere_tabellenkopie_as_zweidim_liste(alleSpalten = alleSpalten)
        daten['tabelle'] = tabellenkopie
        daten['spaltentitel'] = spaltentitel
        return daten

    def erstelle_excel_datei(self, dateiname: str, tabelle=None):
        spaltennamen = []
        spaltentitel = []
        for spalte in self.spaltenliste:
            if spalte.spaltennr is not None:
                spaltennamen.append(spalte.name)
                spaltentitel.append(spalte.titel)

        if tabelle is None:
            csvTabelle = self.tabelle[spaltennamen].copy()
        else:
            csvTabelle = tabelle[spaltennamen].copy()

        csvTabelle.columns = spaltentitel
        if self.teilaufgabenliste and self.teilaufgabenliste[0].ohneZielspalte():
            csvTabelle[csvTabelle.columns[-1]] = numpy.nan
        csvTabelle.to_excel(dateiname, index=False, sheet_name="Tabelle1", encoding='utf8')

    def bewerte_loesung(self, vbaCode: str, neueDaten=True, vollstaendigeInfos=False):
        if neueDaten:
            self.fuelle_tabelle(Aufgabe.ANZAHL_PRUEFZEILEN)
        else:
            self.fuelle_tabelle(20)
            

        (tabellenkopie, spaltentitel) = self.generiere_tabellenkopie_as_zweidim_liste(formatiert=False,leereZusatzzeile=True)
        # Die Überschriftenzeile erstellen, damit das VBA-Programm in der richtigen Zeile starten kann
        spaltentitel.append("")
        sheets_tmp = [spaltentitel]
        sheets_tmp.extend(tabellenkopie)
        sheets = [sheets_tmp]

        vba = helper.vbaimport.vbaimport()
        vba.stelle_loesungsprogramm_zur_verfuegung(vbaCode)
        vba.stelle_tabelle_zur_verfuegung(sheets)
        ergebnis = {}
        for teilaufgabe in self.teilaufgabenliste:
            ergebnis[teilaufgabe.get_name()] = teilaufgabe.bewerte_loesung(sheets, neueDaten, vollstaendigeInfos)
        return ergebnis

    def ermittle_vba_code(self):
        vba_code_fragmente = {}
        for teilaufgabe in self.teilaufgabenliste:
            vba_code_fragmente[teilaufgabe.get_name()] = teilaufgabe.ermittle_vba_code()
        return vba_code_fragmente
