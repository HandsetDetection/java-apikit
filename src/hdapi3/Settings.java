package hdapi3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * A class of loading HDAPI configuration settings. 
 */
public class Settings extends Properties{
	
	/** The Constant DEFAULT_COMMON_API_SERVER. */
	private final static String DEFAULT_COMMON_API_SERVER = "http://api.handsetdetection.com";
	
	/** The Constant DEFAULT_COMMON_LOG_SERVER. */
	private final static String DEFAULT_COMMON_LOG_SERVER = "http://log.handsetdetection.com";
	
	/** The Constant DEFAULT_COMMON_NON_MOBILE. */
	private final static String DEFAULT_COMMON_NON_MOBILE = "/(^Feedfetcher|^FAST|^gsa_crawler|^Crawler|^goroam|^GameTracker|^http://|^Lynx|^Link|^LegalX|libwww|^LWP::Simple|FunWebProducts|^Nambu|^WordPress|^yacybot|^YahooFeedSeeker|^Yandex|^MovableType|^Baiduspider|SpamBlockerUtility|AOLBuild|Link Checker|Media Center|Creative ZENcast|GoogleToolbar|MEGAUPLOAD|Alexa Toolbar|^User-Agent|SIMBAR|Wazzup|PeoplePal|GTB5|Dealio Toolbar|Zango|MathPlayer|Hotbar|Comcast Install|WebMoney Advisor|OfficeLiveConnector|IEMB3|GTB6|Avant Browser|America Online Browser|SearchSystem|WinTSI|FBSMTWB|NET_lghpset)/";
	
	/** The Constant DEFAULT_COMMON_MATCH_FILTER. */
	private final static String DEFAULT_COMMON_MATCH_FILTER = " |_|\\\\|\\#|-|,|\\.|/|:|\"|'|";
	
	/** The Constant COMMON_USERNAME. */
	public final static String COMMON_USERNAME = "common.username";
	
	/** The Constant COMMON_SECRET. */
	public final static String COMMON_SECRET = "common.secret";
	
	/** The Constant COMMON_API_SERVER. */
	public final static String COMMON_API_SERVER = "common.api_server";
	
	/** The Constant COMMON_LOG_SERVER. */
	public final static String COMMON_LOG_SERVER = "common.log_server";
	
	/** The Constant COMMON_SITE_ID. */
	public final static String COMMON_SITE_ID = "common.site_id";
	
	/** The Constant COMMON_MOBILE_SITE. */
	public final static String COMMON_MOBILE_SITE = "common.mobile_site";
	
	/** The Constant COMMON_USE_LOCAL. */
	public final static String COMMON_USE_LOCAL = "common.use_local"; 
	
	/** The Constant COMMON_NON_MOBILE. */
	public final static String COMMON_NON_MOBILE = "common.non_mobile";
	
	/** The Constant COMMON_MATCH_FILTER. */
	public final static String COMMON_MATCH_FILTER = "common.match_filter";
		
	/** The Constant CONNECT_TIMEOUT. */
	public final static String CONNECT_TIMEOUT = "connection.connect_timeout";
	
	/** The Constant READ_TIMEOUT. */
	public final static String READ_TIMEOUT = "connection.read_timeout";

	/** The Constant LOCAL_FILES_DIRECTORY. */
	public final static String LOCAL_FILES_DIRECTORY = "local.files.directory";
	
	/** The Constant PROXY_ENABLED. */
	public final static String PROXY_ENABLED = "proxy.enabled";
	
	/** The Constant PROXY_ADDRESS. */
	public final static String PROXY_ADDRESS = "proxy.address";
	
	/** The Constant PROXY_PORT. */
	public final static String PROXY_PORT = "proxy.port";
	
	/** The Constant PROXY_USERNAME. */
	public final static String PROXY_USERNAME = "proxy.username";
	
	/** The Constant PROXY_PASSWORD. */
	public final static String PROXY_PASSWORD = "proxy.password";
	
	/** The Constant CONFIG_NAME. */
	public final static String CONFIG_NAME = "hdapi_config.properties";
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2L;
	
	/** The g_instance. */
	private static Settings g_instance;

	/**
	 * Load setting from default classpath root.
	 *
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	@SuppressWarnings("deprecation")
	public synchronized static boolean init() throws IOException, Exception
	{
		if (g_instance == null) {
			try {
				File config_file = new File(CONFIG_NAME);
				URL url = config_file.toURL(); //ClassLoader.getSystemResource(CONFIG_NAME);						
				g_instance = new Settings();								
				if (url == null) {
					return false;
				}
				g_instance.load(url.openStream());				
			} catch (IOException ie) {
				throw ie;
			}
		}
		return true;
	}
	
	/**
	 * Load setting from a specific java.io.InputStream object.
	 *
	 * @param is InputStream object
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public synchronized static boolean init(InputStream is) throws IOException
	{
		if (g_instance == null) {
			try {
				g_instance = new Settings();
				g_instance.load(is);
			} catch (IOException ie) {
				g_instance = null;
				throw ie;
			}
		}
		return true;
	}
	/**
	 * Getter of local files directory setting: local.files.directory . 
	 * @return the directory where locally stored files are expected to be found (tress, specs JSON files)
	 *         <code>null</code> is returned if local.files.directory is not configured.
	 */
	public static String getLocalFilesDirectory() {
		return g_instance.getProperty(LOCAL_FILES_DIRECTORY);
	}
	
	/**
	 * Getter of use proxy setting: proxy.enabled .
	 * @return Return true if use_proxy is not 0. Return false if setting value is 0.
	 */
	public static boolean isUseProxy() {
		return Boolean.parseBoolean(g_instance.getProperty(PROXY_ENABLED, "false"));
	}
	
	/**
	 * Getter of proxy server address setting: proxy.address . 
	 *
	 * @return the proxy address
	 */
	public static String getProxyAddress() {
		return g_instance.getProperty(PROXY_ADDRESS, "");
	}
	
	/**
	 * Getter of proxy port setting : proxy.port . 
	 *
	 * @return the proxy port
	 */
	public static int getProxyPort() {
		int port = 80;
		try {
			String sPort = g_instance.getProperty(PROXY_PORT, "80");
			if (HD3Util.isNullOrEmpty(sPort)) return port;
			port = Integer.parseInt(sPort);
		} catch (NumberFormatException nfe) {
			
		}
		return port;
	}
	
	/**
	 * Getter of proxy username for basic authentication: proxy.username.
	 *
	 * @return the proxy username
	 */
	public static String getProxyUsername() {
		return g_instance.getProperty(PROXY_USERNAME, "");
	}
	
	/**
	 * Getter of proxy password for basic authentication: proxy.password
	 *
	 * @return the proxy password
	 */
	public static String getProxyPassword() {
		return g_instance.getProperty(PROXY_PASSWORD, "");
	}
	
	
	/**
	 * Getter of API username setting: common.username
	 *
	 * @return the username
	 */
	public static String getUsername() {
		return g_instance.getProperty(COMMON_USERNAME, "");
	}
	
	/**
	 * Getter of API secret setting: common.secret
	 *
	 * @return the secret
	 */
	public static String getSecret() {
		return g_instance.getProperty(COMMON_SECRET, "");
	}
	
	/**
	 * Getter of site id setting: common.site_id
	 *
	 * @return the site id
	 */
	public static String getSiteId() {
		return g_instance.getProperty(COMMON_SITE_ID, "");
	}
	
	/**
	 * Getter of URL of API server setting: common.api_server  . The value of this setting can be one URL or multiple URLs separated by comma.
	 *
	 * @return the api server
	 */
	public static String getApiServer() {
		String v = g_instance.getProperty(COMMON_API_SERVER, DEFAULT_COMMON_API_SERVER);
		if (HD3Util.isNullOrEmpty(v)) {
			v = DEFAULT_COMMON_API_SERVER;
		}
		return v;
	}
	
	/**
	 * Getter of URL of LOG server setting: common.log_server  . The value of this setting can be one URL or multiple URLs separated by comma.
	 *
	 * @return the log server
	 */
	public static String getLogServer() {
		String v = g_instance.getProperty(COMMON_LOG_SERVER, DEFAULT_COMMON_LOG_SERVER);
		if (HD3Util.isNullOrEmpty(v)) {
			v = DEFAULT_COMMON_LOG_SERVER;
		}
		return v;
	}
	
	/**
	 * Get non-mobile match expression: common.non_mobile.
	 *
	 * @return the non mobile
	 */
	public static String getNonMobile()
	{
		String v = g_instance.getProperty(COMMON_NON_MOBILE, DEFAULT_COMMON_NON_MOBILE);
		if (HD3Util.isNullOrEmpty(v)) {
			v = DEFAULT_COMMON_NON_MOBILE;
		}
		return v;
	}
	
	/**
	 * Get match filter expression: common.match_filter.
	 *
	 * @return the match filter
	 */
	public static String getMatchFilter()
	{
		String v = g_instance.getProperty(COMMON_MATCH_FILTER, DEFAULT_COMMON_MATCH_FILTER);
		if (HD3Util.isNullOrEmpty(v)) {
			v = DEFAULT_COMMON_MATCH_FILTER;
		}
		return v;
	}
	
	/**
	 * Check if it is set to detect by local cached detection data file.
	 * @return  true if use local detection service.
	 */
	public static boolean isUseLocal()
	{
		return Boolean.parseBoolean(g_instance.getProperty(COMMON_USE_LOCAL, "false"));
	}
	
	
	/**
	 * Getter of socket connect timeout setting: connection.connect_timeout
	 *
	 * @return the connect timeout
	 */
	public static int getConnectTimeout() {
		return Integer.parseInt(g_instance.getProperty(CONNECT_TIMEOUT, "5"));
	}
	
	/**
	 * Getter of socket read timeout setting: connection.read_timeout
	 *
	 * @return the read timeout
	 */
	public static int getReadTimeout() {
		return Integer.parseInt(g_instance.getProperty(READ_TIMEOUT, "5"));
	}
	
	/**
	 * Gets the mobile site.
	 *
	 * @return the mobile site
	 */
	public static String getMobileSite() {
		String v = g_instance.getProperty(COMMON_MOBILE_SITE, "");
		return v;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			Settings.init();
			System.out.println("common.username : " + Settings.getUsername());
			System.out.println("common.secret : " + Settings.getSecret());
			System.out.println("common.site_id : " + Settings.getSiteId());
			System.out.println("common.api_server : " + Settings.getApiServer());
			System.out.println("common.log_server : " + Settings.getLogServer());
			System.out.println("common.non_mobile : " + Settings.getNonMobile());
			System.out.println("common.match_filter : " + Settings.getMatchFilter());
			System.out.println("common.use_local : " + Settings.isUseLocal());
			System.out.println("connection.connect_timeout : " + Settings.getConnectTimeout());
			System.out.println("connection.read_timeout : " + Settings.getReadTimeout());
			System.out.println("proxy.enabled : " + Settings.isUseProxy());
			System.out.println("proxy.address : " + Settings.getProxyAddress());
			System.out.println("proxy.port : " + Settings.getProxyPort());
			System.out.println("proxy.username : " + Settings.getProxyUsername());
			System.out.println("proxy.password : " + Settings.getProxyPassword()); 			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
