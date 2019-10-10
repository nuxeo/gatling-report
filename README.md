# About Gatling report

This is a reporting tool that parses [Galting](http://gatling.io/) result files (aka `simulation.log`) and creates HTML 
reports with [Plotly](https://plot.ly/) charts or CSV output.


# gatling-report examples

A report is a single standalone html file, here are some examples:

- [Simulation report](https://min.gitcdn.link/repo/nuxeo/gatling-report/master/docs/simulation-1/index.html): compact representation of response time for a
bench, help to pinpoint slow requests.
- [Differential report](https://min.gitcdn.link/repo/nuxeo/gatling-report/master/docs/diff-sim1-vs-sim2/index.html): compare 2 bench result.
- [Trend report](https://min.gitcdn.link/repo/nuxeo/gatling-report/master/docs/trend-sim1-10/index.html): follow the trend of bench results over time.
- [Default CSV output](./docs/sim1-10.csv)

# Install

## Download

Take the latest jar from [nexus](http://maven-eu.nuxeo.org/nexus/#nexus-search;quick~gatling-report).

## Building from sources

Create the all in one jar:

        mvn package

The `jar` file that include all its dependencies is located here:

        ./target/gatling-report-VERSION-capsule-full.jar

# Usage

## Help

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar -h

## Output CSV stats

The following command will output to stdout a CSV with stats per request.

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log
        
        scenario        request start   startDate       duration        end     count   successCount    errorCount      min     p50     p95     p99     max     avg     stddev  rps	apdex	rating
        sim50bench      _all    1446578664151   2015-11-03 20:24:24     62.16   1446578726313   2397    2397    0       0       10      598     940     1938    104.40  205     38.56	0.88	Good
        sim50bench      Logout  1446578664151   2015-11-03 20:24:24     62.16   1446578725039   21      21      0       1       2       15      17      17      2.38    3       0.34	1.00	Excellent
        ...

| Header|  Description |
| --- |  --- |
| `scenario` | Name of the scenario in the simulation |
| `request` | Name of the request, `_all` is the total of all requests in the simulation |
| `start` | start timestamp of the simulation |
| `startDate` | start date of the simulation |
| `duration` | duration of the simulation |
| `end` | last request timestamp |
| `count` | total number of request |
| `successCount` | number of requests with the OK status |
| `errorCount` | number of requets with the KO status |
| `min` | minimum elapsed duration for the request |
| `p50`, `p95`, `p99` | percentile 50, 95 and 99 |
| `avg` | average |
| `stddev` | standard deviation |
| `rps` | average request per second for the simulation duration |
| `apdex` | Apdex score converts many measurements into one number on a uniform scale of 0-to-1 |
| `rating` | Apdex rating |

You can also submit multiple simulation files, the output will concatenate stats. 

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log [path/to/simulation2.log ...]


You can also submit gzipped simulation files:

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log.gz
         

        
## Generate HTML report with Plotly charts

When using the `-o REPORT_PATH` option a report is generated.
 
When submitting a single simulation file it creates a simulation report:
 
       java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/simulation.log.gz -o /path/to/report/directory

 
When submitting two simulations files it creates a differential report:


       java -jar path/to/gatling-report-VERSION-capsule-fat.jar path/to/ref/simulation.log.gz path/to/challenger/simuation2.log -o /path/to/report/directory

When submitting more than two simulations files it creates a trend report.


## Customizing the report

You can use your own mustache template to customize the report:

        java -jar path/to/gatling-report-VERSION-capsule-fat.jar --template /my/template.mustache path/to/ref/simulation.log.gz -o /path/to/report/directory

Take example to the default templates located in src/main/resources.

Note that [Plotly charts](https://plot.ly/) can be edited online.

# Limitations

It has been tested successfully with default Gatling 2.3.1, 2.1.7, 3.0.0 and 3.2.1 `simulation.log` format.

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
