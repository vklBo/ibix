# VBA-Funktionen, die auf Python-Funktionen gemapped werden müssen

def IsNumeric(s):
    if s is None:
        return False
    try:
        float(s)
        return True
    except ValueError:
        return False


def CDbl(wert):
    return float(wert)


def CLng(wert):
    return float(wert)


def CInt(wert):
    return float(wert)


def RGB(r, g, b):
    return "RGB({},{},{})".format(r, g, b)


# die ReDim-Befehle aus VBA werden auf die folgenden Funktionen gemapped:
def redim_preserve(a, i):
    if i >= len(a):
        a.extend(list(range(i-len(a) + 1)))
    return a

def redim(a, i):
    if i >= 0:
        a = list(range(i+1))
    else:
        a = []
    return a

def UBound(fehlerArray):
    return len(fehlerArray) - 1


def MsgBox(text):
    pass


def IsEmpty(zelle):
    return (zelle == '')

def Round(zahl, stellen=0):
    return (zahl)
