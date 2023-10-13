import locale

import helper.allgemeinehelper


class Bedingung:
    def __init__(self, name: str, vergleichsoperator: str, wert, format: str = None, wert2=None):
        self.name = name
        self.vergleichsoperator = vergleichsoperator
        self.wert = wert
        self.format = format
        self.wert2 = wert2
        self.vergleichstext = None  # Wird benötigt, um aufeinanderfolgende Vergleich mit demselben Text zu erzeugen

    def get_bedingungstext(self):
        if helper.allgemeinehelper.is_number(self.wert):
            if self.wert2 is None:
                bedingungstext = "{} {} {}".format(self.name.capitalize(), self.get_vergleichstext(),
                                                   locale.format_string(self.format, self.wert))
            else:
                (umkehrtext, umkehroperator) = helper.allgemeinehelper.kehre_vergleich_um(self.vergleichsoperator)
                bedingungstext = "{} {} {} und {} {}".format(self.name.capitalize(), self.get_vergleichstext(),
                                                             locale.format_string(self.format, self.wert), umkehrtext,
                                                             locale.format_string(self.format, self.wert2))
        else:
            if self.wert is None or self.wert == "":
                bedingungstext = " {} ist leer".format(self.name.capitalize())
            else:
                bedingungstext = " {}: {}".format(self.name.capitalize(), self.wert)
        return bedingungstext

    def get_vergleichstext(self):
        if self.vergleichstext is None:
            self.vergleichstext = helper.allgemeinehelper.waehle_vergleichstext_aus(self.vergleichsoperator)
        return self.vergleichstext

    def get_bedingung(self):
        if helper.allgemeinehelper.is_number(self.wert):
            if self.wert2 is None:
                bedingung = "{}{}{}".format(self.name, self.vergleichsoperator, self.wert)
            else:
                (umkehrtext, umkehroperator) = helper.allgemeinehelper.kehre_vergleich_um(self.vergleichsoperator)
                bedingung = "({}{}{}) & ({}{}{})".format(self.name, self.vergleichsoperator, self.wert, self.name,
                                                         umkehroperator, self.wert2)
        else:
            bedingung = "{}=='{}'".format(self.name, self.wert)
        return bedingung

    def get_bedingung_mit_tabelle(self):
        if helper.allgemeinehelper.is_number(self.wert):
            if self.wert2 is None:
                bedingung = "tabelle.{}{}{}".format(self.name, self.vergleichsoperator, self.wert)
            else:
                (umkehrtext, umkehroperator) = helper.allgemeinehelper.kehre_vergleich_um(self.vergleichsoperator)
                bedingung = "(tabelle.{}{}{}) & (tabelle.{}{}{})".format(self.name, self.vergleichsoperator, self.wert,
                                                                         self.name,
                                                                         umkehroperator, self.wert2)
        else:
            bedingung = "tabelle.{}=='{}'".format(self.name, self.wert)
        return bedingung

    @classmethod
    def erstelle_bedingungen_fuer_zahlen(cls, name: str, vergleichsoperator: str, grenzen: list, format: str):
        if (">" in vergleichsoperator):
            grenzen = list(reversed(grenzen))
        (umkehrtext, umkehroperator) = helper.allgemeinehelper.kehre_vergleich_um(vergleichsoperator)

        bedingungen = []
        for i in range(len(grenzen) - 1):  # Der letzte braucht eine Sonderbehandlung
            if i > 0:
                bedingungsobjekt = Bedingung(name, vergleichsoperator, grenzen[i], format, grenzen[i - 1])
            else:
                bedingungsobjekt = Bedingung(name, vergleichsoperator, grenzen[i], format)
            bedingungen.append(bedingungsobjekt)

        bedingungsobjekt = Bedingung(name, umkehroperator, grenzen[-2], format)
        bedingungen.append(bedingungsobjekt)
        if (">" in vergleichsoperator):
            bedingungen = list(reversed(bedingungen))
        return bedingungen

    @classmethod
    def erstelle_bedingungen_fuer_texte(cls, name: str, auswahlwerte: list):
        bedingungen = []
        for wert in auswahlwerte:
            bedingungsobjekt = Bedingung(name, "==", wert)
            bedingungen.append(bedingungsobjekt)
        return bedingungen

    @classmethod
    def erstelle_aus_konfiguration(cls, config:dict):
        name = config.get("name")
        vergleichsoperator = config.get("vergleichsoperator")
        wert = config.get("wert")
        format = config.get("format")
        wert2 = config.get("wert2")
        bedingung = Bedingung(name, vergleichsoperator, wert, format, wert2)
        bedingung = Bedingung(**config)
        return bedingung

    def to_JSON(self):
        json_dict = {}
        json_dict["name"] = self.name
        json_dict["vergleichsoperator"] = self.vergleichsoperator
        json_dict["wert"] = self.wert
        json_dict["format"] = self.format
        json_dict["wert2"] = self.wert2
        return json_dict
