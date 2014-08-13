package hd3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SecretConfig {
	
	private static Properties p = new Properties();
	
	static {
		FileInputStream fis;
		try {
			fis = new FileInputStream("hdapi_config.properties");
			p.load(fis);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}  catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public static String getUserName(String secretName) {
		String username = p.get("common.username").toString();
		return (secretName.equalsIgnoreCase(username)) ? username : secretName;
	} 
	
	public static String getSecret(String secretKey) {
		String key = p.get("common.secret").toString();
		return (secretKey.equalsIgnoreCase(key)) ? key : secretKey;
	}
	
	public static String getSiteId(String siteId) {
		String site_id = p.get("common.site_id").toString();
		return (siteId.equalsIgnoreCase(site_id)) ?  site_id : siteId;
	}	
	
}
