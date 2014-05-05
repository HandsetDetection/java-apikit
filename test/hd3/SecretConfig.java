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
		return (secretName.equalsIgnoreCase("SECRET_NAME")) ? p.get("common.username").toString() : "0";
	} 
	
	public static String getSecret(String secretKey) {
		return (secretKey.equalsIgnoreCase("SECRET_KEY")) ? p.get("common.secret").toString() : "0";
	}
	
	public static String getSiteId(String siteId) {
		return (siteId.equalsIgnoreCase("SITE_ID")) ? p.get("common.site_id").toString() : "0";
	}	
	
}
