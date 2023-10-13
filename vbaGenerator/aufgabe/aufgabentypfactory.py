from aufgabe.aufgabentyp1 import Aufgabentyp1
from aufgabe.aufgabentyp2 import Aufgabentyp2
from aufgabe.aufgabentyp3 import Aufgabentyp3
from aufgabe.aufgabentyp4 import Aufgabentyp4


def aufgabentyp_from_konfiguration(aufgabe, typ, config):
    if typ == 1 or typ == "1":
        teilaufgabe = Aufgabentyp1(aufgabe)
    if typ == 2 or typ == "2":
        teilaufgabe = Aufgabentyp2(aufgabe)
    if typ == 3 or typ == "3":
        teilaufgabe = Aufgabentyp3(aufgabe)
    if typ == 4 or typ == "4":
        teilaufgabe = Aufgabentyp4(aufgabe)
    teilaufgabe.erstelle_aufgabe_aus_konfiguration(config)
    return teilaufgabe
