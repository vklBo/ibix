import locale

from sys import argv
import sys

import helper.vbaimport
from aufgabe.aufgabe import Aufgabe

locale.setlocale(locale.LC_ALL, '')

def bewertevba(vbacode, konkreteaufgaben_id=None, pruefung=0):
    aufgabe = Aufgabe.aufgabe_from_db(konkreteaufgaben_id)
    try:
        pycode = helper.vbaimport.vbaimport().importiere_vba_code(vbacode)
    except Exception as err:
        ergebnis = "{'0': -1}"  # Code ist nicht übersetzbar
        return ergebnis
    
    try:
        ergebnis = aufgabe.bewerte_loesung(pycode, pruefung == 1)
    except ImportError as err:
        ergebnis = "{'0': -3}" # Funktion konnte nicht importiert werden
    except Exception as err:
        ergebnis = "{'0': -2}" # Code kann nicht ausgeführt werden (z.B. Laufzeitfehler)
        
    print("Bewertung von Aufgabe {}: {}".format(konkreteaufgaben_id, str(ergebnis)))
    
    return ergebnis


print(argv)

if len(argv) <= 1: # Beim Start aus der Kommandozeile ohne Argumente
    argv = ["main_bewertung.py"]
    argv.append("vbaCode.txt")
    argv.append(4001)
    argv.append(0)

filename = argv[1]
konkreteaufgaben_id = argv[2]
pruefung = argv[3]

try:
    with open(filename, 'r') as datei:
        vbacode = datei.read()
        ergebnis = bewertevba(vbacode, konkreteaufgaben_id, pruefung)
        
        print ("\n")
        print (ergebnis)
except Exception as err:
    print ("\n")
    print("{'0': -4}")


