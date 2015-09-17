#! /usr/bin/python3

import re
import sys

cardinals = {
    'lat' : { True : 'N', False : 'S' },
    'lon' : { True : 'E', False : 'W' }
}

formats = ('dms', 'noaa', 'dmm')

def conv(ds, l):
    dd = float(ds)
    cardinal = cardinals[l][dd >= 0]
    minutes, seconds = divmod(abs(dd) * 3600, 60)
    degrees, minutes = divmod(minutes, 60)
    if fmt == 'dms':
        return '{0:s}{1:.0f} {2:.0f} {3:.3f}'.format(cardinal, degrees, minutes, seconds)
    if fmt == 'noaa':
        return '{1:.0f}-{02:.0f}-{3:06.3f}{0:s}'.format(cardinal, degrees, minutes, seconds)
    if fmt == 'dmm':
        return '{0:s}{1:.0f} {2:.6f}'.format(cardinal, degrees, minutes + seconds / 60)
    return ds

def coords(match):
    return '"{}","{}",'.format(conv(match.group(1), 'lat'), conv(match.group(2), 'lon'))

fmt = sys.argv[1]
if not fmt in formats:
    print('Unsupported format. Please use one of {}'.format(formats))
    exit(1)

p = re.compile(r'^([-.\d]+),([-.\d]+),')

for line in sys.stdin:
    print(re.sub(p, coords, line), end='')
