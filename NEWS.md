**2022-11-19**

 * Parse [YRA Decription of Marks](http://yra.org/descriptionofmarks/)
   content instead of [YRA Buoy Status](http://yra.org/buoy-status/)
   because the latter no longer has GPS coordinates of marks.
 * Renamed "YRA-30" to "YRA-NR6" because that is the name of that mark in
   [YRA Decription of Marks](http://yra.org/descriptionofmarks/).
   
**2022-02-07**

 * Removed LLNR from generated files.  These are required to extract GPS coordinates from USCG
   but not useful for ordinary users.
 
**2021-06-10**

 * Added Garmin POI format

**2019-04-01**

 * Updated Sequoia Yacht club marks, fixed "YRA-NG7", and updated Google maps with latest GPX file.
 
**2018-12-11**

 * Now using the coordinates from YRA [Buoy
   Status](http://yra.org/buoy-status/) for all marks that are not NOAA
   maintained

**2018-07-11**

 * Added/updated racing marks for SBYC, SYC, YRA

**2017-02-06**

 * Updated YRA marks from yra.org web site
 * Ordered marks to be in sync with YRA
 * YRA-13 "Crissy Field Buoy" and YRA-TYC "Privately maintained White mark “TYC”
   off entrance to Paradise Cove Harbor" are missing because I do not know their
   location.  Email or merge requests more than welcome for this!

**2016-12-02**

 * Removed Nauteek file (mine is broken, and I was the only user)
 * Updated Sequoia racing marks (added E, new positions for X and Y)
 * Now using gpsbabel-1.5.3

**2016-02-13**

 * Added script for getting coordinates and description from NOAA weekly updated
   list.  Note that this resulted in a re-ordering of marks.

**2015-09-16**

 * Replaced the text formats for DMS, DMM with CSV files
 * Dropped MD format as Github now displays CVS files nicely
 * dropped DDD format as this was identical to CSV
 * Now using gpsbabel-1.5 (which no longer uses `&apros;` for `'` in GPX)
 * `/gpx/time` in GPX is now set to modification time of CSV file instead of
   conversion time
