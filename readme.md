# Java API Kit v4.0 #

This API Kit implements v4.0 of the HandsetDetection API.
API Kits can use our web service or resolve detections locally 
depending on your configuration.

This repo contains two projects. The main project is HDlib,
a Java library for working with Handset Detection. The other
project is WebServiceHDTest, a light weight wrapper for using
the library with Tomcat 7.

## Building ##

Both projects build with maven 3.x. 

	mvn package -Dmaven.test.skip=true

OR

	mvn install -Dmaven.test.skip=true

### Building with Unit Tests ###

Unit tests require two configuration files in the project root named
hd4CloudConfig.properties and hd4UltimateConfig.properties. Copy the 
hdapi_config.properties file as a template and change the access credentials.
In the hd4UltimateConfig.properties set use_local to true (Note: this requires
an Ultimate licence). The run

	mvn package

OR 

	mvn install
	 
## Configuration ##

You'll find your API Kit credentials from the dashboard. Follow these steps :

1. Login to your dashboard
2. Click 'Add a Site'
3. Configure your new site 
4. Grab the config file variables for your API Kit (from the site settings)
5. Place the variables into the hdconfig.php file


## Examples ##

### Instantiate the HD4 object ###

	// Using the default config file
	hd = new HD();

OR
	// Note : cloudConfig is a String eg "/tmp/somedir/myConfigFIle"
	hd = new HD(cloudConfig);

### List all vendors ###

	hd.deviceVendors();
	JsonObject reply = hd.getReply();

### List all models for a vendor ###

	hd.deviceModels("Nokia");
	JsonObject reply = hd.getReply();

### View information for a specific device (Nokia N95) ###

	hd.deviceView("Nokia", "N95");
	JsonObject reply = hd.getReply();

### What devices have this attribute ? ###

	hd.deviceWhatHas("design_dimensions", "101 x 44 x 16");
	JsonObject = hd.getReply();

### Device detection using HTTP Headers ###

	Map<String, String> headers = new HashMap<String, String>();
	headers.put("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
	hd.deviceDetect(headers);
	JsonObject reply = hd.getReply();

### Device detection using Android Build Information ###

	Map<String, String> headers = new HashMap<String, String>();
	headers.put("ro.build.PDA", "I9500XXUFNE7");
	headers.put("ro.build.changelist", "699287");
	headers.put("ro.build.characteristics", "phone");
	headers.put("ro.build.date.utc", "1401287026");
	headers.put("ro.build.date", "Wed May 28 23:23:46 KST 2014");
	headers.put("ro.build.description", "ja3gxx-user 4.4.2 KOT49H I9500XXUFNE7 release-keys");
	headers.put("ro.build.display.id", "KOT49H.I9500XXUFNE7");
	headers.put("ro.build.fingerprint", "samsung/ja3gxx/ja3g:4.4.2/KOT49H/I9500XXUFNE7:user/release-keys");
	headers.put("ro.build.hidden_ver", "I9500XXUFNE7");
	headers.put("ro.build.host", "SWDD5723");
	headers.put("ro.build.id", "KOT49H");
	headers.put("ro.build.product", "ja3g");
	headers.put("ro.build.tags", "release-keys");
	headers.put("ro.build.type", "user");
	headers.put("ro.build.user", "dpi");
	headers.put("ro.build.version.codename", "REL");
	headers.put("ro.build.version.incremental", "I9500XXUFNE7");
	headers.put("ro.build.version.release", "4.4.2");
	headers.put("ro.build.version.sdk", "19");
	headers.put("ro.product.board", "universal5410");
	headers.put("ro.product.brand", "samsung");
	headers.put("ro.product.cpu.abi2", "armeabi");
	headers.put("ro.product.cpu.abi", "armeabi-v7a");
	headers.put("ro.product.device", "ja3g");
	headers.put("ro.product.locale.language", "en");
	headers.put("ro.product.locale.region", "GB");
	headers.put("ro.product.manufacturer", "samsung");
	headers.put("ro.product.model", "GT-I9500");
	headers.put("ro.product.name", "ja3gxx");
	headers.put("ro.product_ship", "true");
	
	hd.deviceDetect(headers);
	JsonObject reply = hd.getReply();

### Device Deteciton using iOS build information ###

	Map<String, String> headers = new HashMap<String, String>();
	headers.put("utsname.machine", "iphone4,1");
	headers.put("utsname.brand", "Apple");
	
	hd.deviceDetect(headers);
	JsonObject reply = hd.getReply();

### Download the Full Ultimate Edition ###

	// The archive is downloaded, unpacked & installed ready for detection.
	// Ultimate customers should call this daily to freshen the dataset.
	hd.deviceFetchArchive();

### Download the Community Edition ###

	hd.communityFetchArchive();


## Getting Started with the Free usage tier and Community Edition ##

After signing up with our service you'll be on a free usage tier which entitles you to 20,000 Cloud detections (web service)
per month, and access to our Community Edition for Ultimate (stand alone) detection. The archive for stand alone detection
can be downloaded manually however its easiest to configure the API kit with your credentials and let the API kit do the
heavy lifting for you. See examples above for how to do this.

Instructions for manually installing the archive are available at [v4 API Ultimate Community Edition, Getting Started](https://handsetdetection.readme.io/docs/getting-started-with-ultimate-community-full-editions)


## API Documentation ##

See the [v4 API Documentation](https://handsetdetection.readme.io).


## API Kits ##

See the [Handset Detection GitHub Repo](https://github.com/HandsetDetection).


## Support ##

Let us know if you have any hassles (hello@handsetdetection.com).