import locale
import re
import logging

from helper.db_helper import DBHelper
from aufgabe.aufgabentyp import Aufgabentyp


class Aufgabentyp1(Aufgabentyp):
    def __init__(self, aufgabe):
        super(Aufgabentyp1, self).__init__(aufgabe)
        self.zeileFuerFunktionsaufruf = 2

    def erstelle_aufgabe_aus_konfiguration(self, config):
        # nichts zu tun, da ja alles schon durch die Spaltenstruktur für die Tabelle bestimmt wurde
        pass

    def bestimme_verwendete_spalten(self):
        berechnungsdaten = []
        # Alles, was nach Variablennamen aussieht, in den Berechnungen suchen
        for spalte in self.aufgabe.spaltenliste:
            if spalte.typ in ["bestimmt", "berechnet"]:
                berechnungsdaten.extend(re.findall("([a-zA-Z]\w*)", spalte.inhalt))

        verwendeteSpalten = {spalte.spaltennr: spalte.name for spalte in self.aufgabe.spaltenliste if
                             spalte.spaltennr is not None and spalte.name in berechnungsdaten}
        keys = verwendeteSpalten.keys()
        keys = sorted(keys)
        sortierte_verwendete_spalten = {key: verwendeteSpalten[key] for key in keys}
        return sortierte_verwendete_spalten

    def ermittle_funktionsname_aus_db(self):
        sql = "SELECT funktionsname FROM config_funktionen, aufgaben_template where aufgaben_template.config_funktionen_id = config_funktionen.id and aufgaben_template.id = %s"
        (funktionsname, ) = DBHelper.lies_einzelnen_datensatz_als_tupel(sql, (self.aufgabe.aufgabentemplate_id,))
        if funktionsname and funktionsname.strip():
            return funktionsname
        return None

    def ermittle_funktionsaufruf(self, mit_buchstaben=True):
        verwendeteSpalten = self.bestimme_verwendete_spalten()
        
        funktionsname = self.ermittle_funktionsname_aus_db();
        if not funktionsname:
            if "funktionsname" in self.config:
                funktionsname = self.config["funktionsname"]
            else:
                ergebnisspaltennummer = max([spalte.spaltennr for spalte in self.aufgabe.spaltenliste if
                                             spalte.typ == "berechnet" and spalte.spaltennr is not None])
                for spalte in self.aufgabe.spaltenliste:
                    if spalte.spaltennr == ergebnisspaltennummer:
                        funktionsname = "berechne" + spalte.name.capitalize()
                        break

        if mit_buchstaben:
            funktionsaufruf = "{}({})".format(funktionsname, "; ".join(
                [chr(spaltennummer + 64) + str(self.zeileFuerFunktionsaufruf) for spaltennummer in
                 verwendeteSpalten.keys()]))
        else:
            funktionsaufruf = "{}({})".format(funktionsname,
                                              ", ".join(["{" + name + "}" for name in verwendeteSpalten.values()]))
        return funktionsaufruf

    def generiere_daten_fuer_darstellung(self) -> dict:
        self.speicher_template()
        daten = {}
        daten["aufgabe1"] = True
        daten["funktionsaufruf"] = self.ermittle_funktionsaufruf()
        for spalte in self.aufgabe.spaltenliste:
            if spalte.ordnung == 0:
                daten[spalte.name] = locale.format_string(spalte.get_format_for_locale(), spalte.wert)
            if spalte.typ == "bestimmt":
                auswahlliste = []
                formatstring = spalte.get_format_for_locale()
                for i, bedingung in enumerate(spalte.abhaengigeSpalte.bedingungen):
                    auswahl = {}
                    auswahl["text"] = bedingung.get_bedingungstext()
                    auswahl["auswahlwert"] = bedingung.get_bedingungstext()
                    auswahl["zielwert"] = locale.format_string(formatstring, spalte.werte_liste[i])
                    auswahlliste.append(auswahl)
                daten[spalte.name] = auswahlliste
        return daten

    def bewerte_loesung(self, sheets, neueDaten=True, vollstaendigeInfos=False):
        fehlerhafteDatensaetze = []
        korrekt = 0
        fehler = 0

        funktionsaufruf = self.ermittle_funktionsaufruf(False)
        spaltennamen = self.aufgabe.tabelle.columns.values.tolist()
        zielspalte = len(spaltennamen) - 1

        funktionsname = funktionsaufruf.split("(")[0]
        print("Bewertung 'Eigendefinierte Funktion':")
        try:
            exec("from helper.vbaimport import " + funktionsname)
        except Exception as err:
            print("FEHLER: Funktion '{}' nicht gefunden".format(funktionsname))
            return 0

        for i in self.aufgabe.tabelle.index:
            parameter = self.aufgabe.tabelle.iloc[i].to_dict()
            for k in parameter:
                if type(parameter[k]) == str:
                    parameter[k] = "'{}'".format(parameter[k])
            funktionsaufrufMitParametern = funktionsaufruf.format(**parameter)
            try:
                ergebnis = eval(funktionsaufrufMitParametern)
            except Exception as err:
                print("FALSCH. Funktion konnte nicht ausgeführt werden ({})".format(err))
                fehler += 1
                fehlerhafteDatensaetze.append((funktionsaufrufMitParametern, err, 0))
            else:
                zielwert = self.aufgabe.tabelle.iloc[i].iloc[zielspalte]
                if type(zielwert) is str or type(ergebnis) is str:
                    if ergebnis == zielwert:
                        korrekt += 1
                        print("OK. {}, Berechnet: {}.".format(funktionsaufrufMitParametern, ergebnis))
                    else:
                        fehler += 1
                        print("FALSCH. {}, Zielwert: {}, Berechnet: {}.".format(funktionsaufrufMitParametern, zielwert, ergebnis))
                        fehlerhafteDatensaetze.append((funktionsaufrufMitParametern, zielwert, ergebnis))
                else:
                    if abs(zielwert - ergebnis) < 0.0001:
                        korrekt += 1
                        print("OK. {}, Berechnet: {}.".format(funktionsaufrufMitParametern, ergebnis))
                    else:
                        fehler += 1
                        print("FALSCH. {}, Zielwert: {}, Berechnet: {}.".format(funktionsaufrufMitParametern, zielwert, ergebnis))
                        fehlerhafteDatensaetze.append((funktionsaufrufMitParametern, zielwert, ergebnis))
        if (vollstaendigeInfos):
            return (fehlerhafteDatensaetze)
        return (korrekt / (korrekt + fehler) * 100)

    def speicher_template(self):
        sql = "SELECT aufgabentext FROM config_funktionen, aufgaben_template where aufgaben_template.config_funktionen_id = config_funktionen.id and aufgaben_template.id = %s"
        (aufgabentext, ) = DBHelper.lies_einzelnen_datensatz_als_tupel(sql, (self.aufgabe.aufgabentemplate_id,))
        path = "templates/" + self.get_templatename();
        datei = open(path,'w')
        datei.write(aufgabentext)

    def get_templatename(self):
        return "aufgabe1/{}.jinja".format(self.aufgabe.aufgabentemplate_id)

    def to_JSON(self):
        return {}

    def ermittle_vba_code(self):
        return {"aufgabe1": True}

    def get_name(self) -> str:
        return ("1")

    def get_aufgabennummer(self) -> int:
        return 1
    
    def ohneZielspalte(self):
        return True;

