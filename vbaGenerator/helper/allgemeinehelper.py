import random


def erzeuge_textliste(text: str) -> list:
    if not isinstance(text, str):
        return text
    return [i.strip() for i in text.replace("[", "").replace("]", "").replace("(", "").replace(")", "").split(',')]


STATISTIKAUFGABENTYPEN = {
    1: {"berechnung": "tabelle.{name}.count()", "text": "Anzahl der Datensätze", "var": "anzahlDatensaetze",
        "format": "%.0f"},
    2: {"berechnung": "tabelle.{name}.sum()", "text": "Gesamtsumme von {name}", "var": "gesamt{name}"},
    3: {"berechnung": "tabelle.{name}.mean()", "text": "Durchschnitt von {name}", "var": "durchschnitt{name}"},
    4: {"berechnung": "tabelle.{name}.min()", "text": "Minimaler Wert von {name}", "var": "min{name}"},
    5: {"berechnung": "tabelle.{name}.max()", "text": "Maximaler Wert von {name}", "var": "max{name}"},
    6: {"berechnung": "tabelle[tabelle.{bedingung}].{name}.count()", "text": "Anzahl Datensätze mit {bedingung}",
        "var": "anzahlMit{bedingung}", "format": "%.0f"},
    7: {"berechnung": "tabelle[tabelle.{bedingung}].{name}.sum()", "text": "Gesamtsumme von {name} mit {bedingung}",
        "var": "gesamt{name}Mit{bedingung}"},
    8: {"berechnung": "tabelle[tabelle.{bedingung}].{name}.mean()",
        "text": "Durchschnitt von {name} mit {bedingung}",
        "var": "durchschnitt{name}Mit{bedingung}"},
    9: {"berechnung": "tabelle[tabelle.{bedingung}].{name}.min()",
        "text": "Minimaler Wert von {name} mit {bedingung}",
        "var": "min{name}Mit{bedingung}"},
    10: {"berechnung": "tabelle[tabelle.{bedingung}].{name}.max()",
         "text": "Maximaler Wert von {name} mit {bedingung}",
         "var": "max{name}Mit{bedingung}"},
    11: {"berechnung": "tabelle[tabelle.{bedingung}].{name}.count()/tabelle.{name}.count() * 100",
         "text": "Prozentaler Anteil von Datensätzen mit {bedingung}", "var": "anteil{bedingung}", "format": "%.0f"}
}

VERGLEICHE = [("unter", "<"), ("kleiner als", "<"),
              ("kleiner oder gleich", "<="), ("höchstens", "<="),
              ("größer als", ">"), ("über", ">"),
              ("mindestens", ">="), ("größer oder gleich", ">=")]

PLAUSIAUFGABENTYPEN = {
    1: {"typ": "auswahl",
        "text": ["{name} darf nur {texte} sein.", "{name} muss {texte} sein."],
        "fehler": "Wert für {name} nicht zulässig",
        "fehlerwerte": ["random.choice(helper.allgemeinehelper.unsinnigeTexte())"]},

    2: {"typ": "zahl",
        "offen": {"text": ["{name} muss eine positive Zahl sein.",
                           "{name} muss eine Zahl größer 0 sein."],
                  "fehler": "{name} ist keine positive Zahl",
                  "fehlerwerte": ["random.choice(helper.allgemeinehelper.unsinnigeTexte())",
                                  "-random.uniform(0.3, 10)"], "pruefwerte": [0]},
        "geschlossen": {"text": ["{name} muss eine positive Zahl oder 0 sein.",
                                 "{name} muss eine Zahl größer oder gleich 0 sein."],
                        "fehler": "{name} ist keine positive Zahl oder 0",
                        "fehlerwerte": ["random.choice(helper.allgemeinehelper.unsinnigeTexte())",
                                        "-random.uniform(0.3, 10)"]}},

    3: {"typ": ">",
        "neueSpalte": True,
        "offen": {"text": ["{name} muss größer als {minwert} sein.", "{name} muss über {minwert} sein."],
                  "fehler": "{name} ist zu klein",
                  "fehlerwerte": ["self.untereGrenze() / random.uniform(1.2, 2)"],
                  "pruefwerte": ["minwert"]},
        "geschlossen": {
            "text": ["{name} muss mindestens {minwert} sein.", "{name} muss größer oder gleich {minwert} sein."],
            "fehler": "{name} ist zu klein",
            "fehlerwerte": ["self.untereGrenze()     / random.uniform(1.2, 2)"],
            "pruefwerte": ["minwert - 0.01"]}},

    4: {"typ": "<",
        "neueSpalte": True,
        "offen": {"text": ["{name} muss kleiner als {maxwert} sein.", "{name} muss unter {maxwert} sein."],
                  "fehler": "{name} ist zu gross",
                  "fehlerwerte": ["self.groessteZahl() * random.uniform(1.2, 2)"],
                  "pruefwerte": ["maxwert"]},
        "geschlossen": {
            "text": ["{name} darf höchstens {maxwert} sein.", "{name} muss kleiner oder gleich {maxwert} sein."],
            "fehler": "{name} ist zu gross",
            "fehlerwerte": ["self.groessteZahl() * random.uniform(1.2, 2)"],
            "pruefwerte": ["maxwert + 0.01"]}},

    5: {"typ": "zwischen",
        "neueSpalte": True,
        "text": ["{name} muss zwischen {minwert} und {maxwert} liegen."],
        "fehler": "{name} ist außerhalb der zulässigen Grenzen",
        "fehlerwerte": ["self.kleinsteZahl() / random.uniform(1.2, 2) - 10",
                        "self.groessteZahl() * random.uniform(1.2, 2) + 10"],
        "pruefwerte": ["self.kleinsteZahl() - 0.01", "self.groessteZahl() + 0.01"]},

    6: {"typ": ">",
        "neueSpalte": True,
        "offen": {"text": [
            "{name} muss größer als {minwert} sein, außer bei {bedingungstext}, dann muss {name} größer als {minwert2} sein."],
            "fehler": "{name} ist zu klein",
            "fehlerwerte": ["self.kleinsteZahl() / random.uniform(1.1, 1.3)"],
            "fehlerwertebedingung": ["self.kleinsteZahl() / random.uniform(1.7, 2.5)"],
            "pruefwerte": ["minwert"]},
        "geschlossen": {"text": [
            "{name} muss mindestens {minwert} sein, außer bei {bedingungstext}, dann muss {name} mindestens {minwert2} sein."],
            "fehler": "{name} ist zu klein",
            "fehlerwerte": ["self.kleinsteZahl() / random.uniform(1.1, 1.3)"],
            "fehlerwertebedingung": ["self.kleinsteZahl() / random.uniform(1.7, 2.5)"]}},

    7: {"typ": "<",
        "neueSpalte": True,
        "offen": {"text": [
            "{name} muss kleiner als {maxwert} sein, außer bei {bedingungstext}, dann muss {name} keiner als {maxwert2} sein."],
            "fehler": "{name} ist zu groß",
            "fehlerwerte": ["self.groessteZahl() * random.uniform(1.1, 1.3)"],
            "fehlerwertebedingung": ["self.groessteZahl() * random.uniform(1.7, 2.5)"],
            "pruefwerte": ["maxwert"]},
        "geschlossen": {"text": [
            "{name} darf höchstens {maxwert} sein, außer bei {bedingungstext}, dann darf {name} höchstens {maxwert2} sein."],
            "fehler": "{name} ist zu groß",
            "fehlerwerte": ["self.groessteZahl() * random.uniform(1.1, 1.3)"],
            "fehlerwertebedingung": ["self.groessteZahl() * random.uniform(1.7, 2.5)"]}}}

FORMATIERUNGSAUFGABENTYPEN = {0: {"name": "faerbung auswahl"},
                              1: {"name": "faerbung sonderfall"},
                              2: {"name": "schriftart"}}

FAERBUNGEN = {0: {"name": "Hintergrundfarbe", "style": "background-color"},
              1: {"name": "Schriftfarbe", "style": "color"}}
SCHRIFTEN = {10: {"name": "fetter", "style": "font-weight: bold"},
             11: {"name": "kursiver", "style": "font-style: italic"}}
STANDARDFARBEN = {0: ("Rot", (255, 0, 0)), 1: ("Gelb", (255, 255, 0)), 2: ("Grün", (0, 255, 0)),
                  3: ("Blau", (0, 0, 255)),
                  4: ("Magenta", (255, 0, 255)), 5: ("Cyan", (0, 255, 255))}
SONDERFARBEN = {6: ("Lachsrosa", (255, 166, 122)),
                7: ("Schokoladenbraun", (210, 105, 30)),
                8: ("Orange", (255, 165, 0)),
                9: ("Gold", (255, 215, 0)),
                10: ("Olivgrün", (128, 128, 0)),
                11: ("Frühlingsgrün", (0, 255, 127)),
                12: ("Türkis", (64, 224, 208)),
                13: ("Aquamarinblau", (0, 255, 255)),
                14: ("Himmelblau", (0, 191, 255)),
                15: ("Kornblumenblau", (100, 149, 237)),
                16: ("Magenta", (255, 0, 255)),
                17: ("Purpur", (128, 0, 128)),
                18: ("Pflaumenblau", (221, 160, 221)),
                19: ("Lavendel", (230, 230, 250)),
                20: ("Petrol", (47, 79, 79)),
                21: ("Hellgrün", (144, 238, 144))
                }


def kehre_vergleich_um(operator):
    neuer_operator = {"<": ">=", "<=": ">", ">": "<=", ">=": "<"}[operator]
    neuer_vergleichstext = waehle_vergleichstext_aus(neuer_operator)
    return (neuer_vergleichstext, neuer_operator)


def waehle_vergleichstext_aus(operator):
    moegliche_vergleichstexte = [text for (text, op) in VERGLEICHE if (op == operator)]
    return random.choice(moegliche_vergleichstexte)


def erzeuge_text_fuer_textauswahl(liste):
    wertetext = liste[0]
    for text in liste[1:-1]:
        wertetext += ", " + text
    if liste[-1] == "":
        wertetext += " oder leer"
    else:
        wertetext += " oder " + liste[-1]
    return wertetext


def is_number(s):
    if s is None:
        return False
    try:
        float(s)
        return True
    except ValueError:
        return False


def unsinnigeTexte():
    return (
        "Lorem ipsum dolor sit amet consetetur sadipscing elitr sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat sed diam voluptua At vero eos et accusam et justo duo dolores et ea rebum Stet clita kasd gubergren no sea takimata sanctus est Lorem ipsum".split(
            " "))


def gueltiger_name(s):
    chars = {'ö': 'oe', 'ä': 'ae', 'ü': 'ue', 'Ö': 'OE', 'Ä': 'AE', 'Ü': 'UE', ".": "_", ",": "_"}  # usw.
    for char in chars:
        s = s.replace(char, chars[char])
    return s
