package api.hd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HDStore 
{
	public static final String dirname = "hd40store";
	static final Logger g_logger = Logger.getLogger(HDStore.class.getName());
	
	private String path = "";
	private String directory = "";
	private final Gson gson;
	private final HDCache cache;
	private static HDStore _instance = null;
	
	/**
	 * Singleton Constructor
	 *
	 * @param string $path Location of storage ROOT dir.
	 * @param boolean $createDirectory - Create storage directory if it does not exist
	 * @throws IOException 
	 **/
	HDStore() throws IOException 
	{
		this.gson = new Gson();
		this.path = Config.getLocalDirectory();		
		if (null == path)
			g_logger.log(Level.WARNING, "local.files.directory not set inside the protperty file (hdapi_config.properties). Continuing with a null path");
		
		setPath(path, true);
		this.cache = new HDCache();
	}
	
	/**
	 * Get the Singleton
	 *
	 * @param void
	 * @return Object $_instance
	 **/
	public static HDStore getInstance() 
	{
        if (_instance == null) 
        {
        	try
        	{
        		_instance = new HDStore();
        		
        	} catch (IOException e) {
        		
        	}
        }
        
        return _instance;
    }
	
	/**
	 * Sets the path to the root directory for storage operations, optionally creating the storage directory in it.
	 *
	 * @param string $path
	 * @param boolean $createDirectory
	 * @return void
	 * @throws IOException 
	 **/
	public void setPath(String path, boolean createDir) throws IOException 
	{
		if (null == path && null == this.path)
		{
			g_logger.log(Level.SEVERE, "No valid path for local storage supplied. Unable to continue");
			throw new InvalidPathException(path, "No valid path for local storage supplied. Unable to continue");
		}
		
		this.directory = path + File.separator + HDStore.dirname;
		
		if (createDir) 
		{
			File file = new File(this.directory);
			file.mkdir();
		}
	}
	
	/**
	 * Write data to cache & disk
	 *
	 * @param string $key
	 * @param array $data
	 * @return boolean true on success, false otherwise
	 */
	public boolean write(String key, Object data) 
	{
		if (null == data)
			return false;
		if (this.store(key, data))
			return false;
		
		this.cache.write(key, data);
		return true; 
	}
	
	/**
	 * Store data to disk
	 *
	 * @param string $key The search key (becomes the filename .. so keep it alphanumeric)
	 * @param array $data Data to persist (will be persisted in json format)
	 * @return boolean true on success, false otherwise
	 */
	public boolean store(String key, Object data) 
	{
		FileOutputStream fos = null; 
		
		String jsonStr = gson.toJson(data);
		try
		{
			fos = new FileOutputStream(this.directory + File.separator + key + ".json");
			fos.write(jsonStr.getBytes("UTF-8"));
			
		} catch (Exception ex) {
			
			g_logger.log(Level.WARNING, "Unable to store the reqeusted data - " + ex.getMessage(), ex);
			return false;
			
		} finally {

			try
			{
				if (null != fos)
					fos.close();		
				
			} catch (IOException ioe) {
				
			}
		}
		
		return true;
	}
	
	/**
	 * Read $data, try cache first
	 *
	 * @param sting $key Key to search for
	 * @return boolean true on success, false
	 */
	public JsonElement read(String key) 
	{
		JsonElement reply = this.cache.read(key);
		if (null != reply)
			return reply;
		
		reply = this.fetch(key);
		if (null == reply)
			return null;
		
		this.cache.write(key, reply);
		return reply;
	}
	
	/**
	 * Fetch data from disk
	 *
	 * @param string $key.
	 * @reply mixed
	 **/
	JsonElement fetch(String key) 
	{
		FileInputStream fis = null; 
		JsonElement jsonElem = null;
		
		try
		{
			fis = new FileInputStream(this.directory + File.separator + key + ".json");
			jsonElem = HDUtil.parseJson(fis);
			
		} catch (Exception ex) {
			
			g_logger.log(Level.WARNING, "Unable to retrieve the reqeusted data - " + ex.getMessage(), ex);
			
		} finally {

			try
			{
				if (null != fis)
					fis.close();		
				
			} catch (IOException ioe) {
				
			}
		}
			
		return jsonElem;
	}
	
	/**
	 * Returns all devices inside one giant array
	 *
	 * Used by localDevice* functions to iterate over all devies
	 *
	 * @param void
	 * @return array All devices in one giant assoc array
	 **/
	public JsonObject fetchDevices() 
	{
		final JsonObject jsonObj = new JsonObject();
		final FileVisitor fv = new FileVisitor(FileVisitor.Modes.MODE_FETCH_DEVICES);
		
		try
		{
			System.out.println(this.directory);
			Files.walkFileTree(Paths.get(this.directory), fv);
			jsonObj.add("devices", fv.getJsonElem());
			
		} catch (Exception ex) {
			
			g_logger.log(Level.WARNING, "Unable to complete the fetchDevices() request: " + ex.getLocalizedMessage(), ex);
			
			try
			{
				fv.getFIS().close();
			
			} catch (IOException e) {
				
			}
		}
		
		return jsonObj;
	}
	
	/**
	 * Moves a json file into storage.
	 *
	 * @param string $srcAbsName The fully qualified path and file name eg /tmp/sjjhas778hsjhh
	 * @param string $destName The key name inside the cache eg Device_19.json
	 * @return boolean true on success, false otherwise
	 */
	public boolean moveIn(String srcAbsName, String destName)
	{
		try 
		{
			Files.move(Paths.get(srcAbsName), Paths.get(this.directory + File.separator + destName), StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e) {

			g_logger.log(Level.WARNING, "Unable to complete a moveIn operation: " + e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Cleans out the store - Use with caution
	 *
	 * @param void
	 * @return true on success, false otherwise
	 **/
	public boolean purge() 
	{
		FileVisitor fv = new FileVisitor(FileVisitor.Modes.MODE_PURGE);
		try
		{
			Files.walkFileTree(Paths.get(this.directory), fv);
			
		} catch (Exception ex) {
			
			g_logger.log(Level.WARNING, "Unable to complete the purge() request: " + ex.getLocalizedMessage(), ex);
			return false;
		}

		this.cache.purge();
		return true;
	}
	
	static class FileVisitor extends SimpleFileVisitor<Path>
	{
		enum Modes
		{
			MODE_FETCH_DEVICES,
			MODE_PURGE
		}
		
		Modes mode;
		FileInputStream fis;
		final JsonArray fileJsonData = new JsonArray();
		final PathMatcher matcher;
		
		private FileVisitor()
		{
			matcher = null;
		}
		
		public FileVisitor(Modes mode)
		{
			this.mode = mode;
			switch (mode)
			{
			case MODE_FETCH_DEVICES:
				matcher = FileSystems.getDefault().getPathMatcher("glob:" + HDStore.getInstance().directory + File.separator + "Device*.json");
				break;
				
			case MODE_PURGE:
				matcher = FileSystems.getDefault().getPathMatcher("glob:" + HDStore.getInstance().directory + File.separator + "*.json");
				break;
				
			default:
				matcher = null;
			}
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
				throws IOException 
		{
			System.out.println(file.toString());
			if (matcher.matches(file)) 
			{
				switch (mode)
				{
				case MODE_FETCH_DEVICES:
					fis = new FileInputStream(file.toFile());
					fileJsonData.add(HDUtil.parseJson(fis));
					try
					{
						fis.close();
						
					} catch (IOException ex) {
						
					}
					break;
					
				case MODE_PURGE:
					try
					{
						Files.delete(file);
						
					} catch (Exception e) {
						
						HDStore.g_logger.log(Level.WARNING, "Could not delete file: " + file.toString(), e);
					}
					break;
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) 
				throws IOException 
		{
			HDStore.g_logger.log(Level.WARNING, "Could not visit file: " + file.toString(), exc);
			return FileVisitResult.CONTINUE;
		}
		
		public JsonArray getJsonElem()
		{
			return fileJsonData;
		}
		
		public FileInputStream getFIS()
		{
			return fis;
		}
	}

	public String getDirectory() {
		return directory;
	}
	
	public HDCache getCache()
	{
		return cache;
	}
	
	/**
	 * The Class HDCache.
	 */
	public static class HDCache 
	{
		/** The Constant MAX_ENTRIES. */
		private static final int MAX_ENTRIES = 50000;
		
		/** The hdcached objects. */
		private static Cache<String, Object> HDCACHED_OBJECTS;
		
		private static HDCache _instance = null;

		
		/**
		 * Instantiates a new HD cache.
		 */
		private HDCache() {
			HDCACHED_OBJECTS = CacheBuilder.newBuilder().maximumSize(MAX_ENTRIES).build();
		}
		
		/**
		 * Put.
		 *
		 * @param key the key
		 * @param value the value
		 */
		public void write(String key, Object value) {
			HDCACHED_OBJECTS.put(key, value);
		}
		
		/**
		 * Gets the.
		 *
		 * @param key the key
		 * @return the object
		 */
		public JsonElement read(String key) {
			Object reply;
			reply = HDCACHED_OBJECTS.getIfPresent(key);
			return (JsonElement) reply;						//catalin: might crash; it didn't
		}	
		
		/**
		 * Clears cache.
		 *
		 * @param key the key
		 * @return the object
		 */
		public void purge() {
			HDCACHED_OBJECTS.invalidateAll();
		}	
		
		/**
		 * 
		 * 
		 * @param args
		 * @throws Exception
		 */
		public static void main(String[] args) throws Exception{
			final HDCache myCache = new HDCache();
			Thread t1 = new Thread() {
				@Override
				public void run() {
					myCache.write("k1", "v1");
					myCache.write("k2", "v2");
					myCache.write("k3", "v3");
				}
			};
			Thread t2 = new Thread() {
				@Override
				public void run() {
					myCache.write("k4", "v4");
					myCache.write("k5", "v5");
					myCache.write("k6", "v6");
				}
			};
			Thread t3 = new Thread() {
				@Override
				public void run() {
					myCache.write("k7", "v7");
					myCache.write("k8", "v8");
					myCache.write("k9", "v9");
				}
			};
			t3.start();
			t2.start();
			t1.start();
			while (t1.isAlive() || t2.isAlive() || t3.isAlive()) {
				Thread.sleep(3000);
			}
			System.out.println(myCache.read("k1"));
			System.out.println(myCache.read("k2"));
			System.out.println(myCache.read("k3"));
			System.out.println(myCache.read("k4"));
			System.out.println(myCache.read("k5"));
			System.out.println(myCache.read("k6"));
			System.out.println(myCache.read("k7"));
			System.out.println(myCache.read("k8"));
			System.out.println(myCache.read("k9"));		
		}
	}
}
