
## Purpose

The module consists the original <a href="http://cmusphinx.sourceforge.net/" target="_blank">CMU Sphinx</a> source code slightly modified. The original source code API has some restrictions on method visibility, if those methods weren't defined as private there is no need for this module. The pull data approach has been changed in a independent processor approach. Every filter or processing unit is performing its computations and returns the result to the integration layer.
