# Handset Detection

Handset Detection is a service that lets you Detect Mobile Browsers. Our Database has over 50,000 handsets and growig at 60 to 80 new handsets per day. This is particularly useful if you want to target the ever growing number mobile users. 

## Whats new in Java-API-Kit Version 3.5?
* Supports stand alone as well as web service device detection.
* Supports enhanced detection for operating system (platform) and browser detection.
* Supports v3 of the Handset Detection schema.
* Proxy Configuration - Optional configuration of local proxy's for http requests.
* Server Failover - The api kit will seek out different Handset Detection servers if a connection times out.
* A config file.

## How to deploy:
1. Create new folder name WebServiceHD3Test in your tomcat webapps/ directory.
2. Download the WebServiceHD3Test.war file and WEB-INF folder inside WebServiceHD3Test/src/ directory.
3. Put the WebServiceHD3Test.war file and WEB-INF folder inside your tomcat webapps/WebServiceHD3Test directory.
3. Edit WEB-INF/hdapi_config.properties - insert your email address and secret from http://www.handsetdetection.com
6. Run your tomcat server applicaton and go to this url http://localhost:8080/WebServiceHD3Test/. 8080 is the default port number of your tomcat server.

Happy Detecting - email hello@handsetdetection.com if you have any issues.



