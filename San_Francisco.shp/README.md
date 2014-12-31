Shapefile is a common geographic information system (GIS) format. The San_Francisco.shp file here was created with the following command:
% ogr2ogr -overwrite -f "ESRI Shapefile" . convert-to-shp.vrt

Documentation on how the San_Francisco.csv file was convertred from CSV to Shapefile is here:
http://www.gdal.org/drv_csv.html
http://www.gdal.org/drv_vrt.html
