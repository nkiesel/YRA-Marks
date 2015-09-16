#! /usr/bin/python3

import re
import sys

cardinals = {
    'lat' : { True : 'N', False : 'S' },
    'lon' : { True : 'E', False : 'W' }
}

formats = ('dms', 'noaa', 'dmm')

def conv(fmt, ds, l):
    dd = float(ds)
    cardinal = cardinals[l][dd >= 0]
    dd = abs(dd)
    if fmt == 'dms':
        minutes, seconds = divmod(dd * 3600, 60)
        degrees, minutes = divmod(minutes, 60)
        return '"{0:s}{1:.0f} {2:.0f} {3:.3f}"'.format(cardinal, degrees, minutes, seconds)
    if fmt == 'noaa':
        minutes, seconds = divmod(dd * 3600, 60)
        degrees, minutes = divmod(minutes, 60)
        return '"{1:.0f}-{02:.0f}-{3:06.3f}{0:s}"'.format(cardinal, degrees, minutes, seconds)
    if fmt == 'dmm':
        degrees, minutes = divmod(dd * 60, 60)
        return '"{0:s}{1:.0f} {2:.6f}"'.format(cardinal, degrees, minutes)
    return ds
    
def coords(match):
    return '{},{},'.format(conv(f, match.group(1), 'lat'),
                           conv(f, match.group(2), 'lon'))

f = sys.argv[1]
if not f in formats:
    print('Unsupported format. Please use one of {}'.format(formats))
    exit(1)

p = re.compile(r'^(-?\d{1,3}\.\d{6}),(-?\d{1,3}\.\d{6}),')

for line in sys.stdin:
    print(re.sub(p, coords, line), end='')
