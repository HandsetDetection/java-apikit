package api.hd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class HDStore {
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
	HDStore() throws IOException  {
		this.gson = new Gson();
		this.cache = new HDCache();
	}
	
	/**
	 * Get the Singleton
	 *
	 * @param void
	 * @return Object $_instance
	 **/
	public static HDStore getInstance()  {
        if (_instance == null) {
        	try {
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
	public void setPath(String path, boolean createDir) throws IOException {
		if (null == path && null == this.path) {
			g_logger.log(Level.SEVERE, "No valid path for local storage supplied. Unable to continue");
			throw new InvalidPathException(path, "No valid path for local storage supplied. Unable to continue");
		}
		this.directory = path + File.separator + HDStore.dirname;
		
		if (createDir) {
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
	public boolean write(String key, Object data) {
		if (null == data)
			return false;
		if (! store(key, data))
			return false;		
		cache.write(key, data);
		return true;
	}
	
	/**
	 * Store data to disk
	 *
	 * @param string $key The search key (becomes the filename .. so keep it alphanumeric)
	 * @param array $data Data to persist (will be persisted in json format)
	 * @return boolean true on success, false otherwise
	 */
	public boolean store(String key, Object data) {
		FileOutputStream fos = null; 
		String jsonStr = gson.toJson(data);
		try {
			fos = new FileOutputStream(this.directory + File.separator + key + ".json");
			fos.write(jsonStr.getBytes("UTF-8"));
		} catch (Exception ex) {
			g_logger.log(Level.WARNING, "Unable to store the reqeusted data - " + ex.getMessage(), ex);
			return false;
		} finally {
			try {
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
	 * @return the object or nunll on failure
	 */
	public JsonElement read(String key) {
		JsonElement reply = gson.toJsonTree(this.cache.read(key));
		
		if (! HDUtil.isNullElement(reply))
			return reply;
		
		reply = this.fetch(key);
		if (HDUtil.isNullElement(reply))
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
	public JsonElement fetch(String key) {
		FileInputStream fis = null; 
		JsonElement jsonElem = null;
		String filePath = this.directory + File.separator + key + ".json";
		try {
			fis = new FileInputStream(filePath);
			jsonElem = HDUtil.parseJson(fis);
		} catch (Exception ex) {
			g_logger.log(Level.WARNING, "Unable to retrieve the reqeusted data - " + ex.getMessage(), ex);
		} finally {
			try {
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
	public JsonObject fetchDevices() {
		JsonObject jsonObj = new JsonObject();
		JsonArray jsonArr = new JsonArray();
		FileInputStream fis;
		
		try {
			for (File file: new File(this.directory).listFiles()) {
				if (file.getName().startsWith("Device")) {
					fis = new FileInputStream(file.getPath());
					jsonArr.add(HDUtil.parseJson(fis));
					fis.close();
				}
			}
			jsonObj.add(JsonConstants.DEVICES, jsonArr);
		} catch (Exception ex) {			
			g_logger.log(Level.WARNING, "Unable to complete the fetchDevices() request: " + ex.getLocalizedMessage(), ex);
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
	public boolean moveIn(String srcAbsName, String destName) {
		try {
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
	public boolean purge() {
		File tmpDir = new File(this.directory);
		if (! tmpDir.isDirectory())
			return true;
		for (File file: new File(this.directory).listFiles()) 
			file.delete();
		this.cache.purge();
		return true;
	}

	public String getDirectory() {
		return directory;
	}
	
	public HDCache getCache() {
		return cache;
	}
	
	/**
	 * HDCache - Note : Objects are stored by reference. So modifying them outside the cache can change them in the cache.
	 */
	public static class HDCache {
		private static final int MAX_ENTRIES = 50000;
		private static Cache<String, Object> HDCACHED_OBJECTS;
		
		/**
		 * Instantiates new HD cache
		 */
		private HDCache() {
			HDCACHED_OBJECTS = CacheBuilder.newBuilder().maximumSize(MAX_ENTRIES).build();
		}
		
		/**
		 * Put an object into the cache
		 *
		 * @param key the key
		 * @param value the value
		 */
		public void write(String key, Object value) {
			HDCACHED_OBJECTS.put(key, value);
		}
		
		/**
		 * Gets object from the cache
		 *
		 * @param key the key
		 * @return an object or null if not found
		 */
		public Object read(String key) {
			return HDCACHED_OBJECTS.getIfPresent(key);
		}	
		
		/**
		 * Purges the cache
		 *
		 * @param void
		 * @return void
		 */
		public void purge() {
			HDCACHED_OBJECTS.invalidateAll();
		}
	}
}
