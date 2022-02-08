import re
import sys

def decdeg2dms(ds):
    dd = float(ds)
    is_positive = dd >= 0
    dd = abs(dd)
    minutes,seconds = divmod(dd*3600,60)
    degrees,minutes = divmod(minutes,60)
    degrees = degrees if is_positive else -degrees
    return "{:.0f}-{:02.0f}-{:.3f}".format(degrees, minutes, seconds)

def dms2decdeg(d, m, s, c):
    seconds = float(d) * 3600 + float(m) * 60 + float(s)
    if c == 'S' or c == 'W':
        seconds = -seconds
    return "{:.6f}".format(seconds / 3600)
    
def conv(match):
    return dms2decdeg(match.group(1), match.group(2), match.group(3), match.group(4))
    
p = re.compile('(\d+)-(\d+)-(\d+.\d+) ?([NSEW])')
for line in sys.stdin:
    print(re.sub(p, conv, line), end='')
        
