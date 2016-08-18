#! /usr/bin/python3
# Copyright (c) 2016 Norbert Kiesel <nk@iname.com>

#
# Updates the CSV file from the weekly updated NOAA list.
#

import xml.etree.ElementTree as ET
import re
import csv
import urllib.request

# input and output file
csvfile = '../San_Francisco.csv'

# updated noaa list in XML format
noaaurl = 'http://www.navcen.uscg.gov/?Do=weeklyLLCXML&id=6'

noaa_coord_pattern = re.compile('(\d+)-(\d+)-(\d+.\d+) ?([NSEW])')
name_pattern = re.compile('.+ (\d+)$')
num_pattern = re.compile('^\[(\d+)\]')
dual_color_pattern = re.compile('^(\w)\w+ and (\w)\w+ ')
single_color_pattern = re.compile('^(Red|Yellow|Green|White)')

# convert DMS into a decimal
def dms2decdeg(d, m, s, c):
    seconds = float(d) * 3600 + float(m) * 60 + float(s)
    if c in ['S', 'W']:
        seconds = -seconds
    return "{:.6f}".format(seconds / 3600)

# convert DMS into a decimal
def conv(n):
    match = noaa_coord_pattern.match(n)
    return dms2decdeg(match.group(1), match.group(2), match.group(3), match.group(4))

# returns stripped text of child node named `tag`
def text(e, tag):
    return e.find(tag).text.strip()

# returns color abbreviation from either structure of light description
def color(c, s):
    color = dual_color_pattern.match(s)
    if color:
        color = color.group(1) + color.group(2)
    else:
        color = single_color_pattern.match(s)
        if color:
            color = str(color.group(1)[0])
        else:
            color = c.split()
            if color:
                color = str(color[1][-1])
    if color:
        color = color.upper()
        if color[0] in ['R', 'G', 'Y', 'W']:
            return "{} ".format(color)
    return ''

# returns description in our own format
def description(nr, name, characteristic, structure):
    n = name_pattern.match(name)
    num = "'{}' ".format(n.group(1)) if n else ''
    col = color(characteristic, structure)
    cha = "{} ".format(characteristic) if characteristic else ''
    return '[{}] {}{}{}{}'.format(nr, col, num, cha, name)


# contains mapping from NOAA light number to YRA number
noaa = {}

# updated list of marks
marks = []

# read file and fill `noaa` and `marks`
with open(csvfile) as sffile:
    sf = csv.reader(sffile)
    # skip header
    next(sf)
    for row in sf:
        num = num_pattern.match(row[3])
        if num:
            noaa[num.group(1)] = row[2]
        else:
            marks.append(row)

# download and parse updated NOAA list and append required marks to `marks`
with urllib.request.urlopen(noaaurl) as xml:
    dataroot = ET.parse(xml).getroot().find('dataroot')
    for e in dataroot:
        llnr = text(e, 'LLNR')
        yranum = noaa.get(llnr)
        if yranum:
            marks.append([
                conv(text(e, 'Position_x0020__x0028_Latitude_x0029_')),
                conv(text(e, 'Position_x0020__x0028_Longitude_x0029_')),
                yranum,
                description(llnr, text(e,'Aid_x0020_Name'), text(e, 'Characteristic'), text(e, 'Structure'))])

# replace csvfile content with updated marks
with open(csvfile, mode="w", newline='\r\n') as sffile:
    print('Latitude,Longitude,Name,Description', file=sffile)
    for d in sorted(marks):
        print('{},{},"{}","{}"'.format(*d), file=sffile)
