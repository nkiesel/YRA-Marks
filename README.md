YRA-Marks
=========

GPS coordinates for YRA Racing marks in San Francisco Bay

This project contains GPS coordinates for nearly all racing marks in the San
Francisco Bay and some other "interesting" marks.  The information here was
collected from multiple sources:

 * Existing web sites
 * NOAA ENC charts
 * Sailing clubs
 * [USCG Weekly Light List Volume6][llv6]
   
Of course I do not claim that any of this information is correct and strongly
advise not to use this for navigation.  Having said that, if anyone finds
mistakes or has additions, please feel free to send me updates.

For most marks that are listed in the [Weekly Light List][llv6], the description
contains the light number in `[]` (e.g. `[360]` for the Lightship).

The coordinates are offered in multiple formats.  However, the canonical source
is the CSV format and all other formats are generated from that using
[GPSBabel](http://www.gpsbabel.org/).

As an example, the GPX file was generated using

    gpsbabel -i unicsv -f San_Francisco.csv -o gpx -F San_Francisco.gpx

Right now I offer the following formats (all but CSV generated from CSV):

 * **CSV** simple text format with coordinates in decimal degrees (e.g. `-122.358967`)
 
 * **GPX** very common format.  This can also be directly used as a layer in the
   excellent [OpenCPN](http://opencpn.org/ocpn/) chartplotter and navigation
   software.
 
 * **DMM** CSV with coordinates in degrees and decimal minutes (e.g. `W122 21.538020`)

 * **DMS** CSV with coordinates in degrees, minutes and decimal seconds (e.g. `W122 21 32.281`)

 * **NOAA** CSV with coordinates in degrees, minutes and decimal seconds
   (e.g. `122-21-32.281W`).  This is the format used in the NOAA light lists.

 * **SHP** format commonly used in mapping applications

 * **HTML** simple text version

If you need another format, drop me a note and I might add it.  Of course you
can also always use [GPSBabel](http://www/gpsbabel.org/) yourself.
   
   Note:
   The official GpsBabel does not support the **NK** output format.  In case
   anyone is interested I can provide patches or Linux 32bit or 64bit binaries
   for this.

[llv6]: http://www.navcen.uscg.gov/?pageName=lightListWeeklyUpdates
