import re

# Modul, mit dem VBA-Code in Python-Code umgewandelt wird.
# Es gibt eine Ignoreliste, die festlegt, welche Funktionen und Prozeduren nicht umgewandelt werden: diese müssen durch Python-Programme ersetzt werden

regulaere_ausdruecke = [
    ("^\s*", ""),
    ("\n\s*", "\n"),
    ("\s* ", " "),
    ("private\s*", ""),
    ("\'.*", ""),
    ("const (\w*)( as \w*){0,1}(\s*=.*)", "\g<1>\g<3>"),  # Konstanten durch Python-Variablen ersezten
    # ReDim-Befehler durch Funktionsaufrufe ersetzen
    ("ReDim Preserve (.*)\((.*)\)", "\g<1> = redim_preserve(\g<1>, \g<2>)"),
    ("ReDim (.*)\((.*)\)", "\g<1> = redim(\g<1>, \g<2>)"),
    # Variablendeklarationen durch Zuweisungen von Startwerten ersetzen
    ("Dim (.*)\(\)", "\g<1> = []"),
    ("dim farbe .*", "farbe = 'ohne'"),        
    ("dim (.*)[!#@]", "\g<1> = 9999.9"),
    ("dim (.*)[%&]", "\g<1> = 9999"),
    ("dim (.* )as (double|single|currency)", "\g<1> = 9999.9"),
    ("dim (.* )as (long|integer)", "\g<1> = 9999"),
    ("dim (.*)[$]", "\g<1> = 'aasdfsdf'"),
    ("dim (.* )as (string)", "\g<1> = 'asdfas'"),
    ("dim (.* )as (boolean)", "\g<1> = True"),
    ("^dim (.*)$", "\g<1> = 989898"),  # Variable ohne Datentyp deklariert
    ("(function [\w\(,_0-9 ]*\(.*)\(\)(.*)", "\g<1>\g<2>"),  # Runde Klammern, die Arrays im Funktionskopf darstellen, entfernen
    ("function (.*\)).*", "def \g<1>:"),  # Function durch def .. : ersetzen
    ("(sub [\w\(,_0-9 ]*)\(\)([\w\(,_0-9 ]+)", "\g<1>\g<2>"),  # Runde Klammern, die Arrays im Prozedurkopf darstellen, entfernen
    ("sub (.*\)).*", "def \g<1>:"),  # Sub durch def .. : ersetzen
    ("((\w*) as (double|single|long|integer|string|boolean))", "\g<2>"),  # verbleibende deklarationen rauswerfen
    ("if( .*) then", "if\g<1>:"),
    ("elseif", "elif"),
    ("else:?", "else:"),
    (" or ", " or "),
    (" and ", " and "),
    (" not", " not"),
    ("for (\w*) = (\w*) to (.*)", "for \g<1> in range(\g<2>, \g<3> + 1):"),  # For-Schleifen in for .. in range(...)
    ("do while (.*)", "while \g<1>:"),  # While-Schleifen 
    ("exit sub", "return"),
    (" & (\w*)", " + str(\g<1>)"),
    ("call ([a-z0-9_]*)$", "\g<1>()"),    # Aufruf von Prozeduren ohne Parameter
    ("call ", ""),
    # Cells in 2- bzw. 3-dim-Listen-Aufruf umwandeln
    ("Sheets\((\w*)\)\.Cells\((\w*),\s(\w*)\)\.(\w*).(\w*)", "sheets\g<4>\g<5>[\g<2>-1][\g<3>-1]"),
    ("Sheets\(([\w +-]*)\)\.Cells\(([\w +-]*),\s([\w +-]*)\)", "sheets[\g<1>-1][\g<2>-1][\g<3>-1]"),
    ("vbRed", "RGB(255, 0, 0)"),
    ("vbGreen", "RGB(0, 255, 0)"),
    ("vbYellow", "RGB(255, 255, 0)"),
    ("vbBlue", "RGB(0, 0, 255)"),
    ("vbMagenta", "RGB(255, 0, 255)"),
    ("vbCyan", "RGB(0, 255, 255)"),
    ("xlNone", "''"),
    ("\^", "**"),
    ("([0-9]+)([!#@%&])", "\g<1>"),
]

indentkeywords = "(if .*\w+.*|else|for .*|def .*|while .*)"
removeindentkeywords = "(elif|else|end sub|end function|end if|next|loop)"
removelinekeyword = "(end sub|end function|end if|next|Option Explicit|Attribute|loop)"
ignorefunction = ["zeilenEntformatieren_Click", "tabelleFuellen_Click"]
blacklist = ["import os", "from os import", "os.system"]

class vbaimport:
    def __init__(self):
        self.funktionsnamen = set([])

    def sonderbehandlung_if_anweisung(self, zeile):
        if "if" in zeile.lower() and "then" in zeile.lower():
            return zeile.replace(" = ", " == ").replace(" <> ", " != ")
        return zeile
    
    def sonderbehandlung_array_def(self, zeile, arraynamen):
        suchergebnis = re.findall("dim ([\w]*)\(\)", zeile, flags=re.IGNORECASE)
        if suchergebnis != []:
            arraynamen.extend(suchergebnis)
        return arraynamen
    
    def sonderbehandlung_array_in_funktionskopf(self, zeile):
        parameter = re.findall("\w* \w*\((.*)\)", zeile, flags=re.IGNORECASE)
        if parameter == ['']:
            return []
        suchergebnis = re.findall("([\w]*)\(\)", parameter[0], flags=re.IGNORECASE)
        return suchergebnis
    
    def sonderbehandlung_array_verwendung(self, zeile, arraynamen):
        zeile_in_klein = zeile.lower();
        if not "dim" in zeile_in_klein and not "function " in zeile_in_klein and not "sub " in zeile_in_klein:
            for name in arraynamen:
                if name in zeile:
                    regAusdruck = name + r"\((\w*)\)"
                    ersetzung = name + r"[\g<1>]"
                    zeile = re.sub(regAusdruck, ersetzung, zeile)
        return zeile
    
    def behandle_blacklist(self, zeile):
        for eintrag in blacklist:
            if eintrag in zeile:
                zeile = "verbotener_befehl() in VBA-Programm"
        return zeile

    def importiere_vba_code(self, code):
        arraynamen = []
        funktionsname = "dummydummy"
        funktionsnamevar = "dummydummy_var"
        ignoriereFunktion = False
        # Zeilenumbrüche zusammenführen
        vba = re.sub(" _\n", " ", code)
        # Zeilenwechelsel vereinheitlichen
        vba = re.sub("\r", "\n", vba)
        # Einzeilige if then/else: Befehle aufbrechen
        vba = re.sub("Then (.*\w+.*\n)", "Then\n\g<1> End If", vba)
        vba = re.sub("Else: (.*\n)", "Else\n\g<1>", vba)
        vbazeilen = vba.split("\n")

        neuevbazeilen = []
        indent = 0
        for zeile in vbazeilen:
            endfunction = False;
            zusaetzlicheZeilen = []
            zeile = zeile.strip()
            funktionnamensuchergebnis = re.findall("(function|sub) (\w*)", zeile, flags=re.IGNORECASE)
            if len(funktionnamensuchergebnis) > 0:
                funktionsname = funktionnamensuchergebnis[0][1]
                if funktionsname[-6:].lower() == "_click":     # Alles Prozedurnamen, die auf _click enden, werden in Kleinbuchstaben umgewandelt
                    zeile = "Sub " + funktionsname.lower() + "()"
                funktionsnamevar = funktionsname + "_var"
                zusaetzlicheZeilen.append(4 * " " + funktionsnamevar + " = 4711")   # Sicherheitshalbe immer Variable für Funktionsergebnis vorbelegen, bzw. Zeile in Prozedure einfügen, damit diese nicht leer bleibt.
                self.funktionsnamen.add(funktionsname)
                ignoriereFunktion = funktionsname in ignorefunction
                arraynamen = self.sonderbehandlung_array_in_funktionskopf(zeile)

            if not ignoriereFunktion:
                zeile = self.behandle_blacklist(zeile)
                zeile = self.sonderbehandlung_if_anweisung(zeile)
                arraynamen = self.sonderbehandlung_array_def(zeile, arraynamen)
                zeile = self.sonderbehandlung_array_verwendung(zeile, arraynamen)
                for (pattern, repl) in regulaere_ausdruecke:
                    zeile = re.sub(pattern, repl, zeile, flags=re.IGNORECASE)

                if zeile.find("def ") != 0:
                    #funktionsname durch entsprechenden Variablennamen ersetzen
                    #-> wenn er von Leerzeichen oder Klammern umschlossen ist
                    pattern = "([ (])" + funktionsname + "([ )])"
                    repl = "\g<1>" + funktionsnamevar +    "\g<2>"            
                    zeile = re.sub(pattern, repl, zeile, flags=re.IGNORECASE)
                    
                    #-> wenn er von am Zeilenanfang steht und mit Leerzeichen oder Klammern endet
                    pattern = "^" + funktionsname + "([ )])"
                    repl = funktionsnamevar +    "\g<1>"            
                    zeile = re.sub(pattern, repl, zeile, flags=re.IGNORECASE)
                    
                    #-> wenn er von am Zeilenende steht und mit Leerzeichen oder Klammern beginnt
                    pattern = "([ (])" + funktionsname + "$"
                    repl = "\g<1>" + funktionsnamevar           
                    zeile = re.sub(pattern, repl, zeile, flags=re.IGNORECASE)
                    #zeile = zeile.replace(funktionsname, funktionsnamevar)
                
                if zeile.lower().find("exit function") == 0:
                    zeile = "return " + funktionsnamevar;
                    
                if zeile.lower().find("end function") == 0:
                    zeile = "    return " + funktionsnamevar;
                    endfunction = True
                    funktionsname = "dummydummy"
                    funktionsnamevar = "dummydummy_var"
                    
                if zeile.lower().find("end sub") == 0:
                    funktionsname = "dummydummy"
                    funktionsnamevar = "dummydummy_var"



                newindent = re.search(indentkeywords, zeile, flags=re.IGNORECASE) is not None
                removeindent = endfunction or re.search(removeindentkeywords, zeile, flags=re.IGNORECASE) is not None
                removeline = re.search(removelinekeyword, zeile, flags=re.IGNORECASE) is not None
                if removeindent:
                    indent -= 1
                if not removeline:
                    neuevbazeilen.append(indent * 4 * " " + zeile)
                    for zusatzlich in zusaetzlicheZeilen:
                        neuevbazeilen.append(indent * 4 * " " + zusatzlich)
                if newindent:
                    indent += 1
        return "\n".join(neuevbazeilen)

    # Der Import ist wegen der Sichtbarkeiten sehr gefrickelt:
    # Nachdem der VBA-Code umgewandelt und per exec geladen wurde, wird nachgesehen, welche Funktionen jetzt definiert sind,
    # und diese werden über globals() zur Verfügung gestellt.
    # Um eine Funktion/Prozedur aus dem VBA-Code aufzurufen, muss diese zunächst mit from vbaimport import fktname
    # importiert werden.

    def stelle_loesungsprogramm_zur_verfuegung(self, vbacode=None):
        if vbacode is None:
            vbacode = self.lade_loesungsprogramm()
        try:
            with open("vbafunktionen.py", "r", encoding="utf-8") as f:
                vbafunktionen = f.read()
            code = vbafunktionen + vbacode
            exec(code)
        except Exception as err:
            print(err)
            raise ("Code nicht ladbar: " + str(err))
        l = locals().copy()
        v = l.values()
        k = l.keys()
        neue_globals = globals()
        
        for key in k:
            #            print(key, ": ", type(l.get(key)) )
            if str(type(l.get(key))).find("function") > 0:
                neue_globals[key] = l.get(key)

    def stelle_tabelle_zur_verfuegung(self, tabelle, name="sheets"):
        globals()[name] = tabelle

    @classmethod
    def lade_loesungsprogramm(self, vbacode: str) -> str:
        with open("vbaCode.txt", "r", encoding="utf-8") as f:
            vbacode = f.read()
        with open("vbafunktionen.py", "r", encoding="utf-8") as f:
            vbafunktionen = f.read()
        pyCode = vbafunktionen + vbaimport().importiere_vba_code(vbacode)
        return pyCode
    
    @classmethod
    def execute_vbacode(self, filename: str):
        with open(filename, "r", encoding="utf-8") as f:
            vbacode = f.read()
        pycode = vbaimport().importiere_vba_code(vbacode)
        print (pycode)
        vbaimport().stelle_loesungsprogramm_zur_verfuegung(pycode)
        exec("test()")

