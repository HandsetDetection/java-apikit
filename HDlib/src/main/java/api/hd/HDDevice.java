package api.hd;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class HDDevice extends HDBase 
{
	private final HDExtra extra;
	private final HDStore store;
	private JsonElement device;
	private Map<String, String> buildInfo;
	private SortedSet<RatingEntry> ratingResult;
	private final Pattern httpHeadersSplitPattern = Pattern.compile("[,;]");
	private final Pattern httpHeadersReplacePattern = Pattern.compile(" ");
	private final Gson gson = new Gson();
	
	public HDDevice() throws IOException 
	{
		super();
		store = HDStore.getInstance();
		extra = new HDExtra();
	}
	
	public HDDevice(String cfgFilename) throws IOException 
	{
		super(cfgFilename);
		store = HDStore.getInstance();
		extra = new HDExtra(cfgFilename);
	}
	
	public HDDevice(Config cfg) throws IOException 
	{
		super(cfg);
		store = HDStore.getInstance();
		extra = new HDExtra(cfg);
	}

	public HDDevice(byte[] isCfg) throws IOException 
	{
		super(isCfg);
		store = HDStore.getInstance();
		extra = new HDExtra(isCfg);
	}
	
	/**
	 * Find all device vendors
	 *
	 * @return true, if successful
	 */
	protected synchronized boolean localVendors() 
	{
		JsonObject data = this.fetchDevices().getAsJsonObject();						
		boolean ret = false;
		if (data == null)
			return ret;
		
		this.reply = new JsonObject();
		SortedSet<String> vendors = new TreeSet<String>();
		JsonElement devices = data.get(JsonConstants.DEVICES);
		
		if (devices != null && devices.isJsonArray())
		{
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) 
			{
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) 
				{
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonConstants.DEVICE);
					if (HDUtil.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonConstants.HD_SPECS);
					if (HDUtil.isNullElement(hdSpecs)) continue;
					JsonElement vendor = hdSpecs.get(JsonConstants.GENERAL_VENDOR);
					if (HDUtil.isNullElement(vendor)) continue;
					vendors.add(vendor.getAsString());
				}
			}
			
//			reply.add("vendor", HDUtil.toUniqueJsonArray(vendors));
			reply.add("vendor", gson.toJsonTree(vendors));				//catalin: vednors is no longer an ArrayList now
			ret = this.setError(0, "OK");
			
		} else {
			
			ret = this.setError(299, "Error: No devices data");
		}		
		
		return ret;				
	}

	/**
	 * Find all models for the sepecified vendor
	 *
	 * @param vendor the vendor
	 * @return true, if successful
	 */
	protected synchronized boolean localModels(String vendor) {
		boolean ret = false;
		JsonObject data = this.fetchDevices().getAsJsonObject();
		if (data == null)
			return ret;
		
		this.reply = new JsonObject();
		Set<String> models = new TreeSet<String>();
		JsonElement devices = data.get(JsonConstants.DEVICES);
		if (devices != null && devices.isJsonArray()) {
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonConstants.DEVICE);
					if (HDUtil.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonConstants.HD_SPECS);
					if (HDUtil.isNullElement(hdSpecs)) continue;
					JsonElement vendorElement = hdSpecs.get(JsonConstants.GENERAL_VENDOR);
					if (HDUtil.isNullElement(vendorElement)) continue;
					String strVendor = vendorElement.getAsString();
					if (strVendor.equalsIgnoreCase(vendor)) {
						models.add(hdSpecs.get(JsonConstants.GENERAL_MODEL).getAsString());
					}
					String vendorKey = strVendor + " ";
					JsonElement aliasesElement = hdSpecs.get(JsonConstants.GENERAL_ALIASES);
					if (HDUtil.isNullElement(aliasesElement)) continue;
					if (aliasesElement.isJsonArray()) {
						JsonArray tempAliasesArray = (JsonArray)aliasesElement;
						Iterator<JsonElement> aliasesIter = tempAliasesArray.iterator();
						while (aliasesIter.hasNext()) {
							JsonElement alias = aliasesIter.next();
							if (alias.isJsonPrimitive()) {
								String strAlias = alias.getAsString();
								if (!HDUtil.isNullOrEmpty(strAlias) && strAlias.indexOf(vendor) == 0) {
									models.add(strAlias.replace(vendor, "").trim());
								}
							}
						}
					} else if (aliasesElement.isJsonPrimitive()) {
						String strAlias = aliasesElement.getAsString();
						if (!HDUtil.isNullOrEmpty(strAlias) && strAlias.indexOf(vendorKey) == 0) {
							models.add(strAlias.replace(vendorKey, ""));
						}
					}
				}
			}
			
			reply.add("model", gson.toJsonTree(models));				//catalin: vednors is no longer an ArrayList now
			ret = setError(0, "OK");
			
		} else {
			
			ret = setError(299, "Error: No devices data");
		}
		
		return ret;
	}

	/**
	 * Finds all the specs for a specific device
	 *
	 * @param vendor the vendor
	 * @param model the model
	 * @return true, if successful
	 */
	protected synchronized boolean localView(String vendor, String model) 
	{
		boolean ret = false;
		JsonObject data = this.fetchDevices().getAsJsonObject();		
		if (data == null)
			return ret;
		
		this.reply = new JsonObject();
		JsonElement devices = data.get(JsonConstants.DEVICES);
		if (devices != null && devices.isJsonArray()) {
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonConstants.DEVICE);
					if (HDUtil.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonConstants.HD_SPECS);
					if (HDUtil.isNullElement(hdSpecs)) continue;
					JsonElement vendorElement = hdSpecs.get(JsonConstants.GENERAL_VENDOR);
					JsonElement modelElement = hdSpecs.get(JsonConstants.GENERAL_MODEL);
					if (HDUtil.isNullElement(vendorElement) || HDUtil.isNullElement(modelElement)) continue;
					if (vendorElement.getAsString().equalsIgnoreCase(vendor) 
							&& modelElement.getAsString().equalsIgnoreCase(model)) {
						this.reply.add("device", hdSpecs);
						ret = this.setError(0, "OK");
						return ret;
					}
				}
			}
			
			ret = this.setError(301, "Error: Nothing found");
			
		} else {
			
			ret = this.setError(299, "Error: No devices data");
		}
		return ret;
	}

	/**
	 * Finds all devices that have a specific property
	 *
	 * @param key the key
	 * @param value the value
	 * @return true, if successful
	 */
	protected synchronized boolean localWhatHas(String key, String value) 
	{
		//catalin: no this.reply = new JsonBoject() ?
		boolean ret = false;
		JsonObject data = this.fetchDevices().getAsJsonObject();
		if (data == null)
			return ret;
		
		JsonArray matches = new JsonArray();
		JsonElement devices = data.get(JsonConstants.DEVICES);
		if (!HDUtil.isNullElement(devices) && devices.isJsonArray()) {
			ret = true;
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonConstants.DEVICE);
					if (HDUtil.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonConstants.HD_SPECS);
					if (HDUtil.isNullElement(hdSpecs)) continue;
					JsonElement keyElement = hdSpecs.get(key);
					if (HDUtil.isNullElement(keyElement)) continue;
					boolean match = false;
					//just search entire string no matter if it"s array of string
					match = keyElement.toString().toLowerCase().indexOf(value.toLowerCase()) >= 0;
	
					if (match) {
						JsonObject matched = new JsonObject();
						matched.add("id", device.get(JsonConstants.ID));
						JsonElement vendorElement = hdSpecs.get(JsonConstants.GENERAL_VENDOR);
						JsonElement modelElement = hdSpecs.get(JsonConstants.GENERAL_MODEL);
						matched.add(JsonConstants.GENERAL_VENDOR, vendorElement);
						matched.add(JsonConstants.GENERAL_MODEL, modelElement);
						matches.add(matched);
						
					}
				}
			}
			this.reply.add("devices", matches);
			ret = this.setError(0, "OK");
			
		} else {
			
			ret = this.setError(299, "Error: No devices data");
		}
		
		return ret;
	}
	
	/**
 	 * Perform a local detection
	 *
	 * @param array $headers HTTP headers as an assoc array. keys are standard http header names eg user-agent, x-wap-profile
	 * @return bool true on success, false otherwise
	 */
	boolean localDetect(Map<String, String> headers) 
	{
		initRequest();
		// lowercase headers on the way in.
		SortedMap<String, String> headersMap = new TreeMap<String, String>();
		for (Entry<String, String> entry : headers.entrySet()) 
			headersMap.put(entry.getKey().toLowerCase(), null == entry.getValue() ? "" : entry.getValue().toLowerCase());
		
		String hardwareInfo = headersMap.get("x-local-hardwareinfo");
		headersMap.remove("x-local-hardwareinfo");
		
		// Is this a native detection or a HTTP detection ?
		if (null != hasBiKeys(headersMap))
			return v4MatchBuildInfo(headersMap);
		
		return v4MatchHttpHeaders(headersMap, hardwareInfo);
	}
	

	/**
	 * Returns the rating score for a device based on the passed values
	 *
	 * @param string $deviceId : The ID of the device to check.
	 * @param array $props Properties extracted from the device (display_x, display_y etc .. )
	 * @return array of rating information. (which includes "score" which is an int value that is a percentage.)
	 */
	private Map<String, Integer> findRating(String deviceId, Map<String, String> props) 
	{
		JsonElement device = findById(deviceId);
		JsonObject specs = gson.toJsonTree((JsonPath.read(device.toString(), "$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS)), new TypeToken<Map<String, String>>() {}.getType()).getAsJsonObject();
		if (HDUtil.isNullElement(specs))
			return null;
		
		Integer total = 70;
		Integer score = 0;
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		// Display Resolution - Worth 40 points if correct
		result.put("resolution", 0);
		if (!HDUtil.isNullOrEmpty(props.get("display_x")) && !HDUtil.isNullOrEmpty(props.get("display_y")))
		{
			Integer pMaxRes = Math.max(Integer.parseInt(props.get("display_x")), Integer.parseInt(props.get("display_y")));
			Integer pMinRes = Math.min(Integer.parseInt(props.get("display_x")), Integer.parseInt(props.get("display_y")));
			Integer sMaxRes = Math.max(specs.get("display_x").getAsInt(), specs.get("display_y").getAsInt());
			Integer sMinRes = Math.min(specs.get("display_x").getAsInt(), specs.get("display_y").getAsInt());

			if (pMaxRes.equals(sMaxRes) && pMinRes.equals(sMinRes)) 
			{
				// Native resolution
				result.put("resolution", 40);
				score += 40;
			} 
			else 
			{
				// Check CSS screen sizes
				String[] cssScreenSizes = specs.get("display_css_screen_sizes").getAsString().split(",");
				for(String size : cssScreenSizes)
				{
					String[] res = size.split("x");
					if (res.length == 2) 
					{
						Integer tmpMaxRes = Math.max(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
						Integer tmpMinRes = Math.min(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
						if (pMaxRes.equals(tmpMaxRes) && pMinRes.equals(tmpMinRes)) 
						{
							result.put("resolution", 40);
							score += 40;
							break;
						}
					}
				}
			}
		}
		
		// Display pixel ratio - Worth 20 points
		result.put("display_pixel_ratio", 0);
		if (!HDUtil.isNullOrEmpty(props.get("display_pixel_ratio"))) 
		{	
			// Note : display_pixel_ratio will be a string stored as 1.33 or 1.5 or 2, perhaps 2.0 ..
			Float spr = specs.get("display_pixel_ratio").getAsFloat();
			Float ppr = Float.parseFloat(props.get("display_pixel_ratio"))/100.f;
			if (spr.equals(ppr)) 
			{
				result.put("display_pixel_ratio", 20);
				score += 20;
			}
		}
		
		// Benchmark - 10 points - Enough to tie break but not enough to overrule display or pixel ratio.
		result.put("benchmark", 0);
		if (!HDUtil.isNullOrEmpty(props.get("benchmark"))) 
		{
			if (!HDUtil.isNullOrEmpty(specs.get("benchmark_min").getAsString()) && !HDUtil.isNullOrEmpty(specs.get("benchmark_max").getAsString())) 
			{
				if (Integer.parseInt(props.get("benchmark")) >= specs.get("benchmark_min").getAsInt() 
						&& Integer.parseInt(props.get("benchmark")) <= specs.get("benchmark_max").getAsInt())
				{
					// Inside range - Yay.
					result.put("benchmark", 10);
					score += 10;
				}
			}
		}
		
		result.put("score", score);
		result.put("possible", total);
		
		// Distance from mean used in tie breaking situations if two devices have the same score.
		result.put("distance", 100000);
		
		if (!HDUtil.isNullOrEmpty(specs.get("benchmark_min").getAsString()) && !HDUtil.isNullOrEmpty(specs.get("benchmark_max").getAsString()) 
				&& !HDUtil.isNullOrEmpty(props.get("benchmark"))) 
		{
			Integer distance = Math.abs((specs.get("benchmark_min").getAsInt() + specs.get("benchmark_max").getAsInt())/2 - Integer.parseInt(props.get("benchmark")));
			result.put("distance", distance);
		}
		return result;
	}

	/**
	 * Overlays specs onto a device
	 *
	 * @param string specsField : Either 'platform', 'browser', 'language'
	 **/
	private void specsOverlay(String specsField, JsonElement device, JsonElement specs) 
	{
//		DocumentContext dcDevice = JsonPath.parse(gson.toJson(device));
		DocumentContext dcSpecs = JsonPath.parse(gson.toJson(specs));
		
		switch (specsField) 
		{
			case "platform" : 
				if (!HDUtil.isNullOrEmpty((String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM)))
				{
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM, 
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_PLATFORM, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM));
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM_VERSION,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_PLATFORM_VERSION, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM_VERSION));
				}
			break;
			case "browser" :
				if (!HDUtil.isNullOrEmpty((String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_BROWSER))) 
				{
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_BROWSER, 
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_BROWSER,  
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_BROWSER));
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_BROWSER_VERSION,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_BROWSER_VERSION, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_BROWSER_VERSION));
				}
			break;
			case "app" :
				if (!HDUtil.isNullOrEmpty((String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP))) 
				{
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_APP, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP));
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP_VERSION,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_APP_VERSION, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP_VERSION));
//					dcDevice.set("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP_CATEGORY,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_APP_CATEGORY, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_APP_CATEGORY));
				}
			break;
			case "language" :
				if (!HDUtil.isNullOrEmpty((String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "[0]." + JsonConstants.GENERAL_LANGUAGE))) 
				{
//					dcDevice.set("$." +  JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_LANGUAGE,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_LANGUAGE, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "[0]." + JsonConstants.GENERAL_LANGUAGE));
//					dcDevice.put("$." + JsonConstants.DEVICE + "." + JsonConstants.HD_SPECS, JsonConstants.GENERAL_LANGUAGE_FULL,
					device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get(JsonConstants.HD_SPECS).getAsJsonObject().addProperty(JsonConstants.GENERAL_LANGUAGE_FULL, 
							(String) dcSpecs.read("$." + JsonConstants.HD_SPECS + "[1]." + JsonConstants.GENERAL_LANGUAGE_FULL));
				}
			break;
		}
	}
	
	/**
	 * Takes a string of onDeviceInformation and turns it into something that can be used for high accuracy checking.
	 *
	 * Strings a usually generated from cookies, but may also be supplied in headers.
	 * The format is w;h;r;b where w is the display width, h is the display height, r is the pixel ratio and b is the benchmark.
	 * display_x, display_y, display_pixel_ratio, general_benchmark
	 *
	 * @param string hardwareInfo String of light weight device property information, separated by ":"
	 * @return array partial specs array of information we can use to improve detection accuracy
	 **/
	private Map<String, String> infoStringToArray(String hardwareInfo) 
	{
		Map<String, String> reply = new HashMap<String, String>();
		
		// Remove the header or cookie name from the string "x-specs1a="
		int idx = hardwareInfo.indexOf('=');
		if (idx > 0) 
		{
			hardwareInfo = hardwareInfo.substring(idx);
			
			if (HDUtil.isNullOrEmpty(hardwareInfo))
				return reply;
		}
		
		String info[] = hardwareInfo.split(":");
		
		if (info.length != 4) 
			return reply;
		
		reply.put("display_x", info[0].trim());
		reply.put("display_y", info[1].trim());
		reply.put("display_pixel_ratio", info[2].trim());			//catalin: non -integer, better return a map of just strings
		reply.put("benchmark", info[3].trim());
		return reply;
	}
	
	/**
	 * Overlays hardware info onto a device - Used in generic replys
	 *
	 * @param array device
	 * @param hardwareInfo
	 * @return void
	 **/
	private void hardwareInfoOverlay(JsonElement device, Map<String, String> hwProps) 
	{
		if (null == hwProps)
			return;
		
//		DocumentContext dcDevice = JsonPath.parse(gson.toJson(device));
		
		if (!HDUtil.isNullOrEmpty(String.valueOf(hwProps.get("display_x"))))
//			dcDevice.set("$." + JsonConstants.DEVICE + "." + "hd_specs" + "." + "display_x", hwProps.get("display_x"));
			device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get("hd_specs").getAsJsonObject().addProperty("display_x", hwProps.get("display_x"));
		if (!HDUtil.isNullOrEmpty(String.valueOf(hwProps.get("display_y"))))
//			dcDevice.set("$." + JsonConstants.DEVICE + "." + "hd_specs" + "." + "display_y", hwProps.get("display_y"));
			device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get("hd_specs").getAsJsonObject().addProperty("display_y", hwProps.get("display_y"));
		if (!HDUtil.isNullOrEmpty(String.valueOf(hwProps.get("display_pixel_ratio"))))
//			dcDevice.set("$." + JsonConstants.DEVICE + "." + "hd_specs" + "." + "display_pixel_ratio", hwProps.get("display_pixel_ratio"));
			device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get("hd_specs").getAsJsonObject().addProperty("display_pixel_ratio", hwProps.get("display_pixel_ratio"));
	}
	
	/**
	 * Device matching
	 *
	 * Plan of attack :
	 *  1) Look for opera headers first - as they"re definitive
	 *  2) Try profile match - only devices which have unique profiles will match.
	 *  3) Try user-agent match
	 *  4) Try other x-headers
	 *  5) Try all remaining headers
	 *
	 * @param void
	 * @return array The matched device or null if not found
	 **/
	private JsonElement matchDevice(Map<String, String> headers) 
	{
		JsonElement _id = null;
		String agent = "";											// Remember the agent for generic matching later.
		
		// Opera mini sometimes puts the vendor # model in the header - nice! ... sometimes it puts ? # ? in as well
		if (!HDUtil.isNullOrEmpty(headers.get("x-operamini-phone")) && !"? # ?".equals(headers.get("x-operamini-phone").trim())) 
		{
			_id = getMatch("x-operamini-phone", headers.get("x-operamini-phone"), DETECTIONV4_STANDARD, "x-operamini-phone", JsonConstants.DEVICE);
			if (null != _id) 
				return findById(_id.getAsString());
			
			agent = headers.get("x-operamini-phone");
			headers.remove("x-operamini-phone");
		}
		
		// Profile header matching
		if (!HDUtil.isNullOrEmpty(headers.get(JsonConstants.PROFILE))) 
		{
			_id = getMatch(JsonConstants.PROFILE, headers.get(JsonConstants.PROFILE), DETECTIONV4_STANDARD, JsonConstants.PROFILE, JsonConstants.DEVICE);
			if (null != _id) 
				return findById(_id.getAsString());
			
			headers.remove(JsonConstants.PROFILE);
		}
		
		// Profile header matching
		if (!HDUtil.isNullOrEmpty(headers.get("x-wap-profile"))) {
			_id = getMatch(JsonConstants.PROFILE, headers.get("x-wap-profile"), DETECTIONV4_STANDARD, "x-wap-profile", JsonConstants.DEVICE);
			if (null != _id) 
				return findById(_id.getAsString());
			
			headers.remove("x-wap-profile");
		}
		
		// Match nominated headers ahead of x- headers
		List<String> order = new ArrayList<String>(detectionConfigMap.get("device-ua-order"));
		for (Entry<String, String> keyVal : headers.entrySet())
		{
			if (!order.contains(keyVal.getKey()) && xPattern.matcher(keyVal.getKey()).find())
				order.add(keyVal.getKey());
		}
		
		for (String item : order) 
		{
			if (!HDUtil.isNullOrEmpty(headers.get(item)))
			{
				//this.log("Trying user-agent match on header item");
				_id = getMatch("user-agent", headers.get(item), DETECTIONV4_STANDARD, item, JsonConstants.DEVICE);
				if (_id != null)
					return findById(_id.getAsString());
			}
		}
		
		// Generic matching - Match of last resort
		//this.log("Trying Generic Match");
		if (headers.containsKey("x-operamini-phone-ua")) 
			_id = getMatch("user-agent", headers.get("x-operamini-phone-ua"), DETECTIONV4_GENERIC, "agent", JsonConstants.DEVICE);
		
		if (HDUtil.isNullElement(_id) && headers.containsKey("agent")) 
			_id = getMatch("user-agent", headers.get("agent"), DETECTIONV4_GENERIC, "agent", JsonConstants.DEVICE);
		
		if (HDUtil.isNullElement(_id) && headers.containsKey("user-agent")) 
			_id = getMatch("user-agent", headers.get("user-agent"), DETECTIONV4_GENERIC, "agent", JsonConstants.DEVICE);
		
		if (_id != null)
			return findById(_id.getAsString());
		
		return null;
	}
	
	/**
	 * BuildInfo Matching
	 *
	 * Takes a set of buildInfo key/value pairs & works out what the device is
	 *
	 * @param array buildInfo - Buildinfo key/value array
	 * @return mixed device array on success, false otherwise
	 */
	private boolean v4MatchBuildInfo(Map<String, String> buildInfo) 
	{
		JsonElement device;
		JsonElement platform;
				
		// Nothing to check		
		if (null == buildInfo || buildInfo.isEmpty())
			return false;
		
		this.buildInfo = buildInfo;
		
		// Device Detection
		device = this.v4MatchBIHelper(buildInfo, JsonConstants.DEVICE);
		if (HDUtil.isNullElement(device))
			return false;
		
		// Platform Detection
		platform = this.v4MatchBIHelper(buildInfo, "platform");
		if (!HDUtil.isNullElement(platform))
			specsOverlay("platform", device, platform.getAsJsonObject().get("Extra"));
		
		this.reply.add("hd_specs", device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get("hd_specs"));
		return setError(0, "OK");
	}
	
	/**
	 * buildInfo Match helper - Does the build info match heavy lifting
	 *
	 * @param array buildInfo A buildInfo key/value array
	 * @param string category - JsonConstants.DEVICE or "platform" (cant match browser or app with buildinfo)
	 * @return device or extra on success, false otherwise
	 **/
	private JsonElement v4MatchBIHelper(Map<String, String> buildInfo, String category) 
	{
		JsonElement platform;
		
		if (HDUtil.isNullOrEmpty(category))
			category = JsonConstants.DEVICE;
		category = category.toLowerCase();
		
		// ***** Device Detection *****
		List<Map>confBIKeys = detectionConfigMap.get(category + "-bi-order");
		if (confBIKeys.isEmpty() || buildInfo.isEmpty())
			return null;
		
		List<String> hints = new ArrayList<String>();
		for (Map<String, List> platformToSet : confBIKeys)
		{
			String value = "";
			for (List<List<String>> tuple : platformToSet.values()) 
			{
				boolean checking = true;
				for (String item : tuple.get(0)) 
				{
					if (!buildInfo.containsKey(item)) 
					{
						checking = false;
						break;
						
					} else {
						
						value += "|" + buildInfo.get(item);
					}
				}
				
				if (checking) 
				{
					value.trim();
					int i1 = 0 == value.indexOf('|') ? 1 : 0;
					int i2 = value.length() - (value.length() - 1  == value.lastIndexOf('|') ? 1 : 0);  
					try
					{
						value = value.substring(i1, i2);
					
					} catch (IndexOutOfBoundsException ex) {
						
						value = "";
					}
					
					hints.add(value);
					String subtree = (category.equalsIgnoreCase(JsonConstants.DEVICE)) ? DETECTIONV4_STANDARD : category;
					JsonElement _id = getMatch("buildinfo", value, subtree, "buildinfo", category);
					
					if (!HDUtil.isNullElement(_id)) 
						return (category.equalsIgnoreCase(JsonConstants.DEVICE)) ? findById(_id.getAsString()) : this.extra.findById(_id.getAsInt());
					
				}
			}
		}
		
		// If we get this far then not found, so try generic.
		platform = gson.toJsonTree(hasBiKeys(buildInfo));
		if (!HDUtil.isNullElement(platform))
		{
			List<String> shots = new ArrayList<String>();
			shots.add("generic|" + platform.getAsString());
			shots.add(platform.getAsString() + "|generic");
			for (String value : shots) 
			{
				String subtree = (category.equalsIgnoreCase(JsonConstants.DEVICE)) ? DETECTIONV4_GENERIC : category;
				JsonElement _id = getMatch("buildinfo", value, subtree, "buildinfo", category);
				if (!HDUtil.isNullElement(_id))
					return (category.equalsIgnoreCase(JsonConstants.DEVICE)) ? findById(_id.getAsString()) : this.extra.findById(_id.getAsInt());
			}
		}		
		return null;
	}
	
	/**
	 * Find the best device match for a given set of headers and optional device properties.
	 *
	 * In "all" mode all conflicted devces will be returned as a list.
	 * In "default" mode if there is a conflict then the detected device is returned only (backwards compatible with v3).
	 * 
	 * @param array headers Set of sanitized http headers
	 * @param string hardwareInfo Information about the hardware
	 * @return array device specs. (device.hd_specs)
	 **/
	private boolean v4MatchHttpHeaders(SortedMap<String, String> headers, String hardwareInfo) 
	{
		JsonElement device = null;
		JsonElement platform = null;
		JsonElement browser = null;
		JsonElement app = null;
		JsonElement language;
		Map<String, String> deviceHeaders = new HashMap<String, String>();
		Map<String, String> extraHeaders = new HashMap<String, String>();
		Map<String, String> hwProps = null;
		this.detectedRuleKey = new HashMap<String, String>();
		this.reply = new JsonObject();
		this.device = null;
		
		// Nothing to check		
		if (headers.isEmpty())
			return false;
		
		headers.remove("ip");
		headers.remove("host");
		
		// Sanitize headers & cleanup language
		for (Entry<String, String> keyVal : headers.entrySet()) 
		{
			String key = keyVal.getKey();
			String val = keyVal.getValue().toLowerCase();
			
			if ("accept-language".equals(key) || "content-language".equals(key))
			{
				key = "language";
				val = httpHeadersReplacePattern.matcher(val).replaceAll("");
				String tmp[] = val.split(httpHeadersSplitPattern.pattern());
				if (!HDUtil.isNullOrEmpty(tmp[0]))
				{
					val = tmp[0];
				}
				else
					continue;
			}
			
			deviceHeaders.put(key.toLowerCase(), cleanStr(val));
			extraHeaders.put(key.toLowerCase(), this.extra.extraCleanStr(val));
		}

		// Match device
		device = this.matchDevice(deviceHeaders);
		
		if (HDUtil.isNullElement(device))
			return this.setError(301, "Not Found");
		
		if (!HDUtil.isNullOrEmpty(hardwareInfo))
			hwProps = this.infoStringToArray(hardwareInfo);
		
		// Stop on detect set - Tidy up and return
		DocumentContext dcDevice = JsonPath.parse(gson.toJson(device));
		boolean aux = false;
		Object stopOnDetect = dcDevice.read("$." + JsonConstants.DEVICE + "." + "hd_ops" + "." + "stop_on_detect");
		if (stopOnDetect instanceof String)
			stopOnDetect = Integer.parseInt((String)stopOnDetect);
		aux = (Integer)stopOnDetect != 0;

		if (aux)
		{
			// Check for hardwareInfo overlay
			Object overlaySpecs = dcDevice.read("$." + JsonConstants.DEVICE + "." + "hd_ops" + "." + "overlay_result_specs");
			aux = false;
			if (overlaySpecs instanceof String) 
				overlaySpecs = Integer.parseInt((String)overlaySpecs);
			
			aux = (Integer)overlaySpecs != 0;
			if (aux)
				hardwareInfoOverlay(device, hwProps);
			
			reply.add("hd_specs", device.getAsJsonObject().get(JsonConstants.DEVICE).getAsJsonObject().get("hd_specs"));
			return this.setError(0, "OK");
		}
		
		// Get extra info
		platform = this.extra.matchExtra("platform", extraHeaders);
		browser = this.extra.matchExtra("browser", extraHeaders);
		app = this.extra.matchExtra("app", extraHeaders);
		language = this.extra.matchLanguage(extraHeaders);

		// Find out if there is any contention on the detected rule.
		List<String> deviceList = getHighAccuracyCandidates();
		if (null != deviceList && !deviceList.isEmpty()) 
		{
			// Resolve contention with OS check
			this.extra.set(platform);
			List<String> pass1List = new ArrayList<String>();
			for (String _id : deviceList) 
			{
				JsonElement tryDevice = this.findById(_id);
				if (this.extra.verifyPlatform(gson.toJsonTree((JsonPath.read(gson.toJson(tryDevice), "$." + JsonConstants.DEVICE + "." + "hd_specs"))))) 
					pass1List.add(_id);
				
			}
			
			// Contention still not resolved .. check hardware
			if (pass1List.size() >= 2 && null != hwProps && !hwProps.isEmpty())
			{
				// Score the list based on hardware
				Set<RatingEntry> result = new TreeSet<RatingEntry>();
				for (String _id : pass1List)
				{
					Map<String, Integer> tmp = findRating(_id, hwProps);
					if (null != tmp && !tmp.isEmpty())
					{
						tmp.put("_id", Integer.parseInt(_id));
						result.add(new RatingEntry(tmp));
					}
				}
				
				// Use the first result
				if (result.iterator().hasNext())
				{
					int scoreForDevice = result.iterator().next().entry.get("score");
					if (0 != scoreForDevice) 
					{
						int idForDevice = result.iterator().next().entry.get("_id");
						JsonElement tmpDevice = findById(String.valueOf(idForDevice));
						if (!HDUtil.isNullElement(device)) 
							device = tmpDevice;
					}
				}
			}
		}		
		
		// Overlay specs
		if (null != platform)
			specsOverlay("platform", device, platform.getAsJsonObject().get(JsonConstants.EXTRA));
		if (null != browser)
			specsOverlay("browser", device, browser.getAsJsonObject().get(JsonConstants.EXTRA));
		if (null != app)
			specsOverlay("app", device, app.getAsJsonObject().get(JsonConstants.EXTRA));
		if (null != language)
			specsOverlay("language", device, language.getAsJsonObject().get(JsonConstants.EXTRA));
		
		// Overlay hardware info result if required
		if (!HDUtil.isNullElement(gson.toJsonTree((JsonPath.read(gson.toJson(device), "$.Device.hd_ops.overlay_result_specs"))))
			&& !HDUtil.isNullOrEmpty(hardwareInfo)) {
			hardwareInfoOverlay(device, hwProps);
		}
		
		this.reply.add("hd_specs", gson.toJsonTree((JsonPath.read(gson.toJson(device), "$.Device.hd_specs"))));
		return setError(0, "OK");
	}
	
	/**
	 * Determines if high accuracy checks are available on the device which was just detected
	 *
	 * @param void
	 * @returns array, a list of candidate devices which have this detection rule or false otherwise.
	 */
	private List<String> getHighAccuracyCandidates() 
	{
		JsonElement branch = getBranch("hachecks");
		String ruleKey = detectedRuleKey.get(JsonConstants.DEVICE.toLowerCase());
		if (!HDUtil.isNullElement((branch.getAsJsonObject().get(ruleKey)))) 
		{
			JsonElement elem = branch.getAsJsonObject().get(ruleKey);
			List<String> ret = gson.fromJson(elem, new TypeToken<List<String>>() {}.getType());
			return ret;
		}
		
		return null;
	}
	
	/**
	 * Determines if hd4Helper would provide more accurate results.
	 *
	 * @param array headers HTTP Headers
	 * @return boolean true if useful, false otherwise
	 **/
	public boolean isHelperUseful(Map<String, String>headers) 
	{
		if (headers.isEmpty())
			return false;
		
		if (!localDetect(headers))
			return false;
		
		List<String> lstCandidates = getHighAccuracyCandidates();
		if (null == lstCandidates || lstCandidates.isEmpty())
			return false;
		
		return true;
	}
	
	/**
	 * Custom sort class for sorting rating results.
	 *
	 * Includes a tie-breaker for results which score out the same
	 *
	 * @param array result1
	 * @param array result2
	 * @return negative (other < this), 0 (other == this) , positive (other > this)
	 **/
	static class RatingEntry implements Comparable<RatingEntry>
	{
		final Map<String, Integer> entry;
		
		RatingEntry(Map<String, Integer> entry)
		{
			this.entry = entry;
		}

		@Override
		public int compareTo(RatingEntry other) 
		{
			int res = other.entry.get("score") - entry.get("score");
			if (0 != res)
				return res;
			
			return entry.get("distance") - other.entry.get("distance");
		}
	}

}