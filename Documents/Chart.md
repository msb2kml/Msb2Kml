# Charting module
This module is used to plot the data from the processed CSV files.

### General disposition
It is possible to plot any number of columns as Y versus an X column.
The graph is auto scaling. For the Y two simultaneous scales are
possible: one is displayed on the left of the plot and the
other on the right. Each curve is attributed a color from the
list: black, blue, cyan, magenta, green, yellow and red.
The color of curves related to the left scale is attributed starting
with black and starting with red for the curves related to the
right scale.

### Axis attribution
Columns are referenced by their heading. The columns that are
used are initially selected by the preferences stored from a
precedent usage. If there is no valid preference, the X axis is
the first column and a single Y axis is the second column.

### Graph interaction
The graph is controlled by the buttons at the bottom of the screen.
Middle button: let you select the column for the X values.
The button at the extreme left let you select one or several
column(s) to plot, sharing the left scale.
And the extreme right button has the similar function for the right axis.
The button at the right of the middle button is used to quit this
module and return to the previous screen.

The button at the left of the middle button let you save the graph
as an image in the Gallery of your device for sharing or archive.
This image is saved in a sub directory Msb2Kml. The name is
composed from the base of the name of the log file (MSB_XXXX) and
numbers that are the indexes of the used columns.

It is possible to zoom and pan using the fingers.

### Acknowledgement

This module uses the [MPAndroidChart library](https://github.com/PhilJay/MPAndroidChart).

