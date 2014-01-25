YRA-Marks
=========

GPS coordinates for YRA Racing marks in San Francisco Bay

This project contains GPS coordinates for nearly all racing marks in
the San Francisco Bay and some other "interesting" marks.  The
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

Right now I offer the following formats:

 * **CSV** simple text format with coordinates in decimal degrees.
 
 * **GPX** very common format.  This can also be directly used as a
   layer in the excellent [OpenCPN](http://opencpn.org/ocpn/) chartplotter
   and navigation software.
 
 * **NK** binary format used by the (no longer developed)
   [Nauteek Tactical Speedo Compass](http://www.nauteek.com/EN/)
   
 * **MD** markdown format

 * **DDD** formatted text file with coordinates in decimal degrees

 * **DMM** formatted text file with coordinates in degrees and decimal minutes

 * **DMS** formatted text file with coordinates in degrees, minutes
   and decimal seconds

If you need another format, drop me a note and I might add it.  Of course you
can also always use [GPSBabel](http://www/gpsbabel.org/) yourself.
   
   Note:
   The official GpsBabel does not support the **NK** output format.  In case
   anyone is interested I can provide patches or Linux 32bit or 64bit
   binaries for this.

