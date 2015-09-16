# Conversions

The actual conversion is done running `./convert` from within this directory.

# Tips and Tricks

Lookup of coordinates in the lightlist can be simplified by converting the PDF
to a text file, and converting the DMS coordinates into decimal degrees as
required by the CSV file.

`pdftotext -nopgbrk -layout LightList\ V6.pdf`

For increased readability, the PDF file can be preprocessed with [Briss][briss]
to remove the header and footer text before that.

[briss]: http://sourceforge.net/projects/briss/
