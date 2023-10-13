import helper.vbaimport
from sys import argv

filename = "/Users/vk/git/ibix/src/test/resources/vbaCodeTestFiles/FunktionLeer_ok_.vba"

if len(argv) >= 2:
    filename = argv[1];

helper.vbaimport.vbaimport().execute_vbacode(filename)