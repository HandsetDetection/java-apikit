package hd;

import org.junit.Ignore;
import org.junit.Test;

import api.hd.Config;
import api.hd.HDStore;
import api.hd.HDStore.HDCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static org.junit.Assert.*;


public class HDCacheTest {
	private Gson gson = new GsonBuilder().registerTypeAdapter(TestData.class, new TestData.TestDataDeserializer()).create();
	private TestData testData;

	public HDCacheTest() {
		testData = new TestData("red", "blue", "sweet", 4);
	}
	
//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testA() {
		HDCache cache = HDStore.getInstance().getCache();
		String now = String.valueOf(System.currentTimeMillis());
		cache.write(now, gson.toJsonTree(testData));
		TestData reply = gson.fromJson(cache.read(now).toString(), TestData.class);
		assertEquals(testData, reply);
	}

//	@Ignore("expected:<{fish=blue, roses=red, number=4, sugar=sweet}> but was:<{fish=blue, roses=red, number=4.0, sugar=sweet}> - due to deserialization error, not caching error")
	@Test
	public void testVolume() {
		HDCache cache = HDStore.getInstance().getCache();
		String now = String.valueOf(System.currentTimeMillis());
		
		for(int i = 0; i < 10000; i++) 
		{
			String key = "test" + now + i;
			cache.write(key, gson.toJsonTree(testData));
		}
		
		for(int i = 0; i < 10000; i++) 
		{
			String key = "test" + now + i;
			TestData reply = gson.fromJson(cache.read(key).toString(), TestData.class);
			assertEquals(testData, reply);
		}
	}
}
