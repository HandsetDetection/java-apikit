package api.hd;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;

import api.hd.HDStore.HDCache;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/* 
* ****************************************************
* * The HD3 class
* * Release 3.00 * 
* * HandsetDetection
* ****************************************************
*/
public class HD extends HDBase {
		
	
	/** The use local. */
	private boolean useLocal;
	
	/** The site id. */
	private String siteId;
	
	/** The mobile site. */
	private String mobileSite;
	
	/** The match filter. */
	private String matchFilter;
	
	/** The m_detect request. */
	JsonObject detectRequest;
	
	/** The m_specs. */
	private JsonObject specs;
	
	/** The m_cache. */
	private HDCache cache;

	/** The device. */
	private final HDDevice device;

	private final Pattern patternNonMobile;
	private final Pattern patternFastKey = Pattern.compile(" ");
	
	private void init() throws IOException
	{	
		if (HDUtil.isNullOrEmpty(username))
			throw new InvalidParameterException("Error : API username not set. Download a premade config from your Site Settings.");
		if (HDUtil.isNullOrEmpty(secret)) 
			throw new InvalidParameterException("Error : API secret not set. Download a premade config from your Site Settings.");
		
		store = HDStore.getInstance();
		cache = store.getCache();
		cache.purge();							// a cople of tests will fail if not purging teh static cache
		setup();
	}
	
	public void initVariables() {
		this.useLocal = Config.isUseLocal();
		this.siteId = Config.getSiteId();
		this.mobileSite = Config.getMobileSite();	
		this.matchFilter = Config.getMatchFilter();
	}
	public HD() throws IOException 
	{
		super();
		initVariables();
		device = new HDDevice();
		this.patternNonMobile = Pattern.compile(Config.getNonMobile());
		init();
	}
	
	public HD(String cfgFileName) throws IOException 
	{
		super (cfgFileName);
		initVariables();
		device = new HDDevice(cfgFileName);
		this.patternNonMobile = Pattern.compile(Config.getNonMobile());
		init();
	}
		
	public HD(Config config) throws IOException 
	{
		super (config);
		initVariables();
		device = new HDDevice(config);
		this.patternNonMobile = Pattern.compile(Config.getNonMobile());
		init();
	}
	
	public HD(byte[] isConfig) throws IOException 
	{
		super (isConfig);
		initVariables();
		device = new HDDevice(isConfig);
		this.patternNonMobile = Pattern.compile(Config.getNonMobile());
		init();
	}

	/**
	 * Setup.
	 */
	public void setup() {
		setup(null, null, null);
	}
	
	/**
	 * Setup.
	 *
	 * @param headers the headers
	 * @param serverIpAddress the server ip address
	 * @param requestURI the request uri
	 */
	public void setup(Map<String, String> headers, String serverIpAddress, String requestURI) {
		try {
			resetDetectRequest();
			if (headers != null) {
				Set<String> keys = headers.keySet();
				for (String key : keys) {
					if (!"Cookie".equalsIgnoreCase(key)) {
						this.detectRequest.addProperty(key, headers.get(key));
					}
				}
			}
			detectRequest.addProperty("ipaddress", serverIpAddress);
			detectRequest.addProperty("request_uri", requestURI);
		} catch (Exception ex) {
			g_logger.severe(ex.getMessage());
			setError(299, "Failed to setup detect request. Cause: " + ex.getMessage());
		}		
	}
	
	/**
	 * Device vendors.
	 *
	 * @return true, if successful
	 */
	public boolean deviceVendors() 
	{
		initRequest();
		if (!isUseLocal())
			return remote("device/vendors", null, null, null);
		
		device.localVendors();
		return setError(device.getStatus(), device.getMessage());
	}
	
	/**
	 * Device models.
	 *
	 * @param vendor the vendor
	 * @return true, if successful
	 */
	public boolean deviceModels(String vendor) {
		initRequest();
		if (!isUseLocal())
			return remote("device/models/" + vendor, null, null, null);
		
		device.localModels(vendor);
		return setError(device.getStatus(), device.getMessage());
	}
	
	/**
	 * Device view.
	 *
	 * @param vendor the vendor
	 * @param model the model
	 * @return true, if successful
	 */
	public boolean deviceView(String vendor, String model) {
		initRequest();
		if (!isUseLocal())
		{
			StringBuilder sb = new StringBuilder();
			String service = sb.append("device/view/").append(vendor).append("/").append(model).toString();
			return remote(service, null, null, null);
		}
		
		device.localView(vendor, model);
		return setError(device.getStatus(), device.getMessage());
	}
	
	/**
	 * Device what has.
	 *
	 * @param key the key
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean deviceWhatHas(String key, String value) {
		initRequest();
		if (!isUseLocal())
		{
			StringBuilder sb = new StringBuilder();
			String service = sb.append("device/whathas/").append(key).append("/").append(value).toString();
			return remote(service, null, null, null);
		}
		
		device.localWhatHas(key, value);
		return setError(device.getStatus(), device.getMessage());
	}
	
	/**
	 * device fetch archive.
	 *
	 * @return true, if successful
	 */
	
	public boolean deviceFetchArchive()
	{
		return fetchArchive("device/fetcharchive/", true);
	}
	
	/**
	 * community fetch archive.
	 *
	 * @return true, if successful
	 */
	
	public boolean communityFetchArchive()
	{
		return fetchArchive("community/fetcharchive/", false);
	}
	
	/**
	 * fetch archive.
	 * @param urlPath 
	 * @param b 
	 *
	 * @return true, if successful
	 */
	private synchronized boolean fetchArchive(String urlPath, Boolean auth) 
	{
		initRequest();
		String zipFile;		
		this.getFilesDirectory();	// If directory is not created then create one
		zipFile = this.localFilesDirectory + File.separator + "ultimate.zip";		
		try {
			// Increase the timeout, because the default of 5 seconds just isnt enough.
			// Note : Errors will be JSON documents whereas the Archive will be a ZIP file.
			int saveConnectTimeout = getConnectTimeout();
			int saveReadTimeout = getReadTimeout();
			setConnectTimeout(Math.max(saveConnectTimeout, 450));
			setReadTimeout(Math.max(saveReadTimeout, 450));

			if (remote(urlPath, new JsonObject(), "zip", auth))
			{
				setConnectTimeout(saveConnectTimeout);
				setReadTimeout(saveReadTimeout);
				if (0 == rawReply.length) {
					this.createErrorReply(299, "Failed to download archive properly. File is zero size.");
					return false;				
				} else if (rawReply.length < 900000) {
//					try {
						ByteArrayInputStream in = new ByteArrayInputStream(rawReply);
						JsonObject response = (JsonObject) HDUtil.parseJson(in);					
						if (response.isJsonObject()) {
							JsonElement status = response.get(JsonConstants.STATUS);
							JsonElement message = response.get(JsonConstants.MESSAGE);
							if (!HDUtil.isNullElement(message) && !HDUtil.isNullElement(status))
								return this.createErrorReply(status.getAsInt(), message.getAsString());
							
							return this.createErrorReply(299, "Error : FetchArchive failed. Bad Download. File too short at " + rawReply.length + " bytes.");
						}
//					} catch (Exception e) {
//						if (reply.size() < 800000) {
//							this.createErrorReply(299, "Failed to download archive properly. File is too small.");
//							return false;										
//						}
//						// If there"s an Exception then its probably all good :-)
//					}
				}
				
				// Write the zipfile out.
				FileOutputStream stream = new FileOutputStream(zipFile, false);
				try {
				    stream.write(rawReply);
					stream.flush();
				} finally {
				    stream.close();
				}
			} else {
				setConnectTimeout(saveConnectTimeout);
				setReadTimeout(saveReadTimeout);
				this.createErrorReply(200, "API : AuthMethod digest. Error : Unknown User");
				return false;
			}
		} catch (Exception e) {
			this.createErrorReply(299, "Failed to download archive.", e.getMessage());
			return false;
		}	
		
		return installArchive(zipFile);
	}
	
	private boolean installArchive(String zipFile)
	{
		// Unzip ultimate.zip file.
		// Based on : http://www.justexample.com/wp/unzip-zip-file-using-java/
		File tempDir = null;
		
		try 
		{
			tempDir = Files.createTempDir();
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
	        while (ze != null) {
	        	String entryName = ze.getName().replace(":", "_");
	            //g_logger.warning("Extracting " + entryName + " -> " + localFilesDirectory + File.separator +  entryName + "...");
	            File f = new File(tempDir, entryName);	        
	            synchronized (f) {
	            	 // Create folders needed to store in correct relative path.
		            f.getParentFile().mkdirs();
		            FileOutputStream fos = new FileOutputStream(f);
		            int len;
		            byte buffer[] = new byte[1024];
		            while ((len = zis.read(buffer)) > 0) {
		                fos.write(buffer, 0, len);
		            }
		            fos.close();  
				}		       
	            store.moveIn(tempDir.getAbsolutePath() + File.separator + entryName, entryName);
	            ze = zis.getNextEntry();
	        }
	        zis.closeEntry();	        
	        zis.close();
	        
		} catch (Exception e) {
			
			this.createErrorReply(299, "Failed to unzip archive.", e.getMessage());
			return false;
		
		} finally {
			
			if (null != tempDir)
				tempDir.delete();
		}
		 
//		g_logger.warning("Done");
		return true;
	}
		
	/**
	 * Adds the detect var.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addDetectVar(String key, String value) {		
		if (this.detectRequest == null) {
			this.detectRequest = new JsonObject();
		}
		this.detectRequest.addProperty(key, value);		
	}
	
	/**
	 * Site detect.
	 *
	 * @return true, if successful
	 */
	public synchronized boolean deviceDetect(Map<String, String> data) 
	{
		initRequest();
		SortedMap<String, String> headersMap = new TreeMap<String, String>();
		if (null != data)
			for (Entry<String, String> entry : data.entrySet()) 
				headersMap.put(entry.getKey().toLowerCase(), null == entry.getValue() ? "" : entry.getValue().toLowerCase());
		
		String id = !HDUtil.isNullOrEmpty(headersMap.get("id")) ? headersMap.get("id") : siteId;
		String fastKeyHeadersJson = null;
		Gson gson = new Gson();
		boolean result = false;

		// If there are no headers then there"s nothing to detect.
		if (null == data && detectRequest.isJsonNull()) 
		{
			this.createErrorReply(301, "FastFail : No headers provided");
			return false;			
		}

		Map <String, String> tempDetectReq = gson.fromJson(detectRequest, new TypeToken<Map <String, String>>() {}.getType());
		for (Entry<String, String> entry : tempDetectReq.entrySet()) 
			headersMap.put(entry.getKey().toLowerCase(), null == entry.getValue() ? "" : entry.getValue().toLowerCase());				
		
		
		if (Config.isCacheRequests())
		{			
			fastKeyHeadersJson = gson.toJson(headersMap, new TypeToken<Map <String, String>>() {}.getType());
			fastKeyHeadersJson = patternFastKey.matcher(fastKeyHeadersJson).replaceAll("");
			JsonElement jsonElem = cache.read(fastKeyHeadersJson);
			if (!HDUtil.isNullElement(jsonElem))
			{
				reply =jsonElem.getAsJsonObject();
				rawReply = new byte[0];
				return setError(0, "OK");
			}
		}
		
		if (isUseLocal()) 
		{
			result = device.localDetect(headersMap);
			setError(device.getStatus(), device.getMessage());
			reply = device.getReply();
			// Log unknown headers if enabled
			if (Config.isLogUnknown() && !result) 
				send_remote_syslog(headersMap);
			
		} else {
		
			detectRequest = gson.toJsonTree(headersMap, new TypeToken<Map <String, String>>() {}.getType()).getAsJsonObject();
			result =  remote("device/detect/" + id, this.detectRequest, null, null); 
		}
		
		if (result && Config.isCacheRequests())
			cache.write(fastKeyHeadersJson, reply);
		
		return result;
	}

	/**
	 * Creates the error reply.
	 *
	 * @param status the status
	 * @param msg the msg
	 * @return 
	 */
	boolean createErrorReply(int status, String msg) {
		this.reply = new JsonObject();
		this.reply.addProperty(JsonConstants.STATUS, status);
		this.reply.addProperty(JsonConstants.MESSAGE, msg);
		StringBuilder sb = new StringBuilder();
		sb.append(", Message: ").append(msg);
		g_logger.severe(sb.toString());
		return this.setError(status, sb.toString());
	}

	/**
	 * Creates the error reply.
	 *
	 * @param status the status
	 * @param msg the msg
	 * @param exceptionMessage the exception message
	 */
	private void createErrorReply(int status, String msg, String exceptionMessage) {
		this.reply = new JsonObject();
		this.reply.addProperty(JsonConstants.STATUS, status);
		this.reply.addProperty(JsonConstants.MESSAGE, msg);
		StringBuilder sb = new StringBuilder();
		sb.append(", Message: ").append(msg);
		sb.append(", Exception Message:").append(exceptionMessage);
		this.setError(status, sb.toString());
		g_logger.severe(sb.toString());
	}	

	/**
	 * This method can indicate if using the js Helper would yeild more accurate results.
	 *
	 * @param array $headers
	 * @return true if helpful, false otherwise.
	 **/
	public boolean isHelperUseful(Map<String, String> headers) 
	{
		return device.isHelperUseful(headers);
	}
	
	/**
	 * Gets the directory.
	 *
	 * @return the localFilesDirectory
	 */
	public String getLocalFilesDirectory() { return this.localFilesDirectory; }
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() { return username; }
	
	/**
	 * Gets the secret.
	 *
	 * @return the secret
	 */
	public String getSecret() { return secret; }
	
	/**
	 * Checks if is use local.
	 *
	 * @return true, if is use local
	 */
	public boolean isUseLocal() { return useLocal; }
	
	/**
	 * Gets the site id.
	 *
	 * @return the site id
	 */
	public String getSiteId() { return siteId; }
	
	/**
	 * Gets the mobile site.
	 *
	 * @return the mobile site
	 */
	public String getMobileSite() { return mobileSite; }
	
	/**
	 * Gets the match filter.
	 *
	 * @return the match filter
	 */
	public String getMatchFilter() { return matchFilter; }

	
	/**
	 * Gets the detect request.
	 *
	 * @return the detect request
	 */
	public JsonObject getDetectRequest() { return detectRequest; }
	
	/**
	 * Gets the reply.
	 *
	 * @return the reply
	 */
	public JsonObject getReply() { return reply; }
	
	/**
	 * Gets the raw reply.
	 *
	 * @return the raw reply
	 */
	public byte[] getRawReply() { return rawReply; }
	
	/**
	 * Sets the use local.
	 *
	 * @param useLocal the new use local
	 */
	public void setUseLocal(boolean useLocal) { this.useLocal = useLocal; }
	
	/**
	 * Sets the site id.
	 *
	 * @param siteId the new site id
	 */
	public void setSiteId(String siteId) { this.siteId = siteId; }
	
	/**
	 * Sets the mobile site.
	 *
	 * @param mobileSite the new mobile site
	 */
	public void setMobileSite(String mobileSite) { this.mobileSite = mobileSite; }
	
	/**
	 * Sets the match filter.
	 *
	 * @param matchFilter the new match filter
	 */
	public void setMatchFilter(String matchFilter) { this.matchFilter = matchFilter; }
	
	/**
	 * Sets the detect request.
	 *
	 * @param request the new detect request
	 */
	public void setDetectRequest(JsonObject request) { this.detectRequest = request; }
	
	/**
	 * Reset detect request.
	 */
	public void resetDetectRequest() { this.detectRequest = new JsonObject(); }	
	
	/**
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Logger topLogger = java.util.logging.Logger.getLogger("");
		Handler consoleHandler = null;
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				consoleHandler = handler;
				break;
			}
		}
		if (consoleHandler == null) {
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		// set the console handler to fine:
		consoleHandler.setLevel(java.util.logging.Level.FINEST);
		g_logger.setLevel(Level.FINEST);
				
		try {					
			FileInputStream fis = new FileInputStream("hdapi_config.properties");
			Config.init(fis);
			fis.close();				
			
			HD hd3 = new HD();
			
			hd3.setup(null, "127.0.0.1", "http://localhost");

			if (hd3.deviceVendors()) {
				g_logger.fine(hd3.getReply().toString());				
			} else {
				g_logger.severe(hd3.getError());
			}
			
			if (hd3.deviceModels("Nokia")) {
				g_logger.fine(hd3.getReply().toString());
			} else {
				g_logger.severe(hd3.getError());
			}

			if (hd3.deviceView("Nokia", "660")) {
				g_logger.fine(hd3.getReply().toString());
			} else {
				g_logger.severe(hd3.getError());
			}
			
		    if (hd3.deviceWhatHas("general_vendor", "Nokia")) {
				g_logger.fine(hd3.getReply().toString());
			} else {
				g_logger.severe(hd3.getError());
			}
	    			
//			hd3.addDetectVar("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
//			if (hd3.siteDetect()) {
//				g_logger.fine(hd3.getReply().toString());
//			} else {	
//				g_logger.severe(hd3.getError());
//			}
//			
//			hd3.addDetectVar("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
//			if (hd3.siteDetect()) {
//				g_logger.fine(hd3.getReply().toString());
//			} else {
//				g_logger.severe(hd3.getError());
//			}
//			
//			hd3.addDetectVar("user-agent", "Opera/9.80 (Android; OperaMini/7.0.29952/28.2144; U; pt) Presto/2.8.119 Version/11.10");
//			hd3.addDetectVar("x-operamini-phone", "Android #");
//			hd3.addDetectVar("x-operamini-phone-ua", "Mozilla/5.0 (Linux; U;Android 2.1-update1; pt-br; U20a Build/2.1.1.A.0.6) AppleWebKit/530.17(KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
//			if (hd3.siteDetect()) {
//				g_logger.fine(hd3.getReply().toString());
//			} else {
//				g_logger.severe(hd3.getError());
//			}
//			if (hd3.siteFetchTrees()) {
//				g_logger.fine("trees fetched.");
//			} else {
//				g_logger.severe(hd3.getError());
//			}
//			if (hd3.siteFetchSpecs()) {
//				g_logger.fine("specs fetched.");
//			} else {
//				g_logger.severe(hd3.getError());
//			}
//			if (hd3.siteFetchArchive()) {
//				g_logger.fine("archive fetched.");
//			} else {
//				g_logger.severe(hd3.getError());
//			} 
		} catch (Exception ie) {
			ie.printStackTrace();
			g_logger.severe(ie.getMessage());
		}	
	}
		
}
