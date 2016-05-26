package hd;

import hd.TestData;
import hd.TestData.TestDataDeserializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.junit.After;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import api.hd.Config;
import api.hd.HDStore;
import api.hd.JsonConstants;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class HDStoreTest {
	private Gson gson = new GsonBuilder().registerTypeAdapter(TestData.class, new TestDataDeserializer()).create();
	private TestData testData;
	private String cloudConfig = "hd4CloudConfig.properties";
	
	@Before
	public void setUp() {
		HDStore store = HDStore.getInstance();
		store.purge();
		try {
			store.setPath("/tmp",  true);
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}

	public HDStoreTest() {
		testData = new TestData("red", "blue", "sweet", 4);
	}

	// Writes to store & cache
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testReadWrite() {
		String key = "storekey" + System.currentTimeMillis();
		HDStore store = HDStore.getInstance();
		store.write(key, testData);
		TestData data = gson.fromJson(store.read(key), TestData.class);
		assertEquals(testData, data);
	}
	
	// Writes to store & not cache
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testStoreFetch() {
		String key = "storekey2" + System.currentTimeMillis();
		HDStore store = HDStore.getInstance();
		store.store(key, testData);
		TestData data = gson.fromJson(store.fetch(key), TestData.class);
		System.out.println(data.toString());
		assertEquals(testData, data);
		boolean exists = Files.exists(Paths.get(store.getDirectory() + File.separator + key + ".json"));
		assertTrue(exists);
	}
	
	// Test purge
	@Test
	public void testPurge() throws IOException {
		Integer fileCount = 0;
		HDStore store = HDStore.getInstance();
		store.store("testPurgeKey", testData);
		fileCount = new File(store.getDirectory()).listFiles().length;		
		assertTrue(0 < fileCount);

		store.purge();
		fileCount = new File(store.getDirectory()).listFiles().length;		
		assertTrue(0 == fileCount);
	}
	
	// Reads all devices from Disk (Keys need to be in Device*json format)
	@Test
	public void testFetchDevices() {
		HDStore store = HDStore.getInstance();
		// Store 3 fake devices
		String key = "Device" + System.currentTimeMillis();
		store.store(key, testData);
		key = "Device" + System.currentTimeMillis();
		store.store(key, testData);
		key = "Device" + System.currentTimeMillis();
		store.store(key, testData);
		
		JsonObject devices = store.fetchDevices();
		JsonArray d = (JsonArray) devices.get(JsonConstants.DEVICES);
		assertEquals(d.isJsonArray(), true);
		for (JsonElement je: d) {
			// Test each fake device
			TestData data = gson.fromJson(je, TestData.class);
			assertEquals(testData, data);
		}
		
		// Purge the store
		store.purge();
	}
	
	// Moves a file from disk into store (vanishes from previous location).
	@Test
	public void testMoveIn() throws IOException {
		HDStore store = HDStore.getInstance();
		String jsonstr = gson.toJson(testData);
		FileOutputStream stream = new FileOutputStream("TmpDevice.json");
		try {
		    stream.write(jsonstr.getBytes(Charsets.UTF_8));
			stream.flush();
		} finally {
		    try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		store.moveIn("TmpDevice.json",  "TmpDevice.json");
		assertTrue(!Files.exists(Paths.get("TmpDevice.json")));
		assertTrue(Files.exists(Paths.get(store.getDirectory() + File.separator + "TmpDevice.json")));
	}
	
	// Test singleton"ship
	@Test
	public void testSingleton() throws IOException {
		HDStore store = HDStore.getInstance();
		HDStore store2 = HDStore.getInstance();
		store.setPath("/tmp", true);
		assertEquals(store2.getDirectory(), "/tmp/hd40store");
	}
}
