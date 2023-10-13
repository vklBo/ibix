import random

import helper.allgemeinehelper
import json
from aufgabe.bedingung import Bedingung
from aufgabe.intervall import Intervall


class Spaltenbeschreibung:
    def __init__(self, beschreibungstupel: tuple) -> None:
        (spalten_id, ordnung, spaltennr, titel, name, typ, auswahl, inhalt, intervalle,
         config) = beschreibungstupel
        self.ordnung = ordnung
        self.spaltennr = spaltennr
        self.titel = titel
        self.name = name
        self.typ = typ
        if auswahl is None:
            self.auswahl = None
        elif "[" in auswahl:
            self.auswahl = helper.allgemeinehelper.erzeuge_textliste(auswahl)
        elif str(auswahl).isdigit():
            self.auswahl = int(auswahl)
        else:
            self.auswahl = auswahl
        self.inhalt = "" if not inhalt else inhalt.replace('"', '')
        self.intervallliste = [] if not intervalle  else Intervall.erzeuge_intervalle_aus_text(intervalle)
        self.config = {} if not config else json.loads(config)
        self.bedingungen = None
        self.wert = None

    def initialisiere(self, config: dict):
        pass

    def erstelle_einzelne_bedingung(self, config: dict):
        pass

    def erstelle_bedingungen(self, config: dict):
        pass

    def generiere_wert(self, werte_der_zeile: dict) -> dict:
        pass

    def get_format(self):
        formatstring = None
        if len(self.intervallliste) > 0:
            stellen = max(self.intervallliste[0].nachkommastellen, 0)
            formatstring = "{:." + str(stellen) + "f}"
        if "format" in self.config:
            formatstring = self.config["format"]
        return formatstring

    def get_format_for_locale(self):
        return self.get_format().replace("{:", "%").replace("}", "")

    def to_JSON(self):
        json_dict = {}
        if self.wert is not None:
            json_dict["wert"] = self.wert
        return json_dict

    @classmethod
    def from_beschreibungstupel(cls, beschreibungstupel):
        (aufgabe_id, spalten_id, ordnung, spaltennr, titel, name, typ, auswahl, inhalt, intervalle,
         config) = beschreibungstupel
        beschreibungstupel = (spalten_id, ordnung, spaltennr, titel, name, typ, auswahl, inhalt, intervalle,
                              config)
        if typ == "zahl":
            return SpaltenbeschreibungZahl(beschreibungstupel)
        if typ == "text":
            return SpaltenbeschreibungText(beschreibungstupel)
        if typ == "bestimmt":
            return SpaltenbeschreibungBestimmt(beschreibungstupel)
        if typ == "berechnet":
            return SpaltenbeschreibungBerechnet(beschreibungstupel)

    @classmethod
    def get_spalte_by_name(cls, spaltenliste: list, name: str):
        for spalte in spaltenliste:
            if spalte.name == name:
                return spalte


class SpaltenbeschreibungZahl(Spaltenbeschreibung):
    def initialisiere(self, andereSpalten: list, config: dict = {}):
        if self.ordnung == 0:
            if self.name in config:
                wert = config[self.name]
            elif "wert" in config:
                wert = config["wert"]
            else:
                wert = self.intervallliste[0].erzeuge_zufallszahl()
            self.wert = wert
        if self.auswahl or "bedingungen" in config:
            self.erstelle_bedingungen(config)

    def erstelle_einzelne_bedingung(self, config: dict = {}):
        super().erstelle_einzelne_bedingung(config)
        if "vergleichswert" in config.keys():
            grenze = config["vergleichswert"]
        else:
            grenze = self.intervallliste[0].bestimmeGrenze()
        if "vergleichsoperator" in config.keys():
            vergleichsoperator = config["vergleichsoperator"]
        else:
            (vergleichstext, vergleichsoperator) = random.choice(helper.allgemeinehelper.VERGLEICHE)
        bedingungsobjekt = Bedingung(self.name, vergleichsoperator, grenze, self.get_format_for_locale())
        return bedingungsobjekt

    def erstelle_bedingungen(self, config: dict):
        super().erstelle_bedingungen(config)
        # Grenzen und Operator anhand der Konfiguration bestimmen
        if "bedingungen" in config:
            (self.vergleichsoperator, self.auswahl) = config["bedingungen"]
        elif type(self.auswahl) is list:
            (vergleichtext, self.vergleichsoperator) = random.choice(helper.allgemeinehelper.VERGLEICHE)
        else:
            if helper.allgemeinehelper.is_number(self.auswahl):
                anzahl = int(self.auswahl)
            else:
                anzahl = config.get("anzahl", 3)
            (vergleichtext, self.vergleichsoperator) = random.choice(helper.allgemeinehelper.VERGLEICHE)
            self.auswahl = self.intervallliste[0].bestimmeGrenzen(anzahl)

        self.bedingungen = Bedingung.erstelle_bedingungen_fuer_zahlen(self.name, self.vergleichsoperator, self.auswahl,
                                                                      self.get_format_for_locale())

    def generiere_wert(self, werte_der_zeile: dict) -> dict:
        if self.ordnung == 0:
            return {self.name: self.wert}
        else:
            return {self.name: self.intervallliste[0].erzeuge_zufallszahl()}

    # Wenn die Intervalle der berechneten Spalten nicht vorgegeben werden, werden Minimum und Maximum der Tabellendaten verwendet.
    def setzeMinMax(self, min, max):
        if len(self.intervallliste) == 0:
            if (helper.allgemeinehelper.is_number(str(min)) and helper.allgemeinehelper.is_number(str(max))):
                self.intervallliste.append(Intervall(('[', min, max, ']', 2)))

    def get_min_max(self):
        if len(self.intervallliste) > 0:
            return self.intervallliste[0].get_min_max()
        return (None, None,)

    def get_stellen(self):
        if len(self.intervallliste) > 0:
            return self.intervallliste[0].get_stellen()

    def to_JSON(self):
        json_dict = super(SpaltenbeschreibungZahl, self).to_JSON()
        if self.bedingungen is not None and len(self.bedingungen) > 0:
            json_dict["bedingungen"] = (self.vergleichsoperator, self.auswahl)
        return json_dict


class SpaltenbeschreibungText(Spaltenbeschreibung):
    def __init__(self, beschreibungstupel: tuple) -> None:
        super(SpaltenbeschreibungText, self).__init__(beschreibungstupel)
        self.inhalt = helper.allgemeinehelper.erzeuge_textliste(self.inhalt)
        self.auswahlliste = None
        self.anzahl_ausgewaehlter_werte = 0

    def initialisiere(self, andereSpalten: list, config: dict = {}):
        if self.ordnung == 0:
            if self.name in config:
                wert = config[self.name]
            elif "wert" in config:
                wert = config["wert"]
            else:
                wert = random.choice(self.inhalt)
            self.wert = wert
        if self.auswahl or "bedingungen" in config:
            self.erstelle_bedingungen(config)

    def erstelle_einzelne_bedingung(self, config: dict = {}):
        super().erstelle_einzelne_bedingung(config)
        if "vergleichswert" in config.keys():
            bedingungswert = config["vergleichswert"]
        else:
            bedingungswert = random.choice(self.inhalt)
        bedingungsobjekt = Bedingung(self.name, "==", bedingungswert)
        return bedingungsobjekt

    def erstelle_bedingungen(self, config: dict = {}):
        super().erstelle_bedingungen(config)
        # Grenzen und Operator anhand der Konfiguration bestimmen

        if "bedingungen" in config:
            (self.vergleichsoperator, self.auswahl) = config["bedingungen"]
        elif type(self.auswahl) is not list:
            self.auswahl = self.inhalt.copy()

        self.bedingungen = Bedingung.erstelle_bedingungen_fuer_texte(self.name, self.auswahl)

    def generiere_wert(self, werte_der_zeile: dict) -> dict:
        if self.ordnung == 0:
            return {self.name: self.wert}
        else:
            # Sicherheitshalber erstelle ich eine gleichverteilte Liste der zulässigen Werte für 6 Zeilen,
            # damit alle Werte halbwegs gleichhäufig vorkommen
            ANZAHL_GLEICHVERTEILTER_WERTE = 6
            if self.anzahl_ausgewaehlter_werte == 0:
                texte = self.inhalt.copy()
                anz = len(texte)
                self.auswahlliste = [texte[i % anz] for i in range(ANZAHL_GLEICHVERTEILTER_WERTE)]
                random.shuffle(self.auswahlliste)
            if self.anzahl_ausgewaehlter_werte < ANZAHL_GLEICHVERTEILTER_WERTE:
                wert = self.auswahlliste[self.anzahl_ausgewaehlter_werte]
                self.anzahl_ausgewaehlter_werte += 1
            else:
                wert = random.choice(self.inhalt)
            return {self.name: wert}

    def get_format(self):
        formatstring = super(SpaltenbeschreibungText, self).get_format()
        if not formatstring:
            formatstring = "{:s}"
        return formatstring

    def to_JSON(self):
        json_dict = super(SpaltenbeschreibungText, self).to_JSON()
        if self.bedingungen and len(self.bedingungen) > 0:
            json_dict["bedingungen"] = ("==", self.auswahl)
        return json_dict


class SpaltenbeschreibungBestimmt(SpaltenbeschreibungZahl):
    def __init__(self, beschreibungstupel: tuple) -> None:
        super(SpaltenbeschreibungBestimmt, self).__init__(beschreibungstupel)

    def initialisiere(self, andereSpalten: list, config: dict = {}):
        self.abhaengigeSpalte = Spaltenbeschreibung.get_spalte_by_name(andereSpalten, self.inhalt)
        if self.name in config:
            werte = config[self.name]
        elif "werte_liste" in config:
            werte = config["werte_liste"]
        else:
            werte = [intervall.erzeuge_zufallszahl() for intervall in self.intervallliste]
        self.werte_liste = werte

    def generiere_wert(self, werte_der_zeile: dict) -> dict:
        for i in range(len(self.abhaengigeSpalte.bedingungen)):
            erg = eval(self.abhaengigeSpalte.bedingungen[i].get_bedingung(), {}, werte_der_zeile)
            wert = self.werte_liste[i]
            if erg:
                returnwert = wert
        return {self.name: returnwert}

    def to_JSON(self):
        json_dict = super(SpaltenbeschreibungBestimmt, self).to_JSON()
        if self.werte_liste:
            json_dict["werte_liste"] = self.werte_liste
        return json_dict


class SpaltenbeschreibungBerechnet(SpaltenbeschreibungZahl):
    def generiere_wert(self, werte_der_zeile: dict) -> dict:
        wert = eval(self.inhalt, {}, {**werte_der_zeile, **globals()})
        return {self.name: wert}

    def get_format(self):
        formatstring = super(SpaltenbeschreibungBerechnet, self).get_format()
        if not formatstring:
            formatstring = "{:.2f}"
        return formatstring
