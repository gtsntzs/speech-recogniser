----

## Creating new Components

New components are generated using the [Sphinx4soc :: Component :: Archetype]().

## Purpose

Components are special services which have the capability to store some amount of messages before they produce a result. For example, in order to estimate the deltas, it is needed at least two previous cepsta to be available. Therefore, a feature vector with deltas is produced only when two messages exist in the component.



