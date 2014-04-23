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
		// Pass based on what is inside the file properties
		assertEquals(Settings.getUsername(), "203a2c5495");
		assertEquals(Settings.getSecret(), "4Mcy7r7wDFdCDbg2");
		assertEquals(Settings.getSiteId(), "50538");
		assertFalse(Settings.isUseLocal());
		
		// Test fail
		assertEquals(Settings.getUsername(), "your_username");
		assertTrue(Settings.isUseLocal());		
	}

}
