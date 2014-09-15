
[input]:input.html


# Usage

Speech recognizer is divided into four conceptual parts:

	1. Input
	2. Front-end
	3. Decoder
	4. Evaluator

Audio source comes in form of pre-recorded data into database form, ideal for evaluating and testing
the algorithm implementation. Front-end part is ... Decoder receives the result of the front-end and
tries to convert byte arrays to text.  

One posible configuration
![lego-overview](/images/lego-overview.png "Logo Title Text 1")

	

## Input 

Audio information is streamed from a file or microphone.  

[Input detailed description &raquo;][input]

## Front-end 

A pipeline of processing, filtering and altering the audio stream.

## Decoder

Decoder speech to text.

## Evaluator

Evaluating the result produced by the recognizer.

## Monitor

Resources consumed and durations.

## Scale

Resources consumed and durations.