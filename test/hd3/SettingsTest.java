package hd3;

import junit.framework.TestCase;
import hdapi3.Settings;

import org.junit.Test;

public class SettingsTest extends TestCase {

	/*
	 * Call first before every methods
	 */
	protected void setUp() {	
		try {
			Settings.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	@Test
	public void testSettings() {
		// Test Passed
		assertEquals(Settings.getUsername(), SecretConfig.getUserName("SECRET_NAME"));	
		assertEquals(Settings.getSecret(), SecretConfig.getSecret("SECRET_KEY"));
		assertEquals(Settings.getSiteId(), SecretConfig.getSiteId("SITE_ID"));
		assertFalse(Settings.isUseLocal());		
		
		// Failed
		assertEquals(Settings.getUsername(), "your_username");
		assertTrue(Settings.isUseLocal());
	}

}
