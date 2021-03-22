# Remote GPS

The Flight Recorder stores, beside the telemetry data, the location data
(latitude, latitude and absolute altitude) that is put on the MSB bus
by the GPS module when they are together on board the plane.

When the Flight Logger is used on the ground, connected to a
Souffleur or to the COM port of a MPX HF module, the location
data is not available.
The Msb2Kml application is able to reconstruct the location
data from the telemetry data.
This is also true for the log files recorded by the sibling
application Msb2And.
The following explanations are relative to the reconstruction
of the location data. 

## Preparation of the GPS module

The Multiplex Launcher program should have been used to attribute
a MSB address to three essentials measurements:

+ Azimuth: angle (degree) from the North under which the pilot see the plane.
+ Distance 2D: distance (m) from the pilot to the projection of the plane
 on the ground (use the Expert Mode).
+ Height: height (m) relative to the pilot.

Together theses measurements gives the position of the plane in
a cylindrical system of coordinates centered on the pilot (in reality
the center is the place of the first valid fix by the GPS module).

Theses measures could be used in a formula to convert them to a geographic
location if the location of the pilot is known.

## Pilot location

A table of known pilot locations is maintained in a file **StartGPS.gpx**
in the MSBlog directory. Each location is referenced by a name.  
A named location could have been prepared before or it could be specified
when processing the log file.

The bottom panel of the first window of the application is used
to prepare a location.

There are three methods available to record such a location:

+ use the GPS accessory of the tablet or smartphone.
+ write the location known by another mean (GPS, map, ...).
+ copy the location from a previously processed flight with
 the logger on board.
 
The file StartGPS.gpx could be opened by an application such as
[Vtrk](https://github.com/msb2kml/Vtrk) to check the locations on a map.  
This application could also be used to add locations to this file.
  
### Location with GPS of device

You could be asked to modify two settings:

+ Enable fine location: allow the GPS to work.
+ Give the application the access to the location.

It takes some time for the GPS to obtain a fix.
When it is acquired, you are shown the coordinates with the estimated
accuracy.
You should give a meaningful name to this location before accepting it.
The default name is a combination of the date and hour.

### Entering a location

You are presented with a form to enter the latitude and longitude in
decimal degrees, and the altitude in meters.
Also, a meaningful and unique name. The default name is a combination
of the date and hour.
If the location you have is in degrees, minutes, seconds you could
convert it with some utilities like
[RapidTables](https://www.rapidtables.com/convert/number/degrees-minutes-seconds-to-degrees.html).

### Copying a location

You are first presented a list of the previous flight for which there
exists a GPX file. You select one on the basis of the displayed comments.

The more time the GPS module has had to follow the satellites and the better the
accuracy of the fix. If it is assumed that the plane has returned exactly where
its flight has begun, the last fix recorded could be of a better quality.
Thus, you have the choice to use the first or the last fix.
You should enter a unique and meaningful name but the default 
name is a combination of the date and hour when the fix has been taken.

## Processing

There are two requirements for the reconstruction of the location data:

+ The AddrSens.txt file should contain an expression "=GPS" with
 valid parameters.
+ The name of a location defined in the file StartGPS.txt should have been
 provided.
 
If in the last menu before processing of a log file, the line
"Use remote GPS?" is checked you are presented with a list of the
locations known in the StartGPS.txt file. You could select one of
theses locations or create a new entry with one of the last 3
lines that correspond to the options explained before. 

The reconstructed location data is processed exactly like the
genuine thing.

The reconstruction has the priority on the recorded location:
you could omit to specify a starting location name to use the recorded data.
This could be used for testing.
