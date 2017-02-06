# Steps for Updating Official Marks and Convertions

## Official Marks

All NOAA administered marks have a unique number in their district.  YRA marks are all
in the 11th district, and we thus can use this number as their identifier.

Running `./updates_from_noaa.py` does exactly that: it

 - parses the CSV file and converts it into a map indexed by their id
 - downloads the weekly updated XML file from NOAA
 - updates the coordinates and description of matching markers in the map
 - writes the resulting map back to the CSV file
 
## Conversions

The next step is converting the updated CSV file into the supported variants.  This
is done by running `./convert`from within this directory.

## Upload

The final step is a simple `git push` to update the site on GitHub.
