Shapefile is a common geographic information system (GIS) format. The San_Francisco.shp file here was created with the following command:

    % ogr2ogr -overwrite -f "ESRI Shapefile" . convert-to-shp.vrt

Documentation on how the San_Francisco.csv file was converted from CSV to
Shapefile is [here](http://www.gdal.org/drv_csv.html) and
[here](http://www.gdal.org/drv_vrt.html).

For Debian and derived distributions like Ubuntu, `ogr2ogr` can be installed
using:

    % sudo apt-get install gdl-bin
