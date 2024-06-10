YRA-Marks
=========

GPS coordinates for [YRA Racing marks][chart] in San Francisco Bay

See [NEWS.md](NEWS.md) for recent updates.

This project contains GPS coordinates for nearly all racing marks in
the San Francisco Bay and some other "interesting" marks.  The
information here was collected from multiple sources:

 * Existing web sites
 * NOAA ENC charts
 * Sailing clubs
 * [USCG Weekly Light List Volume6][llv6]

You can download the list in multiple formats or look at them using
[Google Maps][chart] which also allows to search for these marks on
the map using the mark name (e.g. "YRA-18").

The easiest way to download all files is to left-click on the green
"Code" button in the lop-right corner and then left-click the
"Download ZIP" at the bottom of the resulting popup windows. The
resulting ZIP file contains the marks in all the generated formats
listed below.

If you just want a single file, you can also right-click on the file
name above and then select "Save Link as...".
   
Of course I do not claim that any of this information is correct and
strongly advise not to use this for navigation.  Having said that, if
anyone finds mistakes or has additions, please feel free to send me
updates.

For most marks that are listed in "Volume 6 - District 11" of the
[Weekly Light List][llv6], the description in [San_Francisco.csv](San_Francisco.csv)
contains the light number in `[]` (e.g. `[360]` for the Lightship).
However, that number is omitted from all the other files because it
is not of interest to most users.

The marks are offered in multiple formats from generated
[San_Francisco.csv](San_Francisco.csv) using
[GPSBabel].

As an example, the GPX file was generated using

    gpsbabel -i unicsv -f San_Francisco.csv -o gpx -F San_Francisco.gpx

Right now I offer the following formats:

 * **CSV** simple text format with coordinates in decimal degrees (e.g. `-122.358967`)
 
 * **GPX** very common format.  This can also be directly used as a layer in the
   excellent [OpenCPN](https://opencpn.org/) chartplotter and navigation
   software.
 
 * **POI** Garmin POI format (should work for Garmin Marine handhelds)

 * **DMM** CSV with coordinates in degrees and decimal minutes (e.g. `W122 21.538020`)

 * **DMS** CSV with coordinates in degrees, minutes and decimal seconds (e.g. `W122 21 32.281`)

 * **NOAA** CSV with coordinates in degrees, minutes and decimal seconds
   (e.g. `122-21-32.281W`).  This is the format used in the NOAA light lists.

 * **SHP** format commonly used in mapping applications

 * **HTML** simple text version

If you need another format, drop me a note and I might add it.  Of course you
can also always use [GPSBabel] yourself.
   
[llv6]: https://www.navcen.uscg.gov/weekly-light-lists
[chart]: https://www.google.com/maps/d/u/0/edit?mid=1-9oOlBeR2zTQUb8ltyKwN68LgAvBEaYQ&usp=sharing
[GPSBabel]: https://www/gpsbabel.org/
