package hd;


import hd.TestData;
import hd.TestData.TestDataDeserializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import api.hd.Config;
import api.hd.HDStore;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class HDStoreTest 
{
	private Gson gson = new GsonBuilder().registerTypeAdapter(TestData.class, new TestDataDeserializer()).create();
//	private Map<String, Object> testData = new HashMap<String, Object>();
	private TestData testData;
	
	
	private String cloudConfig = "hd4CloudConfig.properties";
//	private Map<String, Object> testData = new HashMap<String, Object>();
	private static int globbedFiles = 0;
	
	@Before
	public void setUp() {		
		try {
			FileInputStream fis = new FileInputStream(cloudConfig); 
			Config.init(fis);					
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}

	public HDStoreTest()
	{
//		testData.put("roses", "red");
//		testData.put("fish", "blue");
//		testData.put("sugar", "sweet");
//		testData.put("number", 4);
		testData = new TestData("red", "blue", "sweet", 4);
	}

	// Writes to store & cache
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testReadWrite() 
	{
		String key = "storekey" + System.currentTimeMillis();
		HDStore store = HDStore.getInstance();
		store.write(key, testData);
//		Map<String, Object> data = (Map<String, Object>) gson.fromJson(store.read(key), new TypeToken<Map<String, Object>>(){}.getType());
		TestData data = gson.fromJson(store.read(key), TestData.class);
		assertEquals(testData, data);
//		data = (Map<String, Object>) gson.fromJson(cache.read(key), new TypeToken<Map<String, Object>>(){}.getType());
		data = gson.fromJson(store.getCache().read(key), TestData.class);
		assertEquals(testData, data);
		boolean exists = Files.exists(Paths.get(store.getDirectory() + File.separator + key + ".json"));
		assertTrue(exists);
	}
	
	// Writes to store & not cache
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testStoreFetch() 
	{
		String key = "storekey2" + System.currentTimeMillis();
		HDStore store = HDStore.getInstance();
		store.store(key, testData);
		TestData data = gson.fromJson(store.read(key), TestData.class);
//		Map<String, Object> data = (Map<String, Object>) gson.fromJson(cache.read(key), new TypeToken<Map<String, Object>>(){}.getType());
		assertEquals(testData, data);
		boolean exists = Files.exists(Paths.get(store.getDirectory() + File.separator + key + ".json"));
		assertTrue(exists);
	}
	
	// Test purge
	@Test
	public void testPurge() throws IOException 
	{
		globbedFiles = 0;
		HDStore store = HDStore.getInstance();
		FileVisitor fv = new FileVisitor();
		Files.walkFileTree(Paths.get(store.getDirectory()), fv);
		assertTrue(0 < globbedFiles);

		store.purge();
		globbedFiles = 0;
		fv = new FileVisitor();
		Files.walkFileTree(Paths.get(store.getDirectory()), fv);
		assertTrue(0 == globbedFiles);
	}
	
	// Reads all devices from Disk (Keys need to be in Device*json format)
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testFetchDevices() 
	{
		String key = "Device" + System.currentTimeMillis();
		HDStore store = HDStore.getInstance();
		store.store(key, testData);
//		Map<String, Object> devices = (Map<String, Object>) gson.fromJson(store.fetchDevices().get("devices").getAsJsonArray().get(0), new TypeToken<Map<String, Object>>(){}.getType());
		TestData devices = gson.fromJson(store.read(key), TestData.class);
		assertEquals(devices, testData);
		store.purge();
	}
	
	// Moves a file from disk into store (vanishes from previous location).
	@Test
	public void testMoveIn() throws IOException 
	{
		HDStore store = HDStore.getInstance();
//		String jsonstr = gson.toJson(testData, new TypeToken<Map<String, Object>>(){}.getType());
		String jsonstr = gson.toJson(testData);
		FileOutputStream stream = new FileOutputStream("TmpDevice.json");
		try {
		    stream.write(jsonstr.getBytes(Charsets.UTF_8));
			stream.flush();
		} finally {
		    try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		store.moveIn("TmpDevice.json",  "TmpDevice.json");
		assertTrue(!Files.exists(Paths.get("TmpDevice.json")));
		assertTrue(Files.exists(Paths.get(store.getDirectory() + File.separator + "TmpDevice.json")));
	}
	
	// Test singleton"ship
	@Test
	public void testSingleton() throws IOException 
	{
		HDStore store = HDStore.getInstance();
		HDStore store2 = HDStore.getInstance();
		store.setPath("/tmp", true);
		assertEquals(store2.getDirectory(), "/tmp/hd40store");
	}

	
	
	static class FileVisitor extends SimpleFileVisitor<Path>
	{
		FileInputStream fis;
		final List<JsonElement> fileData = new ArrayList<JsonElement>(10000);
		final PathMatcher matcher;
		
		public FileVisitor()
		{
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + HDStore.getInstance().getDirectory() + File.separator + "*.json");
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
				throws IOException 
		{
			if (matcher.matches(file)) 
			{
				globbedFiles++;
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) 
				throws IOException 
		{
			fail(exc.getMessage());
			return FileVisitResult.CONTINUE;
		}
		
		public List<JsonElement> getJsonElem()
		{
			return fileData;
		}
		
		public FileInputStream getFIS()
		{
			return fis;
		}
	}
}
