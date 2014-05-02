package hd3;

public class SecretConfig {
	
	public static String getUserName(String secretName) {
		return (secretName.equalsIgnoreCase("SECRET_NAME")) ? "203a2c5495" : "0";
	} 
	
	public static String getSecret(String secretKey) {
		return (secretKey.equalsIgnoreCase("SECRET_KEY")) ? "4Mcy7r7wDFdCDbg2" : "0";
	}
	
	public static String getSiteId(String siteId) {
		return (siteId.equalsIgnoreCase("SITE_ID")) ? "50538" : "0";
	}
}
