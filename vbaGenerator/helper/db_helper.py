"""
Daten mit Klassen, die Datenbankfunktionalität unterstützt
"""

import mysql.connector


class DBCursor:
    config = {
        'user': 'user',
        'password': 'password',
        'host': '127.0.0.1',
        'database': 'databasename',
        'raise_on_warnings': True
         }


class DBHelper:
    @classmethod
    def lies_jsonspalte_als_dict_from_db(cls, sql: str, parameter: tuple) -> dict:
        with mysql.connector.connect(**DBCursor.config) as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, parameter)
                treffer = cursor.fetchone()
                ergebnis = {} if treffer is None else treffer[0]
        return ergebnis

    @classmethod
    def lies_datensaetze_als_liste(cls, sql: str, parameter: tuple) -> list:
        with mysql.connector.connect(**DBCursor.config) as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, parameter)
                ergebnis = cursor.fetchall()
        return ergebnis

    @classmethod
    def lies_einzelnen_datensatz_als_tupel(cls, sql: str, parameter: tuple) -> tuple:
        with mysql.connector.connect(**DBCursor.config) as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, parameter)
                ergebnis = cursor.fetchone()
        return ergebnis

    @classmethod
    def speicher_datensatz(cls, sql: str, parameter: tuple) -> int:
        with mysql.connector.connect(**DBCursor.config) as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, parameter)
                id_des_datensatzes = cursor.lastrowid
                conn.commit()
        return id_des_datensatzes