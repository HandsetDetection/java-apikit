package hd3;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import hdapi3.HD3;
import hdapi3.HD3Util;
import hdapi3.Settings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HD3Test extends TestCase {	
	private FileInputStream fis;
	private HD3 hd3;		
	
	protected void setUp() {		
		try {
			fis = new FileInputStream("hdapi_config.properties"); 
			Settings.init(fis);		
			Settings.isUseLocal();
			fis.close();
			hd3 = new HD3();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	@Test
	public void testuserCredentials() {
		assertEquals(hd3.getUsername(), SecretConfig.getUserName("SECRET_NAME"));
		assertEquals(hd3.getUsername(), SecretConfig.getSecret("SECRET_KEY"));
		assertEquals(Settings.getSiteId(), SecretConfig.getSiteId("SITE_ID"));		
	}
	
	@Test
	public void testDeviceVendorsNokia() {				
		hd3.deviceVendors();		
		try {
			assertTrue( this.jsonArrayToList( getJson(), "vendor", null, "Nokia" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}

	@Test
	public void testDeviceVendorsLG() {				
		hd3.deviceVendors();		
		try {
			assertTrue( this.jsonArrayToList( getJson(), "vendor", null, "LG" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceVendorsSamsung() {				
		hd3.deviceVendors();	
		try {
			assertTrue( this.jsonArrayToList( getJson(), "vendor", null, "Samsung" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceModelsNokia() {
		hd3.deviceModels("Nokia");		
		try {
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "N95" ) );
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "3310i" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceModelsSamsung() {
		hd3.deviceModels("Samsung");
		try {			
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "ATIV Odyssey" ) );
			assertTrue( this.jsonArrayToList( getJson(), "model", null, "Galaxy Y Duos Lite" ) );
		} catch (JSONException e) {			
			e.printStackTrace();
		}	
	}
	
	@Test
	public void testDeviceViewNokiaLumia() {					 
		hd3.deviceView("Nokia", "Lumia 610 NFC");		
		try {			
			assertEquals(HD3Util.get("general_platform", hd3.getReply()).getAsString(), "Windows Phone");
			assertEquals(HD3Util.get("general_model", hd3.getReply()).getAsString(), "Lumia 610 NFC");		
			assertTrue( this.jsonArrayToList( getJson(), "device", "display_other", "Gorilla Glass"));	
			assertTrue( this.jsonArrayToList( getJson(), "device", "display_other", "Multitouch"));
		} catch (JSONException e) {			
			e.printStackTrace();
		}				
	}
	
	@Test
	public void testDeviceViewAppleIPhone5S() {					 
		hd3.deviceView("Apple", "iPhone 5S");
		assertEquals(HD3Util.get("design_dimensions", hd3.getReply()).getAsString(), "123.8 x 58.6 x 7.6");
		assertEquals(HD3Util.get("display_colors", hd3.getReply()).getAsString(), "16M");			
		try {			
			assertTrue( this.jsonArrayToList(getJson(), "device", "features", "Ambient Light Sensor") );
			assertTrue( this.jsonArrayToList(getJson(), "device", "connectors", "Lightning Connector") );
		} catch (JSONException e) {			
			e.printStackTrace();
		}				
	}
	
	@Test
	public void testDeviceWhatHasLG() {		
		hd3.deviceWhatHas("general_vendor", "LG");
		JsonObject jsonObj = hd3.getReply();
		JsonElement elem = jsonObj.get("devices").getAsJsonArray().get(0);
		String _id = elem.getAsJsonObject().get("id").getAsString();
		String _model = elem.getAsJsonObject().get("general_model").getAsString();
		assertEquals(_id, "4198");
		assertEquals(_model, "100");		
		elem = jsonObj.get("devices").getAsJsonArray().get(3);
		_id = elem.getAsJsonObject().get("id").getAsString();
		_model = elem.getAsJsonObject().get("general_model").getAsString();
		assertEquals(_id, "1572");
		assertEquals(_model, "145PRE");
	}
	
	@Test
	public void testDeviceWhatHasSony() {		
		hd3.deviceWhatHas("general_vendor", "Sony");
		JsonObject jsonObj = hd3.getReply();
		JsonElement elem = jsonObj.get("devices").getAsJsonArray().get(0);
		String _id = elem.getAsJsonObject().get("id").getAsString();
		String _model = elem.getAsJsonObject().get("general_model").getAsString();
		assertEquals(_id, "41983");
		assertEquals(_model, "C1504");		
		elem = jsonObj.get("devices").getAsJsonArray().get(7);
		_id = elem.getAsJsonObject().get("id").getAsString();
		_model = elem.getAsJsonObject().get("general_model").getAsString();
		assertEquals(_id, "44068");
		assertEquals(_model, "C2005");
	}
	
	@Test
	public void testSiteDetectBrowser() {
		hd3.addDetectVar("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
		hd3.siteDetect();
		try {
			JSONObject json = getJson();			
			assertEquals(json.getJSONObject("hd_specs").get("general_platform"), "Symbian");
			assertEquals(json.getJSONObject("hd_specs").get("general_platform_version"), "9.2");
			assertNotNull(json.getJSONObject("hd_specs").get("general_browser_version").toString());						
			assertTrue( this.jsonArrayToList(json, "hd_specs", "connectors", "USB") );
			assertTrue( this.jsonArrayToList(json, "hd_specs", "features", "Push to talk") );
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}

	@Test
	public void testSiteDetectXOperaMini() {
		hd3.addDetectVar("x-operamini-phone-ua", "Mozilla/5.0 (Linux; U;Android 2.1-update1; pt-br; U20a Build/2.1.1.A.0.6) AppleWebKit/530.17(KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
		hd3.siteDetect();
		try {
			JSONObject json = getJson();
			assertEquals(json.getJSONObject("hd_specs").get("general_platform"), "Android");
			assertEquals(json.getJSONObject("hd_specs").get("general_model"), "U20a");
			assertNotNull(json.getJSONObject("hd_specs").get("general_image").toString());						
			assertTrue( this.jsonArrayToList(json, "hd_specs", "connectors", "microUSB 2.0"));
			assertTrue( this.jsonArrayToList(json, "hd_specs", "features", "Stereo FM radio with RDS"));
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	} 
	
	
/*
	@Test
	public void testSiteFetchArchive() {		
		assertTrue(hd3.siteFetchArchive());
	}
 */
	
	@Ignore
	private boolean jsonArrayToList(JSONObject json, String key1, String key2, String value) throws JSONException {
		JSONArray jsonArray = (key2 == null) ? (JSONArray) json.get(key1) : 
			(JSONArray) json.getJSONObject(key1).get(key2); 		
		List<String> list = new ArrayList<String>();		
		for(int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.getString(i));	
		}
		Collections.sort(list);	
		return ( Collections.binarySearch(list, value) > -1 ) ? true : false;		
	}
	
	@Ignore
	private JSONObject getJson() throws JSONException {		
		return (new JSONObject(hd3.getReply().toString()));
	}

}
