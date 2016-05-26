package hd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import api.hd.HD;
import api.hd.HDDevice;
import api.hd.HDStore;
import static org.junit.Assert.*;

//The device class performs the same functions as our Cloud API, but locally.
//It is only used when use_local is set to true in the config file.
//To perform tests we need to setup the environment by populating the the Storage layer with device specs.
//So install the latest community edition so there is something to work with.

public class HDDeviceTest {
	private static String cloudConfig = "hd4CloudConfig.properties";
	
	// Setup community edition for tests. Takes 60s or so to download and install.
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		HD hd = new HD(cloudConfig);
		hd.deviceFetchArchive();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		HDStore store = HDStore.getInstance();
		store.purge();
	}
	
	@Test
	public void testIsHelperUsefulTrue() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3 like Mac OS X; en-gb) AppleWebKit/533.17.9 (KHTML, like Gecko)");
		HDDevice hdDevice = new HDDevice(cloudConfig);
		boolean result = hdDevice.isHelperUseful(headers);
		assertTrue(result);
	}

	@Test
	public void testIsHelperUsefulFalse() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		HDDevice hdDevice = new HDDevice(cloudConfig);
		boolean result = hdDevice.isHelperUseful(headers);
		assertFalse(result);
	}
}