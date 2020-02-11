# First use
### Permissions
On the most recent versions of Android the permission to read and write
files is requested at the first use. The permission to obtain the
position from the GPS sensor is also needed for the "RemoteGPS" feature.

### Source directory
It is possible to process log files directly from the SD card
removed from the Flight Logger and mounted on the tablet or a
USB card reader.
It is also possible to create a dedicated directory on the tablet
and to copy the files from the SD card to this directory:
it is then easier to re-process some file.
Only the files with a name matching the pattern
"MSB\_XXXX.csv" are considered.
The program remembers the last used directory in its preferences.

The selection of the file to process is performed through the
use of a specialized file explorer. It is possible to go "up"
or "down" in the hierarchy of directories. Two "root" directories
are possible: the local storage (usually /storage/emulated/0/) and
the remote storage (usually /remote/).  
The application switch from one to the other when going "up" from
the root.

### Output directory
All the files produced by the application are placed in
the directory MSBlog. It is created if
not already existing. This directory is located
in the directory /storage/sdcard/ of /storage/emulated/0/,
function of the Android version.
This directory could contain a file "AddrSens.txt"
used to customize the headings and to provide formulas
for advanced usage. If this file does not exist,
the program proposes to create a default one.

# Basic use
### Product files
From a logger file named "MSB\_XXXX.csv" the application
produce the files:

* "MSB\_XXXX.txt": this is the meta file. It contains the date,
       hour of the flight, name of the plane, comment, name
       of the start location.
       It contains also the minimum and maximum value observed
       for each parameter and also a copy of "AddrSens.txt"
       that has been used for the processing.
* "MSB\_XXXX.html": this is an HTML file of all the data presented
       in tables, one table by minute of flight with navigation
       links.
* "MSB\_XXXXf.csv": this is essentially a copy of the input
       file cleaned off the GPS data and transformed for further
       processing: there is only one line of headings, the
       decimal separator is the dot (12.34) instead of the
       comma (12,34).
* "MSB\_XXXXd.csv": this is the same as the previous file but decimated
       to 1 measure per second: this is generally sufficient and
       faster to display. Only one of the "f" or "d" file is produced.
* "MSB\_XXXX.gpx": this is the track in a GPX file (if GPS present).
* "MSB\_XXXX.kml": the track (if GPS present) in a KML file as a chain
       of colored segments.

### Launching the application
It has a red and green icon with the letters "M/K".
There are two panels on the screen, the bottom one is
concerned by the processing from a remote GPS module:
see the file "RemoteGPS" in this directory.
From the top panel there are 2 branches: one to browse the previously
processed logs and one to process a new log.
It works with a succession of menus with the possibility to get
back to the upper level.

### Browsing processed logs
The application look in the directory MSBlog for all files with
a name of the form "MSB\_XXXX.txt" (meta files). 
For each file are displayed: the date of the flight, the name of
the plane and the comment. The file name is preceded by some
characters:

+ d: the file MSB\_XXXXd.csv is available.
+ f: the file MSB\_XXXXf.csv is available.
+ h: the file MSB\_XXXX.html is available.
+ g: the file MSB\_XXXX.gpx is available.
+ k: the file MSB\_XXXX.kml is available.

A choice of visualization options is presented when a file is selected.

There is a specialized viewer for the meta file.
The 3 parts are specifically displayed:

+ the first part contains the conditions of the 
 flight (start instant, plane name, etc...).
+ the second part concerns the minimum and maximum of each parameter.
+ the third part (available through a button) contains a copy of
 the AddrSens.txt that has been used for the processing.

The HTML file is viewed with the HtmlViewer.  
The CSV file is viewed with the Chart module: see explanations in the
file Chart in this directory.  
The GPX file could be viewed with the "GPX on map" module: see the
file GpxOnMap in this directory.  
Finally, the KML file could be viewed with "Google Earth". 
The last version of GE permits to view the track in 3D.
And, out of the application, this file could also be transferred to
a PC where Google Earth has more capabilities: perspective view,
hiding some segments, profile...
The track is divided in segments each of 1 minute flight time,
alternating red and blue. There are other possibilities with
the advanced usage.

### Processing a new log
As seen before, the file to process is selected with a specialized
file explorer.

When a file has been selected there is a succession of choices
for the processing. Usually the default choices are to be used.
One has then to provide the meta information. If a corresponding
meta file is found in MSBlog the information in this
file is used as default for the meta information.
A progression bar is displayed while the log file is processed.
Then the same visualization choices are presented as explained before.

### Progressive setup
Some sensors put their data on the MSB bus with a
delay after having been powered up.  
This could result in output files with a variable number
of columns.  
To keep the homogeneity of theses files,
the application discards all previous data at the appearance
of a new sensor.

### Basic use of AddrSens.txt
This file in the directory MSBlog provides some customization.
All lines of this file starting with a star "\*" are comments that
are skipped.
Each active line is composed of 3 fields separated by a semi-colon ";"
but the third field is optional.
For the basic usage the first field has to be exactly as the heading of
a column as delivered by the Flight Logger: "Time" and " A:00" to " A:16"
(note the space). If the Logger heading match a first field, it is replaced
with the second field. But if the second field is a minus "-" the column
is hidden from the listings (but could be used for computation).
An exception is made for the first field: it is always reproduced
in the CSV file as the time has a function in some display module.
If the third field is present it should be a single character (any character,
any case). This character is used as name of a variable that takes the
value of the column for each sample: see the advanced usage.
The significant character for this third field could be preceded
by ignored white spaces: this provides a better readability.

