package main.java.container.hd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import api.hd.Config;

public class Utility {
//	public static void initHDAPISettings(ServletContext context)
//			throws IOException {
//		String configFile = "/WEB-INF/hdapi_config.properties";
//		Config.init(context.getResourceAsStream(configFile));
//	}

	public static HashMap<String, String> getRequestHeaders(
			HttpServletRequest request) {
		HashMap<String, String> ret = new HashMap<String, String>();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			ret.put(key, request.getHeader(key));
		}
		return ret;
	}
	
	public static byte[] getBytesFromIS(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[102400];
		int readed = 0;
		try {
			while((readed = is.read(buffer)) != -1) {
				baos.write(buffer, 0, readed);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return baos.toByteArray();
	}
}
