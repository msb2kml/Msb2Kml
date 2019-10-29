# GPX track on map module
This module draws the track from the GPX file on a map.  
It works with interaction with the
[Msb2Map](https://github.com/msb2kml/Msb2Map) application.

See the [menu](Gallery/MenuVtrk.jpg).
  
### Color
The color of each segment of the track could be modulated by the
corresponding value of one of the columns in the CSV file.  
The color could vary from blue, through green, to red in 12 steps.
The values for each end of the range could be specified.  
Default values are taken from what is recorded in the meta file.

If no column is selected, a segment is red if the altitude (from the
GPX file) is rising and blue if sinking.

A segment is black if there is no valid value for the selected
column in the CVS file.

### Mode "Entire"
The whole track, from the beginning to the end, is drawn on
the map by the Msb2Map application.

The information field in the bottom of the map displays the minimum and
maximum values for the column that has been used to colorize the track.  
If no column has been selected, the minimum and maximum altitudes
are displayed.

See the [screenshot](Gallery/FullTrack.jpg).

### Mode "Vapor trail"
See the explanations for the Msb2Map application.

The track is drawn progressively, location by location, on the map.
It is performed approximately at the real speed or accelerated
X2 or X10.

It is possible to skip some time from the current position on
the track: 2 minutes or 10 minutes.

It is possible to interrupt the drawing while the map is displayed,
to modify the color setup or the speed, to skip some part and then
to continue from the point of suspension.

Using the "Entire" mode reset the position at the beginning of the track.

The information field displays the current value for the column
that is used to modulate the color or the altitude from the GPX file.

See the [screenshot](Gallery/VaporTrail.jpg).

### Markers
There is an option to display with markers the locations contained
in the file StartGPS.gpx.

If this option is not selected, a marker is displayed at the first
location of the track.

### Orientation
This module and the Msb2Map application are not reacting to a change
of orientation of the screen.




