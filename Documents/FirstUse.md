# First use
### Permissions
On the most recent versions of Android the permission to read and write
files is requested at the first use.

### Source directory
It is possible to process log files directly from the SD card
removed from the Flight Logger and mounted on the tablet.
But it is preferable to create a dedicated directory on the tablet
and to copy the files from the SD card to this directory:
it is then easier to re-process some file.
Only the files with a name matching the pattern
"MSB_XXXX.csv" are considered.
The program remembers in its preferences the last used directory.

### Output directory
All the files produced by the application are placed in
the directory MSBlog. It is created if
not already existing. This directory is located
in the directory /storage/sdcard/ of /storage/emulated/0/,
function of the Andoid version.
This directory could contain a file "AddrSens.txt"
used to customize the headings and to provide formulas
for advanced usage. If this file does not exist,
the program proposes to create a default one.

# Basic use
### Product files
From a logger file named "MSB_XXXX.csv" the application
produce the files:

* "MSB_XXXX.txt": this is the meta file. It contains the date,
       hour of the flight, name of the plane, comment.
       It contains also the minimum and maximum value observed
       for each parameter and also a copy of "AddrSens.txt"
       that has been used for the processing.
* "MSB_XXXX.html": this is an HTML file of all the data presented
       in tables, one table by minute of flight with navigation
       links.
* "MSB_XXXXf.csv": this is essentially a copy of the input
       file cleaned off the GPS data and transformed for further
       processing: there is only one line of headings, the 
       decimal separator is the dot (12.34) instead of the
       comma (12,34).
* "MSB_XXXXd.csv": this is the same as the previous file but decimated
       to 1 measure per second: this is generally sufficient and
       faster to display. Only one of the "f" or "d" file is produced.
* "MSB_XXXX.gpx": this is the track in a GPX file (if GPS present).
* "MSB_XXXX.kml": the track (if GPS present) in a KML file as a chain
       of colored segments.

### Launching the application
It has an red and green icon with the letters "M/K".
From the start there are 2 branches: one to browse the previously
processed logs and one to process a new log.
It works with a succession of menus with the possibility to get
back to the upper level.

### Browsing processed logs
The application look in the directory MSBlog for all files with
a name of the form "MSB_XXXX.txt" (meta files). 
For each file are displayed: the date of the flight, the name of 
the plane and the comment.
A choice of visualization options is presented when a file is selected.
The meta file and the HTML file are viewed with the HtmlViewer.
Use the back touch to get out of this module.
The CSV file is viewed with the Chart module: see explanations in Chart.txt.
The GPX file could be viewed with "Track browser" (on-line or with
cached maps/photos) and with "OsmAnd" (off-line maps).
"Track browser" and "OsmAnd" could display some KML files but not
segmented tracks.
Finally, the KML file could be viewed with "Google Earth". 
The last version of GE permits to view the track in 3D.
And, out of the application, this file could also be transferred to 
a PC where Google Earth has more capabilities: perspective view, 
hiding some segments, profile...
The track is divided in segments each of 1 minute flight time,
alternating red and blue. There are other possibilities with
the advanced usage.

### Processing a new log
A specialized file manager shows only files with the appropriate 
form of name and directories. At the first ever use
the display start at the /mnt directory; at subsequent uses it
starts at the last directory used. The "../" entry move up in the hierarchy.
When a file has been selected there is a succession of choices
for the processing. Usually the default choices are to be used.
One has then to provide the meta information. If a corresponding
meta file is found in /sdcard/MSBlog the information in this
file is used as default for the meta information.
A progression bar is displayed while the log file is processed.
Then the same visualization choices are presented as explained before.

### Basic use of AddrSens.txt
This file in the directory /sdcard/MSBlog provide some customization.
All lines of this file starting with a star "\*" are comments that
are skipped.
Each active line is composed of 3 fields separated by a semi-colon ";"
but the third field is optional.
For the basic usage the first field has to be exactly as the heading of
a column as delivered by the Flight Logger: "Time" and " A:00" to " A:16"
(note the space). If the Logger heading match a first field it is replaced
with the second field. But if the second field is a minus "-" the column
is hidden from the listings (but could be used for computation).
If the third field is present it should be a single character (any character,
any case). This character is used as name of a variable that takes the
value of the column for each sample: see the advanced usage.
The significant character for this third field could be preceded
by ignored white spaces: this provides a better readability.

