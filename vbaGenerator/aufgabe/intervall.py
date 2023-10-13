import random
import re


class Intervall:
    def __init__(self, beschreibungstupel: tuple) -> None:
        self.untereGrenze = float(beschreibungstupel[1])
        self.obereGrenze = float(beschreibungstupel[2])
        self.nachkommastellen = 0 if beschreibungstupel[4] == '' else int(beschreibungstupel[4])
        self.untenOffen = (beschreibungstupel[0] == '(')
        self.obenOffen = (beschreibungstupel[3] == ')')
        self.untereGrenze = int(self.untereGrenze * 10 ** self.nachkommastellen) / 10 ** self.nachkommastellen
        self.kleinsteZahl = self.untereGrenze
        if self.untenOffen:
            self.kleinsteZahl = round(self.kleinsteZahl + 1 / 10 ** self.nachkommastellen, self.nachkommastellen)
        self.obereGrenze = int(self.obereGrenze * 10 ** self.nachkommastellen) / 10 ** self.nachkommastellen
        self.groessteZahl = self.obereGrenze
        if self.obenOffen:
            self.groessteZahl = round(self.groessteZahl - 1 / 10 ** self.nachkommastellen, self.nachkommastellen)

    def __str__(self):
        ausgabetext = "(" if self.untenOffen else "["
        ausgabetext += repr(self.untereGrenze)
        ausgabetext += ", "
        ausgabetext += repr(self.obereGrenze)
        ausgabetext += ")" if self.obenOffen else "]"
        ausgabetext += ", "
        ausgabetext += repr(self.nachkommastellen)
        return ausgabetext

    @classmethod
    def erzeuge_intervalle_aus_text(cls, beschreibungstext: str) -> list:
        reZahl = r"(-?\d*(?:\.\d+)?)"
        reIntervall = r"([\[\(])" + reZahl + r"\s*,\s*" + reZahl + r"([\)\]])"
        reStellen = r"\s*,+\s*(-?\d*)"
        reZahlenbeschreibung = reIntervall + reStellen

        tupelliste = re.findall(reZahlenbeschreibung, beschreibungstext)
        liste = [Intervall(beschreibungstupel) for beschreibungstupel in tupelliste]
        return liste

    def erzeuge_zufallszahl(self) -> float:
        zufallszahl = round(random.uniform(self.kleinsteZahl, self.groessteZahl), self.nachkommastellen)
        return zufallszahl

    def erzeuge_zufallszahlenliste(self, anzahl: int) -> list:
        zufallszahlenliste = [self.erzeugeZufallszahl() for i in range(anzahl)]
        return zufallszahlenliste

    def bestimmeGrenzen(self, anzahl: int = 1):
        diff = self.groessteZahl - self.kleinsteZahl
        stellen = max(0, self.nachkommastellen)
        # jeweils 20 %Abstand zur den Rändern bilden
        intervalle = [(diff / anzahl * (i + 0.2), diff / anzahl * (i + 0.8)) for i in range(anzahl)]
        grenzen = [round(random.uniform(a, b) + self.kleinsteZahl, stellen) for (a, b) in intervalle]
        return grenzen

    def bestimmeGrenze(self):
        return self.bestimmeGrenzen(1)[0]

    def get_min_max(self):
        return (self.untereGrenze, self.obereGrenze,)

    def get_stellen(self):
        return self.nachkommastellen
