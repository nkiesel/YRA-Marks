#! /usr/bin/python3
# Copyright (c) 2016 Norbert Kiesel <nk@iname.com>

#
# Updates the CSV file from the weekly updated NOAA list.
#

import xml.etree.ElementTree as ET
import re
import csv
import urllib.request
import requests

# input and output file
csvfile = '../San_Francisco.csv'

# updated NOAA list in XML format
noaaurl = 'https://www.navcen.uscg.gov/?Do=weeklyLLCXML&id=6'

noaa_coord_pattern = re.compile('(\d+)-(\d+)-(\d+.\d+) ?([NSEW])')
name_pattern = re.compile('.+ (\d+)$')
num_pattern = re.compile('^\[(\d+)\]')
dual_color_pattern = re.compile('^(\w)\w+ and (\w)\w+ ')
single_color_pattern = re.compile('^(Red|Yellow|Green|White)')
buoy_status_pattern = re.compile(r'<strong>(\w+)</strong>\D+(\d+) ([\d.]+)\D+(\d+) ([\d.]+) \((.+)\)')

# convert DMS into a decimal
def dms2decdeg(d, m, s, c):
    seconds = float(d) * 3600 + float(m) * 60 + float(s)
    if c in ['S', 'W']:
        seconds = -seconds
    return "{:.6f}".format(seconds / 3600)

def dmm2decdeg(d, m, c):
    seconds = float(d) * 3600 + float(m) * 60
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


# contains mapping from NOAA light number to YRA name
noaa = {}

# set of all marks with NOAA light numbers
has_noaa_light_number = set()

# cotains mapping from YRA names to mark record/array
marks = {}

# contains YRA names in correct order
order = []

# read file and fill `noaa` and `marks`
with open(csvfile) as sffile:
    sf = csv.reader(sffile)
    # skip header
    next(sf)
    for row in sf:
        id = row[2]
        order.append(id)
        num = num_pattern.match(row[3])
        if num:
            noaa[num.group(1)] = id
            has_noaa_light_number.add(id)
        else:
            marks[id] = row

# use coordinates from YRA "buoy status page" unless mark has a NOAA light number
with requests.get('http://yra.org/buoy-status/') as response:
   for line in response.iter_lines(decode_unicode=True):
      m = buoy_status_pattern.match(line)
      if m:
         yraname = "YRA-{}".format(m.group(1))
         if yraname not in has_noaa_light_number:
             existing = marks.get(yraname)
             if existing:
                 lat = dmm2decdeg(m.group(2), m.group(3), 'N')
                 lon = dmm2decdeg(m.group(4), m.group(5), 'W')
                 marks[yraname] = [lat, lon, yraname, existing[3]]
             else:
                 print("Mark {} is in the YRA Buoy status list but not in our CSV file".format(yraname))

# download and parse updated NOAA list and append required marks to `marks`
with urllib.request.urlopen(noaaurl) as xml:
    dataroot = ET.parse(xml).getroot().find('dataroot')
    for e in dataroot:
        llnr = text(e, 'LLNR')
        yraname = noaa.get(llnr)
        if yraname:
            marks[yraname] = [
                conv(text(e, 'Position_x0020__x0028_Latitude_x0029_')),
                conv(text(e, 'Position_x0020__x0028_Longitude_x0029_')),
                yraname,
                description(llnr, text(e, 'Aid_x0020_Name'), text(e, 'Characteristic'), text(e, 'Structure'))]

# replace csvfile content with updated marks
with open(csvfile, mode="w", newline='\r\n') as sffile:
    print('Latitude,Longitude,Name,Description', file=sffile)
    for id in order:
        d = marks[id]
        print('{},{},"{}","{}"'.format(*d), file=sffile)
