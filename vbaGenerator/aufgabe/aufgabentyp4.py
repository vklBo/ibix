import random
import json
import logging

import helper.allgemeinehelper
import helper.vbaimport
from aufgabe.aufgabentyp import Aufgabentyp
from aufgabe.bedingung import Bedingung
from helper.db_helper import DBHelper



class Aufgabentyp4(Aufgabentyp):
    def __init__(self, aufgabe):
        super(Aufgabentyp4, self).__init__(aufgabe)
        self.einzelaufgabenliste = []
        
    def lies_konfiguration(self) -> dict:
        sql = "Select subtypen, spalten, bedingungsspalten, prozedurname, allgemeiner_text from aufgaben_template, config_restaufgaben where aufgaben_template.id=%s and aufgaben_template.config_formatierung_id=config_restaufgaben.id"
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
            aufgabentyp = teilaufgabe["aufgabentyp"]
            neueTeilaufgabe = Faerbeaufgabe.erstelle_formatierungsaufgabe(aufgabentyp, spalteninfo, teilaufgabe)
            self.einzelaufgabenliste.append(neueTeilaufgabe)

    def erstelleAufgabe(self, config):
        self.waehle_spalten_aus(config)
        verwendete_spaltennummer = 99999
        if len(self.einzelaufgabenliste) > 0 and self.einzelaufgabenliste[0].aufgabentyp == 0:
            verwendete_spaltennummer = self.einzelaufgabenliste[0].spalte
        zulaessigeSpalten = [spalte for spalte in self.aufgabe.spaltenliste if
                             spalte.typ in ["bestimmt", "text", "zahl", "berechnet"] and
                             spalte.ordnung in (self.zahlenspalten + self.textspalten) and 
                             spalte.spaltennr != verwendete_spaltennummer]
        random.shuffle(zulaessigeSpalten)
        vorgegebeneAufgabentypen = self.getConfig(config, "typen")
        for aufgabentyp in vorgegebeneAufgabentypen:
            spalteninfo = zulaessigeSpalten.pop()
            self.einzelaufgabenliste.append(Formatierungsaufgabe.erstelle_formatierungsaufgabe(aufgabentyp, spalteninfo, config))

    def generiere_daten_fuer_darstellung(self) -> dict:
        daten = {}
        daten["aufgabe4"] = True

        formatierungsaufgaben = {}
        formatierungsbefehle = []
        for einzelaufgabe in self.einzelaufgabenliste:
            formatierungsaufgaben.update(einzelaufgabe.generiere_daten_fuer_darstellung())
        daten["formatierungstypen"] = [aufgabe.aufgabentyp for aufgabe in self.einzelaufgabenliste]
        daten["formatierungsaufgaben"] = formatierungsaufgaben
        daten["formatierungen"] = self.generiere_formatierungen_fuer_darstellung()
        if "allgemeiner_text" in self.config.keys():
            daten["formatierungtext"] = self.config.get("allgemeiner_text");
        daten["formatierungprozedurname"] = self.config.get("prozedurname", "formatieren_click");
        return daten

    def generiere_formatierungen_fuer_darstellung(self) -> dict:
        formatierungen = []
        aufgabeliste_dict = {einzelaufgabe.aufgabentyp: einzelaufgabe for einzelaufgabe in self.einzelaufgabenliste}
        for i in range(len(self.aufgabe.tabelle)):
            zeile = self.aufgabe.tabelle.iloc[i].to_dict()
            formatierung = {}
            # Formatierung in der richtigen Reihenfolge durchführen, sofern in Aufgabenstellung
            if 1 in aufgabeliste_dict.keys():
                formatierung = aufgabeliste_dict[1].generiere_formatierung_fuer_darstellung(zeile)
            if formatierung == {} and 0 in aufgabeliste_dict.keys():
                formatierung = aufgabeliste_dict[0].generiere_formatierung_fuer_darstellung(zeile)
            if 2 in aufgabeliste_dict.keys():
                formatierung.update(aufgabeliste_dict[2].generiere_formatierung_fuer_darstellung(zeile))
            formatierungen.append(formatierung)
        return formatierungen

    def bewerte_loesung(self, sheets, neueDaten=True, vollstaendigeInfos=False):
        prozedurname = self.config.get("prozedurname", "formatieren_click")
        datentabelle = self.aufgabe.generiere_daten_fuer_darstellung()["tabelle"]
        sheetsInteriorColor = [["ohne"] * len(sheets[0][0]) for i in range(len(sheets[0]))]
        sheetsFontColor = [["ohne"] * len(sheets[0][0]) for i in range(len(sheets[0]))]
        sheetsFontBold = [["ohne"] * len(sheets[0][0]) for i in range(len(sheets[0]))]
        sheetsFontItalic = [["ohne"] * len(sheets[0][0]) for i in range(len(sheets[0]))]
        vba = helper.vbaimport.vbaimport()
        vba.stelle_tabelle_zur_verfuegung(sheetsInteriorColor, "sheetsInteriorColor")
        vba.stelle_tabelle_zur_verfuegung(sheetsFontColor, "sheetsFontColor")
        vba.stelle_tabelle_zur_verfuegung(sheetsFontBold, "sheetsFontBold")
        vba.stelle_tabelle_zur_verfuegung(sheetsFontItalic, "sheetsFontItalic")
        formatierungen = self.generiere_formatierungen_fuer_darstellung()
        for einzelaufgabe in self.einzelaufgabenliste:
            sa = einzelaufgabe.get_formatierungswert()
            aufgabefett = False
            aufgabekursiv = False
            if (sa == 10 or sa == 11):
                aufgabefett =  sa == 10
                aufgabekursiv = sa == 11
        
        fehlerhafteDatensaetze = []
        korrekt = 0
        fehler = 0
        print("Bewertung 'Formatierung':")
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

        for i in range(len(sheets[0]) - 2):      # -1):     -2, da die letzte Zeile für das isEmpty mit leeren Daten gefüllt ist.
            farbformatierungKorrekt = True
            zielwerte = formatierungen[i]
            k = len(sheets[0][i]) - 2            # -1       -2 da für die Berechnung der Tabellenbreite eine zusätzliche Spalte gebraucht wird.
            if zielwerte.get("faerbung") == "color":
                farbstring = sheetsFontColor[i + 1][k]
                if farbstring != "ohne":
                    farben = farbstring[4:-1].split(",")
                    farbe = tuple([int(i) for i in farben])
                else:
                    farbe = "ohne"
                farbformatierungKorrekt = farbe == zielwerte.get("farbe") and (sheetsInteriorColor[i + 1][k] == "ohne" or sheetsInteriorColor[i + 1][k] == "")
            elif zielwerte.get("faerbung") == "background-color":
                farbstring = sheetsInteriorColor[i + 1][k]
                if farbstring != "ohne":
                    farben = farbstring[4:-1].split(",")
                    farbe = tuple([int(i) for i in farben])
                else:
                    farbe = "ohne"
                farbformatierungKorrekt = farbe == zielwerte.get("farbe") and (sheetsFontColor[i + 1][k] == "ohne" or sheetsFontColor[i + 1][k] == "")
            
            formatierungFett = sheetsFontBold[i + 1][k]
            formatierungKursiv = sheetsFontItalic[i + 1][k]
            zielwertFett = zielwerte.get("schrift") == "font-weight: bold"
            zielwertKursiv = zielwerte.get("schrift") == "font-style: italic"

            schriftartKorrekt = True
            if zielwertFett:
                schriftartKorrekt = formatierungFett != "ohne" and formatierungFett and (formatierungKursiv == "ohne" or formatierungKursiv == "")
                zielwertKursiv = "ohne"
            elif zielwertKursiv:
                schriftartKorrekt = formatierungKursiv != "ohne" and formatierungKursiv and (formatierungFett == "ohne" or formatierungFett == "")
                zielwertFett = "ohne"
            elif aufgabefett:
                schriftartKorrekt = formatierungFett != "ohne" and not formatierungFett and (formatierungKursiv == "ohne" or formatierungKursiv == "")
                zielwertKursiv = "ohne"
            elif aufgabekursiv:
                schriftartKorrekt = formatierungKursiv != "ohne" and not formatierungKursiv and (formatierungFett == "ohne" or formatierungFett == "")
                zielwertFett = "ohne"
                
            zeile = datentabelle[i]
            print ()
            print ("Zeile: {}".format(zeile))
            if farbformatierungKorrekt:
                print("Farbe korrekt.")
                korrekt += 1
            else:
                print("Farbe falsch: Erwartet: {}, Berechnet: {}".format(zielwerte.get("farbe"), farbe))
                fehlerhafteDatensaetze.append(("Zeile {} - Farbformatierung falsch".format(i + 2), zielwerte.get("farbe"), farbe))
                fehler += 1
                
            if schriftartKorrekt:
                print("Schriftart korrekt.")
                korrekt += 1
            else:
                print("Schriftart falsch (Erwartet vs Berechnet): Fett: {} vs. {} | Kursiv: {} vs. {} ".format(zielwertFett, formatierungFett, zielwertKursiv, formatierungKursiv))
                fehlerhafteDatensaetze.append(("Zeile {} - Schriftart falsch".format(i + 2), "", ""))
                fehler += 1
        if (vollstaendigeInfos):
            return (fehlerhafteDatensaetze)
        return (korrekt / (korrekt + fehler) * 100)

    def get_templatename(self):
        return "aufgabe4.jinja"

    def to_JSON(self):
        json_list = []
        for einzelaufgabe in self.einzelaufgabenliste:
            json_list.append(einzelaufgabe.to_JSON())
        return {"teilaufgabenliste": json_list}

    def ermittle_vba_code(self):
        konstanten = set()
        for einzelaufgabe in self.einzelaufgabenliste:
            konstanten.update(einzelaufgabe.get_konstanten())
        return {"aufgabe4": True, "konstanten_aufgabe4": list(konstanten)}

    def get_name(self) -> str:
        return ("4")

    def get_aufgabennummer(self) -> int:
        return 4


class Formatierungsaufgabe:
    faerbeartnr = None

    def __init__(self, aufgabentyp, spalteninfo, teilaufgabe, faerbeartnr=None):
        self.spalte = spalteninfo.ordnung
        self.spalteninfo = spalteninfo
        self.aufgabentyp = aufgabentyp
        self.bedingungen = []

    @classmethod
    def erstelle_formatierungsaufgabe(cls, aufgabentyp, spalteninfo, aufgaben_config):
        if aufgabentyp == 0:
            return Faerbeaufgabe(aufgabentyp, spalteninfo, aufgaben_config)
        elif aufgabentyp == 1:
            return Faerbeaufgabe_Zusatz(aufgabentyp, spalteninfo, aufgaben_config)
        elif aufgabentyp == 2:
            return Schriftartaufgabe(aufgabentyp, spalteninfo, aufgaben_config)

    def get_anzahl(self):
        return len(self.bedingungen)

    def get_aufgabentext(self, nr=0):
        return self.bedingungen[nr].get_bedingungstext()

    def get_farbe(self, farbnr):
        if farbnr in helper.allgemeinehelper.STANDARDFARBEN:
            return helper.allgemeinehelper.STANDARDFARBEN[farbnr]
        return helper.allgemeinehelper.SONDERFARBEN[farbnr]

    def to_JSON(self):
        json_dict = {"aufgabentyp": self.aufgabentyp, "spalte": self.spalte}
        json_dict["bedingungen"] = [bedingung.to_JSON() for bedingung in self.bedingungen]
        return json_dict

    def get_konstanten(self):
        konstanten = set()
        konstanten.add(helper.allgemeinehelper.gueltiger_name(self.spalteninfo.name.upper() + "_SPALTE"))
        return konstanten


class Faerbeaufgabe(Formatierungsaufgabe):
    def __init__(self, aufgabentyp, spalteninfo, teilaufgabe):
        super(Faerbeaufgabe, self).__init__(aufgabentyp, spalteninfo, teilaufgabe)
        Formatierungsaufgabe.faerbeartnr = teilaufgabe.get("faerbeart")
        if Formatierungsaufgabe.faerbeartnr is None:
            Formatierungsaufgabe.faerbeartnr = random.choice(list(helper.allgemeinehelper.FAERBUNGEN.keys()))
        if "bedingungen" in teilaufgabe:
            self.bedingungen = [Bedingung.erstelle_aus_konfiguration(bedingungsconfig) for bedingungsconfig in
                                teilaufgabe["bedingungen"]]
        else:
            spalteninfo.erstelle_bedingungen(teilaufgabe)
            self.bedingungen = spalteninfo.bedingungen

        self.farbnr = teilaufgabe.get("farben")
        if self.farbnr is None:
            standardfarben = list(helper.allgemeinehelper.STANDARDFARBEN.keys())
            random.shuffle(standardfarben)
            sonderfarben = list(helper.allgemeinehelper.SONDERFARBEN.keys())
            random.shuffle(sonderfarben)
            self.farbnr = [standardfarben.pop(), standardfarben.pop()]
            for i in range (len(self.farbnr), len(self.bedingungen)):
                self.farbnr.append(sonderfarben.pop())
            random.shuffle(self.farbnr)

    def get_formatierungstyp(self, nr=0):
        return helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]["name"]

    def get_formatierungswert(self, nr=0):
        return self.get_farbe(self.farbnr[nr])

    def get_formatierungsstye(self, nr=0):
        faerbeart = helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]
        return {"bedingung": self.bedingungen[nr].get_bedingung(), "style": faerbeart["style"]}

    def get_farbtext(self, i):
        farbnr = self.farbnr[i]
        if farbnr in helper.allgemeinehelper.STANDARDFARBEN:
            farbe = helper.allgemeinehelper.STANDARDFARBEN[farbnr]
        else:
            farbe = helper.allgemeinehelper.SONDERFARBEN[farbnr]
        farbtext = farbe[0]
        rgb = list(farbe[1])
        if len(rgb) > 0 and farbtext not in {"Rot", "Grün", "Gelb", "Blau"}:
            anteilnamen = [" Rotanteil", " Grünanteil", " Blauanteil"]
            farbtext += " (" + ", ".join([str(rgb[i]) + anteilnamen[i] for i in range(3) if int(rgb[i]) > 0]) + ")"
        return farbtext

    def to_JSON(self):
        json_dict = super(Faerbeaufgabe, self).to_JSON()
        json_dict["faerbeart"] = Formatierungsaufgabe.faerbeartnr
        json_dict["farben"] = self.farbnr
        return json_dict

    def generiere_daten_fuer_darstellung(self):
        daten = {}
        daten["name"] = self.bedingungen[0].name.capitalize()
        daten["wasFaerben"] = helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]["name"]
        liste = [((self.bedingungen[i].get_bedingungstext(), self.get_farbtext(i),)) for i in
                 range(len(self.bedingungen))]
        daten["farbauswahl"] = liste
        return daten

    def generiere_formatierung_fuer_darstellung(self, zeile):
        befehle = {}
        style = helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]["style"]
        for i in range(len(self.bedingungen)):
            bedingung = self.bedingungen[i].get_bedingung()
            if eval(bedingung, zeile):
                farbnr = self.farbnr[i]
                if farbnr in helper.allgemeinehelper.STANDARDFARBEN:
                    (farbtext, rgb) = helper.allgemeinehelper.STANDARDFARBEN[farbnr]
                else:
                    (farbtext, rgb) = helper.allgemeinehelper.SONDERFARBEN[farbnr]
                befehle["faerbung"] = style
                befehle["farbe"] = rgb
                return befehle
        return befehle


class Faerbeaufgabe_Zusatz(Faerbeaufgabe):
    def __init__(self, aufgabentyp, spalteninfo, teilaufgabe):
        super(Faerbeaufgabe_Zusatz, self).__init__(aufgabentyp, spalteninfo, teilaufgabe)
        if "bedingungen" in teilaufgabe:
            self.bedingungen = [Bedingung.erstelle_aus_konfiguration(bedingungsconfig) for bedingungsconfig in
                                teilaufgabe["bedingungen"]]
        else:
            self.bedingungen = [spalteninfo.erstelle_einzelne_bedingung()]
        self.farbnr = teilaufgabe.get("farben")
        if self.farbnr is None:
            standardfarben = list(helper.allgemeinehelper.STANDARDFARBEN.keys())
            self.farbnr = [random.choice(standardfarben)]

    def generiere_daten_fuer_darstellung(self):
        daten = {}
        daten["bedingungFarbeSonderfall"] = self.bedingungen[0].get_bedingungstext()
        daten["farbeSonderfall"] = self.get_farbtext(0)
        daten["wasFaerbenSonderfall"] = helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]["name"]
        return daten

    def generiere_formatierungsbefehle_fuer_darstellung(self):
        style = helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]["style"]
        bedingung = self.bedingungen[0].get_bedingung()
        farbnr = self.farbnr[0]
        if farbnr in helper.allgemeinehelper.STANDARDFARBEN:
            (farbtext, rgb) = helper.allgemeinehelper.STANDARDFARBEN[farbnr]
        else:
            (farbtext, rgb) = helper.allgemeinehelper.SONDERFARBEN[farbnr]
        befehle = (bedingung, rgb, style)
        return befehle

    def generiere_formatierung_fuer_darstellung(self, zeile):
        befehle = {}
        style = helper.allgemeinehelper.FAERBUNGEN[Formatierungsaufgabe.faerbeartnr]["style"]
        bedingung = self.bedingungen[0].get_bedingung()
        if eval(bedingung, zeile):
            farbnr = self.farbnr[0]
            if farbnr in helper.allgemeinehelper.STANDARDFARBEN:
                (farbtext, rgb) = helper.allgemeinehelper.STANDARDFARBEN[farbnr]
            else:
                (farbtext, rgb) = helper.allgemeinehelper.SONDERFARBEN[farbnr]
            befehle["faerbung"] = style
            befehle["farbe"] = rgb
            return befehle
        return befehle


class Schriftartaufgabe(Formatierungsaufgabe):
    def __init__(self, aufgabentyp, spalteninfo, teilaufgabe):
        super(Schriftartaufgabe, self).__init__(aufgabentyp, spalteninfo, teilaufgabe)
        if "bedingungen" in teilaufgabe:
            self.bedingungen = [Bedingung.erstelle_aus_konfiguration(bedingungsconfig) for bedingungsconfig in
                                teilaufgabe["bedingungen"]]
        else:
            self.bedingungen = [spalteninfo.erstelle_einzelne_bedingung()]
        self.schriftart = teilaufgabe.get("schriftart", random.choice(list(helper.allgemeinehelper.SCHRIFTEN.keys())))

    def get_formatierungstyp(self, nr=0):
        return helper.allgemeinehelper.SCHRIFTEN[self.schriftartnr]["name"]

    def get_formatierungswert(self, nr=0):
        return self.schriftart

    def get_formatierungsstye(self, nr=0):
        schrift = helper.allgemeinehelper.SCHRIFTEN[self.schriftartnr]
        return {"bedingung": self.bedingungen[0].get_bedingung(), "style": schrift["style"]}

    def to_JSON(self):
        json_dict = super(Schriftartaufgabe, self).to_JSON()
        json_dict["schriftart"] = self.schriftart
        return json_dict

    def generiere_daten_fuer_darstellung(self):
        daten = {}
        daten["bedingungSchrift"] = self.bedingungen[0].get_bedingungstext()
        daten["wasSchrift"] = helper.allgemeinehelper.SCHRIFTEN[self.schriftart]["name"]
        return daten

    def generiere_formatierungsbefehle_fuer_darstellung(self):
        style = helper.allgemeinehelper.SCHRIFTEN[self.schriftart]["style"]
        bedingung = self.bedingungen[0].get_bedingung()
        befehle = (bedingung, "", style)
        return befehle

    def generiere_formatierung_fuer_darstellung(self, zeile):
        befehle = {}
        style = helper.allgemeinehelper.SCHRIFTEN[self.schriftart]["style"]
        bedingung = self.bedingungen[0].get_bedingung()
        if eval(bedingung, zeile):
            befehle["schrift"] = style
            return befehle
        return befehle
