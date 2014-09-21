---

## Purpose

A custom Maven Archetype for generating Sphinx4soc camel component projects.

## Usage

mvn archetype:generate \
   -DarchetypeGroupId=soa.speech.recogniser.archetypes \
   -DarchetypeArtifactId=component-archetype \
   -DarchetypeVersion=0.3-SNAPSHOT \
   -DgroupId=soa.speech.recogniser.components \
   -DartifactId=amodel \
   -Dname=test \
   -Dscheme=test
