package hd3;

import java.io.FileInputStream;

import junit.framework.TestCase;
import hdapi3.HD3;
import hdapi3.HD3Util;
import hdapi3.Settings;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HD3Test extends TestCase {
	
	HD3 hd3;
	
	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 * Description: This will run first before any other test methods.
	 */
	protected void setUp() {		
		try {
			FileInputStream fis = new FileInputStream("hdapi_config.properties");
			Settings.init(fis);
			Settings.isUseLocal();
			fis.close();
			hd3 = new HD3();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	/*
	 * Test our constructor
	 */
	@Test
	public void testHD3() {
		assertEquals(hd3.getUsername(), "11111111");		// Fail
		assertEquals(hd3.getUsername(), "203a2c5495");		// Pass		
	}	

	@Test
	public void testDeviceVendors() {
		assertTrue(hd3.isUseLocal());	// This will fail; we set our local=false
		
		// Set it to False; Fail
		assertFalse(hd3.isUseLocal());
	}

	@Test
	public void testDeviceModels() {
		assertTrue(hd3.isUseLocal());
		
		assertFalse(hd3.isUseLocal());		
		hd3.deviceModels("Nokia");
		JsonObject rootobj = hd3.getReply();
		assertEquals(rootobj.get("model").getAsJsonArray().get(0).getAsString(), "100");
	}


	@Test
	public void testDeviceView() {
		assertFalse(hd3.isUseLocal());
		
		// Set Nokia as our device; general_mode: 660
		hd3.deviceView("Nokia", "660");	
		assertEquals(HD3Util.get("general_model", hd3.getReply()).getAsString(), "660");
		
		// This will fail; We don't have 001 in our database			
		assertEquals(HD3Util.get("general_model", hd3.getReply()).getAsString(), "001");
	}


	@Test
	public void testDeviceWhatHas() {
		assertFalse(hd3.isUseLocal());
		
		// Set Nokia as our device; Test success
		hd3.deviceWhatHas("general_vendor", "Nokia");
		JsonObject rootobj = hd3.getReply();
		JsonElement elem = rootobj.get("devices").getAsJsonArray().get(0);	// First device id: 1353, model:100
		String id = elem.getAsJsonObject().get("id").getAsString();
		String model = elem.getAsJsonObject().get("general_model").getAsString();
		assertEquals(id, "1353");   // compare id
		assertEquals(model, "100");  // compare model
		
		
		// Test fail
		elem = rootobj.get("devices").getAsJsonArray().get(1);	// Second device id: 6110, model:1006
		id = elem.getAsJsonObject().get("id").getAsString();
		elem.getAsJsonObject().get("general_model").getAsString();
		assertEquals(id, "10");		// 01 != 6110
		assertEquals(model, "01");	// 10 != 1006
	}

/*	
	@Test
	public void testSiteAdd() {
		fail("Not yet implemented");
	}

	@Test
	public void testSiteEdit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSiteView() {
		fail("Not yet implemented");
	}

	@Test
	public void testSiteDelete() {
		fail("Not yet implemented");
	}

	@Test
	public void testSiteFetchArchive() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddDetectVar() {
		fail("Not yet implemented");
	}

	@Test
	public void testSiteDetect() {
		fail("Not yet implemented");
	}

	@Test
	public void testLocalGetSpecs() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRealm() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetError() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUsername() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSecret() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsUseLocal() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsUseProxy() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetApiServer() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLogServer() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSiteId() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMobileSite() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMatchFilter() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProxyAddress() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProxyUsername() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProxyPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetReadTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConnectTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetProxyPort() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDetectRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetReply() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRawReply() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNonMobile() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRealm() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetError() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUsername() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSecret() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUseLocal() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetApiServer() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetLogServer() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetSiteId() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMobileSite() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMatchFilter() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetConnectTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetReadTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUseProxy() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetProxyAddress() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetProxyPort() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetProxyUsername() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetProxyPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDetectRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testResetDetectRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetNonMobile() {
		fail("Not yet implemented");
	}
*/
}
