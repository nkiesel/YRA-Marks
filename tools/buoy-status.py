#!/usr/bin/python3

import csv
import re
import requests

p = re.compile(r'<strong>(\w+)</strong>\D+([\d .]+)\D+([\d .]+) \((.+)\)')

dmm = {}
for row in csv.reader(open('../San_Francisco.dmm')):
   dmm[row[2]] = row

with requests.get('http://yra.org/buoy-status/') as response:
   for line in response.iter_lines(decode_unicode=True):
      m = p.match(line)
      if m:
         mark = "YRA-{}".format(m.group(1))
         if mark not in dmm:
            print('"N{}","W{}","{}","{}"'.format(m.group(2), m.group(3), mark, m.group(4)))
#            print(dmm[mark])


# next steps are
# - convert the coordinates to DDD format so that we can use the csv file instead of the dmm file
# - compute distance between csv file and coordinates form YRA and report is distance is above some margin
