package hdapi3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.codec.binary.Base64;
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
public class HD3 {
		
	/** Logger for this class and subclasses */
	private final static Logger g_logger = Logger.getLogger(HD3.class.getName());
	
	/** The realm. */
	private String realm;
	
	/** The username. */
	private String username;	

	/** The secret key. */
	private String secret;
	
	/** The use local. */
	private boolean useLocal;
	
	/** The api server. */
	private String apiServer;
	
	/** The log server. */
	private String logServer;
	
	/** The site id. */
	private String siteId;
	
	/** The mobile site. */
	private String mobileSite;
	
	/** The non mobile. */
	private String nonMobile;
	
	/** The match filter. */
	private String matchFilter;
	
	/** The connect timeout. */
	private int connectTimeout;
	
	/** The read timeout. */
	private int readTimeout;

	/** The use proxy. */
	private boolean useProxy;
	
	/** The proxy address. */
	private String proxyAddress;
	
	/** The proxy port. */
	private int proxyPort;
	
	/** The proxy username. */
	private String proxyUsername;
	
	/** The proxy password. */
	private String proxyPassword;
	
	/** The local files directory. */
	private String localFilesDirectory;
	
	/** The m_detect request. */
	private JsonObject m_detectRequest;
	
	/** The m_reply. */
	private JsonObject m_reply;
	
	/** The m_raw reply. */
	private byte[] m_rawReply;
	
	/** The m_error. */
	private String m_error;
	
	/** The m_cache. */
	private HDCache m_cache;
	
	/** The m_specs. */
	private JsonObject m_specs;
	
	/**
	 * Sets HD3 properties from Settings.
	 */
	public HD3() {
		this.realm = "APIv3";
		this.username = Settings.getUsername();		
		this.secret = Settings.getSecret();
		this.useLocal = Settings.isUseLocal();
		this.apiServer = Settings.getApiServer();
		this.logServer = Settings.getLogServer();
		this.siteId = Settings.getSiteId();
		this.nonMobile = Settings.getNonMobile();
		this.mobileSite = Settings.getMobileSite();
		this.matchFilter = Settings.getMatchFilter();

		this.connectTimeout = Settings.getConnectTimeout();
		this.readTimeout = Settings.getReadTimeout();

		this.useProxy = Settings.isUseProxy();
		this.proxyAddress = Settings.getProxyAddress();
		this.proxyPort = Settings.getProxyPort();
		this.proxyUsername = Settings.getProxyUsername();
		this.proxyPassword = Settings.getProxyPassword();
		this.localFilesDirectory = Settings.getLocalFilesDirectory();
		if (this.localFilesDirectory == null || this.localFilesDirectory == "" || this.localFilesDirectory.isEmpty())
			this.localFilesDirectory = "files";

		this.m_cache = HDCache.getInstance();
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
	public void setup(Map<String, String> headers, String serverIpAddress,
			String requestURI) {
		try {
			resetDetectRequest();
			if (headers != null) {
				Set<String> keys = headers.keySet();
				for (String key : keys) {
					if (!"Cookie".equalsIgnoreCase(key)) {
						this.m_detectRequest.addProperty(key, headers.get(key));
					}
				}
			}
			m_detectRequest.addProperty("ipaddress", serverIpAddress);
			m_detectRequest.addProperty("request_uri", requestURI);
		} catch (Exception ex) {
			g_logger.severe(ex.getMessage());
			setError("Failed to setup detect request. Cause: " + ex.getMessage());
		}		
	}
	
	/**
	 * Device vendors.
	 *
	 * @return true, if successful
	 */
	public boolean deviceVendors() {
		initRequest();
		return isUseLocal() ? localDeviceVendors() :remote("device/vendors", null);
	}
	
	/**
	 * Local device vendors.
	 *
	 * @return true, if successful
	 */
	private synchronized boolean localDeviceVendors() {
		JsonObject data = this.localGetSpecs();						
		boolean ret = false;
		if (data == null)
			return ret;
		JsonObject reply = new JsonObject();
		List<String> vendors = new ArrayList<String>();
		JsonElement devices = data.get(JsonContants.DEVICES);
		if (devices != null && devices.isJsonArray()) {
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonContants.DEVICE);
					if (HD3Util.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonContants.HD_SPECS);
					if (HD3Util.isNullElement(hdSpecs)) continue;
					JsonElement vendor = hdSpecs.get(JsonContants.GENERAL_VENDOR);
					if (HD3Util.isNullElement(vendor)) continue;
					vendors.add(vendor.getAsString());
				}
			}
			reply.add("vendor", HD3Util.toUniqueJsonArray(vendors));
			reply.addProperty(JsonContants.STATUS, 0);
			reply.addProperty(JsonContants.MESSAGE, "OK");
			this.m_reply = reply;
			ret = true;
		} else {
			this.createErrorReply(299, "Error: No devices data");
		}		
		return ret;				
	}

	/**
	 * Device models.
	 *
	 * @param vendor the vendor
	 * @return true, if successful
	 */
	public boolean deviceModels(String vendor) {
		initRequest();
		return isUseLocal()? localDeviceModels(vendor) : remote("device/models/" + vendor, null); 
	}
	
	/**
	 * Local device models.
	 *
	 * @param vendor the vendor
	 * @return true, if successful
	 */
	private synchronized boolean localDeviceModels(String vendor) {
		boolean ret = false;
		JsonObject data = this.localGetSpecs();
		if (data == null)
			return ret;
		
		JsonObject reply = new JsonObject();
		List<String> models = new ArrayList<String>();
		JsonElement devices = data.get(JsonContants.DEVICES);
		if (devices != null && devices.isJsonArray()) {
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonContants.DEVICE);
					if (HD3Util.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonContants.HD_SPECS);
					if (HD3Util.isNullElement(hdSpecs)) continue;
					JsonElement vendorElement = hdSpecs.get(JsonContants.GENERAL_VENDOR);
					if (HD3Util.isNullElement(vendorElement)) continue;
					String strVendor = vendorElement.getAsString();
					if (strVendor.equalsIgnoreCase(vendor)) {
						models.add(hdSpecs.get(JsonContants.GENERAL_MODEL).getAsString());
					}
					String vendorKey = strVendor + " ";
					JsonElement aliasesElement = hdSpecs.get(JsonContants.GENERAL_ALIASES);
					if (HD3Util.isNullElement(aliasesElement)) continue;
					if (aliasesElement.isJsonArray()) {
						JsonArray tempAliasesArray = (JsonArray)aliasesElement;
						Iterator<JsonElement> aliasesIter = tempAliasesArray.iterator();
						while (aliasesIter.hasNext()) {
							JsonElement alias = aliasesIter.next();
							if (alias.isJsonPrimitive()) {
								String strAlias = alias.getAsString();
								if (!HD3Util.isNullOrEmpty(strAlias) && strAlias.indexOf(vendor) == 0) {
									models.add(strAlias.replace(vendor, "").trim());
								}
							}
						}
					} else if (aliasesElement.isJsonPrimitive()) {
						String strAlias = aliasesElement.getAsString();
						if (!HD3Util.isNullOrEmpty(strAlias) && strAlias.indexOf(vendorKey) == 0) {
							models.add(strAlias.replace(vendorKey, ""));
						}
					}
				}
			}
			
			reply.add("model", HD3Util.toUniqueJsonArray(models));
			reply.addProperty(JsonContants.STATUS, 0);
			reply.addProperty(JsonContants.MESSAGE, "OK");
			this.m_reply = reply;
			ret = true;
		} else {
			this.createErrorReply(299, "Error: No devices data");
		}
		
		return ret;
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
		StringBuilder sb = new StringBuilder();
		String service = sb.append("device/view/").append(vendor).append("/").append(model).toString();
		return isUseLocal() ? localDeviceView(vendor, model) : remote(service, null);
	}
	
	/**
	 * Local device view.
	 *
	 * @param vendor the vendor
	 * @param model the model
	 * @return true, if successful
	 */
	private synchronized boolean localDeviceView(String vendor, String model) {
		boolean ret = false;
		JsonObject data = this.localGetSpecs();		
		if (data == null)
			return ret;
		JsonObject reply = new JsonObject();
		JsonElement devices = data.get(JsonContants.DEVICES);
		if (devices != null && devices.isJsonArray()) {
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonContants.DEVICE);
					if (HD3Util.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonContants.HD_SPECS);
					if (HD3Util.isNullElement(hdSpecs)) continue;
					JsonElement vendorElement = hdSpecs.get(JsonContants.GENERAL_VENDOR);
					JsonElement modelElement = hdSpecs.get(JsonContants.GENERAL_MODEL);
					if (HD3Util.isNullElement(vendorElement) || HD3Util.isNullElement(modelElement)) continue;
					if (vendorElement.getAsString().equalsIgnoreCase(vendor) 
							&& modelElement.getAsString().equalsIgnoreCase(model)) {
						reply.add("device", hdSpecs);
						reply.addProperty(JsonContants.STATUS, 0);
						reply.addProperty(JsonContants.MESSAGE, "OK");
						this.m_reply = reply;
						ret = true;
						return ret;
					}
				}
			}
			this.createErrorReply(301, "Error: Nothing found");
		} else {
			this.createErrorReply(299, "Error: No devices data");
		}
		return ret;
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
		StringBuilder sb = new StringBuilder();
		String service = sb.append("device/whathas/").append(key).append("/").append(value).toString();
		return isUseLocal()? localDeviceWhatHas(key, value) : remote(service, null);
	}
	
	/**
	 * Local device what has.
	 *
	 * @param key the key
	 * @param value the value
	 * @return true, if successful
	 */
	private synchronized boolean localDeviceWhatHas(String key, String value) {
		boolean ret = false;
		JsonObject data = this.localGetSpecs();
		if (data == null)
			return ret;
		JsonObject reply = new JsonObject();
		JsonArray matches = new JsonArray();
		JsonElement devices = data.get(JsonContants.DEVICES);
		if (!HD3Util.isNullElement(devices) && devices.isJsonArray()) {
			ret = true;
			JsonArray arrayTemp = (JsonArray) devices;
			Iterator<JsonElement>  iter = arrayTemp.iterator();
			while (iter.hasNext()) {
				JsonElement temp = iter.next();
				if (temp.isJsonObject()) {
					JsonObject row = (JsonObject) temp;
					JsonObject device = row.getAsJsonObject(JsonContants.DEVICE);
					if (HD3Util.isNullElement(device)) continue;
					JsonObject hdSpecs = device.getAsJsonObject(JsonContants.HD_SPECS);
					if (HD3Util.isNullElement(hdSpecs)) continue;
					JsonElement keyElement = hdSpecs.get(key);
					if (HD3Util.isNullElement(keyElement)) continue;
					boolean match = false;
					//just search entire string no matter if it's array of string
					match = keyElement.toString().toLowerCase().indexOf(value.toLowerCase()) >= 0;

					if (match) {
						JsonObject matched = new JsonObject();
						matched.add("id", device.get(JsonContants.ID));
						JsonElement vendorElement = hdSpecs.get(JsonContants.GENERAL_VENDOR);
						JsonElement modelElement = hdSpecs.get(JsonContants.GENERAL_MODEL);
						matched.add(JsonContants.GENERAL_VENDOR, vendorElement);
						matched.add(JsonContants.GENERAL_MODEL, modelElement);
						matches.add(matched);
						
					}
				}
			}
			reply.add("devices", matches);
			reply.addProperty(JsonContants.STATUS, 0);
			reply.addProperty(JsonContants.MESSAGE, "OK");
			m_reply = reply;
			
		} else {
			this.createErrorReply(299, "Error: No devices data");
		}
		return ret;
	}
	
	
	/**
	 * Site add.
	 *
	 * @param data the data
	 * @return true, if successful
	 */
	public boolean siteAdd(JsonObject data) {
		initRequest();
		return remote("site/add", data);
	}
	
	/**
	 * Site edit.
	 *
	 * @param data the data
	 * @return true, if successful
	 */
	public boolean siteEdit(JsonObject data) {
		initRequest();
		return remote("site/edit/" + getSiteId(), data);
	}
	
	/**
	 * Site view.
	 *
	 * @return true, if successful
	 */
	public boolean siteView() {
		initRequest();
		return remote("site/view/" + getSiteId(), null);
	}
	
	/**
	 * Site delete.
	 *
	 * @return true, if successful
	 */
	public boolean siteDelete() {
		initRequest();
		return remote("site/delete/" + getSiteId(), null);
	}
	
	/**
	 * Gets the files directory.
	 *
	 * @return the files directory
	 */
	private boolean getFilesDirectory() {
		File f = new File(this.localFilesDirectory);
		return f.exists() ? false : f.mkdir();
	}
	
	/**
	 * Site fetch archive.
	 *
	 * @return true, if successful
	 */
	public synchronized boolean siteFetchArchive() {
		initRequest();
		ByteArrayOutputStream reply = new ByteArrayOutputStream();
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

			if (this.post(null, "site/fetcharchive/" + getSiteId(), reply)) {
				setConnectTimeout(saveConnectTimeout);
				setReadTimeout(saveReadTimeout);
				if (reply.size() == 0) {
					this.createErrorReply(299, "Failed to download archive properly. File is zero size.");
					return false;				
				} else {
					try {
						byte[] content = reply.toByteArray();
						this.m_rawReply = content;
						ByteArrayInputStream in = new ByteArrayInputStream(content);
						JsonObject response = (JsonObject) HD3Util.parseJson(in);					
						if (response.isJsonObject()) {
							JsonElement status = response.get(JsonContants.STATUS);
							JsonElement message = response.get(JsonContants.MESSAGE);
							this.createErrorReply(status.getAsInt(), message.getAsString());
							return false;
						}
					} catch (Exception e) {
						if (reply.size() < 800000) {
							this.createErrorReply(299, "Failed to download archive properly. File is too small.");
							return false;										
						}
						// If there's an Exception then its probably all good :-)
					}
				}
				// Write the zipfile out.
				byte[] content = reply.toByteArray();
				this.m_rawReply = content;
				FileOutputStream stream = new FileOutputStream(zipFile);
				try {
				    stream.write(content);
					stream.flush();
				} finally {
				    stream.close();
				}
			} else {
				setConnectTimeout(saveConnectTimeout);
				setReadTimeout(saveReadTimeout);
				this.createErrorReply(299, "Failed to download archive.");
				return false;
			}
		} catch (Exception e) {
			this.createErrorReply(299, "Failed to download archive.", e.getMessage());
			return false;
		}	

		// Unzip ultimate.zip file.
		// Based on : http://www.justexample.com/wp/unzip-zip-file-using-java/		
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
	        while (ze != null) {
	        	String entryName = ze.getName().replace(':', '_');
	            //g_logger.warning("Extracting " + entryName + " -> " + localFilesDirectory + File.separator +  entryName + "...");
	            File f = new File(this.localFilesDirectory, entryName);	            
		        // Create folders needed to store in correct relative path.
	            f.getParentFile().mkdirs();
	            FileOutputStream fos = new FileOutputStream(f);
	            int len;
	            byte buffer[] = new byte[1024];
	            while ((len = zis.read(buffer)) > 0) {
	                fos.write(buffer, 0, len);
	            }
	            fos.close();  
	            ze = zis.getNextEntry();
	        }
	        zis.closeEntry();
	        zis.close();
		} catch (Exception e) {
			this.createErrorReply(299, "Failed to unzip archive.", e.getMessage());
			return false;
		}
		g_logger.warning("Done");
		return true;
	}
		
	/**
	 * Adds the detect var.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addDetectVar(String key, String value) {
		if (this.m_detectRequest == null) {
			this.m_detectRequest = new JsonObject();
		}
		this.m_detectRequest.addProperty(key, value);
	}
	
	/**
	 * Site detect.
	 *
	 * @return true, if successful
	 */
	public synchronized boolean siteDetect() {
		initRequest();
		String id = getSiteId();
		String header;
		boolean valid;

		// If there are no headers then there's nothing to detect.
		if (m_detectRequest.isJsonNull()) {
			this.createErrorReply(301, "FastFail : No headers provided");
			return false;			
		}

		// If any individual header is valid then proceed, else fastfail it out now.	
		valid = false;
		for (Map.Entry<String,JsonElement> entry : m_detectRequest.entrySet()) {
			if (! entry.getValue().isJsonNull()) {
				header = entry.getValue().getAsString();				
				if (header != "" && validateUserAgent(header)) {
					valid = true;
					break;
				}
			}			
		}				
		if (valid == false) {
			this.createErrorReply(301, "FastFail : Probable bot, spider or script");
			return false;
		} 

		if (isUseLocal()) {
			//g_logger.fine("Starting local detecting with "+ m_detectRequest.toString());
			boolean result = localSiteDetect();			
			return result;
		} else {
			return remote("site/detect/" + id, this.m_detectRequest) 
				&& !HD3Util.isNullElement(m_reply.get(JsonContants.STATUS))
				&& m_reply.get(JsonContants.STATUS).getAsString().equals("0");
		}
	}
	
	/**
	 * Local site detect.
	 *
	 * @return true, if successful
	 */
	private synchronized boolean localSiteDetect() {
		JsonObject device = null;
		JsonObject specs = null;
		
		HashMap<String, String> headers = HD3Util.parseHeaders(m_detectRequest);
		Object fastReply = m_cache.get(headers.toString());
		if (fastReply instanceof JsonObject) {
			this.m_reply = (JsonObject) fastReply;
			return true;
		} 
		
		JsonElement id = getDevice();
		if (! HD3Util.isNullElement(id)) {
			device = getCacheSpecs(id.getAsString(), JsonContants.DEVICE);
			specs = (JsonObject) HD3Util.get("hd_specs", device);			
			if (specs == null) {
				this.createErrorReply(299, "Malformed JSON Object Device:"+ id.toString());
				return false;
			}
			
			JsonElement platformId = getExtra(JsonContants.PLATFORM);
			JsonElement browserId = getExtra(JsonContants.BROWSER);
			JsonObject platform = null;
			JsonObject browser = null;
			String generalPlatform = null;
			String generalPlatformVersion = null;
			String generalBrowser = null;
			String generalBrowserVersion = null;

			if (! HD3Util.isNullElement(platformId)) {
				platform = (JsonObject) getCacheSpecs(platformId.getAsString(), JsonContants.EXTRA);
				generalPlatform = HD3Util.get(JsonContants.GENERAL_PLATFORM, platform).getAsString();
				if (generalPlatform != null) {
					generalPlatformVersion = HD3Util.get(JsonContants.GENERAL_PLATFORM_VERSION, platform).getAsString();				
				}
				generalBrowser = HD3Util.get(JsonContants.GENERAL_BROWSER, platform).getAsString();
				if (generalBrowser != null) {
					generalBrowserVersion =HD3Util.get(JsonContants.GENERAL_BROWSER_VERSION, platform).getAsString();
				}
			}
			
			if (! HD3Util.isNullElement(browserId)) {
				browser = getCacheSpecs(browserId.getAsString(), JsonContants.EXTRA);
				generalBrowser = HD3Util.get(JsonContants.GENERAL_BROWSER, browser).getAsString();
				if (generalBrowser != null) {
					generalBrowserVersion = HD3Util.get(JsonContants.GENERAL_BROWSER_VERSION, browser).getAsString();
				}
			}

			if (generalPlatform != null) {
				specs.addProperty(JsonContants.GENERAL_PLATFORM, generalPlatform);
			}
			if (generalPlatformVersion != null) {
				specs.addProperty(JsonContants.GENERAL_PLATFORM_VERSION, generalPlatformVersion);
			}
			if (generalBrowser != null) {
				specs.addProperty(JsonContants.GENERAL_BROWSER, generalBrowser);
			}
			if (generalBrowserVersion != null) {
				specs.addProperty(JsonContants.GENERAL_BROWSER_VERSION, generalBrowserVersion);
			}
			
			this.m_reply = new JsonObject();
			this.m_reply.add("hd_specs", specs);
			m_reply.addProperty(JsonContants.STATUS, 0);
			m_reply.addProperty(JsonContants.MESSAGE, "OK");
			if (HD3Util.isNullElement(specs.get(JsonContants.GENERAL_TYPE))) {
				m_reply.addProperty(JsonContants.CLASS_ATTR, "Unknown");
			} else {
				m_reply.addProperty(JsonContants.CLASS_ATTR, specs.get(JsonContants.GENERAL_TYPE).getAsString());
			}
			m_cache.put(headers.toString(), this.m_reply);
			return true;
		}
		this.createErrorReply(301, "Not Found");
		return false;
	}
	
	/**
	 * Gets the cache specs.
	 *
	 * @param id the id
	 * @param type the type
	 * @return the cache specs
	 */
	private JsonObject getCacheSpecs(String id, String type) {
		if (HD3Util.isNullOrEmpty(id) || HD3Util.isNullOrEmpty(type)) return null;
		StringBuilder cacheKey = new StringBuilder();
		cacheKey.append(type).append("_").append(id); //cacheKey.append(type).append(":").append(id);		
		return getCache(cacheKey.toString());
	}
	
	/**
	 * Gets the cache.
	 *
	 * @param key the key
	 * @return the cache
	 */
	private JsonObject getCache(String key) {
		if (HD3Util.isNullOrEmpty(key)) return null; 

		// Try read from memory cache
		Object cacheReply = (Object) m_cache.get(key);
		if (cacheReply instanceof JsonObject) {
			JsonObject ret = (JsonObject) cacheReply;
			return ret;
		} 

		// Try read from disk blob
		try {
			StringBuilder cacheKeyFile = new StringBuilder(key);
			cacheKeyFile.append(".json");
			File f = new File(this.localFilesDirectory, cacheKeyFile.toString());
			if (f.exists()) {
				FileInputStream fis = new FileInputStream(f);
				JsonElement element = HD3Util.parseJson(fis);
				JsonObject fileReply = element.getAsJsonObject();
				m_cache.put(key, fileReply);
				fis.close();
				return fileReply;
			}
		} catch (Exception e) {
		}

		// This should never happen .. A device key was not found.
		g_logger.fine("getCacheSpecs key not found (This should never happen)");
		g_logger.fine(key);
		return null;
	}
		
	/**
	 * Gets the device.
	 *
	 * @return the device
	 */
	private JsonElement getDevice() {
		String agent = null;
		HashMap<String, String> headers = HD3Util.parseHeaders(m_detectRequest);
		if (!HD3Util.isNullOrEmpty(headers.get(JsonContants.X_OPERAMINI_PHONE))
				&&!"? # ?".equals(headers.get(JsonContants.X_OPERAMINI_PHONE))) {
			JsonElement id = matchDevice(JsonContants.X_OPERAMINI_PHONE, headers.get(JsonContants.X_OPERAMINI_PHONE));
			if (!HD3Util.isNullElement(id)) {
				return id;
			}
			headers.remove(JsonContants.X_OPERAMINI_PHONE);
		}
		
		if (!HD3Util.isNullOrEmpty(headers.get(JsonContants.PROFILE))) {
			JsonElement id = matchDevice(JsonContants.PROFILE, headers.get(JsonContants.PROFILE));
			if (!HD3Util.isNullElement(id)) {
				return id;
			}
			headers.remove(JsonContants.PROFILE);				
		}
		
		if (!HD3Util.isNullOrEmpty(headers.get(JsonContants.X_WAP_PROFILE))) {
			JsonElement id = matchDevice(JsonContants.X_WAP_PROFILE, headers.get(JsonContants.X_WAP_PROFILE));
			if (!HD3Util.isNullElement(id)) {
				return id;
			}
			headers.remove(JsonContants.X_WAP_PROFILE);				
		}
		ArrayList<String> order = new ArrayList<String>();
		order.add("x-operamini-phone-ua");
		order.add("x-mobile-ua");
		order.add(JsonContants.USER_AGENT);
		Set<String> keys = headers.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (!order.contains(key)) {
				Pattern p = Pattern.compile("/^x-/i");
				Matcher m = p.matcher(key);
				if (m.matches()) {
					order.add(key);
				}
			}
		}
		agent = headers.get(JsonContants.USER_AGENT);
		for (int i = 0 ; i < order.size() ; i++) {
			String header = order.get(i);
			if (!HD3Util.isNullOrEmpty(headers.get(header))) {
				JsonElement id = matchDevice(JsonContants.USER_AGENT, headers.get(header));
				if (!HD3Util.isNullElement(id)) return id;
			}
		}
		return matchDevice(JsonContants.USER_AGENT, agent, 1);
		
	}
	
	/**
	 * Match device.
	 *
	 * @param header the header
	 * @param value the value
	 * @return the json element
	 */
	private JsonElement matchDevice(String header, String value) {
		return matchDevice(header, value, 0);
	}
	
	/**
	 * Match device.
	 *
	 * @param header the header
	 * @param value the value
	 * @param generic the generic
	 * @return the json element
	 */
	private JsonElement matchDevice(String header, String value, int generic) {
		if (HD3Util.isNullOrEmpty(value)) return null;
		value = value.toLowerCase().replaceAll(getMatchFilter(), "");
		String treeTag = header + generic;
		return match(header, value, treeTag);
	}
	
	/**
	 * Match.
	 *
	 * @param header the header
	 * @param newValue the new value
	 * @param treeTag the tree tag
	 * @return the json element
	 */
	private JsonElement match(String header, String newValue, String treeTag) {

		if (HD3Util.isNullOrEmpty(newValue) || newValue.length() < 4) {
			return null;
		}
		JsonElement branch = getBranch(treeTag);
		if (HD3Util.isNullElement(branch)) return null;
		if (JsonContants.USER_AGENT.equals(header)) {
			if (branch.isJsonObject()) {
				JsonObject branchObj = (JsonObject) branch;
				Set<Map.Entry<String, JsonElement>> branchEntries = branchObj.entrySet();
				Iterator<Map.Entry<String, JsonElement>> branchIter = branchEntries.iterator();
				while (branchIter.hasNext()) {
					Map.Entry<String, JsonElement> branchItem = branchIter.next();
					//String order = branchItem.getKey();
					//g_logger.fine("order : " + order);
					JsonElement filterElem = branchItem.getValue(); 
					if (filterElem instanceof JsonObject) {
						JsonObject filterObj = (JsonObject) filterElem;
						Set<Map.Entry<String, JsonElement>> filterEntries = filterObj.entrySet();
						Iterator<Map.Entry<String, JsonElement>> filterIter = filterEntries.iterator();
						while (filterIter.hasNext()) {
							Map.Entry<String, JsonElement> filterItem = filterIter.next();
							String filter = filterItem.getKey();
							//g_logger.fine("filter : " + filter);
							if (newValue.indexOf(filter) >= 0) {
								JsonElement matchesElem = filterItem.getValue();
								if (matchesElem instanceof JsonObject) {
									JsonObject matchesObj = (JsonObject) matchesElem;
									Set<Map.Entry<String, JsonElement>> matchesEntries = matchesObj.entrySet();
									Iterator<Map.Entry<String, JsonElement>> matchesIter = matchesEntries.iterator();
									while (matchesIter.hasNext()) {
										Map.Entry<String, JsonElement> matchItem = matchesIter.next();
										String match = matchItem.getKey();
										//g_logger.fine("match : " + match);
										JsonElement nodeElem = matchItem.getValue();
										if (newValue.indexOf(match) >= 0) {
											//g_logger.fine("matched : " + newValue);
											return nodeElem;
										}
									}
								}
							}
						}
						
					}
					//if ()
				}
			}
		} else {
			if (branch.isJsonObject()) {
				JsonObject temp = (JsonObject) branch;
				return temp.get(newValue);
			}
		}
		return null;
	}
	
	/**
	 * Gets the branch.
	 *
	 * @param name the name
	 * @return the branch
	 */
	private JsonElement getBranch(String name) {
		if (HD3Util.isNullOrEmpty(name)) return null;
		return getCache(name);
	}
			
	/**
	 * Gets the extra.
	 *
	 * @param classKey the class key
	 * @return the extra
	 */
	private JsonElement getExtra(String classKey) {
		HashMap<String, String> headers = HD3Util.parseHeaders(m_detectRequest);
		ArrayList<String> checkOrder = new ArrayList<String>();
		if (JsonContants.PLATFORM.equals(classKey)) {
			checkOrder.add("x-operamini-phone-ua");
			checkOrder.add(JsonContants.USER_AGENT);
			checkOrder.addAll(headers.keySet());
		} else if (JsonContants.BROWSER.equals(classKey)) {
			checkOrder.add("agent");
			checkOrder.addAll(headers.keySet());
		}
		for (String field : checkOrder) {
			if (!HD3Util.isNullOrEmpty(headers.get(field)) 
					&& (JsonContants.USER_AGENT.equals(field) || field.indexOf("x-") >=0)){
				JsonElement id = matchExtra(JsonContants.USER_AGENT, headers.get(field),classKey);
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Match extra.
	 *
	 * @param header the header
	 * @param value the value
	 * @param classKey the class key
	 * @return the json element
	 */
	private JsonElement matchExtra(String header ,String value, String classKey) {
		value = value.toLowerCase().replaceAll(" ", "");
		String treeTag = header + classKey;
		return match(header, value, treeTag);
	}
	
	/**
	 * Validate user agent.
	 *
	 * @param userAgent the user agent
	 * @return true, if successful
	 */
	private boolean validateUserAgent(String userAgent) {
		if (HD3Util.isNullOrEmpty(userAgent)) return false;
		Pattern p = Pattern.compile(getNonMobile());
		Matcher m = p.matcher(userAgent);
		if (m.matches()) {
			return false;
		}
		return true;
	}
	
	// TODO : Add retries into remote
	/**
	 * Remote.
	 *
	 * @param service the service
	 * @param data the data
	 * @return true, if successful
	 */
	private boolean remote(String service, JsonObject data) {				
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		boolean ret = false;		
		try {
			if (this.post(data == null ? null : data.toString(), service, out) ) {
				byte[] content = out.toByteArray();
				this.m_rawReply = content;
				in = new ByteArrayInputStream(content);
				JsonElement response = HD3Util.parseJson(in);
				if (response == null || response.isJsonNull()) {
					this.setError("Empty reply");
				} else if (response.isJsonObject()) {
					JsonObject temp = (JsonObject) response;
					this.m_reply = temp;
					if (temp.get(JsonContants.STATUS) == null) {
						this.setError("Error : No status set in reply");
					} else if (temp.get("status").getAsInt() != 0) {
						int status = temp.get(JsonContants.STATUS).getAsInt();
						String message = temp.get(JsonContants.MESSAGE) == null ? "": temp.get(JsonContants.MESSAGE).getAsString();						
						StringBuilder sb = new StringBuilder();
						sb.append("Error : ").append(status).append(", Message : ").append(message);
						this.setError(sb.toString());
					} else {
						ret = true;
					}
				} else {
					this.setError("Error: No JsonObject in response.");
				}					
			}			
		} finally {
			try {
				out.close();
			} catch (Exception e) {
				g_logger.warning(e.getMessage());
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				g_logger.warning(e.getMessage());
			}
		}			
		return ret;
	}

	/**
	 * Post.
	 *
	 * @param data the data
	 * @param service the service
	 * @param response the response
	 * @return true, if successful
	 */
	private boolean post(String data, String service, OutputStream response) {
		boolean ret = false;		
		try {
			String apiUrl = getApiServer();			
			String contentLength = "0";
			if (! HD3Util.isNullOrEmpty(data)) {
				contentLength = Integer.toString(data.length());
			}			
			StringBuilder sb = new StringBuilder();
			sb.append(apiUrl.toString()).append("/").append(getRealm().toLowerCase()).append("/").append(service).append(".json");			
			URL newURL = new URL(sb.toString());			
			URLConnection conn;
			if (isUseProxy() && ! HD3Util.isNullOrEmpty(getProxyAddress())) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyAddress(), getProxyPort()));
				conn = newURL.openConnection(proxy);
				if (! HD3Util.isNullOrEmpty(getProxyUsername())) {
					conn.setRequestProperty("Proxy-Authorization", getBasicProxyPass());
				}
			} else {
				g_logger.fine("connecting to : " + newURL.toExternalForm());
				conn = newURL.openConnection();
			}			
			conn.setConnectTimeout(getConnectTimeout() * 1000);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length",contentLength);
			conn.setRequestProperty("Authorization", getAuthorizationHeader(newURL));
			conn.setReadTimeout(getReadTimeout() * 1000);			
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());			
			if (! HD3Util.isNullOrEmpty(data)) {
				writer.write(data);
			}						
			writer.flush();
			writer.close();			
			InputStream is = conn.getInputStream();
			int bLength;
			byte[] b = new byte[1024];
			while ((bLength = is.read(b)) != -1) {
				response.write(b, 0, bLength);
			}					
			response.flush();
			response.close();
			is.close();
			return ret = true;
		} catch (Exception ex) {
			g_logger.warning("Exception occured while trying to access " + ex.getLocalizedMessage());
		}
		return ret;
	}

	/**
	 * Gets the authorization header.
	 *
	 * @param requestUrl the request url
	 * @return the authorization header
	 * @throws Exception the exception
	 */
	private String getAuthorizationHeader(URL requestUrl)
			throws Exception {
		String nc = "00000001";
		String snonce = "APIv3";
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis()).append(getSecret());
		String cnonce = HD3Util.md5(sb.toString());
		String qop = "auth";
		sb.setLength(0);
		sb.append(getUsername()).append(":").append(this.getRealm())
				.append(":").append(getSecret());
		String ha1 = HD3Util.md5(sb.toString());
		sb.setLength(0);
		sb.append("POST:").append(requestUrl.getPath());
		String ha2 = new String(HD3Util.md5(sb.toString()));
		sb.setLength(0);
		sb.append(ha1).append(":").append(snonce).append(":").append(nc)
				.append(":").append(cnonce).append(":").append(qop).append(":")
				.append(ha2);
		String response = HD3Util.md5(sb.toString());
		sb.setLength(0);
		sb.append("Digest username=\"").append(getUsername())
				.append("\", realm=\"").append(getRealm()).append("\", nonce=\"")
				.append(snonce).append("\", uri=\"")
				.append(requestUrl.getPath()).append("\", qop=").append(qop)
				.append(", nc=").append(nc).append(", cnonce=\"")
				.append(cnonce).append("\", response=\"").append(response)
				.append("\", opaque=\"").append(getRealm()).append("\"");
		return sb.toString();
	}

	/**
	 * Gets the basic proxy pass.
	 *
	 * @return the basic proxy pass
	 */
	private String getBasicProxyPass() {
		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");
		sb.append(new String(Base64.encodeBase64((new String(getProxyUsername() + ":" + getProxyPassword())).getBytes())));
		return sb.toString();
	}
	
	// Build up the full devices tree
	/**
	 * Local get specs.
	 *
	 * @return the json object
	 */
	public JsonObject localGetSpecs()  {
		String file;
		JsonArray specs = new JsonArray();
		JsonObject reply = new JsonObject();
		JsonElement data = null;
		
		File dir = new File(localFilesDirectory);		
		File [] fileList = dir.listFiles(); 		
		if (fileList == null) {
			createErrorReply(299, "Unable to list files in localFilesDirectory.");
			return null;			
		}
		if (this.m_specs != null) {
			return this.m_specs;
		}

		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile()) {
				file = fileList[i].getName();				
				// If the file is in the cache already then grab it form there.		    	
				if (file.endsWith(".json") && file.startsWith("Device_")) {					// Device:					
		    		String intValue = file.replaceAll("[a-zA-Z._]", "");					// [a-zA-Z.:]
					data = getCacheSpecs(intValue, "Device");								
	    			if (data == null || data.isJsonNull() || ! data.isJsonObject()) {
	    				createErrorReply(299, "Unable to parse Device file : " + file);
	    				return null;
	    			} else {
	    				specs.add(data);
	    			}
		    	}
			}
		}				
		if (specs != null) {
			reply.add("devices", specs);
			this.m_specs = reply;
		}		
		return reply;
	}
	
	/**
	 * Creates the error reply.
	 *
	 * @param status the status
	 * @param msg the msg
	 */
	private void createErrorReply(int status, String msg) {
		this.m_reply = new JsonObject();
		this.m_reply.addProperty(JsonContants.STATUS, status);
		this.m_reply.addProperty(JsonContants.MESSAGE, msg);
		StringBuilder sb = new StringBuilder();
		sb.append("Error: ").append(String.valueOf(status));
		sb.append(", Message: ").append(msg);
		this.setError(sb.toString());
		g_logger.severe(sb.toString());
	}

	/**
	 * Creates the error reply.
	 *
	 * @param status the status
	 * @param msg the msg
	 * @param exceptionMessage the exception message
	 */
	private void createErrorReply(int status, String msg, String exceptionMessage) {
		this.m_reply = new JsonObject();
		this.m_reply.addProperty(JsonContants.STATUS, status);
		this.m_reply.addProperty(JsonContants.MESSAGE, msg);
		StringBuilder sb = new StringBuilder();
		sb.append("Error: ").append(String.valueOf(status));
		sb.append(", Message: ").append(msg);
		sb.append(", Exception Message:").append(exceptionMessage);
		this.setError(sb.toString());
		g_logger.severe(sb.toString());
	}	
	
	/**
	 * Inits the request.
	 */
	private void initRequest() {
		this.m_reply = null;
		this.m_rawReply = null;
		setError("");
	}

	/**
	 * Gets the realm.
	 *
	 * @return the realm
	 */
	public String getRealm() { return realm; }
	
	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public String getError() { return m_error; }
	
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
	 * Checks if is use proxy.
	 *
	 * @return true, if is use proxy
	 */
	public boolean isUseProxy() { return useProxy; }
	
	/**
	 * Gets the api server.
	 *
	 * @return the api server
	 */
	public String getApiServer() { return apiServer; }
	
	/**
	 * Gets the log server.
	 *
	 * @return the log server
	 */
	public String getLogServer() { return logServer; }
	
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
	 * Gets the proxy address.
	 *
	 * @return the proxy address
	 */
	public String getProxyAddress() { return proxyAddress; }
	
	/**
	 * Gets the proxy username.
	 *
	 * @return the proxy username
	 */
	public String getProxyUsername() { return proxyUsername; }
	
	/**
	 * Gets the proxy password.
	 *
	 * @return the proxy password
	 */
	public String getProxyPassword() { return proxyPassword; }
	
	/**
	 * Gets the read timeout.
	 *
	 * @return the read timeout
	 */
	public int getReadTimeout() { return readTimeout; }
	
	/**
	 * Gets the connect timeout.
	 *
	 * @return the connect timeout
	 */
	public int getConnectTimeout() { return connectTimeout; }
	
	/**
	 * Gets the proxy port.
	 *
	 * @return the proxy port
	 */
	public int getProxyPort() { return proxyPort; }
	
	/**
	 * Gets the detect request.
	 *
	 * @return the detect request
	 */
	public JsonObject getDetectRequest() { return m_detectRequest; }
	
	/**
	 * Gets the reply.
	 *
	 * @return the reply
	 */
	public JsonObject getReply() { return m_reply; }
	
	/**
	 * Gets the raw reply.
	 *
	 * @return the raw reply
	 */
	public byte[] getRawReply() { return m_rawReply; }
	
	/**
	 * Gets the non mobile.
	 *
	 * @return the non mobile
	 */
	public String getNonMobile() { return nonMobile; }
	
	/**
	 * Sets the realm.
	 *
	 * @param realm the new realm
	 */
	public void setRealm(String realm) { this.realm = realm; }
	
	/**
	 * Sets the error.
	 *
	 * @param error the new error
	 */
	public void setError(String error) { this.m_error = error; }
	
	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) { this.username = username; }
	
	/**
	 * Sets the secret.
	 *
	 * @param secret the new secret
	 */
	public void setSecret(String secret) { this.secret = secret; }
	
	/**
	 * Sets the use local.
	 *
	 * @param useLocal the new use local
	 */
	public void setUseLocal(boolean useLocal) { this.useLocal = useLocal; }
	
	/**
	 * Sets the api server.
	 *
	 * @param apiServer the new api server
	 */
	public void setApiServer(String apiServer) { this.apiServer = apiServer; }
	
	/**
	 * Sets the log server.
	 *
	 * @param logServer the new log server
	 */
	public void setLogServer(String logServer) { this.logServer = logServer; }
	
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
	 * Sets the connect timeout.
	 *
	 * @param connectTimeout the new connect timeout
	 */
	public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
	
	/**
	 * Sets the read timeout.
	 *
	 * @param readTimeout the new read timeout
	 */
	public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
	
	/**
	 * Sets the use proxy.
	 *
	 * @param useProxy the new use proxy
	 */
	public void setUseProxy(boolean useProxy) { this.useProxy = useProxy; }
	
	/**
	 * Sets the proxy address.
	 *
	 * @param proxyAddress the new proxy address
	 */
	public void setProxyAddress(String proxyAddress) { this.proxyAddress = proxyAddress; }
	
	/**
	 * Sets the proxy port.
	 *
	 * @param proxyPort the new proxy port
	 */
	public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }
	
	/**
	 * Sets the proxy username.
	 *
	 * @param proxyUsername the new proxy username
	 */
	public void setProxyUsername(String proxyUsername) { this.proxyUsername = proxyUsername; }
	
	/**
	 * Sets the proxy password.
	 *
	 * @param proxyPassword the new proxy password
	 */
	public void setProxyPassword(String proxyPassword) { this.proxyPassword = proxyPassword; }
	
	/**
	 * Sets the detect request.
	 *
	 * @param request the new detect request
	 */
	public void setDetectRequest(JsonObject request) { this.m_detectRequest = request; }
	
	/**
	 * Reset detect request.
	 */
	public void resetDetectRequest() { this.m_detectRequest = new JsonObject(); }
	
	/**
	 * Sets the non mobile.
	 *
	 * @param nonMobile the new non mobile
	 */
	public void setNonMobile(String nonMobile) { this.nonMobile = nonMobile; }

	/**
	 * The main method.
	 *
	 * @param args the arguments
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
			Settings.init(fis);
			fis.close();
			
			HD3 hd3 = new HD3();
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
	    			
			hd3.addDetectVar("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
			if (hd3.siteDetect()) {
				g_logger.fine(hd3.getReply().toString());
			} else {
				g_logger.severe(hd3.getError());
			} 
			
			hd3.addDetectVar("user-agent", "Opera/9.80 (Android; OperaMini/7.0.29952/28.2144; U; pt) Presto/2.8.119 Version/11.10");
			hd3.addDetectVar("x-operamini-phone", "Android #");
			hd3.addDetectVar("x-operamini-phone-ua", "Mozilla/5.0 (Linux; U;Android 2.1-update1; pt-br; U20a Build/2.1.1.A.0.6) AppleWebKit/530.17(KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
			if (hd3.siteDetect()) {
				g_logger.fine(hd3.getReply().toString());
			} else {
				g_logger.severe(hd3.getError());
			} 
		/*	
			if (hd3.siteFetchArchive()) {
				g_logger.fine("archive fetched.");
			} else {
				g_logger.severe(hd3.getError());
			} */

		} catch (Exception ie) {
			ie.printStackTrace();
			g_logger.severe(ie.getMessage());
		}
	}
}
