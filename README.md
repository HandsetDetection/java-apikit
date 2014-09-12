# Handset Detection

Handset Detection is a service that lets you detect Tablets, Handsets and other mobile devices. Our system can detect millions of device variations across tens of thousands of mobile devices. 

We also have built in purpose built detections for Platform, Platform Version, Browser and Browser version. 

## Whats new in Java-API-Kit Version 3.5?
* Supports stand alone as well as web service device detection.
* Supports enhanced detection for operating system (platform) and browser detection.
* Supports v3 of the Handset Detection schema.
* Proxy Configuration - Optional configuration of local proxy's for http requests.
* A config file.

##### This is the installation guide of HDAPI examples. The examples are tested on Apache Tomcat 6.0 +

###### Directories and files description:
1. PROJECT_ROOT/src: Source code of servlets can be found at 
2. PROJECT_ROOT/build.xml: Ant build file.
3. PROJECT_ROOT/WebServiceHD3Test: Tests for Tomcat. Contains classes and servlets. 

###### Tomcat installation steps:
1. Stop Tomcat.
2. Copy directory WebServiceHD3Test/WebContent to {TOMCAT_ROOT}/webapps.
3. Copy the config template file to {TOMCAT_ROOT}/webapps/WebContent/WEB-INF directory.
4. Edit the config file. Substitute in your API credentials (username, secret and site_id).
4a. Optional - Rename {TOMCAT_ROOT}/webapps/WebContent directory to a sane name. :)
5. Start Apache-Tomcat server and try url http://localhost:8080/WebContent or http://localhost:8080/SaneName

Happy Detecting - email hello@handsetdetection.com if you have any issues.



