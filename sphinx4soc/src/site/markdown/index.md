## Idea

Every scientific community faces the problem of having to create frameworks either from scratch or patches in an existing one, in order to apply their new ideas. Creating a framework from scratch is demanding, patches usually do not follow any programming contract and thereafter are difficult to reuse and benefit from them. This paper creates a new environment where Automatic Speech Recognition (ASR) community can take advantage of Service Orientated Computing (SOC). An existing ASR framework will be broken into its elementary parts and assembled back together making use of the enterprise integration patterns (EIP). Resulting in a framework flexible to alter, fast to execute, independent from conventional programming languages and providing a universal testing environment for evaluating different approaches.

The abstract of the [paper]() where the idea was first published.

## Speech Recogniser

Sphinx4soc is speech recogniser based on the original implementation of [CMU Sphinx](http://cmusphinx.sourceforge.net/). Sphinx4soc is promoting the new programming paradigm on the top of the original approach. Sphinx4soc is taking advantage of Service Oriented computing, where each part of the speech recogniser is a "component" and each function a service.

The main components are:

  * input
  * front-end
  * the decoder and
  * the evaluator

The input defines the source of the audio data, currently a file based poller is provided, in a future release input may be retrieve with a use of a nosql database is planed, a semantic content manager. Microphone input is partially supported.

The front-end pipeline component provides the full functionality as of the CMU Sphinx. Creating new services for the front-end is simple as shown in the detailed overview [here]().

The decoder component provides the typical algorithms provided by the CMU Sphinx recogniser.

Lastly, the monitoring component purpose is twofold. The one is the recognition evaluation and the second provides detailed system monitoring capabilities.


## Project Structure

The project is organised in a multi module maven project. The Maven structure was designed to provide simpicity, easy of use and extendability. Every module has its own documentation site, where purpose and usage is explained. The modules are organised in four main components:

  * [**archetypes**](http://gtsntzs.github.io/speech-recogniser/archetypes) for easy generation of new Sphinx4soc modules
  * [**components**](http://gtsntzs.github.io/speech-recogniser/components) are extra camel modules
  * [**persistence**](http://gtsntzs.github.io/speech-recogniser/persistence) the layer storing speech, model files and data created by the recognition process.
  * the libraries with the original source code of the CMU Sphinx slightly modified in [**cmu-sphinx4**](http://gtsntzs.github.io/speech-recogniser/cmu-sphinx4soc) and additional implementation in module [**sphinx4soc-lib**](http://gtsntzs.github.io/speech-recogniser/sphinx4soc-lib).

Sphinx4soc is configured using Spring Context XML files, a nice tutorial about Spring Context and dependency injection can be found in [springindepth](http://www.springindepth.com) and about Camel Context in [Spring Camel DSL](http://camel.apache.org/spring.html).

Orchestrating services or components is a difficult to define task. Defining a domain language is not trivial, but making use of enterprise integration patterns it is possible every potential team to define its own domain language according to their needs. For this reason, the integration is made through [Apache Camel](http://camel.apache.org/) where connecting or changing disparate systems is simple and can be done in real time using moderm OSGI container like [fabric8](http://fabric8.io/) or [JBoss Fuse](http://www.jboss.org/products/fuse/overview/)

---

## Future List

 + MongoDB
 + Apache Cassandra

Functionality

 + LIUM Speaker Diarization
 + speech vs music classification
 + music fingerprint
 + Video speaker identification
 + Video context

---

### Citation

Please if you use or be inspired by this work, cite the paper where the idea was first published.

CMU Sphinx4 speech recognizer in a Service-oriented Computing style.
Georgios Tsontzos, and Reinhold Orglmeister.
SOCA, IEEE, (2011) - The citation in [bibtex]() or in your favorite format [here &raquo;]()
