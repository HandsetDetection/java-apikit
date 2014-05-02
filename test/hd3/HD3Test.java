package hd3;

import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.TestCase;
import hdapi3.HD3;
import hdapi3.HD3Util;
import hdapi3.Settings;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HD3Test extends TestCase {	
	private FileInputStream fis;
	private HD3 hd3;		
	/* This will run first before any other test methods. */
	protected void setUp() {		
		try {
			fis = new FileInputStream("hdapi_config.properties"); // common.use_local=true
			Settings.init(fis);		
			Settings.isUseLocal();
			fis.close();
			hd3 = new HD3();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	@Test
	public void testHD3() throws IOException, Exception {				
		assertTrue(Settings.init());
		assertEquals(hd3.getUsername(), "11111111");				
		assertEquals(hd3.getUsername(), SecretConfig.getUserName("SECRET_NAME"));		
				
	}	
	@Test
	public void testDeviceVendors() {		
		assertTrue(hd3.isUseLocal());				
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
	@Ignore
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
		// First device id: 1353, model:100
		JsonElement elem = rootobj.get("devices").getAsJsonArray().get(0);	
		String id = elem.getAsJsonObject().get("id").getAsString();
		String model = elem.getAsJsonObject().get("general_model").getAsString();		
		// compare id
		assertEquals(id, "1353");   		
		// compare model
		assertEquals(model, "100");  				
		// Test fail
		elem = rootobj.get("devices").getAsJsonArray().get(1);
		// Second device id: 6110, model:1006
		id = elem.getAsJsonObject().get("id").getAsString();
		elem.getAsJsonObject().get("general_model").getAsString();
		// 01 != 6110
		assertEquals(id, "10");	
		// 10 != 1006
		assertEquals(model, "01");
	}
	@Test
	public void testSiteFetchArchive() {		
		//assertTrue(hd3.siteFetchArchive());
	}
	@Test
	public void testSiteDetect() {
		assertFalse(hd3.isUseLocal());		
		hd3.addDetectVar("user-agent", 
				"Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/12.0.013; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) AppleWebKit/413 (KHTML, like Gecko) Safari/413");
		assertFalse(hd3.siteDetect());		
	} 
}
