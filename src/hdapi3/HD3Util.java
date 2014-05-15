package hdapi3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * The Class HD3Util.
 */
public class HD3Util {
	
	/**
	 * Checks if is null or empty.
	 *
	 * @param v the v
	 * @return true, if is null or empty
	 */
	public static boolean isNullOrEmpty(String v) {		
		return v == null || "".equals(v) || "".equals(v.trim());
	}

	/**
	 * Md5.
	 *
	 * @param input the input
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String md5(String input) throws Exception{
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(input.getBytes("ASCII"));
		byte s[] = m.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length; i++) {
			sb.append(Integer.toHexString((0x000000ff & s[i]) | 0xffffff00)
					.substring(6));
		}
		return sb.toString();
	}
	
	/**
	 * To unique json array.
	 *
	 * @param strArray the str array
	 * @return the json array
	 */
	public static JsonArray toUniqueJsonArray(List<String> strArray) {
		Set<String> set = new HashSet<String>(strArray);
		String[] temp = new String[0];
		temp = set.toArray(temp);
		
		List<String> uniqueArray = Arrays.asList(temp);
		Collections.sort(uniqueArray);
		JsonArray ret = new JsonArray();
		for (String str: uniqueArray) {
			ret.add(new JsonPrimitive(str));
		}
		return ret;
	}
	
	/**
	 * Checks if is null element.
	 *
	 * @param elem the elem
	 * @return true, if is null element
	 */
	public static boolean isNullElement(JsonElement elem) {
		return elem == null || elem.isJsonNull();
	}
	
 	/**
	  * Save bytes as file.
	  *
	  * @param filename the filename
	  * @param content the content
	  * @return true, if successful
	  * @throws Exception the exception
	  */
	 public static boolean saveBytesAsFile(String filename, byte[] content) throws Exception{
 		return saveBytesAsFile(new File(filename), content);
 	}

 	/**
	  * Save bytes as file.
	  *
	  * @param file the file
	  * @param content the content
	  * @return true, if successful
	  * @throws Exception the exception
	  */
	 public static boolean saveBytesAsFile(File file, byte[] content) throws Exception{
 		if (file.createNewFile()) {
 			FileOutputStream fos = new FileOutputStream(file);
 			fos.write(content);
 			fos.flush();
 			fos.close();

			return true;
		}
		return false;
	}
	
	/**
	 * Parses the json.
	 *
	 * @param in the in
	 * @return the json element
	 */
	public static JsonElement parseJson(InputStream in) {
		JsonParser parser = new JsonParser();
		InputStreamReader reader = new InputStreamReader(in);
		JsonElement response = parser.parse(reader);
		try {
			reader.close();
		} catch (Exception e) {
			
		}
		return response;
	}
	
	/**
	 * Parses the headers.
	 *
	 * @param obj the obj
	 * @return the hash map
	 */
	public static HashMap<String, String> parseHeaders(JsonObject obj) {
		if (isNullElement(obj)) return null;
		HashMap<String, String> ret = new HashMap<String, String>();
		Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
		Iterator<Map.Entry<String, JsonElement>> iter = entries.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, JsonElement> entry = iter.next();
			String key = entry.getKey();
			String value = entry.getValue().isJsonNull() ? null: entry.getValue().getAsString();
			ret.put(key == null? key: key.toLowerCase(), value == null? null: value.toLowerCase());
		}
		return ret;
	}
	
	/**
	 * Gets the primary value.
	 *
	 * @param elem the elem
	 * @return the primary value
	 */
	public static JsonElement getPrimaryValue(JsonElement elem) {
		if (isNullElement(elem)) return null;
		if (elem.isJsonPrimitive()) {
			return elem;
		} else if (elem.isJsonObject()) {
			JsonObject obj = (JsonObject) elem;
			Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
			for( Map.Entry<String, JsonElement> entry: set) {
				return entry.getValue();
			}
		}
		return null;
	}

	// http://pragmatic-coding.blogspot.com.au/2012/05/gson-missing-get-element-function.html
	/**
	 * Gets the.
	 *
	 * @param value the value
	 * @param jObj the j obj
	 * @return the json element
	 */
	public static JsonElement get(String value, JsonObject jObj) {
		if (jObj == null)
			return null;
		
        // check current level for the key before descending
        if (jObj.isJsonObject() && jObj.has(value)) {
            return jObj.get(value);
        }

        // get all entry sets
        Set<Entry<String, JsonElement>> entries = jObj.entrySet();
       
        for (Entry<String, JsonElement> entry : entries) {
           
            // cache the current value since retrieval is done so much
            JsonElement curVal = entry.getValue();
           
            if (curVal.isJsonArray()) {
                for (JsonElement el : curVal.getAsJsonArray()) {
                    // recursively traverse the sub-tree
                    JsonElement res = get(value, el.getAsJsonObject());
                    if (res != null)
                        return res;
                }
            } else if (curVal.isJsonObject()) {
                // traverse sub-node
                return get(value, curVal.getAsJsonObject());
            }
        }
        return null;
    }
}
