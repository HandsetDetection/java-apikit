package hdapi3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * A class of loading HDAPI configuration settings. 
 */
public class Settings extends Properties{
	private final static String DEFAULT_COMMON_API_SERVER = "http://api.handsetdetection.com";
	private final static String DEFAULT_COMMON_LOG_SERVER = "http://log.handsetdetection.com";
	private final static String DEFAULT_COMMON_NON_MOBILE = "/(^Feedfetcher|^FAST|^gsa_crawler|^Crawler|^goroam|^GameTracker|^http://|^Lynx|^Link|^LegalX|libwww|^LWP::Simple|FunWebProducts|^Nambu|^WordPress|^yacybot|^YahooFeedSeeker|^Yandex|^MovableType|^Baiduspider|SpamBlockerUtility|AOLBuild|Link Checker|Media Center|Creative ZENcast|GoogleToolbar|MEGAUPLOAD|Alexa Toolbar|^User-Agent|SIMBAR|Wazzup|PeoplePal|GTB5|Dealio Toolbar|Zango|MathPlayer|Hotbar|Comcast Install|WebMoney Advisor|OfficeLiveConnector|IEMB3|GTB6|Avant Browser|America Online Browser|SearchSystem|WinTSI|FBSMTWB|NET_lghpset)/";
	private final static String DEFAULT_COMMON_MATCH_FILTER = " |_|\\\\|\\#|-|,|\\.|/|:|\"|'|";
	
	public final static String COMMON_USERNAME = "common.username";
	public final static String COMMON_SECRET = "common.secret";
	public final static String COMMON_API_SERVER = "common.api_server";
	public final static String COMMON_LOG_SERVER = "common.log_server";
	public final static String COMMON_SITE_ID = "common.site_id";
	public final static String COMMON_MOBILE_SITE = "common.mobile_site";
	public final static String COMMON_USE_LOCAL = "common.use_local"; 
	public final static String COMMON_NON_MOBILE = "common.non_mobile";
	public final static String COMMON_MATCH_FILTER = "common.match_filter";
		
	public final static String CONNECT_TIMEOUT = "connection.connect_timeout";
	public final static String READ_TIMEOUT = "connection.read_timeout";

	public final static String LOCAL_FILES_DIRECTORY = "local.files.directory";
	public final static String PROXY_ENABLED = "proxy.enabled";
	public final static String PROXY_ADDRESS = "proxy.address";
	public final static String PROXY_PORT = "proxy.port";
	public final static String PROXY_USERNAME = "proxy.username";
	public final static String PROXY_PASSWORD = "proxy.password";
	public final static String CONFIG_NAME = "hdapi_config.properties";
	private static final long serialVersionUID = 2L;
	private static Settings g_instance;

	/**
	 * Load setting from default classpath root.
	 * @return
	 * @throws IOException
	 */
	public synchronized static boolean init() throws IOException
	{
		if (g_instance == null) {
			try {
				URL url = ClassLoader.getSystemResource(CONFIG_NAME);				
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
	 * @param is InputStream object
	 * @return
	 * @throws IOException
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
	 * @return
	 */
	public static String getProxyAddress() {
		return g_instance.getProperty(PROXY_ADDRESS, "");
	}
	
	/**
	 * Getter of proxy port setting : proxy.port . 
	 * @return
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
	 * @return
	 */
	public static String getProxyUsername() {
		return g_instance.getProperty(PROXY_USERNAME, "");
	}
	
	/**
	 * Getter of proxy password for basic authentication: proxy.password
	 * @return
	 */
	public static String getProxyPassword() {
		return g_instance.getProperty(PROXY_PASSWORD, "");
	}
	
	
	/**
	 * Getter of API username setting: common.username
	 * @return
	 */
	public static String getUsername() {
		return g_instance.getProperty(COMMON_USERNAME, "");
	}
	
	/**
	 * Getter of API secret setting: common.secret
	 * @return
	 */
	public static String getSecret() {
		return g_instance.getProperty(COMMON_SECRET, "");
	}
	
	/**
	 * Getter of site id setting: common.site_id
	 * @return
	 */
	public static String getSiteId() {
		return g_instance.getProperty(COMMON_SITE_ID, "");
	}
	
	/**
	 * Getter of URL of API server setting: common.api_server  . The value of this setting can be one URL or multiple URLs separated by comma.
	 * @return
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
	 * @return
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
	 * @return
	 */
	public static int getConnectTimeout() {
		return Integer.parseInt(g_instance.getProperty(CONNECT_TIMEOUT, "5"));
	}
	
	/**
	 * Getter of socket read timeout setting: connection.read_timeout
	 * @return
	 */
	public static int getReadTimeout() {
		return Integer.parseInt(g_instance.getProperty(READ_TIMEOUT, "5"));
	}
	
	public static String getMobileSite() {
		String v = g_instance.getProperty(COMMON_MOBILE_SITE, "");
		return v;
	}
	
	public static void main(String[] args)
	{
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
