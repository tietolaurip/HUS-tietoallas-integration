
#
# #%L
# CodeServer
# %%
# Copyright (C) 2017 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
 

import sys
import lxml.etree
import unicodecsv

# Given a list of lxml Elements and a list of attrs, returns a list of rows
# Each row is a list of strings, the first one is the value of id attribute of
# the elements parent. The other are elements attribute values for attributes
# given as attrs
# If an attribute does not exist, the result will contain an empty string
def extract_fields(elements,attribs):
    result=[]
    for e in elements:
        row=[]

        for attr in attribs:
            values = e.xpath(attr)
            if values == []:
                value = ''
            else:
                value = values[0]
            row.append(value);

        result.append(row)

    return result

if len(sys.argv) < 3:
    print("This is a script for extracting certain attributes from CodeServer")
    print("ListCodesResponse coming in from stdin to csv in stdout")
    print("Usage: xml_to_csv.py <date> <element xpath> <xpath>*")
    exit(1);

try:
    date = sys.argv[1]
    elem_xpath = sys.argv[2]
    attrs = sys.argv[3:]

    doc = lxml.etree.parse(sys.stdin)
    elems = doc.findall(elem_xpath)

    rows = extract_fields(elems,attrs)

    for row in rows:
        row.append(date);

    writer = unicodecsv.writer(sys.stdout,
                               encoding='utf-8',
                               delimiter='|',
                               quotechar='\\',
                               quoting=unicodecsv.QUOTE_MINIMAL)
    for row in rows:
        writer.writerow(row)


except Exception as e:
    etype, value, traceback = sys.exc_info()
    print >> sys.stderr, 'xml_to_csv failed: %s: %s' % (etype, value)
