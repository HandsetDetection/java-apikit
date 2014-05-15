package hd3;

import junit.framework.TestCase;
import hdapi3.Settings;

import org.junit.Test;

public class SettingsTest extends TestCase {

	protected void setUp() {	
		try {
			Settings.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	@Test
	public void testSettings() {		
		assertEquals(Settings.getUsername(), SecretConfig.getUserName("SECRET_NAME"));	
		assertEquals(Settings.getSecret(), SecretConfig.getSecret("SECRET_KEY"));
		assertEquals(Settings.getSiteId(), SecretConfig.getSiteId("SITE_ID"));
		assertTrue(Settings.isUseLocal());		
	}

}
