import re

import jinja2


class MyFileSystemLoader(jinja2.FileSystemLoader):
    def __init__(self, searchpath, encoding='utf-8', followlinks=False, html=True):
        super(MyFileSystemLoader, self).__init__(searchpath, encoding, followlinks)
        self.filter = self.html_filter if html else self.tex_filter

    def get_source(self, environment, template):
        (source, filename, uptodate) = super(MyFileSystemLoader, self).get_source(environment, template)
        return self.filter(source, filename, uptodate)

    def html_filter(self, source, filename, uptodate):
        # doppelte Leerzeilen durch <p> ersetzen
        split = re.split(r"(?:\r\n|\r(?!\n)|\n){2,}", source)
        result = "<p>\n".join(split)
        return (result, filename, uptodate)

    def tex_filter(self, source, filename, uptodate):
        return (source, filename, uptodate)
