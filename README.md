YRA-Marks
=========

GPS coordinates for YRA Racing marks in San Fancisco Bay

This project contains GPS coordinates for nearly all racing marks in
the San Francisco bay and some other "interesting" marks.  The
information here was collected from multiple sources:

 * existing web sites
 * NOAA ENC charts
 * sailing clubs

Of course I do not claim that any of this information is correct and
strongly advise not to use this for navigation.  Having said that, if
anyone finds mistakes or has additions, please feel free to send me
updates.

The coordinates are offered in multiple formats.  However, the
canonical source is the CSV format and all other formats are generated
using [GPSBabel](http://www.gpsbabel.org/).

As an example, the GPX file was generated using

    gpsbabel -i unicsv -f San_Francisco.csv -o gpx -F San_Francisco.gpx
