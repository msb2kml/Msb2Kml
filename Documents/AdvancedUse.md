# Advanced use: derived values

Advanced use is introduced when the file AddrSens.txt contains
computations. An example is in the same directory as this text.

Purpose: to compute for each sample some secondary data
derived from the raw sensor data.

### Principle

A "tool box" of functions is available for this. Each function
is identified by a name starting with an equal "=", takes
some parameter(s) and acts on some variable(s).
The first field of a line in AddrSens.txt starts with the name
of the function followed by each parameter and variable
separated by commas ",". The variables are identified
by the character they have received (3thd field) preceded by a
dollar "$" character. A variable could be defined before or after
its use in the file.
If a variable is not defined any function that use it is discarded
and also recursively any function using a variable defined
by this function.

The computation is performed in the order or definition in the file.
The raw data and the computed data are kept in a registry between
samples. If a variable is used before it is declared, the value
that it has is thus the one for the previous sample: it is a
memory function.

### Special variables

There is one variable name that does not need to be defined and is
computed by the program if it is used: "\#". It is the accumulated
traveled distance (km) between points of the GPS track.

The variable named "%" could be defined and it has a special
signification for the program. This variable should be in the range
0 to 100 (out of range values are brought back). This range is divided
in six bins, each bin corresponding to a color from blue (0) to red (100).
A new segment of the track in the KML file starts when the color
change as "%" dictates a new bin.


### List of functions

Each function is presented as it could be used on a line of the file.

#### Battery internal resistance

    =BIR,1,$i,$V,$v;mOhm  

+ $i is the current. 
+ $v is the current voltage.
+ $V is the last voltage when the current was less than the threshold
 parameter (1 ampere in this case). See =HVL for $V.
Hint: if there is a too high spike when the current is cut off,
specify a higher threshold.

See: [chart](Gallery/MSB_0095_17_4_12.jpg)

#### Colorization of track in KML file.

    =COL,-0.35,0.35,$B;-;   %

Colorized here by the smoothed vario.
+ $B is the smoothed vario (see =SMTH). The range -0.35 to 0.35 m/s
for the vario is converted to the range 0 to 100 for the color ($%). 

See: [Screenshot](Gallery/Screenshot_2017-11-22-15-49-49.jpg)

#### Summation of all sinking of the flight

    =CUMN,$d,$u;Deniv-;     u

+ $d is the difference of height
from the previous sample (see =DIFF).
+ $u is the summation itself.
It keeps its value if $d is positive otherwise $d is subtracted
from $u.

#### Summation of all rising of the flight

    =CUMP,$d,$U;Deniv+;     U

+ $d is the difference of height
from the previous sample (see =DIFF).
+ $U is the summation itself.
It keeps its value if $d is negative otherwise $d is added to $U.

#### Difference of values between successive samples

    =DIF,$t,$T;-;           s

Here the values are the times.

+ $t is the current value.
+ $T is the memorized value (see =MEM).

#### Glide ratio

    =GLR,0.05,$#,$a;G.Ratio

+ $# is the traveled distance computed from the GPS data.
+ $a is the altitude measured by the GPS.

The glide ratio is not computed (remains null) if the altitude
is not decreasing while the distance traveled has augmented
by the parameter: here 0.05 km (50 m). The parameter should not
be less than 0.03 km.
Glide ratio is defined as the ratio of the distance traveled
horizontally to the distance traveled vertically at constant
airspeed in still air. These conditions could rarely be maintained
for long in a typical flight of our models. You have to apply
some judgment about this parameter.

See: [Chart](Gallery/MSB_0083_12_3_15.jpg)

#### Voltage with no load

    =HVL,1,$i,$v,$V;-;      V

+ $i is the current. 
+ $v is the current voltage.
+ $V is the memorized no load voltage.

Keeps its value if the current is lower than the threshold: here 1 A.
Otherwise follows the current voltage.

#### Memorize the current value for the next sample

    =MEM,$t;-;              T

+ $t is the value
to memorize (here the time).

#### Time scale covering only the working of the motor

    =MOT,1,$i,$s,$M;Motor s;M

+ $i is the current.
+ $s is the difference of time from the previous sample (see =DIFF).
+ $M is the new time scale itself.

If the current is above the threshold (here 1 A) the difference
of time is added to the time scale; otherwise it is not modified.

See: [Chart](Gallery/MSB_0095_17_4_12.jpg)

#### Energy (as limited for F5B and F5D)

    =NRJ,$w,$s,$j;W.min;   j
+ $w is the power (see =PROD).
+ $s is the difference of time from the previous sample (see =DIFF).
+ $j is the energy itself.

The product of the power by the delta time is added to the energy.
The appropriate factor is applied for a reading in Watt\*minute.

See: [Chart](Gallery/MSB_0095_0_4_11.jpg)

#### Product (here power) of 2 values

    =PROD,$i,$v;Watt;       w
+ $i is here the current.
+ $v is here the voltage.

#### Smoothing of a value (here the vario)

    =SMTH,0.1,$b,$B;fVario; B

+ $b is the value to smooth
(here the vario).
+ $B is the smoothed value.

The current value is added with some weight (here 0.1) to the
memorized value multiplied by the complementary weight (here 0.9). 

See: [Chart](Gallery/MSB_0069_0_4_10_3.jpg)

#### Time scale covering only the flight without motor

    =SOA,1,$i,$s,$m;Soar s; m
+ $i is the current.
+ $s is the difference of time from the previous sample (see =DIFF).
+ $m is the new time scale itself.

If the current is below the threshold (here 1 A) the difference
of time is added to the time scale; otherwise it is not modified.

#### Cumulative traveled distance

    =TRV,$K,$#;Km

+ $# is internally computed as this
distance from the successive positions recorded by the GPS.
+ $K is the speed from the GPS sensor: it is not used in the
computation but its inclusion assures than this function
is not invoked if the GPS is not present. 

See: [Chart](Gallery/MSB_0095_17_4_12.jpg)

#### Remote GPS reconstruction

    =GPS,$<,$/,$a;GPS
    
+ $< is the azimuth from the pilot to the plane computed by
 the GPS module.
+ $/ is the distance 2D from the pilot to the plane (module GPS).
+ $a is the altitude measured by the GPS.

This compute the real location (lat, long, alt) from the telemetry
data instead of direct data if the starting location is provided.
See the file "RemoteGPS".
The value that could be printed is only 0.0 or 1.0: 1 if there has
been no conversion error; otherwise 0.

### Note

The functions like =DIFF that make use of current and previous
values should be at the head of the file.
The functions like =MEM of =HVL that store values for the next sample
should be at the bottom of the file.

