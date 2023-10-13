import locale
import random
import json
import logging


import helper.allgemeinehelper
from aufgabe.aufgabentyp import Aufgabentyp
from helper.db_helper import DBHelper



class Aufgabentyp3(Aufgabentyp):
    def __init__(self, aufgabe):
        super(Aufgabentyp3, self).__init__(aufgabe)
        self.ausgabezeile = None
        self.ausgabespalte = None
        self.einzelaufgabenliste = []
        
    def lies_konfiguration(self) -> dict:
        sql = "Select subtypen, spalten, bedingungsspalten, prozedurname, allgemeiner_text from aufgaben_template, config_restaufgaben where aufgaben_template.id=%s and aufgaben_template.config_plausi_id=config_restaufgaben.id"
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
            self.erstelle_aufgabe(config)
        pass  # ist hier noch was zu tun?

    def erweitereAufgabe(self, config):
        self.config = self.lies_konfiguration()
        self.erstelle_aufgabe(config)

    def erstelle_einzelaufgaben_aus_konfiguration(self, config):
        self.ausgabespalte = config.get("ausgabespalte", 2)
        self.ausgabezeile = config.get("ausgabezeile", 3)
        teilaufgabenliste = config["teilaufgabenliste"]
        for teilaufgabe in teilaufgabenliste:
            spalteninfo = self.aufgabe.get_spalte_mit_ordnung(teilaufgabe["spalte"])
            bedingungsspalteninfo = None
            if "bedingungsspalte" in teilaufgabe.keys():
                bedingungsspalteninfo = self.aufgabe.get_spalte_mit_ordnung(teilaufgabe["bedingungsspalte"])
            aufgabentyp = teilaufgabe["aufgabentyp"]
            neueTeilaufgabe = Plausibilitaetsaufgabe(aufgabentyp, spalteninfo, bedingungsspalteninfo, teilaufgabe)
            self.einzelaufgabenliste.append(neueTeilaufgabe)




    def erstelle_aufgabe(self, config):
        if self.ausgabespalte and self.ausgabespalte > 0:
            pass
        elif "ausgabespalte" in config:
            self.ausgabespalte = config["ausgabespalte"]
        else:
            self.ausgabespalte = random.randint(2, 4)
        if self.ausgabezeile and self.ausgabezeile > 0:
            pass
        if "ausgabezeile" in config:
            self.ausgabezeile = config["ausgabezeile"]
        else:
            self.ausgabezeile = random.randint(2, 5)
        self.waehle_spalten_aus(config)
        vorgegebeneAufgabentypen = self.getConfig(config, "typen")
        aufgabenliste = self.erstelle_aufgabe_rekursiv(None, vorgegebeneAufgabentypen, config)
        self.einzelaufgabenliste = reversed(aufgabenliste)

                
    def erstelle_aufgabe_rekursiv(self, letzte_spalte, vorgegebeneAufgabentypen, config):
        #print ("erstelle_aufgabe: Teilaufgaben", teilaufgaben)
        akt_typ = vorgegebeneAufgabentypen[0]
        teilaufgabenliste = self.erstelle_einzelaufgabe_rekursiv(akt_typ, letzte_spalte, vorgegebeneAufgabentypen[1:], config)
        return teilaufgabenliste

    def erstelle_einzelaufgabe_rekursiv(self, akt_typ, letzte_spalte, weitere_teilaufgaben, config):
        #print ("erstelle_einzelaufgabe: akt_typ, weitere_teilaufgaben", akt_typ, weitere_teilaufgaben, aufgaben)
        if not type(akt_typ) is list:
            akt_typ = [akt_typ]
        
        random.shuffle(akt_typ)
        for typ in akt_typ:
            teilaufgabenliste = self.erstelle_einzelaufgabe_fuer_typ_rekursiv(typ, letzte_spalte, weitere_teilaufgaben, config)
            if teilaufgabenliste != []:
                return teilaufgabenliste     #Mögliche Aufgabe erstelle

        return ([])   #Keine mögliche Aufgabe gefunden


    def erstelle_einzelaufgabe_fuer_typ_rekursiv(self, typ, letzte_spalte, weitere_teilaufgaben, config):
        #print ("erstelle_einzelaufgabe_fuer_typ: typ, weitere_teilaufgaben", typ, weitere_teilaufgaben, aufgaben)
        aufgabenbeschreibung = helper.allgemeinehelper.PLAUSIAUFGABENTYPEN[typ]
        
        if not letzte_spalte:
            moegliche_spalten = self.ermittle_sinnvolle_spalten(aufgabenbeschreibung, self.zahlenspalten, self.textspalten)
        else:
            moegliche_spalten = [letzte_spalte]
        
        for spalte in moegliche_spalten:
            teilaufgabenliste = self.erstelle_einzelaufgabe_fuer_typ_und_spalte(typ, spalte, letzte_spalte, weitere_teilaufgaben, config)
            if teilaufgabenliste != []:
                return teilaufgabenliste     #Mögliche Aufgabe erstelle

        return ([])   #Keine mögliche Aufgabe gefunden


    def erstelle_einzelaufgabe_fuer_typ_und_spalte(self, aufgabentyp, spalte, letzte_spalte, weitere_teilaufgaben, config):
        spalteninfo = self.aufgabe.get_spalte_mit_ordnung(spalte)
        aufgabenbeschreibung = helper.allgemeinehelper.PLAUSIAUFGABENTYPEN[aufgabentyp]
        bedingungsspalteninfo = None
        if "offen" in aufgabenbeschreibung and "bedingungstext" in aufgabenbeschreibung["offen"]["text"][0]:
            bedingungsspalten = self.bedingungsspalten.copy()
            if spalte in bedingungsspalten:
                bedingungsspalten.remove(spalte)
            bedingungsSpalte = random.choice(bedingungsspalten)
            bedingungsspalteninfo = self.aufgabe.get_spalte_mit_ordnung(bedingungsSpalte)
            
        neueTeilaufgabe = Plausibilitaetsaufgabe(aufgabentyp, spalteninfo, bedingungsspalteninfo, config)
        
        if aufgabentyp == 2:
            letzte_spalte = spalte
        else:
            letzte_spalte = None
            
        if aufgabentyp > 2:
            self.zahlenspalten.remove(spalte)

        if len(weitere_teilaufgaben) == 0:
            return [neueTeilaufgabe]
    
        teilaufgabenliste = self.erstelle_aufgabe_rekursiv(letzte_spalte, weitere_teilaufgaben, config)

        #Wenn es für die weiteren Teilaufgaben keine Aufgabenstellung gibt, muss diese hier verworfen werden.
        if len(teilaufgabenliste) == 0 and aufgabentyp > 2:
            self.zahlenspalten.add(spalte)
            return ([])
        
        teilaufgabenliste.append(neueTeilaufgabe)
        return (teilaufgabenliste)



    
    def ermittle_sinnvolle_spalten(self, aufgabenbeschreibung, zahlenspalten, textspalten):
        if aufgabenbeschreibung["typ"] == "auswahl":
            return textspalten
            
        if aufgabenbeschreibung["typ"] != ">":
            return zahlenspalten
        
        sinnvolle_spalten = []
        for spaltennr in zahlenspalten:
            spalteninfo = self.aufgabe.get_spalte_mit_ordnung(spaltennr)
            if "offen" in aufgabenbeschreibung and "bedingungstext" in aufgabenbeschreibung["offen"]["text"][0]:
                if spalteninfo.intervallliste[0].untereGrenze >=5 :
                    sinnvolle_spalten.append(spaltennr)
            else:
                if spalteninfo.intervallliste[0].untereGrenze >= 1:
                    sinnvolle_spalten.append(spaltennr)
        return sinnvolle_spalten
            






    def generiere_daten_fuer_darstellung(self) -> dict:
        daten = {}
        daten["aufgabe3"] = True
        daten["ausgabespalte"] = self.ausgabespalte
        daten["ausgabezeile"] = self.ausgabezeile
        plausiaufgaben = []
        for einzelaufgabe in self.einzelaufgabenliste:
            plausiaufgaben.append(einzelaufgabe.get_aufgabentext())
        daten["plausiaufgaben"] = plausiaufgaben
        self.erzeugeTabellenMitFehlern()
        daten["tabelleMitFehlern"] = self.datentabelleMitFehlern
        daten["fehlertexte"] = self.fehlertexte
        
        if "allgemeiner_text" in self.config.keys():
            daten["plausitext"] = self.config.get("allgemeiner_text");
        daten["plausiprozedurname"] = self.config.get("prozedurname", "plausibilitaet_click");

        return daten

    def erzeugeTabellenMitFehlern(self, anzahlFehler=1):
        self.fehlertexte = []
        for einzelaufgabe in self.einzelaufgabenliste:
            einzelaufgabe.erstelle_beispielfehler()

        tabelle = self.aufgabe.tabelle.copy()
        tabelle.reset_index(inplace=True, drop=True)

        anzSpalten = len(tabelle.columns)
        anzZeilen = len(tabelle.index)
        verfuegbareZellen = {}
        for i in range(anzSpalten):
            liste = [j for j in range(anzZeilen)]
            random.shuffle(liste)
            verfuegbareZellen[i + 1] = liste

        for einzelaufgabe in reversed(self.einzelaufgabenliste):
            if einzelaufgabe.fehlerwertelisteBedingung:
                spalte = einzelaufgabe.spalteninfo.spaltennr
                bedingungspalte = einzelaufgabe.bedingungsspalteninfo.spaltennr
                bedingung = einzelaufgabe.bedingung.get_bedingung()
                ausdruck = str.format("list(tabelle[tabelle.{}].index)", bedingung)
                zeilenMitBedingung = eval(ausdruck)
                if len(zeilenMitBedingung) > 0:
                    restzeilen = list(set(tabelle.index) - set(zeilenMitBedingung))
    
                    random.shuffle(zeilenMitBedingung)
                    random.shuffle(restzeilen)
                    fehlerzeileMitBedingung = zeilenMitBedingung.pop()
                    fehlerzeileOhneBedingung = restzeilen.pop()
    
                    verfuegbareZellen[spalte].remove(fehlerzeileMitBedingung)
                    if bedingungspalte is not None:  # könnte passieren, wenn dies eine virtuelle Spalte ist
                        verfuegbareZellen[bedingungspalte].remove(fehlerzeileMitBedingung)
                    verfuegbareZellen[spalte].remove(fehlerzeileOhneBedingung)
    
                    beispielfehler = einzelaufgabe.fehlerwerteliste[0]
                    fehlertext = einzelaufgabe.get_fehlertext()
                    self.fehlertexte.append(fehlertext + " in Zeile " + str(fehlerzeileOhneBedingung + 2))
                    # tabelle[aufgabe["spalte"].name].iloc[fehlerzeileOhneBedingung] = beispielfehler
                    tabelle.at[fehlerzeileOhneBedingung, einzelaufgabe.spalteninfo.name] = beispielfehler
    
                    beispielfehler = einzelaufgabe.fehlerwertelisteBedingung[0]
                    fehlertext = einzelaufgabe.get_fehlertext_bei_bedingung()
                    self.fehlertexte.append(fehlertext + " in Zeile " + str(fehlerzeileMitBedingung + 2))
                    # tabelle[aufgabe["spalte"].name].iloc[fehlerzeileMitBedingung] = beispielfehler
                    tabelle.at[fehlerzeileMitBedingung, einzelaufgabe.spalteninfo.name] = beispielfehler
            else:
                # Für normale Fehler
                spalte = einzelaufgabe.spalteninfo.spaltennr
                for beispielfehler in einzelaufgabe.fehlerwerteliste:
                    zeile = verfuegbareZellen[spalte].pop()
                    fehlertext = einzelaufgabe.get_fehlertext()
                    self.fehlertexte.append(fehlertext + " in Zeile " + str(zeile + 2))
                    tabelle.at[zeile, einzelaufgabe.spalteninfo.name] = beispielfehler

        (tabellenkopie, spaltentitel) = self.aufgabe.generiere_tabellenkopie_as_zweidim_liste(tabelle)
        self.datentabelleMitFehlern = tabellenkopie
        self.fehlertexte.sort(key=lambda zeile: zeile[-2:])

    def bewerte_loesung(self, sheets, neueDaten=True, vollstaendigeInfos=False):
        prozedurname = self.config.get("prozedurname", "plausibilitaet_click")
        ergebnisTabelle = [['', '', '', '', '', '', '', '', '', '', '', '', '', '', '', ''] for i in range(100)]
        sheets.append([])   # Tabellenblatt 2 
        sheets.append(ergebnisTabelle)    # Tabellenblatt 3
        fehlerhafteDatensaetze = []
        korrekt = 0
        fehler = 0
        print("Bewertung 'Plausibilitäten':")
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

        # Erste Ausführung von plausibilitaet_click auf "korrekter" Tabelle, darf keinen Fehler erzeugen
        #Zweiter Ausdruck in der Bedingung wegen fehlerhafter Aufgabenstellung
        if ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] == '' and ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] == '':
            print("OK: In korrekter Tabelle keine Fehler gefunden.")
        else:
            print("FALSCH: In korrekter Tabelle Fehler erkannt.", ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1])
            fehler += 1
            fehlerhafteDatensaetze.append(("Fehlermeldung, obwohl keine Fehler in Tabelle", "", ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1],))

        for einzelaufgabe in self.einzelaufgabenliste:
            einzelaufgabe.erstelle_prueffehler()
            spalte = einzelaufgabe.spalteninfo.spaltennr - 1
            for beispielfehler in einzelaufgabe.fehlerwerteliste:
                fehler, korrekt = self.pruefe_fehlerwert(beispielfehler, einzelaufgabe, spalte, sheets, ergebnisTabelle,
                                                         fehlerhafteDatensaetze, korrekt, fehler)
                fehler, korrekt = self.pruefe_richtigen_wert_bei_bedingung(beispielfehler, einzelaufgabe, spalte,
                                                                           sheets, ergebnisTabelle,
                                                                           fehlerhafteDatensaetze, korrekt, fehler)
            for beispielfehler in einzelaufgabe.fehlerwertelisteBedingung:
                fehler, korrekt = self.pruefe_fehlerwert_mit_bedingung(beispielfehler, einzelaufgabe, spalte, sheets,
                                                                       ergebnisTabelle, fehlerhafteDatensaetze, korrekt,
                                                                       fehler)

        if (vollstaendigeInfos):
            return (fehlerhafteDatensaetze)
        return (korrekt / (korrekt + fehler) * 100)

    def pruefe_fehlerwert(self, beispielfehler, einzelaufgabe, spalte,
                          sheets, ergebnisTabelle, fehlerhafteDatensaetze, korrekt, fehler):
        prozedurname = self.config.get("prozedurname", "plausibilitaet_click")
        ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] = ''
        ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] = ''
        if einzelaufgabe.bedingung is None:
            zeile = random.randint(1, len(sheets[0]) - 2)   #1)  -2, da die letzte Zeile leer ist, damit die Tabellenlänge mit isEmpty berechnet werden kann
        else:
            tabelle = self.aufgabe.tabelle
            bedingung = einzelaufgabe.bedingung.get_bedingung()
            ausdruck = str.format("list(tabelle[~(tabelle.{})].index)", bedingung)
            zeilenOhneBedingung = eval(ausdruck)
            zeile = random.choice(zeilenOhneBedingung) + 1
        originalwert = sheets[0][zeile][spalte]
        sheets[0][zeile][spalte] = beispielfehler
        try:
            exec("from helper.vbaimport import " + prozedurname)
            exec(prozedurname + "()")
        except Exception as err:
            print("FALSCH: Fehler bei Ausführung des Programms (" + str(err) + "). ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
            fehler += 1
            fehlerhafteDatensaetze.append((einzelaufgabe.get_aufgabentext(), str(beispielfehler), str(err),))
        else:
            if ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] == '' and ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] == '':
                fehler += 1
                print("FALSCH: Bewertung Aufgabe 3: Fehler nicht gefunden. ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
                fehlerhafteDatensaetze.append(
                    (einzelaufgabe.get_aufgabentext(), str(beispielfehler), "Fehler nicht gefunden",))
            else:
                print("OK: Fehler gefunden. ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
                korrekt += 1
        sheets[0][zeile][spalte] = originalwert
        return fehler, korrekt

    def pruefe_richtigen_wert_bei_bedingung(self, beispielfehler, einzelaufgabe, spalte,
                                            sheets, ergebnisTabelle, fehlerhafteDatensaetze, korrekt, fehler):
        prozedurname = self.config.get("prozedurname", "plausibilitaet_click")
        ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] = ''
        ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] = ''
        if einzelaufgabe.bedingung is None:
            return fehler, korrekt

        tabelle = self.aufgabe.tabelle
        bedingung = einzelaufgabe.bedingung.get_bedingung()
        ausdruck = str.format("list(tabelle[tabelle.{}].index)", bedingung)
        zeilenMitBedingung = eval(ausdruck)
        zeile = random.choice(zeilenMitBedingung) + 1
        originalwert = sheets[0][zeile][spalte]
        sheets[0][zeile][spalte] = beispielfehler
        try:
            exec("from helper.vbaimport import " + prozedurname)
            exec(prozedurname + "()")
        except Exception as err:
            print("FALSCH: Fehler bei Ausführung des Programms (" + str(err) + "). ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
            fehler += 1
            fehlerhafteDatensaetze.append((einzelaufgabe.get_aufgabentext(), str(beispielfehler), str(err),))
        else:
            if ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] == '' and ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] == '':
                print("OK: Sonderfall erkannt. ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
            else:
                fehler += 1
                print("FALSCH: Fehler zu viel gefunden. ", einzelaufgabe.get_aufgabentext(), ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1])
                fehlerhafteDatensaetze.append(
                    (
                        einzelaufgabe.get_aufgabentext(), str(beispielfehler),
                        "Fehler zu viel gefunden (bei Bedingung): " + ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1],))

        sheets[0][zeile][spalte] = originalwert
        return fehler, korrekt

    def pruefe_fehlerwert_mit_bedingung(self, beispielfehler, einzelaufgabe, spalte, sheets, ergebnisTabelle,
                                        fehlerhafteDatensaetze, korrekt, fehler):
        prozedurname = self.config.get("prozedurname", "plausibilitaet_click")
        ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] = ''
        ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] = ''
        if einzelaufgabe.bedingung is None:
            return fehler, korrekt

        tabelle = self.aufgabe.tabelle
        bedingung = einzelaufgabe.bedingung.get_bedingung()
        ausdruck = str.format("list(tabelle[(tabelle.{})].index)", bedingung)
        zeilenMitBedingung = eval(ausdruck)
        zeile = random.choice(zeilenMitBedingung) + 1
        originalwert = sheets[0][zeile][spalte]
        sheets[0][zeile][spalte] = beispielfehler
        try:
            exec("from helper.vbaimport import " + prozedurname)
            exec(prozedurname + "()")
        except Exception as err:
            print("FALSCH: Fehler bei Ausführung des Programms (" + str(err) + "). ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
            fehler += 1
            fehlerhafteDatensaetze.append((einzelaufgabe.get_aufgabentext(), str(beispielfehler), str(err),))
        else:
            if ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte-1] == '' and ergebnisTabelle[self.ausgabezeile-1][self.ausgabespalte] == '':
                fehler += 1
                print("FALSCH: Fehler nicht gefunden.  ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
                fehlerhafteDatensaetze.append(
                    (einzelaufgabe.get_aufgabentext(), str(beispielfehler), "Fehler nicht gefunden",))
            else:
                print("OK: Fehler gefunden. ", einzelaufgabe.get_aufgabentext(), str(beispielfehler))
                korrekt += 1
        sheets[0][zeile][spalte] = originalwert
        return fehler, korrekt

    def get_templatename(self):
        return "aufgabe3.jinja"

    def to_JSON(self):
        json_list = []
        for einzelaufgabe in self.einzelaufgabenliste:
            json_list.append(einzelaufgabe.to_JSON())
        return {"ausgabezeile": self.ausgabezeile, "ausgabespalte": self.ausgabespalte, "teilaufgabenliste": json_list}

    def ermittle_vba_code(self):
        konstanten = set()
        for einzelaufgabe in self.einzelaufgabenliste:
            konstanten.update(einzelaufgabe.get_konstanten())

        spalten_konstanten = [name for name in list(konstanten) if "SPALTE" in name]
        grenzen_konstanten = sorted([name for name in list(konstanten) if "SPALTE" not in name])

        return {"aufgabe3": True, "spalten_konstanten_aufgabe3": spalten_konstanten,
                "grenzen_konstanten_aufgabe3": grenzen_konstanten}

    def get_name(self) -> str:
        return ("3")

    def get_aufgabennummer(self) -> int:
        return 3


class Plausibilitaetsaufgabe():
    def __init__(self, aufgabentyp, spalteninfo, bedingungsspalteninfo, config):
        self.aufgabentyp = aufgabentyp
        self.spalteninfo = spalteninfo
        self.bedingung = None
        self.bedingungsspalteninfo = None

        aufgabe = self.get_aufgabentypdefinition()
        if "offen" in aufgabe and "fehlerwertebedingung" in aufgabe["offen"]:
            self.bedingungsspalteninfo = bedingungsspalteninfo
            self.bedingung = self.bedingungsspalteninfo.erstelle_einzelne_bedingung(config)

        self.minwertBedingung = config.get("minwertBedingung")
        self.maxwertBedingung = config.get("maxwertBedingung")

    def get_aufgabentypdefinition(self):
        return helper.allgemeinehelper.PLAUSIAUFGABENTYPEN[self.aufgabentyp]

    def get_konstanten(self):
        konstanten = set()
        konstanten.add(helper.allgemeinehelper.gueltiger_name(self.spalteninfo.name.upper() + "_SPALTE"))
        aufgabe = self.get_aufgabentypdefinition()
        if "offen" in aufgabe:
            aufgabe = aufgabe["offen"]
        if "geschlossen" in aufgabe:
            aufgabe = aufgabe["geschlossen"]
        if "maxwert" in aufgabe["text"][0]:
            konstanten.add("MAX_GRENZE_" + helper.allgemeinehelper.gueltiger_name(self.spalteninfo.name.upper()))
        if "minwert" in aufgabe["text"][0]:
            konstanten.add("MIN_GRENZE_" + helper.allgemeinehelper.gueltiger_name(self.spalteninfo.name.upper()))
        if self.bedingungsspalteninfo:
            konstanten.add(helper.allgemeinehelper.gueltiger_name(self.bedingungsspalteninfo.name.upper() + "_SPALTE"))
            if "maxwert2" in aufgabe["text"][0]:
                konstanten.add(helper.allgemeinehelper.gueltiger_name("MAX_GRENZE_" +
                                                                      self.spalteninfo.name.upper() + "_" + self.bedingungsspalteninfo.name.upper()))
            if "minwert2" in aufgabe["text"][0]:
                konstanten.add(helper.allgemeinehelper.gueltiger_name("MIN_GRENZE_" +
                                                                      self.spalteninfo.name.upper() + "_" + self.bedingungsspalteninfo.name.upper()))
        return konstanten

    def get_variablen(self):
        return []

    def get_bedingungstext(self):
        if self.bedingung is None:
            bedingungstext = ""
        elif type(self.bedingung.wert) == str:
            bedingungstext = self.bedingung.name.capitalize() + self.bedingung.wert.capitalize()
        else:
            bedingungstext = self.bedingung.name.capitalize() + str(self.bedingung.wert)
        return bedingungstext

    def get_aufgabentext(self):
        minwert = None
        maxwert = None
        auswahltexte = None
        aufgabe = self.get_aufgabentypdefinition()

        if aufgabe["typ"] in ["<"]:
            offen = self.spalteninfo.intervallliste[0].obenOffen
            aufgabe = aufgabe["offen" if offen else "geschlossen"]
        elif aufgabe["typ"] in [">", "zahl"]:
            offen = self.spalteninfo.intervallliste[0].untenOffen
            aufgabe = aufgabe["offen" if offen else "geschlossen"]

        text = random.choice(aufgabe["text"])
        if self.spalteninfo.typ in ["zahl", "berechnet"]:
            (minwert, maxwert) = self.spalteninfo.get_min_max()
        if self.spalteninfo.typ in ["text"]:
            auswahltexte = helper.allgemeinehelper.erzeuge_text_fuer_textauswahl(self.spalteninfo.auswahl)
        minwertBedingung = self.get_minwert_bedingung(minwert, maxwert) if "minwert2" in text else None
        maxwertBedingung = self.get_maxwert_bedingung(minwert, maxwert) if "maxwert2" in text else None

        localformat = self.spalteninfo.get_format_for_locale()
        if minwert is not None:
            minwert = locale.format_string(localformat, minwert)
        if maxwert is not None:
            maxwert = locale.format_string(localformat, maxwert)
        if minwertBedingung is not None:
            minwertBedingung = locale.format_string(localformat, minwertBedingung)
        if maxwertBedingung is not None:
            maxwertBedingung = locale.format_string(localformat, maxwertBedingung)

        return str.format(text, name=self.spalteninfo.name.capitalize(), minwert=minwert, maxwert=maxwert,
                          minwert2=minwertBedingung, maxwert2=maxwertBedingung, texte=auswahltexte,
                          bedingungstext=(self.bedingung.get_bedingungstext() if self.bedingung else None))

    def get_fehlertext(self):
        aufgabe = self.get_aufgabentypdefinition()

        if aufgabe["typ"] in ["<"]:
            offen = self.spalteninfo.intervallliste[0].obenOffen
            aufgabe = aufgabe["offen" if offen else "geschlossen"]
        elif aufgabe["typ"] in [">", "zahl"]:
            offen = self.spalteninfo.intervallliste[0].untenOffen
            aufgabe = aufgabe["offen" if offen else "geschlossen"]

        return aufgabe["fehler"].format(name=self.spalteninfo.name.capitalize())

    def get_fehlertext_bei_bedingung(self):
        return self.get_fehlertext() + " bei " + self.bedingung.get_bedingungstext()

    def get_minwert_bedingung(self, minwert, maxwert):
        if self.minwertBedingung is None:
            self.minwertBedingung = round(random.uniform(0.65 * minwert, 0.8 * minwert), self.spalteninfo.get_stellen())
        return self.minwertBedingung

    def get_maxwert_bedingung(self, minwert, maxwert):
        if self.maxwertBedingung is None:
            self.maxwertBedingung = round(random.uniform(1.2 * maxwert, 1.5 * maxwert), self.spalteninfo.get_stellen())
        return self.maxwertBedingung

    def erstelle_prueffehler(self, anzahl=1):
        aufgabe = self.get_aufgabentypdefinition()

        if aufgabe["typ"] in ["<"]:
            offen = self.spalteninfo.intervallliste[0].obenOffen
            aufgabe_details = aufgabe["offen" if offen else "geschlossen"]
        elif aufgabe["typ"] in [">", "zahl"]:
            offen = self.spalteninfo.intervallliste[0].untenOffen
            aufgabe_details = aufgabe["offen" if offen else "geschlossen"]
        else:
            aufgabe_details = aufgabe

        fehlerwertbeschreibungsliste = aufgabe_details["fehlerwerte"]

        fehlerwerteliste = set()
        fehlerwertelisteBedingung = set()

        for fehlerwertbeschreibung in fehlerwertbeschreibungsliste:
            for i in range(anzahl):

                if aufgabe["typ"] in ["<"]:
                    fehlerwert = self.obereGrenze() + (0 if offen else 0.001)
                elif aufgabe["typ"] in [">"]:
                    fehlerwert = self.untereGrenze() - (0 if offen else 0.001)
                else:
                    fehlerwert = eval(fehlerwertbeschreibung)
                    
                fehlerwerteliste.add(fehlerwert)

        if "fehlerwertebedingung" in aufgabe_details.keys():
            fehlerwertbeschreibungsliste = aufgabe_details["fehlerwertebedingung"]
            for fehlerwertbeschreibung in fehlerwertbeschreibungsliste:
                for i in range(anzahl):
                    if aufgabe["typ"] in ["<"]:
                        fehlerwertBedingung = self.maxwertBedingung + (0 if offen else 0.001)
                    elif aufgabe["typ"] in [">"]:
                        fehlerwertBedingung = self.minwertBedingung - (0 if offen else 0.001)
                    else:
                        fehlerwertBedingung = eval(fehlerwertbeschreibung)
                    fehlerwertelisteBedingung.add(fehlerwertBedingung)

        self.fehlerwerteliste = list(fehlerwerteliste)
        self.fehlerwertelisteBedingung = list(fehlerwertelisteBedingung)

    def erstelle_beispielfehler(self, anzahl=1):
        aufgabe = self.get_aufgabentypdefinition()

        if aufgabe["typ"] in ["<"]:
            offen = self.spalteninfo.intervallliste[0].obenOffen
            aufgabe_details = aufgabe["offen" if offen else "geschlossen"]
        elif aufgabe["typ"] in [">", "zahl"]:
            offen = self.spalteninfo.intervallliste[0].untenOffen
            aufgabe_details = aufgabe["offen" if offen else "geschlossen"]
        else:
            aufgabe_details = aufgabe

        fehlerwertbeschreibungsliste = aufgabe_details["fehlerwerte"]

        fehlerwerteliste = set()
        fehlerwertelisteBedingung = set()

        for fehlerwertbeschreibung in fehlerwertbeschreibungsliste:
            for i in range(anzahl):
                fehlerwert = eval(fehlerwertbeschreibung)
                if aufgabe["typ"] in ["<"]:
                    abstand = self.bestimme_kleinste_differenz()
                    fehlerwert += abstand
                elif aufgabe["typ"] in [">"]:
                    abstand = self.bestimme_kleinste_differenz()
                    fehlerwert -= abstand
                fehlerwerteliste.add(fehlerwert)

        if "fehlerwertebedingung" in aufgabe_details.keys():
            fehlerwertbeschreibungsliste = aufgabe_details["fehlerwertebedingung"]
            for fehlerwertbeschreibung in fehlerwertbeschreibungsliste:
                for i in range(anzahl):
                    fehlerwertBedingung = eval(fehlerwertbeschreibung)
                    if aufgabe["typ"] in ["<"]:
                        abstand = self.bestimme_kleinste_differenz()
                        fehlerwertBedingung += abstand
                    elif aufgabe["typ"] in [">"]:
                        abstand = self.bestimme_kleinste_differenz()
                        fehlerwertBedingung -= abstand
                    fehlerwertelisteBedingung.add(fehlerwertBedingung)

        self.fehlerwerteliste = list(fehlerwerteliste)
        self.fehlerwertelisteBedingung = list(fehlerwertelisteBedingung)
        
    def bestimme_kleinste_differenz(self):
        stellen = self.spalteninfo.get_stellen();
        kleinste_differenz = 10 ** -stellen;
        return kleinste_differenz;

    def kleinsteZahl(self):
        return self.spalteninfo.intervallliste[0].kleinsteZahl

    def groessteZahl(self):
        return self.spalteninfo.intervallliste[0].groessteZahl
    
    def untereGrenze(self):
        return self.spalteninfo.intervallliste[0].untereGrenze

    def obereGrenze(self):
        return self.spalteninfo.intervallliste[0].obereGrenze
    

    def to_JSON(self):
        json_dict = {}
        json_dict["aufgabentyp"] = self.aufgabentyp
        json_dict["spalte"] = self.spalteninfo.ordnung
        if self.bedingung is not None:
            json_dict["bedingungsspalte"] = self.bedingungsspalteninfo.ordnung
            json_dict["vergleichsoperator"] = self.bedingung.vergleichsoperator
            json_dict["vergleichswert"] = self.bedingung.wert
            if self.spalteninfo.typ in ["zahl", "berechnet"]:
                aufgabentext = self.get_aufgabentypdefinition()["offen"]["text"][0]
                (minwert, maxwert) = self.spalteninfo.get_min_max()
                if "minwert2" in aufgabentext:
                    self.get_minwert_bedingung(minwert, maxwert)
                if "maxwert2" in aufgabentext:
                    self.get_maxwert_bedingung(minwert, maxwert)

        if self.minwertBedingung:
            json_dict["minwertBedingung"] = self.minwertBedingung
        if self.maxwertBedingung:
            json_dict["maxwertBedingung"] = self.maxwertBedingung
        return json_dict
