
Sphinx4soc models define a directory structure for organising the files which contain the following files

  * the grammar file contains the grammatical rules for the voice interface
  * the acoustic models for acoustic scoring
  * the dictionary is a list of accepted words
  * the mdef files contain the weights over word selection
  * finally, in trigram are stored the language models

```dir
  resources
   └── default
       ├── grammar
       │   └── aGrammar.gram
       ├── models
       │   ├── cd_continuous_8gau
       │   │   ├── means
       │   │   ├── mixture_weights
       │   │   ├── transition_matrices
       │   │   └── variances
       │   ├── dict
       │   │   ├── cmudict.0.6d
       │   │   └── fillerdict
       │   ├── etc
       │   │   └── 13dCep_16k_40mel_130Hz_6800Hz.ci.mdef
       │   ├── license.terms
       │   └── README
       └── trigram
           └── tcb05cnp.Z.DMP
```

New model container are generated using the [Sphinx4soc :: Models :: Archetype](http://gtsntzs.github.io/speech-recogniser/archetypes/archetype-model/index.html).

The models currently available

   * [model-default](model-default/index.html)
