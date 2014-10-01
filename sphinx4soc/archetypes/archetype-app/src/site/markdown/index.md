---

## Description


## Usage

mvn archetype:generate \
   -DarchetypeGroupId=soa.speech.recogniser.archetypes \
   -DarchetypeArtifactId=archetype-app \
   -DarchetypeVersion=0.33-SNAPSHOT \
   -DgroupId=soa.speech.recogniser.app \
   -DartifactId=amodel \
   -Dname=test

mvn archetype:generate \
   -DarchetypeGroupId=soa.speech.recogniser.archetypes \
   -DarchetypeArtifactId=archetype-app \
   -DarchetypeVersion=1.0.3 \
   -DgroupId=soa.speech.recogniser.app \
   -DartifactId=mfcc-feature \
   -Dname=mfcc-feature

