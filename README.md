# Handset Detection

Handset Detection is a service that lets you Detect Mobile Browsers. Our Database has over 50,000 handsets and growig at 60 to 80 new handsets per day. This is particularly useful if you want to target the ever growing number mobile users. 

## Whats new in Java-API-Kit Version 3.5?
* Supports stand alone as well as web service device detection.
* Supports enhanced detection for operating system (platform) and browser detection.
* Supports v3 of the Handset Detection schema.
* Proxy Configuration - Optional configuration of local proxy's for http requests.
* Server Failover - The api kit will seek out different Handset Detection servers if a connection times out.
* A config file.

## This is the installation guide of HDAPI examples. The examples are tested on Apache Tomcat 8.0

Directories&files description:
1. PROJECT_ROOT/src: Source code of servlets can be found at 
2. PROJECT_ROOT/build.xml: Ant build file.
3. PROJECT_ROOT/hd3examples: Tomcat webapps directory contains classes and servlets. 

Installation steps:
1. Copy directory hd3examples to {TOMCAT_ROOT}/webapps
2. Start Apache-Tomcat server and try url http://localhost:8080/WebServiceHD3Test/

Happy Detecting - email hello@handsetdetection.com if you have any issues.



