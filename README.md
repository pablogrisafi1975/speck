# Speck - a tiny web framework for Java 21
==============================================

Work in progress, do not even try to use this!

The goal of this project is to produce a working web framework for java 21 that

 * Is more or less compatible with SparkJava, the excellent web framework you can find [here](https://github.com/perwendel/spark)
 * Does not use any dependencies, except for testing
 * Defaults to using VirtualThreads

So I basically forked SparkJava, upgraded some stuff, removed other stuff, replaced other stuff
and got this thing (kind of) working. 

## What is missing?

* Remove copyright notices
* Documentation: I'm sure I can copy/paste/edit Spark documentation and most things would work, but I should test everything
* WebSockets: The server in com.sun.net.httpserver.HttpServer does not support them, I don't think there is a way out
* Logging: It kind of works using System.Logger, but I want to be sure you can add a dependency to Slf4J and use it as expected
* Upload/download files: Didn't get to test it yet
* Attributes: Not working yet
* Many more tests, enable/uncomment all tests.
* Actual performance and load testing using JMeter
* GitHub actions to build and upload to maven central
* and many more things...


## What would be awesome to have

* WebSockets: Maybe there is a way to do it?
* ServerSentEvents: While there is no native support in com.sun.net.httpserver.HttpServer I think they are not so hard to implement 
* Compile time json serialization/deserialization... so still no libraries but full json support!
* Some way to upload files without any library
* Use Graal native to save memory or whatever
