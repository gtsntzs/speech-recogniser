----

## Purpose

Slices up an Audio into a number of overlapping windows, and applies a Windowing function to each of them. The number of resulting windows depends on the window size and the window shift (commonly known as frame size and frame shift in speech world). The Window will be applied to each such window. Since the read() method will return a window, and multiple windows are created for each Audio, this is a 1-to-many processor.

A window (which is an Audio object) is returned each time Windower.getAudio() is called.

The applied Window, W of length N (usually the window size) is given by the following:

 W(n) = (1-a) - (a * cos((2*Math.PI*n)/(N - 1)))
where:
a is commonly known as the "alpha" value, it defaults to 0.46, the value for the HammingWindow, which is commonly used.

#### Dependency


```xml
<dependency>
    <groupId>soa.speech.recogniser</groupId>
	  <artifactId>sphinx4soc-windower</artifactId>
	  <version>x.x.x</version>
    <!-- use the same version as sphinx4soc version -->
</dependency>
```

#### URI format

```URI
  windowSizeInMs=25.625&amp;windowShiftInMs=12.00&amp;alpha=0.46
```

#### Options


| Property        | default    | description  |
| --------------- |:----------:| ------------ |
| windowSizeInMs  |  25.625    |         |
| windowShiftInMs |  12.00     |           |
| alpha           |  0.46      |          df |

#### example

```xml
		<camel:route id="frontend-input">
			<camel:from uri="..." />
			<camel:to uri="windower://raisedCosine?windowSizeInMs=25.625&amp;windowShiftInMs=12.00&amp;alpha=0.46" />
		</camel:route>

		<camel:route id="frontend-window">
			<camel:from uri="windower:raisedCosine?windowSizeInMs=25.625&amp;windowShiftInMs=12.00&amp;alpha=0.46" />
			<camel:to uri="..." />
		</camel:route>
```

### TODO

Register a name for a callback
