package hd;


import org.junit.Ignore;
import org.junit.Test;

import api.hd.HDStore;
import api.hd.HDStore.HDCache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.*;

@Ignore
public class HDCacheTest 
{
	private Gson gson = new GsonBuilder().registerTypeAdapter(TestData.class, new TestData.TestDataDeserializer()).create();
//	private Map<String, Object> testData = new HashMap<String, Object>();
	private TestData testData;

	public HDCacheTest()
	{
//		testData.put("roses", "red");
//		testData.put("fish", "blue");
//		testData.put("sugar", "sweet");
//		testData.put("number", 4);
		testData = new TestData("red", "blue", "sweet", 4);
	}
	
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testA() 
	{
		HDCache cache = HDStore.getInstance().getCache();
		String now = String.valueOf(System.currentTimeMillis());
		cache.write(now, gson.toJsonTree(testData));
		TestData reply = gson.fromJson(cache.read(now), TestData.class);
//		cache.write(now, gson.toJsonTree(testData, new TypeToken<Map<String, Object>>(){}.getType()));
//		Map<String, Object> reply = (Map<String, Object>) gson.fromJson(cache.read(now), new TypeToken<Map<String, Object>>(){}.getType());
		assertEquals(testData, reply);
	}

//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testVolume() 
	{
		HDCache cache = HDStore.getInstance().getCache();
//		HDCache cache = new HDCache();
		String now = String.valueOf(System.currentTimeMillis());
		
		for(int i = 0; i < 10000; i++) 
		{
			String key = "test" + now + i;
			cache.write(key, gson.toJsonTree(testData));
//			cache.write(key, gson.toJsonTree(testData, new TypeToken<Map<String, Object>>(){}.getType()));
		}
		
		for(int i = 0; i < 10000; i++) 
		{
			String key = "test" + now + i;
			TestData reply = gson.fromJson(cache.read(key), TestData.class);
//			Map<String, Object> reply = (Map<String, Object>) gson.fromJson(cache.read(key), new TypeToken<Map<String, Object>>(){}.getType());
			assertEquals(testData, reply);
		}
	}

}
