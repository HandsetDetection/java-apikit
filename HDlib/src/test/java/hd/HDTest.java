package hd;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import api.hd.HD;
import api.hd.HDStore;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@Ignore
public class HDTest 
{	
	private final Gson gson = new Gson();
	private FileInputStream fis;
	private String cloudConfig = "hd4CloudConfig.properties";
	private String ultimateConfig = "hd4UltimateConfig.properties";
	private SortedMap<String, String> device;

	private HD hd;
	private boolean result;
	private JsonObject reply;	

	private String nokiaN95 = "{"+ 
			"\"general_vendor\":\"Nokia\","+
			"\"general_model\":\"N95\","+
			"\"general_platform\":\"Symbian\","+
			"\"general_platform_version\":\"9.2\","+
			"\"general_browser\":\"\","+
			"\"general_browser_version\":\"\","+
			"\"general_image\":\"nokian95-1403496370-0.gif\","+
			"\"general_aliases\":[],"+
			"\"general_eusar\":\"0.50\","+
			"\"general_battery\":[\"Li-Ion 950 mAh\",\"BL-5F\"],"+
			"\"general_type\":\"Mobile\","+
			"\"general_cpu\":[\"Dual ARM 11\",\"332Mhz\"],"+
			"\"design_formfactor\":\"Dual Slide\","+
			"\"design_dimensions\":\"99 x 53 x 21\","+
			"\"design_weight\":\"120\","+
			"\"design_antenna\":\"Internal\","+
			"\"design_keyboard\":\"Numeric\","+
			"\"design_softkeys\":\"2\","+
			"\"design_sidekeys\":[\"Volume\",\"Camera\"],"+
			"\"display_type\":\"TFT\","+
			"\"display_color\":\"Yes\","+
			"\"display_colors\":\"16M\","+
			"\"display_size\":\"2.6\\\"\","+								// catalin: probably bad server jsoning
			"\"display_x\":\"240\","+
			"\"display_y\":\"320\","+
			"\"display_other\":[],"+
			"\"memory_internal\":[\"160MB\",\"64MB RAM\",\"256MB ROM\"],"+
			"\"memory_slot\":[\"microSD\",\"8GB\",\"128MB\"],"+
			"\"network\":[\"GSM850\",\"GSM900\",\"GSM1800\",\"GSM1900\",\"UMTS2100\",\"HSDPA2100\",\"Infrared\",\"Bluetooth 2.0\",\"802.11b\",\"802.11g\",\"GPRS Class 10\",\"EDGE Class 32\"],"+
			"\"media_camera\":[\"5MP\",\"2592x1944\"],"+
			"\"media_secondcamera\":[\"QVGA\"],"+
			"\"media_videocapture\":[\"VGA@30fps\"],"+
			"\"media_videoplayback\":[\"MPEG4\",\"H.263\",\"H.264\",\"3GPP\",\"RealVideo 8\",\"RealVideo 9\",\"RealVideo 10\"],"+
			"\"media_audio\":[\"MP3\",\"AAC\",\"AAC+\",\"eAAC+\",\"WMA\"],"+
			"\"media_other\":[\"Auto focus\",\"Video stabilizer\",\"Video calling\",\"Carl Zeiss optics\",\"LED Flash\"],"+
			"\"features\":[\"Unlimited entries\",\"Multiple numbers per contact\",\"Picture ID\",\"Ring ID\",\"Calendar\",\"Alarm\",\"To-Do\",\"Document viewer\","+
			"\"Calculator\",\"Notes\",\"UPnP\",\"Computer sync\",\"VoIP\",\"Music ringtones (MP3)\",\"Vibration\",\"Phone profiles\",\"Speakerphone\","+
			"\"Accelerometer\",\"Voice dialing\",\"Voice commands\",\"Voice recording\",\"Push-to-Talk\",\"SMS\",\"MMS\",\"Email\",\"Instant Messaging\","+
			"\"Stereo FM radio\",\"Visual radio\",\"Dual slide design\",\"Organizer\",\"Word viewer\",\"Excel viewer\",\"PowerPoint viewer\",\"PDF viewer\","+
			"\"Predictive text input\",\"Push to talk\",\"Voice memo\",\"Games\"],"+
			"\"connectors\":[\"USB\",\"miniUSB\",\"3.5mm AUdio\",\"TV Out\"],"+
			"\"general_platform_version_max\":\"\","+
			"\"general_app\":\"\","+
			"\"general_app_version\":\"\","+
			"\"general_language\":\"\","+
			"\"display_ppi\":154,"+
			"\"display_pixel_ratio\":\"1.0\","+
			"\"benchmark_min\":0,"+ 
			"\"benchmark_max\":0,"+
			"\"general_app_category\":\"\""+
			"}";

	@Before
	public void setUp() {		
		try {
//			fis = new FileInputStream("hdapi_config.properties"); 
//			Config.init(fis);					
//			fis.close();
			device = new TreeMap<String, String>();		
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	/**
	 * test for config file .. required for all cloud tests
	 * @group cloud
	 **/
	@Test 
	public void test_0cloud_0cloudConfigExists() {
		assertEquals(true, true);
	}	

	/**
	 * device vendors test
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1Vendors() throws IOException {
			hd = new HD(cloudConfig);
		result = hd.deviceVendors();
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)(String)JsonPath.read(gson.toJson(reply), "$.message"));		
		assertThat((List<String>) JsonPath.read(gson.toJson(reply), "$.vendor"), hasItem("Nokia"));
		assertThat((List<String>) JsonPath.read(gson.toJson(reply), "$.vendor"), hasItem("Samsung"));
	}
	/**
	 * device models test
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceModels() throws IOException 
	{
			hd = new HD(cloudConfig);
		result = hd.deviceModels("Nokia");
		reply = hd.getReply();
		assertTrue(result);
		assertTrue(700 < reply.get("model").getAsJsonArray().size());
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)(String)reply.get("message").getAsString());
	}
	/**
	 * device view test
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceView() throws IOException {
//		thrown.expect(IOException.class);
			hd = new HD(cloudConfig);
		result = hd.deviceView("Nokia", "N95");
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)(String)reply.get("message").getAsString());
		String devicesReply = gson.toJson(reply.get("device").getAsJsonObject()); 
//		System.out.println(nokiaN95.toLowerCase());
//		System.out.println(devicesReply.toLowerCase());
		assertEquals(nokiaN95.toLowerCase(), devicesReply.toLowerCase());
	}
	/**
	 * device whatHas test
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/		
	@Test
	public void test_0cloud_1DeviceDeviceWhatHas() throws IOException {
			hd = new HD(cloudConfig);
		result = hd.deviceWhatHas("design_dimensions", "101 x 44 x 16");
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		String devicesReply = gson.toJson(reply.get("devices")); 
		assertEquals(true, devicesReply.contains("Asus"));
		assertEquals(true, devicesReply.contains("V80"));
		assertEquals(true, devicesReply.contains("Spice"));
		assertEquals(true, devicesReply.contains("S900"));
		assertEquals(true, devicesReply.contains("Voxtel"));
		assertEquals(true, devicesReply.contains("RX800"));
	}
	/**
	 * Detection test Windows PC running Chrome
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/		
	@Test
	public void test_0cloud_1DeviceDetectHTTPDesktop() throws IOException {
			hd = new HD(cloudConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)(String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Computer", (String)(String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * Detection test Junk user-agent
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/		
	@Test
	public void test_0cloud_1DeviceDetectHTTPDesktopJunk() throws IOException {
			hd = new HD(cloudConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "aksjakdjkjdaiwdidjkjdkawjdijwidawjdiajwdkawdjiwjdiawjdwidjwakdjajdkad" + System.currentTimeMillis());
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertFalse(result);
		assertEquals((Integer)301, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("Not Found", (String)JsonPath.read(gson.toJson(reply), "$.message"));
	}
	/**
	 * Detection test Wii
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTPWii() throws IOException {
			hd = new HD(cloudConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Opera/9.30 (Nintendo Wii; U; ; 2047-7; es-Es)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Console", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * Detection test iPhone
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTP() throws IOException {
		hd = new HD(cloudConfig);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.3", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Detection test iPhone in weird headers
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTPOtherHeader() throws IOException {
		hd = new HD(cloudConfig);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("user-agent", "blahblahblah");
		headers.put("x-fish-header", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.3", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Detection test iPhone 3GS (same UA as iPhone 3G, different x-local-hardwareinfo header)
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTPHardwareInfo() throws IOException {
		hd = new HD(cloudConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:100:100");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3GS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.2.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Detection test iPhone 3G (same UA as iPhone 3GS, different x-local-hardwareinfo header)
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTPHardwareInfoB() throws IOException {
		hd = new HD(cloudConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:100:72");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3G", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.2.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
//		assertThat((List<Strig>) gson.fromJson(reply.get("hd_specs"), new TypeToken<List<String>>() {}.getType()), hasItem("display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Detection test iPhone - Crazy benchmark (eg from emulated desktop) with outdated OS
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTPHardwareInfoC() throws IOException {
		hd = new HD(cloudConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:200:1200");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3G", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("2.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Detection test iPhone 5s running Facebook 9.0 app (hence no general_browser set).
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectHTTPFBiOS() throws IOException {
		
		hd = new HD(cloudConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D201 [FBAN/FBIOS;FBAV/9.0.0.25.31;FBBV/2102024;FBDV/iPhone6,2;FBMD/iPhone;FBSN/iPhone OS;FBSV/7.1.1;FBSS/2; FBCR/vodafoneIE;FBID/phone;FBLC/en_US;FBOP/5]");
			headers.put("Accept-Language", "da, en-gb;q=0.8, en;q=0.7");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 5S", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("7.1.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("da", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Danish", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language_full"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Facebook", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_app"));
		assertEquals("9.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_app_version"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_browser"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_browser_version"));
		
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Detection test Samsung GT-I9500 Native - Note : Device shipped with Android 4.2.2, so this device has been updated.
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectBIAndroid() throws IOException {
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
		hd = new HD(cloudConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Samsung", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("GT-I9500", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("Android", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		//assertEquals("4.4.2", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("Samsung Galaxy S4", (String)JsonPath.read(gson.toJson(reply), "hd_specs.general_aliases[0]"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * Detection test iPhone 4S Native
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectBIiOS() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("utsname.machine", "iphone4,1");
			headers.put("utsname.brand", "Apple");
		hd = new HD(cloudConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 4S", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		// Note : Default shipped version in the absence of any version information
		assertEquals("5.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	
	/**
	 * Detection test Windows Phone Native Nokia Lumia 1020
	 * @throws IOException 
	 * @depends test_cloudConfigExists
	 * @group cloud
	 **/
	@Test
	public void test_0cloud_1DeviceDetectWindowsPhone() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("devicemanufacturer", "nokia");
			headers.put("devicename", "RM-875");
		hd = new HD(cloudConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		assertEquals("Nokia", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("Lumia 1020", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("Windows Phone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals((Integer)332, (Integer)JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
	}
	// ***************************************************************************************************
	// ***************************************** Ultimate Tests ******************************************
	// ***************************************************************************************************
	/**
	 * Fetch Archive Test
	 * @throws IOException 
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_0fetchArchive() throws IOException {		
		// Note : request storage dir to be created if it does not exist. (with TRUE as 2nd param)
		hd = new HD(ultimateConfig);
		result = hd.deviceFetchArchive();
		assertTrue(result);
		byte[] data = hd.getRawReply();
		System.out.println("Downloaded " + data.length + " bytes");
		assertTrue(19000000 < data.length);		// Filesize greater than 19Mb (currently 21Mb).
	}
	/**
	 * device vendors test
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceVendors() throws IOException {
		hd = new HD(cloudConfig);
		result = hd.deviceVendors();
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertThat((List<String>) JsonPath.read(gson.toJson(reply), "$.vendor"), hasItem("Nokia"));
		assertThat((List<String>) JsonPath.read(gson.toJson(reply), "$.vendor"), hasItem("Samsung"));
	}
	/**
	 * device models test
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceModels() throws IOException {
		hd = new HD(cloudConfig);
		result = hd.deviceModels("Nokia");
		reply = hd.getReply();
		assertTrue(result);
		assertTrue(700 < reply.get("model").getAsJsonArray().size());
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
	}
	/**
	 * device view test
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceView() throws IOException {
		hd = new HD(cloudConfig);
		result = hd.deviceView("Nokia", "N95");
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		String devicesReply = gson.toJson(reply.get("device"));
//		Collections.sort(devicesReply);
		
//		System.out.println(gson.toJson(data["device"], new TypeToken<Map<String, String>>() {}.getType()));
//		System.out.println(gson.toJson(devices["NokiaN95"], new TypeToken<Map<String, String>>() {}.getType()));
		assertEquals(nokiaN95.toLowerCase(), devicesReply.toLowerCase());
	}
	/**
	 * device whatHas test
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDeviceWhatHas() throws IOException {
		hd = new HD(cloudConfig);
		result = hd.deviceWhatHas("design_dimensions", "101 x 44 x 16");
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)reply.get("status").getAsInt());
		assertEquals("OK", (String)reply.get("message").getAsString());
		String devicesReply = gson.toJson(reply.get("devices")); 
		assertEquals(true, devicesReply.contains("Asus"));
		assertEquals(true, devicesReply.contains("V80"));
		assertEquals(true, devicesReply.contains("Spice"));
		assertEquals(true, devicesReply.contains("S900"));
		assertEquals(true, devicesReply.contains("Voxtel"));
		assertEquals(true, devicesReply.contains("RX800"));
	}
	
	/**
	 * Windows PC running Chrome
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPDesktop() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Computer", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * Junk user-agent
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPDesktopJunk() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "aksjakdjkjdaiwdidjkjdkawjdijwidawjdiajwdkawdjiwjdiawjdwidjwakdjajdkad" + System.currentTimeMillis());
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertFalse(result);
		assertEquals((Integer)301, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("Not Found", (String)JsonPath.read(gson.toJson(reply), "$.message"));
	}
	/**
	 * Wii
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPWii() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Opera/9.30 (Nintendo Wii; U; ; 2047-7; es-Es)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Console", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * iPhone
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTP() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.3", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone - user-agent in random other header
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPOtherHeader() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "blahblahblah");
			headers.put("x-fish-header", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.3", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone 3GS (same UA as iPhone 3G, different x-local-hardwareinfo header)
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPHardwareInfo() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:100:100");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3GS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.2.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone 3G (same UA as iPhone 3GS, different x-local-hardwareinfo header)
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPHardwareInfoB() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:100:72");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3G", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.2.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone - Crazy benchmark (eg from emulated desktop) with outdated OS
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPHardwareInfoC() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:200:1200");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3G", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("2.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone 5s running Facebook 9.0 app (hence no general_browser set).
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectHTTPFBiOS() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D201 [FBAN/FBIOS;FBAV/9.0.0.25.31;FBBV/2102024;FBDV/iPhone6,2;FBMD/iPhone;FBSN/iPhone OS;FBSV/7.1.1;FBSS/2; FBCR/vodafoneIE;FBID/phone;FBLC/en_US;FBOP/5]");
			headers.put("Accept-Language", "da, en-gb;q=0.8, en;q=0.7");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 5S", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("7.1.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("da", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Danish", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language_full"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Facebook", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_app"));
		assertEquals("9.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_app_version"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_browser"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_browser_version"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Samsung GT-I9500 Native - Note : Device shipped with Android 4.2.2, so this device has been updated.
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectBIAndroid() throws IOException {
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
		hd = new HD(ultimateConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertEquals("Samsung", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("GT-I9500", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("Android", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		//assertEquals("4.4.2", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("Samsung Galaxy S4", (String)JsonPath.read(gson.toJson(reply), "hd_specs.general_aliases[0]"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	//
	/**
	 * iPhone 4S Native
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectBIiOS() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("utsname.machine", "iphone4,1");
			headers.put("utsname.brand", "Apple");
		hd = new HD(ultimateConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 4S", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		// Note : Default shipped version in the absence of any version information
		assertEquals("5.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	//
	/**
	 * Windows Phone Native Nokia Lumia 1020
	 * @throws IOException 
	 * @depends test_fetchArchive
	 * @group ultimate
	 **/
	@Test
	public void test_1ultimate_1DeviceDetectWindowsPhone() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("devicemanufacturer", "nokia");
			headers.put("devicename", "RM-875");
		hd = new HD(ultimateConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertEquals("Nokia", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("Lumia 1020", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("Windows Phone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("Mobile", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals((Integer)332, (Integer)JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
	}

	// Remove ultimate edition
	@Test
	public void test_1ultimate_2Purge()
	{
		HDStore store = HDStore.getInstance();
		store.purge();
	}
	
	// ***************************************************************************************************
	// *********************************** Ultimate Community Tests **************************************
	// ***************************************************************************************************
	/**
	 * Fetch Archive Test
	 *
	 * The community fetchArchive version contains a cut down version of the device specs.
	 * It has general_vendor, general_model, display_x, display_y, general_platform, general_platform_version,
	 * general_browser, general_browser_version, general_app, general_app_version, general_language,
	 * general_language_full, benahmark_min & benchmark_max
	 * @throws IOException 
	 *
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_0fetchArchive() throws IOException {
		// Note : request storage dir to be created if it does not exist. (with TRUE as 2nd param)
		hd = new HD(ultimateConfig);
		result = hd.communityFetchArchive();
		assertTrue(result);
		byte[] data = hd.getRawReply();
		System.out.println("Downloaded " + data.length + " bytes");
		assertTrue(9000000 < data.length);		// Filesize greater than 9Mb.
	}
	/**
	 * Windows PC running Chrome
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPDesktop() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * Junk user-agent
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPDesktopJunk() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "aksjakdjkjdaiwdidjkjdkawjdijwidawjdiajwdkawdjiwjdiawjdwidjwakdjajdkad" + System.currentTimeMillis());
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertFalse(result);
		assertEquals((Integer)301, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("Not Found", (String)JsonPath.read(gson.toJson(reply), "$.message"));
	}
	/**
	 * Wii
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPWii() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Opera/9.30 (Nintendo Wii; U; ; 2047-7; es-Es)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	/**
	 * iPhone
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTP() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String>headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.3", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone - user-agent in random other header
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPOtherHeader() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "blahblahblah");
			headers.put("x-fish-header", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.status"));
		assertEquals("OK", (String)JsonPath.read(gson.toJson(reply), "$.message"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.3", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone 3GS (same UA as iPhone 3G, different x-local-hardwareinfo header)
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPHardwareInfo() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:100:100");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3GS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.2.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone 3G (same UA as iPhone 3GS, different x-local-hardwareinfo header)
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPHardwareInfoB() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:100:72");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3G", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("4.2.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone - Crazy benchmark (eg from emulated desktop) with outdated OS
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPHardwareInfoC() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 2_0 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
			headers.put("x-local-hardwareinfo", "320:480:200:1200");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 3G", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("2.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("en-gb", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * iPhone 5s running Facebook 9.0 app (hence no general_browser set).
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectHTTPFBiOS() throws IOException {
		hd = new HD(ultimateConfig);
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D201 [FBAN/FBIOS;FBAV/9.0.0.25.31;FBBV/2102024;FBDV/iPhone6,2;FBMD/iPhone;FBSN/iPhone OS;FBSV/7.1.1;FBSS/2; FBCR/vodafoneIE;FBID/phone;FBLC/en_US;FBOP/5]");
			headers.put("Accept-Language", "da, en-gb;q=0.8, en;q=0.7");
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		//print_r(reply);
		assertTrue(result);
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 5S", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("7.1.1", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("da", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language"));
		assertEquals("Danish", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_language_full"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals("Facebook", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_app"));
		assertEquals("9.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_app_version"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_browser"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_browser_version"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_pixel_ratio"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_min"));
		assertTrue(null != JsonPath.read(gson.toJson(reply), "$.hd_specs.benchmark_max"));
	}
	/**
	 * Samsung GT-I9500 Native - Note : Device shipped with Android 4.2.2, so this device has been updated.
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectBIAndroid() throws IOException {
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
		hd = new HD(ultimateConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertEquals("Samsung", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("GT-I9500", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("Android", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
//		assertEquals("4.4.2", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "hd_specs.general_aliases"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));										
	}
	// 
	/**
	 * iPhone 4S Native
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectBIiOS() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
			headers.put("utsname.machine", "iphone4,1");
			headers.put("utsname.brand", "Apple");
		hd = new HD(ultimateConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertEquals("Apple", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("iPhone 4S", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("iOS", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		// Note : Default shipped version in the absence of any version information
		assertEquals("5.0", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform_version"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
	}
	// 
	/**
	 * Windows Phone Native Nokia Lumia 1020
	 * @throws IOException 
	 * @depends test_ultimate_community_fetchArchive
	 * @group community
	 **/
	@Test
	public void test_2ultimateCommunity_1DeviceDetectWindowsPhone() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("devicemanufacturer", "nokia");
		headers.put("devicename", "RM-875");
		hd = new HD(ultimateConfig);
		result = hd.deviceDetect(headers);
		reply = hd.getReply();
		assertEquals("Nokia", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_vendor"));
		assertEquals("Lumia 1020", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_model"));
		assertEquals("Windows Phone", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_platform"));
		assertEquals("", (String)JsonPath.read(gson.toJson(reply), "$.hd_specs.general_type"));
		assertEquals((Integer)0, (Integer)JsonPath.read(gson.toJson(reply), "$.hd_specs.display_ppi"));
	}	
	
	// Remove ultimate community edition
	@Test
	public void test_2ultimateCommunity_2Purge()
	{
		HDStore store = HDStore.getInstance();
		store.purge();
	}

}

