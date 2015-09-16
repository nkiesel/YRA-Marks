**2015-09-16**

 * Replaced the text formats for DMS, DMM with CSV files
 * Dropped MD format as Github now displays CVS files nicely
 * dropped DDD format as this was identical to CSV
 * Now using gpsbabel-1.5 (which no longer uses `&apros;` for `'` in GPX)
 * `/gpx/time` in GPX is now set to modification time of CSV file instead of
   conversion time
