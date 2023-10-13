import pickle
import locale
import traceback
import logging
import platform

from flask import Flask#, session
import logging

import helper.vbaimport
from aufgabe.aufgabe import Aufgabe
#from flask_session import Session
from flask import request

if "Linux" in platform.system():
    locale.setlocale(locale.LC_ALL, 'de_DE.utf8')
else:
    locale.setlocale(locale.LC_ALL, 'de_DE')

app = Flask(__name__)
#SESSION_TYPE = 'filesystem'
app.secret_key = 'the random string'
app.config.from_object(__name__)
#Session(app)

logging.basicConfig(level=logging.DEBUG)

@app.route('/')
def zeige():
    return ("VBA Aufgabengenerator")


#######################################################
########## Verwaltung von Daten  ######################
#######################################################

# Anzeige einer neuen Datentabelle für ein Datentemplate
@app.route('/tabelle/<int:datentemplate_id>')
@app.route('/tabelle/<int:datentemplate_id>/<int:alleSpalten>')
def tabelle(datentemplate_id, alleSpalten=True):
    try:
        aufgabe = Aufgabe().daten_from_datentemplate_id(datentemplate_id)
        return aufgabe.erstelle_tabelle(alleSpalten = alleSpalten)
    except Exception as e:
        traceback.print_exc()
        app.logger.error(e)
        return str(e)


# Anzeige einer neuen Datentabelle für ein Datentemplate
@app.route('/tabelle_aus_db/<int:daten_id>')
def tabelle_aus_db(daten_id, alleSpalten=True):
    aufgabe = Aufgabe().daten_from_daten_id(daten_id)
    return aufgabe.erstelle_tabelle(alleSpalten = alleSpalten)

# Erstellen und Speichern einer reinen Datentabelle für ein Datentemplate
@app.route('/createtabelle/<int:datentemplate_id>')
def create_and_save_datentabelle(datentemplate_id):
    aufgabe = Aufgabe().daten_from_datentemplate_id(datentemplate_id)
    id = aufgabe.save_daten_to_db()
    return str(id)

#######################################################
########## Verwaltung von Aufgaben  ######################
#######################################################

# Erstellen  einer Aufgabe ohne Speichern
@app.route('/new/<int:aufgabentemplate_id>/<int:daten_id>')
@app.route('/new/<int:aufgabentemplate_id>/<int:daten_id>/<int:teilaufgaben>')
def create_aufgabe(aufgabentemplate_id, daten_id, teilaufgaben=1):
    aufgabenliste = liste_aus_zahl(teilaufgaben)
    aufgabe = Aufgabe().aufgabe_from_aufgabentemplate(aufgabentemplate_id, daten_id, aufgabenliste)
    return aufgabe.erstelle_dokument()

# Erstellen und Speichern einer reinen Aufgabe
@app.route('/create/<int:aufgabentemplate_id>/<int:daten_id>')
@app.route('/create/<int:aufgabentemplate_id>/<int:daten_id>/<int:teilaufgaben>')
def create_and_save_aufgabe(aufgabentemplate_id, daten_id, teilaufgaben=1):
    aufgabenliste = liste_aus_zahl(teilaufgaben)
    aufgabe = Aufgabe().aufgabe_from_aufgabentemplate(aufgabentemplate_id, daten_id, aufgabenliste)
    aufgaben_pk = aufgabe.save_aufgabe_to_db()
    return str(aufgaben_pk)

# Auslesen und Anzeigen einer zuvor erstellten Aufgabe
@app.route('/read/<int:konkreteaufgaben_id>')
def read(konkreteaufgaben_id):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    return aufgabe.erstelle_dokument()


# Auslesen und Anzeigen einer zuvor erstellten Aufgabe
@app.route('/create_from_previous/<int:aufgabentemplate_id>/<int:previous_id>')
@app.route('/create_from_previous/<int:aufgabentemplate_id>/<int:previous_id>/<int:teilaufgaben>')
def create_from_previous(aufgabentemplate_id, previous_id, teilaufgaben=1):
    aufgabenliste = liste_aus_zahl(teilaufgaben)
    aufgabe = Aufgabe().aufgabe_from_aufgabentemplate_based_on_previous_aufgabe(aufgabentemplate_id, previous_id, aufgabenliste)
    return aufgabe.erstelle_dokument()

# Auslesen und Anzeigen einer zuvor erstellten Aufgabe
@app.route('/create_and_save_from_previous/<int:aufgabentemplate_id>/<int:previous_id>')
@app.route('/create_and_save_from_previous/<int:aufgabentemplate_id>/<int:previous_id>/<int:teilaufgaben>')
def create_and_save_from_previous(aufgabentemplate_id, previous_id, teilaufgaben=1):
    aufgabenliste = liste_aus_zahl(teilaufgaben)
    aufgabe = Aufgabe().aufgabe_from_aufgabentemplate_based_on_previous_aufgabe(aufgabentemplate_id, previous_id, aufgabenliste)
    aufgaben_pk = aufgabe.save_aufgabe_to_db()
    return str(aufgaben_pk)

#######################################################
########## Auslesen von Aufgabendetails  ######################
#######################################################

# Erzeugen und Zurückgeben des VBA-Startcodes zu einer zuvor erstellten Aufgabe
@app.route('/vba/<int:konkreteaufgaben_id>')
def ermittle_vba_code(konkreteaufgaben_id):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    return aufgabe.erstelle_vbacode()

# Erzeugen und Zurückgeben des Tex-Dokuments zu einer zuvor erstellten Aufgabe
# TODO: zusätzliche Route oder optionalen Parameter für Klausuren, erstelle_dokument mit zweiten Parameter "klausur = True" aufgerufen werden kann.
@app.route('/tex/<int:konkreteaufgaben_id>')
@app.route('/tex/<int:konkreteaufgaben_id>/<int:klausur>')
def tex_document(konkreteaufgaben_id, klausur=0):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    return aufgabe.erstelle_dokument(html=False, klausur=klausur!=0)

# Erzeugen und Zurückgeben des Excel-Daten-Dokuments zu einer zuvor erstellten Aufgabe
@app.route('/loadexcel/<int:konkreteaufgaben_id>')
def loadExcel(konkreteaufgaben_id):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    filename = "tabelle_{}.xlsx".format(konkreteaufgaben_id)
    aufgabe.erstelle_excel_datei(filename)
    with open(filename, 'rb') as data_file:
        data = data_file.read()
    return data


#######################################################
########## Bewertung von Aufgaben  ######################
#######################################################

#Bewertung einer Aufgabe mit neuen Daten (im POST-Request steckt der zu testende VBA-Code)
@app.route('/bewertevba/<int:konkreteaufgaben_id>', methods = ["POST"])
@app.route('/bewertevba/<int:konkreteaufgaben_id>/<int:pruefung>', methods = ["POST"])
def bewertevba(konkreteaufgaben_id=None, pruefung=0):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    data = request.form
    vbacode = data['vbacode']
    app.logger.info(vbacode)
    try:
        pycode = helper.vbaimport.vbaimport().importiere_vba_code(vbacode)
    except Exception as err:
        app.logger.info(err) 
        ergebnis = "{'0': -1}"  # Code ist nicht übersetzbar
        return ergebnis
    
    app.logger.info(pycode)
    
    try:
        ergebnis = aufgabe.bewerte_loesung(pycode, pruefung == 1)
    except ImportError as err:
        app.logger.info(err) 
        ergebnis = "{'0': -3}" # Funktion konnte nicht importiert werden
    except Exception as err:
        app.logger.info(err) 
        ergebnis = "{'0': -2}" # Code kann nicht ausgeführt werden (z.B. Laufzeitfehler)
        
    app.logger.info("Bewertung von Aufgabe %d: %s", konkreteaufgaben_id, str(ergebnis))
    return ergebnis


#Bewertung einer Aufgabe mit den Ursprungstabellendaten und Ausgabe der Fehlermeldungen (im POST-Request steckt der zu testende VBA-Code)
@app.route('/bewertungsdetails/<int:konkreteaufgaben_id>', methods = ["POST"])
def lade_bewertungsdetailsvba(konkreteaufgaben_id=None):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    data = request.form
    vbacode = data['vbacode']
    try:
        pycode = helper.vbaimport.vbaimport().importiere_vba_code(vbacode)
    except:
        ergebnis = "{'0': -1}"  # Code ist nicht übersetzbar
        return ergebnis
    
    try:
        ergebnis = aufgabe.bewerte_loesung(pycode, False, True)
    except:
        ergebnis = "{'0': -2}" # Code kann nicht ausgeführt werden (z.B. weil Funktion falsch benannt oder Laufzeitfehler)
        
    app.logger.info("Bewertung von Aufgabe %d: %s", konkreteaufgaben_id, str(ergebnis))
    return ergebnis


#######################################################
########## Laden des Python-Codes zu VBA-Code  ######################
#######################################################

#Bewertung einer Aufgabe mit neuen Daten (im POST-Request steckt der zu testende VBA-Code)
@app.route('/pythoncode', methods = ["POST"])
def pythoncode():
    data = request.form
    vbacode = data.get('vbacode', None)
    if not vbacode:
        filename = data['filename']
        with open(filename, "r", encoding="utf-8") as f:
            vbacode = f.read()
    try:
        pycode = helper.vbaimport.vbaimport().importiere_vba_code(vbacode)
    except Exception as err:
        app.logger.info(err)
        pycode = "Python Code kann nicht generiert werden: " + err
    
    return pycode

#######################################################
########## Testroutinen  ######################
#######################################################

def testebewertung(konkreteaufgaben_id=None, pruefung=0):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    with open("vbaCode.txt", "r", encoding="utf-8") as f:
        vbacode = f.read()
    app.logger.info(vbacode)
    try:
        pycode = helper.vbaimport.vbaimport().importiere_vba_code(vbacode)
    except Exception as err:
        app.logger.info(err) 
        ergebnis = "{'0': -1}"  # Code ist nicht übersetzbar
        return ergebnis
    
    app.logger.info(pycode)
    
    try:
        ergebnis = aufgabe.bewerte_loesung(pycode, pruefung == 1)
    except Exception as err:
        app.logger.info(err) 
        ergebnis = "{'0': -2}" # Code kann nicht ausgeführt werden (z.B. weil Funktion falsch benannt oder Laufzeitfehler)
        
    app.logger.info("Ergebnis der Bewertung: %s", str(ergebnis))
    return ergebnis


# Erzeugen und Zurückgeben der JSON-Konfigurationsdaten zu einer neu generierten Aufgabe
@app.route('/json/<int:aufgabentemplate_id>/<int:daten_id>')
@app.route('/json/<int:aufgabentemplate_id>/<int:daten_id>/<int:teilaufgaben>')
def testjson(aufgabentemplate_id, daten_id, teilaufgaben=12):
    aufgabenliste = liste_aus_zahl(teilaufgaben)
    aufgabe = Aufgabe().aufgabe_from_aufgabentemplate(aufgabentemplate_id, daten_id, aufgabenliste)
    return aufgabe.teilaufgaben_to_JSON()

# Erzeugen und Zurückgeben der JSON-Konfigurationsdaten zu einer vorhanden Aufgabe
@app.route('/jsonread/<int:konkreteaufgaben_id>')
def testjsonread(konkreteaufgaben_id):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    return aufgabe.teilaufgaben_to_JSON()


#######################################################
########## Hilfsroutinen  ######################
#######################################################

def liste_aus_zahl(zahl: int):
    liste = []
    while zahl > 0:
        rest = zahl % 10
        zahl //= 10
        liste.append(rest)
    return (sorted(liste))


app.run()
