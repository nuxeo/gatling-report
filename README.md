# Gatling report

Parse Galting simulation.log file to output request stats in CSV or build HTML reports like trend reports.

# Install

## Download

From [nexus](http://maven.nuxeo.org/nexus/#nexus-search;quick~gatling-report)

## Building from sources

Create the all in one jar:

        mvn package

A jar will all its dependencies is located here:

        ./target/gatling-report-VERSION-capsule-full.jar

# Usage

## Generate CSV stats

The following command will output to stdout a CSV with stats per request.

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log
        
        scenario        request start   startDate       duration        end     count   successCount    errorCount      min     p50     p95     p99     max     avg     stddev  rps
        sim50bench      _all    1446578664151   2015-11-03 20:24:24     62.16   1446578726313   2397    2397    0       0       10      598     940     1938    104.40  205     38.56
        sim50bench      Logout  1446578664151   2015-11-03 20:24:24     62.16   1446578725039   21      21      0       1       2       15      17      17      2.38    3       0.34
        ...

 - scenario: Name of the scenario in the simulation
 - request: Name of the request, _all is the total of all requests in the simulation
 - start: start timestamp of the simulation 
 - startDate: start date of the simulation
 - duration: duration of the simulatoin
 - end: last request timestamp
 - count: total number of request
 - successCount: number of requests with the OK status
 - errorCount: number of requets with the KO status
 - min: minimum elapsed duration for the request
 - p50, p95, p99: percentile 50 95 and 99
 - avg: average
 - stddev: standard deviation
 - rps: average request per second for the simulation duration
 
You can also submit multiple simulation files, the output will concatenate stats. 

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log [path/to/simulation2.log ...]
  

The tool also accept simulation log that are gzipped:
 
        java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log.gz
         
  
        
## Generate an overview report for a simulation

 
       java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log.gz /path/to/report/directory

 
## Generate a trend report for a list of simulations


       java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log.gz path/to/simuation2.log /path/to/report/directory



# About Nuxeo

Nuxeo provides a modular, extensible Java-based
[open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep)
and packaged applications for
[document management](http://www.nuxeo.com/en/products/document-management),
[digital asset management](http://www.nuxeo.com/en/products/dam) and
[case management](http://www.nuxeo.com/en/products/case-management). Designed
by developers for developers, the Nuxeo platform offers a modern
architecture, a powerful plug-in model and extensive packaging
capabilities for building content applications.

More information on: <http://www.nuxeo.com/>
