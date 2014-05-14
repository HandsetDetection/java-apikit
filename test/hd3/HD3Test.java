package hd3;

import java.io.FileInputStream;
import java.io.IOException;
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
	public void testDeviceVendorsNokia() throws JSONException {				
		hd3.deviceVendors();		
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "vendor", null, "Nokia" ) );				
	}
	
	@Test
	public void testDeviceVendorsLG() throws JSONException {				
		hd3.deviceVendors();				
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "vendor", null, "LG" ) );
	}
	
	@Test
	public void testDeviceVendorsSamsung() throws JSONException {				
		hd3.deviceVendors();			
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "vendor", null, "Samsung" ) );
	}
	
	@Test
	public void testDeviceModelsNokia() throws JSONException {
		hd3.deviceModels("Nokia");		
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "model", null, "N95" ) );
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "model", null, "3310i" ) );
	}
	
	@Test
	public void testDeviceModelsSamsung() throws JSONException {
		hd3.deviceModels("Samsung");
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "model", null, "ATIV Odyssey" ) );
		assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "model", null, "Galaxy Y Duos Lite" ) );
	}
	
	@Test
	public void testDeviceViewNokiaLumia() throws JSONException {					 
		hd3.deviceView("Nokia", "Lumia 610 NFC");	
		assertEquals(HD3Util.get("general_platform", hd3.getReply()).getAsString(), "Windows Phone");
		assertEquals(HD3Util.get("general_model", hd3.getReply()).getAsString(), "Lumia 610 NFC");
		
		//assertTrue( this.jsonArrayToList( new JSONObject(hd3.getReply().toString()), "device", "display_other", "ATIV Odyssey" ) );
		String jsonResult = hd3.getReply().toString();			
		try {
			JSONObject json = new JSONObject(jsonResult);
			assertTrue(json.getJSONObject("device").getString("display_other").contains("Multitouch"));
		} catch (JSONException e) {			
			e.printStackTrace();
		}				
	}
	
	@Test
	public void testDeviceViewAppleIPhone5S() {					 
		hd3.deviceView("Apple", "iPhone 5S");	
		assertEquals(HD3Util.get("design_dimensions", hd3.getReply()).getAsString(), "123.8 x 58.6 x 7.6");
		assertEquals(HD3Util.get("display_colors", hd3.getReply()).getAsString(), "16M");	
		String jsonResult = hd3.getReply().toString();			
		try {
			JSONObject json = new JSONObject(jsonResult);
			assertTrue(json.getJSONObject("device").getString("features").contains("Ambient Light Sensor"));
			assertTrue(json.getJSONObject("device").getString("connectors").contains("Lightning Connector"));
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
			JSONObject json = new JSONObject(hd3.getReply().toString());
			assertEquals(json.getJSONObject("hd_specs").get("general_platform"), "Symbian");
			assertEquals(json.getJSONObject("hd_specs").get("general_platform_version"), "9.2");
			assertNotNull(json.getJSONObject("hd_specs").get("general_browser_version").toString());			
			assertTrue(json.getJSONObject("hd_specs").getString("connectors").contains("USB"));
			assertTrue(json.getJSONObject("hd_specs").getString("features").contains("Push to talk"));			
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSiteDetectXOperaMini() {
		hd3.addDetectVar("user-agent", "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
		hd3.siteDetect();
		try {
			JSONObject json = new JSONObject(hd3.getReply().toString());
			assertEquals(json.getJSONObject("hd_specs").get("general_platform"), "Android");
			assertEquals(json.getJSONObject("hd_specs").get("general_model"), "U20a");
			assertNotNull(json.getJSONObject("hd_specs").get("general_image").toString());			
			assertTrue(json.getJSONObject("hd_specs").getString("connectors").contains("microUSB 2.0"));
			assertTrue(json.getJSONObject("hd_specs").getString("features").contains("Push to talk"));
			assertTrue(json.getJSONObject("hd_specs").getString("features").contains("Push to talk"));		
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	} 
	
	@Ignore
	private boolean jsonArrayToList(JSONObject json, String key1, String key2, String value) throws JSONException {
		JSONArray jsonArray = (key2 == null) ? (JSONArray) json.get(key1) : 
			(JSONArray) json.getJSONObject(key1).get(key2); 		
		List<String> list = new ArrayList<String>();		
		for(int i = 0; i < jsonArray.length(); i++) {
			
		}
			//list.add(jsonArray.getString(i));		
		return ( Collections.binarySearch(list, value) > 1 ) ? true : false;		
	}
	
/*
	@Test
	public void testSiteFetchArchive() {		
		assertTrue(hd3.siteFetchArchive());
	}
 */
}
