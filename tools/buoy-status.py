#!/usr/bin/python3

import csv
import re
import requests

p = re.compile(r'<strong>(\w+)</strong>\D+(\d+) ([\d.]+)\D+(\d+) ([\d.]+) \((.+)\)')

yra = {}
for row in csv.reader(open('../San_Francisco.csv')):
   yra[row[2]] = row

def dmm2ddd(d, m, c):
   seconds = float(d) * 3600 + float(m) * 60
   if c in ['S', 'W']:
      seconds = -seconds
   return "{:.6f}".format(seconds / 3600)

with requests.get('http://yra.org/buoy-status/') as response:
   for line in response.iter_lines(decode_unicode=True):
      m = p.match(line)
      if m:
         mark = "YRA-{}".format(m.group(1))
         lat = dmm2ddd(m.group(2), m.group(3), 'N')
         lon = dmm2ddd(m.group(4), m.group(5), 'W')
#         print('"{}","{}","{}","{}"'.format(lat, lon, mark, m.group(6)))
#         print(yra[mark])
         if mark in yra:
            x = yra[mark]
            if not re.match(r'\[\d+\]', x[3]):
               dlat = abs(float(lat) - float(x[0]))
               dlon = abs(float(lon) - float(x[1]))
               if dlat > 0.0001 or dlon > 0.0001:
                  print('Position for mark {} is off by {} lat and {} lon'.format(mark, dlat, dlon))
                  print('"{}","{}","{}","{}"'.format(lat, lon, mark, m.group(6)))
                  print(yra[mark])
         else:
            print('"{}","{}","{}","{}"'.format(lat, lon, mark, m.group(6)))
                  
#            print('"N{} {}","W{} {}","{}","{}"'.format(m.group(2), m.group(3), m.group(4), m.group(5), mark, m.group(6)))


# next steps are
# - convert the coordinates to DDD format so that we can use the csv file instead of the dmm file
# - compute distance between csv file and coordinates form YRA and report is distance is above some margin
