package api.hd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;

public class HDExtra extends HDBase 
{
	private JsonElement data = null;
	private final Map<String, Pattern> langPatternMap = new HashMap<String, Pattern>(); 
	
	public HDExtra() throws IOException
	{
		super();
		this.store = HDStore.getInstance();
		for (String code : detectionLanguagesMap.keySet()) 
			langPatternMap.put(code, Pattern.compile("[; (]" + code + "[; )]", Pattern.CASE_INSENSITIVE));		
	}
	
	public HDExtra(String cfgFilename) throws IOException
	{
		super(cfgFilename);
		this.store = HDStore.getInstance();
		for (String code : detectionLanguagesMap.keySet()) 
			langPatternMap.put(code, Pattern.compile("[; (]" + code + "[; )]", Pattern.CASE_INSENSITIVE));		
	}
	
	public HDExtra(Config cfg) throws IOException
	{
		super(cfg);
		this.store = HDStore.getInstance();
		for (String code : detectionLanguagesMap.keySet()) 
			langPatternMap.put(code, Pattern.compile("[; (]" + code + "[; )]", Pattern.CASE_INSENSITIVE));		
	}
	
	public HDExtra(byte[] isCfg) throws IOException
	{
		super(isCfg);
		this.store = HDStore.getInstance();
		for (String code : detectionLanguagesMap.keySet()) 
			langPatternMap.put(code, Pattern.compile("[; (]" + code + "[; )]", Pattern.CASE_INSENSITIVE));		
	}
	
	public void set(JsonElement data) 
	{ 
		this.data = data;
	}
	
	/**
	 * Matches all HTTP header extras - platform, browser and app
	 *
	 * @param string $class Is "platform","browser" or "app"
	 * @return an Extra on success, false otherwise
	 **/
	public JsonElement matchExtra(String classKey, Map<String, String> headers) 
	{
		headers.remove(JsonConstants.PROFILE);
		List<String> orderLst = new ArrayList<String>(detectionConfigMap.get(classKey + "-ua-order"));
		Set<String> headerKeysSet = headers.keySet();

		for (String key : headerKeysSet) 
		{
			// Append any x- headers to the list of headers to check
			if (!orderLst.contains(key) && xPattern.matcher(key).find())
				orderLst.add(key);
		}
		
		for (String field : orderLst)
		{
			if (headers.containsKey(field))
			{
				JsonElement id = getMatch("user-agent", headers.get(field), classKey, field, classKey);
				if (null != id) 
					return findById(id.getAsInt());
			}
		}
		return null;
	}

	/**
	 * Can learn language from language header or agent
	 *
	 * @param array $headers A key => value array of sanitized http headers
	 * @return array Extra on success, false otherwise
	 **/
	public JsonElement matchLanguage(Map<String, String> headers) 
	{
		JsonObject extra = new JsonObject();
		JsonObject hdSpecsObj = new JsonObject();
		JsonArray hdSpecsArr = new JsonArray();
		JsonObject languageObj = new JsonObject();
		JsonObject languageFullObj = new JsonObject();
		
		String lang = "";
		String det = "";
		
		// Try directly from http header first
		if (headers.containsKey("language"))
		{
			lang = headers.get("language");
			det = detectionLanguagesMap.get(lang);
			if (null == det)
			{
				det = "";
				lang = "";
			}
		}
			
		languageObj.addProperty(JsonConstants.GENERAL_LANGUAGE, lang);
		hdSpecsArr.add(languageObj);
		languageFullObj.addProperty(JsonConstants.GENERAL_LANGUAGE_FULL, det);
		hdSpecsArr.add(languageFullObj);
		hdSpecsObj.add(JsonConstants.HD_SPECS, hdSpecsArr);
		// Mock up a fake Extra for merge into detection reply.
		extra.add(JsonConstants.ID, null);
		extra.add(JsonConstants.EXTRA, hdSpecsObj);
		
		if (!"".equals(det))
			return extra;
		
		List<String> checkOrderLst = new ArrayList<String>(detectionConfigMap.get("language-ua-order"));
		checkOrderLst.addAll(headers.keySet());									//catalin: checked, both are shallow copy constructs
//		$languageList = $this->detectionLanguages;
		
		for (String header : checkOrderLst)
		{
			String agent = headers.get(header);
			if (!HDUtil.isNullOrEmpty(agent))
			{
				for (Entry<String, String> shortToFull : detectionLanguagesMap.entrySet()) 
				{
					if (langPatternMap.get(shortToFull.getKey()).matcher(agent).find()) 
					{
						languageObj.addProperty(JsonConstants.GENERAL_LANGUAGE, shortToFull.getKey());
						languageFullObj.addProperty(JsonConstants.GENERAL_LANGUAGE_FULL, shortToFull.getValue());
						return extra;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns false if this device definitively cannot run this platform and platform version.
	 * Returns true if its possible of if there is any doubt.
	 *
	 * Note : The detected platform must match the device platform. This is the stock OS as shipped
	 * on the device. If someone is running a variant (eg CyanogenMod) then all bets are off.
	 *
	 * @param string $specs The specs we want to check.
	 * @return boolean false if these specs can not run the detected OS, true otherwise.
	 **/
	public boolean verifyPlatform(JsonElement specs) 
	{
		String platform = this.data.toString();
		JsonObject specsObj = null != specs ? specs.getAsJsonObject() : new JsonObject();
		
		String platformName = JsonPath.read(platform, "$." + JsonConstants.EXTRA + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM)
				.toString().trim().toLowerCase();
		String platformVersion = JsonPath.read(platform, "$." + JsonConstants.EXTRA + "." + JsonConstants.HD_SPECS + "." + JsonConstants.GENERAL_PLATFORM_VERSION)
				.toString().trim().toLowerCase();
		String devicePlatformName = specsObj.get(JsonConstants.GENERAL_PLATFORM).getAsString().trim().toLowerCase();
		String devicePlatformVersionMin = specsObj.get(JsonConstants.GENERAL_PLATFORM_VERSION).getAsString().trim().toLowerCase();
		String devicePlatformVersionMax = specsObj.get(JsonConstants.GENERAL_PLATFORM_VERSION_MAX).getAsString().trim().toLowerCase();
			
		// Its possible that we didnt pickup the platform correctly or the device has no platform info
		// Return true in this case because we cant give a concrete false (it might run this version).
		if (HDUtil.isNullOrEmpty(platform) || HDUtil.isNullOrEmpty(platformName) || HDUtil.isNullOrEmpty(devicePlatformName))
			return true;
			
		// Make sure device is running stock OS / Platform
		// Return true in this case because its possible the device can run a different OS (mods / hacks etc..)
		if (!platformName.equals(devicePlatformName))
			return true;
		
		// Detected version is lower than the min version - so definetly false.
		if (!HDUtil.isNullOrEmpty(platformVersion) && !HDUtil.isNullOrEmpty(devicePlatformVersionMin) 
				&& comparePlatformVersions(platformVersion, devicePlatformVersionMin) <= -1)
			return false;
		
		// Detected version is greater than the max version - so definetly false.
		if (!HDUtil.isNullOrEmpty(platformVersion) && !HDUtil.isNullOrEmpty(devicePlatformVersionMax) 
				&& comparePlatformVersions(platformVersion, devicePlatformVersionMax) >= 1)
			return false;
		
		// Maybe Ok ..
		return true;
	}
	
	/**
	 * Helper for determining if a string is a number
	 * 
	 * @param string input
	 * @return true on success, false otherwise
	 **/
	private boolean isInteger(String input) {
		try {
			Integer.parseInt( input );
		    return true;
		} catch(Exception ex) {
			return false;
		}
	}
	
	/**
	 * Breaks a version number apart into its Major, minor and point release numbers for comparison.
	 *
	 * Big Assumption : That version numbers separate their release bits by "." !!!
	 * might need to do some analysis on the string to rip it up right.
	 *
	 * @param string $versionNumber
	 * @return array of ("major" => x, "minor" => y and "point" => z) on success, null otherwise
	 **/
	public String[] breakVersionApart(String versionNumber) {
		String tmp[] = (versionNumber + ".0.0.0").split("\\.", 4);
		return tmp;
	}
	
	/**
	 * Helper for comparing two strings (numerically if possible)
	 *
	 * @param string $a Generally a number, but might be a string
	 * @param string $b Generally a number, but might be a string
	 * @return int
	 **/
	public int compareSmartly(String a, String b) {
		if (isInteger(a) && isInteger(b)) 
			return Integer.parseInt(a) - Integer.parseInt(b);
		return a.compareTo(b);
	}
	
	/**
	 * Compares two platform version numbers
	 *
	 * @param string $va Version A
	 * @param string $vb Version B
	 * @return < 0 if a < b, 0 if a == b and > 0 if a > b : Also returns 0 if data is absent from either.
	 */
	public int comparePlatformVersions(String va, String vb) {
		if (HDUtil.isNullOrEmpty(va) || HDUtil.isNullOrEmpty(vb))
			return 0;
		
		String[] versionA = breakVersionApart(va);
		String[] versionB = breakVersionApart(vb);
		Integer major = compareSmartly(versionA[0], versionB[0]);
		Integer minor = compareSmartly(versionA[1], versionB[1]);
		Integer point = compareSmartly(versionA[2], versionB[2]);
		
		if (0 != major) 
			return major;
		if (0 != minor) 
			return minor;
		if (0 != point) 
			return point;
		
		return 0;
	}

	/**
	 * Find a device by its id
	 *
	 * @param string $_id
	 * @return array device on success, false otherwise
	 **/
	protected JsonElement findById(Integer deviceId) {
		return this.store.read("Extra_" + String.valueOf(deviceId));
	}
}
