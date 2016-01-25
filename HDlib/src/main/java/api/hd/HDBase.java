package api.hd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HDBase 
{
	protected final static String DETECTIONV4_STANDARD = "0";
	protected final static String DETECTIONV4_GENERIC =  "1";
	private final static String configFile = "hdapi_config.properties";	
	
	/** Logger for this class and subclasses */
	protected final static Logger g_logger = Logger.getLogger(HD.class.getName());
	
	private final Pattern deviceUAFilter = Pattern.compile("([ _\\#\\-,./:'\"]+)");
	private final Pattern extraUAFilter = Pattern.compile("([ ]+)");
//	private final Pattern commonUAFilter = Pattern.compile("/[^(\u0020-\u007F)]*/");
	private final Pattern commonUAFilter = Pattern.compile("[^(\u0020-\u007F)]+");

	/** The local files directory. */
	protected String localFilesDirectory;
	/** The log server. */
	private String logServer;
	/** The API server. */
	private String apiServer;
	/** The connect timeout. */
	private int connectTimeout;
	/** The read timeout. */
	private int readTimeout;
	/** The number of retries. */
	private int retries;
	private String realm = "APIv4";
	private String apiBase = "/apiv4/";
	private String  apikit = "Java 4.0.0";
	private String loggerHost = "logger.handsetdetection.com";
	private Integer loggerPort = 80;
	protected String username;		
	protected String secret;
	private Boolean useProxy;
	private String proxyAddress;
	private Integer proxyPort;
	private String proxyUsername;
	private String proxyPassword;
	private String matchFilter;
	
	protected HDStore store;
	/** The m_reply. */
	protected static JsonObject reply;
	/** The m_raw reply. */
	protected static byte[] rawReply;
	/** The m_error. */
	private String error;
//	private Config config;							// catalin: is this really of any use ?
	
	protected final Map<String, List> detectionConfigMap = new HashMap<String, List>();
	protected final Map<String, String> detectionLanguagesMap = new HashMap<String, String>();
//	protected final Pattern xPattern = Pattern.compile("/^x-/i");
	protected final Pattern xPattern = Pattern.compile("x-", Pattern.CASE_INSENSITIVE);
	protected final Map<String, String> detectedRuleKey = new HashMap<String, String>();
	private final Map<String, JsonElement> tree = new HashMap<String, JsonElement>();	

	
	protected HDBase() throws IOException 
	{
		this (configFile);
	}
	
	protected HDBase(byte[] isConfig) throws IOException 
	{
		if (null != isConfig) {
			Config.init(new ByteArrayInputStream(isConfig));
		} else {
			throw new InvalidParameterException("No nulls here please");
		}
		
		System.out.println("Config Local Dir : " + Config.getLocalDirectory());
		bringConfigLocally();
		System.out.println("HD Local Dir : " + localFilesDirectory);
		initDetectionConfigMap();
		initDetectionLanguagesMap();
	}
	
	protected HDBase(String configFileName) throws IOException 
	{
		if (null != configFileName)
		{
			FileInputStream isConfig = new FileInputStream(configFileName);
			Config.init(isConfig);
			
		} else {
			
			throw new InvalidParameterException("No nulls here please");
		}
		
		bringConfigLocally();
		initDetectionConfigMap();
		initDetectionLanguagesMap();
	}

	protected HDBase(Config config) throws IOException 
	{
		if (null != config)
		{
			PipedInputStream isConfig = new PipedInputStream();
			OutputStream osConfig = new PipedOutputStream(isConfig);
			config.store(osConfig, "");
			Config.init(isConfig);
			
		} else {
			
			throw new InvalidParameterException("No nulls here please");
		}
	
		bringConfigLocally();
		initDetectionConfigMap();
		initDetectionLanguagesMap();
	}

	private void bringConfigLocally() 
	{
		this.localFilesDirectory = Config.getLocalDirectory();
		this.username = Config.getUsername();		
		this.secret = Config.getSecret();
		this.connectTimeout = Config.getConnectTimeout();
		this.readTimeout = Config.getReadTimeout();
		this.apiServer = Config.getApiServer();
		this.logServer = Config.getLogServer();
		this.useProxy = Config.isUseProxy();
		this.proxyAddress = Config.getProxyAddress();
		this.proxyPort = Config.getProxyPort();
		this.proxyUsername = Config.getProxyUsername();
		this.proxyPassword = Config.getProxyPassword();
		this.matchFilter = Config.getMatchFilter();
		this.retries = Config.getRetries();
		this.store = HDStore.getInstance();
	}

	private void initDetectionConfigMap() 
	{
		List<String> auxList = new ArrayList<String>();
		auxList.add("x-operamini-phone-ua");
		auxList.add("x-mobile-ua");
		auxList.add("device-stock-ua");
		auxList.add("user-agent");
		auxList.add("agent");
		detectionConfigMap.put("device-ua-order", Collections.unmodifiableList(auxList));
		detectionConfigMap.put("platform-ua-order", Collections.unmodifiableList(auxList));
		
		auxList = new ArrayList<String>();
		auxList.add("user-agent");
		auxList.add("agent");
		auxList.add("device-stock-ua");
		detectionConfigMap.put("browser-ua-order", Collections.unmodifiableList(auxList));
		detectionConfigMap.put("app-ua-order", Collections.unmodifiableList(auxList));
		detectionConfigMap.put("language-ua-order", Collections.unmodifiableList(auxList));

		Map<String, List> auxMap = new HashMap<String, List>();
		List<List<String>> auxList2 = new ArrayList<List<String>>();
		auxList = new ArrayList<String>();
		auxList.add("ro.product.brand");
		auxList.add("ro.product.model");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxList = new ArrayList<String>();
		auxList.add("ro.product.manufacturer");
		auxList.add("ro.product.model");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxList = new ArrayList<String>();
		auxList.add("ro-product-brand");
		auxList.add("ro-product-model");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxList = new ArrayList<String>();
		auxList.add("ro-product-manufacturer");
		auxList.add("ro-product-model");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxMap.put("android", Collections.unmodifiableList(auxList2));
		
		
		auxList2 = new ArrayList<List<String>>();
		auxList = new ArrayList<String>();
		auxList.add("utsname.brand");
		auxList.add("utsname.machine");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxMap.put("ios", Collections.unmodifiableList(auxList2));
		
		
		auxList2 = new ArrayList<List<String>>();
		auxList = new ArrayList<String>();
		auxList.add("devicemanufacturer");
		auxList.add("devicename");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxMap.put("windows phone", Collections.unmodifiableList(auxList2));
		
		
		
		List<Map> auxList3 = new ArrayList<Map>();
		auxList3.add(auxMap);
		detectionConfigMap.put("device-bi-order", Collections.unmodifiableList(auxList3));
		auxList = new ArrayList<String>();
		auxList2 = new ArrayList<List<String>>();
		auxList3 = new ArrayList<Map>();
		auxMap = new HashMap<String, List>();
		
		
		
		auxList.add("ro.build.id");
		auxList.add("ro.build.version.release");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxList = new ArrayList<String>();
		auxList.add("ro-build-id");
		auxList.add("ro-build-version-release");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxMap.put("android", Collections.unmodifiableList(auxList2));
		
		
		auxList2 = new ArrayList<List<String>>();
		auxList = new ArrayList<String>();
		auxList.add("uidevice.systemName");
		auxList.add("uidevice.systemversion");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxMap.put("ios", Collections.unmodifiableList(auxList2));
		
		
		auxList2 = new ArrayList<List<String>>();
		auxList = new ArrayList<String>();
		auxList.add("osname");
		auxList.add("osversion");
		auxList2.add(Collections.unmodifiableList(auxList));
		
		auxMap.put("windows phone", Collections.unmodifiableList(auxList2));
		
		
		
		auxList3.add(auxMap);
		detectionConfigMap.put("platform-bi-order", Collections.unmodifiableList(auxList3));
		

		
		
		detectionConfigMap.put("browser-bi-order", Collections.unmodifiableList(auxList3));
		detectionConfigMap.put("app-bi-order", Collections.unmodifiableList(auxList3));
//		auxList = new ArrayList<String>();
//		auxList2 = new ArrayList<List<String>>();
//		auxList3 = new ArrayList<Map>();
//		auxMap = new HashMap<String, List>();
	}
	
	private void initDetectionLanguagesMap()
	{
		detectionLanguagesMap.put("af", "Afrikaans");
		detectionLanguagesMap.put("sq", "Albanian");
		detectionLanguagesMap.put("ar-dz", "Arabic (Algeria)");
		detectionLanguagesMap.put("ar-bh", "Arabic (Bahrain)");
		detectionLanguagesMap.put("ar-eg", "Arabic (Egypt)");
		detectionLanguagesMap.put("ar-iq", "Arabic (Iraq)");
		detectionLanguagesMap.put("ar-jo", "Arabic (Jordan)");
		detectionLanguagesMap.put("ar-kw", "Arabic (Kuwait)");
		detectionLanguagesMap.put("ar-lb", "Arabic (Lebanon)");
		detectionLanguagesMap.put("ar-ly", "Arabic (libya)");
		detectionLanguagesMap.put("ar-ma", "Arabic (Morocco)");
		detectionLanguagesMap.put("ar-om", "Arabic (Oman)");
		detectionLanguagesMap.put("ar-qa", "Arabic (Qatar)");
		detectionLanguagesMap.put("ar-sa", "Arabic (Saudi Arabia)");
		detectionLanguagesMap.put("ar-sy", "Arabic (Syria)");
		detectionLanguagesMap.put("ar-tn", "Arabic (Tunisia)");
		detectionLanguagesMap.put("ar-ae", "Arabic (U.A.E.)");
		detectionLanguagesMap.put("ar-ye", "Arabic (Yemen)");
		detectionLanguagesMap.put("ar", "Arabic");
		detectionLanguagesMap.put("hy", "Armenian");
		detectionLanguagesMap.put("as", "Assamese");
		detectionLanguagesMap.put("az", "Azeri");
		detectionLanguagesMap.put("eu", "Basque");
		detectionLanguagesMap.put("be", "Belarusian");
		detectionLanguagesMap.put("bn", "Bengali");
		detectionLanguagesMap.put("bg", "Bulgarian");
		detectionLanguagesMap.put("ca", "Catalan");
		detectionLanguagesMap.put("zh-cn", "Chinese (China)");
		detectionLanguagesMap.put("zh-hk", "Chinese (Hong Kong SAR)");
		detectionLanguagesMap.put("zh-mo", "Chinese (Macau SAR)");
		detectionLanguagesMap.put("zh-sg", "Chinese (Singapore)");
		detectionLanguagesMap.put("zh-tw", "Chinese (Taiwan)");
		detectionLanguagesMap.put("zh", "Chinese");
		detectionLanguagesMap.put("hr", "Croatian");
		detectionLanguagesMap.put("cs", "Czech");
		detectionLanguagesMap.put("da", "Danish");
		detectionLanguagesMap.put("da-dk", "Danish");
		detectionLanguagesMap.put("div", "Divehi");
		detectionLanguagesMap.put("nl-be", "Dutch (Belgium)");
		detectionLanguagesMap.put("nl", "Dutch (Netherlands)");
		detectionLanguagesMap.put("en-au", "English (Australia)");
		detectionLanguagesMap.put("en-bz", "English (Belize)");
		detectionLanguagesMap.put("en-ca", "English (Canada)");
		detectionLanguagesMap.put("en-ie", "English (Ireland)");
		detectionLanguagesMap.put("en-jm", "English (Jamaica)");
		detectionLanguagesMap.put("en-nz", "English (New Zealand)");
		detectionLanguagesMap.put("en-ph", "English (Philippines)");
		detectionLanguagesMap.put("en-za", "English (South Africa)");
		detectionLanguagesMap.put("en-tt", "English (Trinidad)");
		detectionLanguagesMap.put("en-gb", "English (United Kingdom)");
		detectionLanguagesMap.put("en-us", "English (United States)");
		detectionLanguagesMap.put("en-zw", "English (Zimbabwe)");
		detectionLanguagesMap.put("en", "English");
		detectionLanguagesMap.put("us", "English (United States)");
		detectionLanguagesMap.put("et", "Estonian");
		detectionLanguagesMap.put("fo", "Faeroese");
		detectionLanguagesMap.put("fa", "Farsi");
		detectionLanguagesMap.put("fi", "Finnish");
		detectionLanguagesMap.put("fr-be", "French (Belgium)");
		detectionLanguagesMap.put("fr-ca", "French (Canada)");
		detectionLanguagesMap.put("fr-lu", "French (Luxembourg)");
		detectionLanguagesMap.put("fr-mc", "French (Monaco)");
		detectionLanguagesMap.put("fr-ch", "French (Switzerland)");
		detectionLanguagesMap.put("fr", "French (France)");
		detectionLanguagesMap.put("mk", "FYRO Macedonian");
		detectionLanguagesMap.put("gd", "Gaelic");
		detectionLanguagesMap.put("ka", "Georgian");
		detectionLanguagesMap.put("de-at", "German (Austria)");
		detectionLanguagesMap.put("de-li", "German (Liechtenstein)");
		detectionLanguagesMap.put("de-lu", "German (Luxembourg)");
		detectionLanguagesMap.put("de-ch", "German (Switzerland)");
		detectionLanguagesMap.put("de-de", "German (Germany)");
		detectionLanguagesMap.put("de", "German (Germany)");
		detectionLanguagesMap.put("el", "Greek");
		detectionLanguagesMap.put("gu", "Gujarati");
		detectionLanguagesMap.put("he", "Hebrew");
		detectionLanguagesMap.put("hi", "Hindi");
		detectionLanguagesMap.put("hu", "Hungarian");
		detectionLanguagesMap.put("is", "Icelandic");
		detectionLanguagesMap.put("id", "Indonesian");
		detectionLanguagesMap.put("it-ch", "Italian (Switzerland)");
		detectionLanguagesMap.put("it", "Italian (Italy)");
		detectionLanguagesMap.put("it-it", "Italian (Italy)");
		detectionLanguagesMap.put("ja", "Japanese");
		detectionLanguagesMap.put("kn", "Kannada");
		detectionLanguagesMap.put("kk", "Kazakh");
		detectionLanguagesMap.put("kok", "Konkani");
		detectionLanguagesMap.put("ko", "Korean");
		detectionLanguagesMap.put("kz", "Kyrgyz");
		detectionLanguagesMap.put("lv", "Latvian");
		detectionLanguagesMap.put("lt", "Lithuanian");
		detectionLanguagesMap.put("ms", "Malay");
		detectionLanguagesMap.put("ml", "Malayalam");
		detectionLanguagesMap.put("mt", "Maltese");
		detectionLanguagesMap.put("mr", "Marathi");
		detectionLanguagesMap.put("mn", "Mongolian (Cyrillic)");
		detectionLanguagesMap.put("ne", "Nepali (India)");
		detectionLanguagesMap.put("nb-no", "Norwegian (Bokmal)");
		detectionLanguagesMap.put("nn-no", "Norwegian (Nynorsk)");
		detectionLanguagesMap.put("no", "Norwegian (Bokmal)");
		detectionLanguagesMap.put("or", "Oriya");
		detectionLanguagesMap.put("pl", "Polish");
		detectionLanguagesMap.put("pt-br", "Portuguese (Brazil)");
		detectionLanguagesMap.put("pt", "Portuguese (Portugal)");
		detectionLanguagesMap.put("pa", "Punjabi");
		detectionLanguagesMap.put("rm", "Rhaeto-Romanic");
		detectionLanguagesMap.put("ro-md", "Romanian (Moldova)");
		detectionLanguagesMap.put("ro", "Romanian");
		detectionLanguagesMap.put("ru-md", "Russian (Moldova)");
		detectionLanguagesMap.put("ru", "Russian");
		detectionLanguagesMap.put("sa", "Sanskrit");
		detectionLanguagesMap.put("sr", "Serbian");
		detectionLanguagesMap.put("sk", "Slovak");
		detectionLanguagesMap.put("ls", "Slovenian");
		detectionLanguagesMap.put("sb", "Sorbian");
		detectionLanguagesMap.put("es-ar", "Spanish (Argentina)");
		detectionLanguagesMap.put("es-bo", "Spanish (Bolivia)");
		detectionLanguagesMap.put("es-cl", "Spanish (Chile)");
		detectionLanguagesMap.put("es-co", "Spanish (Colombia)");
		detectionLanguagesMap.put("es-cr", "Spanish (Costa Rica)");
		detectionLanguagesMap.put("es-do", "Spanish (Dominican Republic)");
		detectionLanguagesMap.put("es-ec", "Spanish (Ecuador)");
		detectionLanguagesMap.put("es-sv", "Spanish (El Salvador)");
		detectionLanguagesMap.put("es-gt", "Spanish (Guatemala)");
		detectionLanguagesMap.put("es-hn", "Spanish (Honduras)");
		detectionLanguagesMap.put("es-mx", "Spanish (Mexico)");
		detectionLanguagesMap.put("es-ni", "Spanish (Nicaragua)");
		detectionLanguagesMap.put("es-pa", "Spanish (Panama)");
		detectionLanguagesMap.put("es-py", "Spanish (Paraguay)");
		detectionLanguagesMap.put("es-pe", "Spanish (Peru)");
		detectionLanguagesMap.put("es-pr", "Spanish (Puerto Rico)");
		detectionLanguagesMap.put("es-us", "Spanish (United States)");
		detectionLanguagesMap.put("es-uy", "Spanish (Uruguay)");
		detectionLanguagesMap.put("es-ve", "Spanish (Venezuela)");
		detectionLanguagesMap.put("es", "Spanish (Traditional Sort)");
		detectionLanguagesMap.put("es-es", "Spanish (Traditional Sort)");
		detectionLanguagesMap.put("sx", "Sutu");
		detectionLanguagesMap.put("sw", "Swahili");
		detectionLanguagesMap.put("sv-fi", "Swedish (Finland)");
		detectionLanguagesMap.put("sv", "Swedish");
		detectionLanguagesMap.put("syr", "Syriac");
		detectionLanguagesMap.put("ta", "Tamil");
		detectionLanguagesMap.put("tt", "Tatar");
		detectionLanguagesMap.put("te", "Telugu");
		detectionLanguagesMap.put("th", "Thai");
		detectionLanguagesMap.put("ts", "Tsonga");
		detectionLanguagesMap.put("tn", "Tswana");
		detectionLanguagesMap.put("tr", "Turkish");
		detectionLanguagesMap.put("uk", "Ukrainian");
		detectionLanguagesMap.put("ur", "Urdu");
		detectionLanguagesMap.put("uz", "Uzbek");
		detectionLanguagesMap.put("vi", "Vietnamese");
		detectionLanguagesMap.put("xh", "Xhosa");
		detectionLanguagesMap.put("yi", "Yiddish");
		detectionLanguagesMap.put("zu", "Zulu");
	}
	
	/**
	 * Inits the request.
	 */
	protected void initRequest() {
		this.reply = new JsonObject();
		this.rawReply = null;
		setError(0, "");
	}
	
	/**
	 * Match.
	 *
	 * @param header the header
	 * @param value the new value
	 * @param actualHeader the tree tag
	 * @return the json element
	 */
	protected synchronized JsonElement getMatch(String header, String value, String subTree, String actualHeader, String classKey) 
	{		
		subTree = null != subTree ? subTree : DETECTIONV4_STANDARD;
		classKey = null != classKey ? classKey : JsonConstants.DEVICE;
		subTree = subTree.toLowerCase();
		classKey = classKey.toLowerCase();
		
		if (JsonConstants.DEVICE.equalsIgnoreCase(classKey))
		{
			value = cleanStr(value);
			actualHeader = header + subTree;
			
		} else {
			
			value = extraCleanStr(value);
			actualHeader = header + subTree;
		}
		
		if (HDUtil.isNullOrEmpty(value) || value.length() < 4) 
			return null;

		JsonElement branch = getBranch(actualHeader);		
		if (HDUtil.isNullElement(branch)) 
			return null;
		
		if (JsonConstants.USER_AGENT.equals(header)) 
		{	
			if (branch.isJsonObject()) 
			{
				JsonObject branchObj = (JsonObject) branch;
				Set<Map.Entry<String, JsonElement>> branchEntries = branchObj.entrySet();
				Iterator<Map.Entry<String, JsonElement>> branchIter = branchEntries.iterator();				
				
				while (branchIter.hasNext()) 
				{
					Map.Entry<String, JsonElement> branchItem = branchIter.next();
					//String order = branchItem.getKey();
					//g_logger.fine("order : " + order);
					JsonElement filterElem = branchItem.getValue(); 
					if (filterElem instanceof JsonObject) 
					{
						JsonObject filterObj = (JsonObject) filterElem;
						Set<Map.Entry<String, JsonElement>> filterEntries = filterObj.entrySet();
						Iterator<Map.Entry<String, JsonElement>> filterIter = filterEntries.iterator();
						
						while (filterIter.hasNext()) 
						{
							Map.Entry<String, JsonElement> filterItem = filterIter.next();
							String filter = filterItem.getKey();
							//g_logger.fine("filter : " + filter);							
							if (value.indexOf(filter) >= 0) 
							{								
								JsonElement matchesElem = filterItem.getValue();
								if (matchesElem instanceof JsonObject) 
								{
									JsonObject matchesObj = (JsonObject) matchesElem;									
									Set<Map.Entry<String, JsonElement>> matchesEntries = matchesObj.entrySet();
									Iterator<Map.Entry<String, JsonElement>> matchesIter = matchesEntries.iterator();
									
									while (matchesIter.hasNext()) 
									{
										Map.Entry<String, JsonElement> matchItem = matchesIter.next();
										String match = matchItem.getKey();										
										//g_logger.fine("match : " + match);
										JsonElement nodeElem = matchItem.getValue();										
										if (value.indexOf(match) >= 0) 
										{
											detectedRuleKey.put(classKey, cleanStr(header) + ":" + cleanStr(filter) + ":" + cleanStr(match));
											return nodeElem;
										}
									}
								}
							}
						}
						
					}					
				}
			}
			
		} else {
			
			if (branch.isJsonObject()) 
			{
				JsonObject temp = (JsonObject) branch;
				return temp.get(value);
			}
		}
		return null;
	}

	/**
	 * Remote.
	 * @return true, if successful
	 */
	public synchronized boolean remote(String suburl, JsonObject data, String filetype, Boolean authRequired) 
	{				
		filetype = null == filetype ? "json" : filetype;
		authRequired = authRequired == null ? true : authRequired; 
		boolean success = false;
		setError(0, "");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		
		String url = apiBase + suburl;
		int attempts = retries + 1;
		int tries = 0;
		
		String requestdata = null != data ? data.toString() : null;
		
		while(tries++ < attempts && !success)
		{
			try 
			{
				out.reset();
				if (this.post(apiServer, url, requestdata, authRequired, out))
				{
					byte[] content = out.toByteArray();
					this.rawReply = content;
					in = new ByteArrayInputStream(content);
					if (content == null || content.length == 0) 
					{
						this.setError(299, "Connection to " + url + " failed");
						
					} else if ("json".equals(filetype)) {
						
						HDUtil.printStream(g_logger, in);
						JsonElement response = HDUtil.parseJson(in);
						
						if (HDUtil.isNullElement(response)) 
						{
							this.setError(299, "Empty reply");
							
						} else {

							this.reply = (JsonObject)response;
							
							if (reply.get(JsonConstants.STATUS) == null) 
							{
								this.setError(299, "No status set in reply");
							
							} else if (reply.get("status").getAsInt() != 0) {
								
								int status = reply.get(JsonConstants.STATUS).getAsInt();
								String message = reply.get(JsonConstants.MESSAGE) == null ? "": reply.get(JsonConstants.MESSAGE).getAsString();						
								this.setError(status, message);
								
							} else {
								
								success = true;
							}
						}
						
					} else {
						
						success = true;
						
					}				
				}			
				
			} finally {
				
				try 
				{
					out.close();
					
				} catch (Exception e) {
					
					g_logger.warning(e.getMessage());
				}
				
				try 
				{
					if (in != null) 
						in.close();
					
				} catch (Exception e) {
					
					g_logger.warning(e.getMessage());
				}
			}
		}
		
		return success;
	}

	/**
	 * Post.
	 *
	 * @return true, if successful
	 */
	protected synchronized boolean post(String server, String url, String jsondata, boolean authRequired, OutputStream os) 
	{	
		boolean ret = false;	
		String host;
		Integer port;
		Proxy proxy;
		
		if (useProxy) 
		{
			host = this.proxyAddress;
			port = this.proxyPort;
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
		
		} else {
			
			host = server;
			port = 80;
			proxy = Proxy.NO_PROXY;
		}
		
		try 
		{
			// * Connect *
			//echo "Connecting to host, port port, url url<br/>";
			StringBuilder sb = new StringBuilder();
			sb.append(host).append(url);			
			URL newURL = new URL(sb.toString());	
			URLConnection conn = newURL.openConnection(proxy);
			
			if (useProxy && !HDUtil.isNullOrEmpty(proxyUsername)) 					
				conn.setRequestProperty("Proxy-Authorization", getBasicProxyPass());					

			g_logger.fine("connecting to : " + newURL.toExternalForm());
			conn = newURL.openConnection();
					
			String contentLength = "0";
			if (! HDUtil.isNullOrEmpty(jsondata)) {
				contentLength = Integer.toString(jsondata.length());
			}					
		
			conn.setConnectTimeout(connectTimeout);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length",contentLength);
			conn.setReadTimeout(readTimeout);
			if (authRequired)
				conn.setRequestProperty("Authorization", getAuthorizationHeader(newURL));
			
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());			
			if (! HDUtil.isNullOrEmpty(jsondata)) {
				writer.write(jsondata);
			}						
			writer.flush();
			writer.close();			
			
			InputStream is = conn.getInputStream();
			int bLength;
			byte[] b = new byte[1024];
			while ((bLength = is.read(b)) != -1) {
				os.write(b, 0, bLength);
			}					
			os.flush();
			os.close();
			is.close();			
			ret = true;
			
		} catch (Exception ex) {
			
			g_logger.warning("Exception occured while trying to access " + ex.getLocalizedMessage());	
			setError(299, ex.getLocalizedMessage());
		}		
		return ret;		
		
	}

	/**
	 * String cleanse for extras matching.
	 *
	 * @param string str
	 * @return string Cleansed string
	 **/
	protected String extraCleanStr(String str) 
	{
		str = str.toLowerCase();
		str = extraUAFilter.matcher(str).replaceAll("");
		str = commonUAFilter.matcher(str).replaceAll("");
		
		return str.trim();
	}
	
	/**
	 * String cleanse for device matching.
	 *
	 * @param string str
	 * @return string Cleansed string
	 **/
	protected String cleanStr(String str) 
	{
		str = str.toLowerCase();
		str = deviceUAFilter.matcher(str).replaceAll("");
		str = commonUAFilter.matcher(str).replaceAll("");
		
		return str.trim();
	}
	

	/**
	 *  Log function - User defined functions can be supplied in the 'logger' config variable.
	 */
	private void log(String msg) 
	{
		System.out.println(Calendar.getInstance().getTimeInMillis() + ": " + msg);
		
		if (Config.isLoggerEnabled()) 
			g_logger.log(Level.INFO, msg);
		
	}
	
	/**
	 * Helper for determining if a header has BiKeys
	 *
	 * @param array $header
	 * @return platform name on success, false otherwise
	 **/
	protected String hasBiKeys(Map<String, String> headers) 
	{
		Map<String, List<List<String>>> biMap = (Map<String, List<List<String>>>) detectionConfigMap.get("device-bi-order").get(0);

		Map<String, String> headersMap = new HashMap<String, String>();
		for (Entry<String, String> entry : headers.entrySet()) 
			headersMap.put(entry.getKey().toLowerCase(), null == entry.getValue() ? "" : entry.getValue().toLowerCase());

		Set<String> dataKeys = headersMap.keySet();
		
		// Fast check
		if (!HDUtil.isNullOrEmpty(headersMap.get("agent")))
			return null;
		if (!HDUtil.isNullOrEmpty(headersMap.get("user-agent")))
			return null;
		
		for (Entry<String, List<List<String>>> entry : biMap.entrySet()) 
		{
			for (List<String> tuple : entry.getValue()) 
			{
				int count = 0;
				int total = tuple.size();			// should be 2 I guess
				for (String item : tuple) 
				{
					if (dataKeys.contains(item))
						count++;
					if (count == total)
						return entry.getKey();		// platform
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets the authorization header.
	 *
	 * @param requestUrl the request url
	 * @return the authorization header
	 * @throws Exception the exception
	 */
	private synchronized String getAuthorizationHeader(URL requestUrl) 
			throws Exception 
	{
		String nc = "00000001";
		String snonce = realm;
		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis()).append(secret);
		String cnonce = HDUtil.md5(sb.toString());
		String qop = "auth";
		sb.setLength(0);
		sb.append(username).append(":").append(realm).append(":").append(secret);
		String ha1 = HDUtil.md5(sb.toString());
		sb.setLength(0);
		sb.append("POST:").append(requestUrl.getPath());
		String ha2 = HDUtil.md5(sb.toString());
		sb.setLength(0);
		sb.append(ha1).append(":").append(snonce).append(":").append(nc)
				.append(":").append(cnonce).append(":").append(qop).append(":")
				.append(ha2);
		String response = HDUtil.md5(sb.toString());
		sb.setLength(0);
		sb.append("Digest username=\"").append(username)
				.append("\", realm=\"").append(realm).append("\", nonce=\"")
				.append(snonce).append("\", uri=\"")
				.append(requestUrl.getPath()).append("\", qop=").append(qop)
				.append(", nc=").append(nc).append(", cnonce=\"")
				.append(cnonce).append("\", response=\"").append(response)
				.append("\", opaque=\"").append(realm).append("\"");
		return sb.toString();
	}

	/**
	 * Gets the basic proxy pass.
	 *
	 * @return the basic proxy pass
	 */
	String getBasicProxyPass() {
		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");
		sb.append(new String(Base64.encodeBase64((new String(proxyUsername + ":" + proxyPassword)).getBytes())));
		return sb.toString();
	}

	/**
	 * Find a branch for the matching process
 	 *
	 * @param string $branch The name of the branch to find
	 * @return an assoc array on success, false otherwise.
	 */
	protected JsonElement getBranch(String branch) 
	{
		if (!HDUtil.isNullElement(this.tree.get(branch)))
			return this.tree.get(branch);
		
		JsonElement tmp = this.store.read(branch);
		if (null != tmp)
		{
			this.tree.put(branch, tmp);
			return tmp;
		}
		return null;
	}
	
	/**
	 * Gets the files directory.
	 *
	 * @return the files directory
	 */
	protected boolean getFilesDirectory() {
		File f = new File(this.localFilesDirectory);
		return f.exists() ? false : f.mkdir();
	}

	/**
	 * Get reply status
	 *
	 * @param void
	 * @return int error status, 0 is Ok, anything else is probably not Ok
	 **/
	public int getStatus() {
		return reply.get("status").getAsInt();
	}
	/**
	 * Get reply message
	 *
	 * @param void
	 * @return string A message
	 **/
	public String getMessage() {
		return reply.get("message").getAsString();
	}
	
	/**
	 * Get reply payload in array assoc format
	 *
	 * @param void
	 * @return array
	 **/
	public JsonObject getReply() {
		return reply;
	}
	
	/**
	 * Find a device by its id
	 *
	 * @param string $_id
	 * @return array device on success, false otherwise
	 **/
	protected JsonElement findById(Integer deviceId) 
	{
		return this.store.read("Device_" + String.valueOf(deviceId));
	}
	
	/**
	 * Internal helper for building a list of all devices.
	 *
	 * @param void
	 * @return array List of all devices.
	 */
	protected JsonElement fetchDevices() 
	{
		JsonElement result = this.store.fetchDevices();
		if (HDUtil.isNullElement(result))
			setError(299, "Error : fetchDevices cannot read files from store.");
		return result;
	}

	/**
	 * Error handling helper. Sets a message and an error code.
	 *
	 * @param int status
	 * @param string msg
	 * @return true if no error, or false otherwise.
	 **/
	protected boolean setError(Integer status, String msg) 
	{
		error = msg;
		reply.addProperty("status", status);
		reply.addProperty("message", msg);
		
		return status > 0 ? false : true;
	}

	/**
	 * UDP Syslog via https://gist.github.com/troy/2220679 - Thanks Troy
	 *
	 * Send a message via UDP, used if log_unknown is set in config && running in Ultimate (local) mode.
	 *
	 * @param array $headers
	 * @return void
	 **/
	protected void send_remote_syslog(Map<String, String> headers)
	{
		headers.put("version", System.getProperty("java.version"));
		headers.put("apikit", apikit);
		DatagramSocket sock = null;
		try 
		{
			sock = new DatagramSocket();
			sock.connect(InetAddress.getByName(loggerHost), loggerPort);
			byte[] buf = new Gson().toJson(headers, new TypeToken<Map <String, String>>() {}.getType()).getBytes(Charsets.UTF_8);
			DatagramPacket dp = new DatagramPacket(buf, buf.length);
			sock.send(dp);
		
		} catch (IOException e) {
		
			log("Failed remote logging: " + e.getMessage());
		
		} finally {
			
			if (null != sock)
				sock.close();
		}
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
	public String getError() { return error; }
	
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
	 * Sets the directory.
	 *
	 * @param directory the new directory
	 */
	public void setLocalFilesDirectory(String directory) { this.localFilesDirectory = directory; }
	
	/**
	 * Sets the realm.
	 *
	 * @param realm the new realm
	 */
	public void setRealm(String realm) { this.realm = realm; }
	
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
		
}