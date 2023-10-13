from helper.db_helper import DBHelper
import random
import logging


class Aufgabentyp:
    def __init__(self, aufgabe):
        self.aufgabe = aufgabe
        self.config = self.lies_konfiguration()
        self.zahlenspalten = []
        self.textspalten = []
        self.bedingungsspalten = []

    def get_name(self) -> str:
        pass

    def get_aufgabennummer(self) -> int:
        pass

    def lies_konfiguration(self) -> dict:
        #sql = "Select config from aufgabe_config where aufgabe_id=%s and aufgabe_nr=%s"
        #config = DBHelper.lies_jsonspalte_als_dict_from_db(sql, (self.aufgabe.aufgaben_id, self.get_aufgabennummer()))
        #return config
        return {}

    def waehle_spalten_aus(self, config):
        vorgegebeneSpalten = self.getConfig(config, "spalten")
        vorgegebeneBedingungsspalten = self.getConfig(config, "bedingungsspalten")
        self.zahlenspalten = []
        self.textspalten = []
        self.bedingungsspalten = []

        for spalte in self.aufgabe.spaltenliste:
            if spalte.name in vorgegebeneSpalten or spalte.ordnung in vorgegebeneSpalten:
                if spalte.typ in ["zahl", "berechnet"]:
                    self.zahlenspalten.append(spalte.ordnung)
                if spalte.typ in ["text"]:
                    self.textspalten.append(spalte.ordnung)
            if spalte.name in vorgegebeneBedingungsspalten or spalte.ordnung in vorgegebeneBedingungsspalten:
                self.bedingungsspalten.append(spalte.ordnung)

        if len(self.bedingungsspalten) == 0:
            self.bedingungsspalten = self.zahlenspalten + self.textspalten

        random.shuffle(self.zahlenspalten)
        random.shuffle(self.textspalten)
        random.shuffle(self.bedingungsspalten)
        
    def ohneZielspalte(self):
        return False;

    def getConfig(self, config, parameter):
        if parameter in config.keys():
            return config[parameter]
        if parameter in self.config.keys():
            return self.config[parameter]
        return []
