package com.handsetdetection.test;

import hdapi3.Settings;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class Utility {
	public static void initHDAPISettings(ServletContext context)
			throws IOException {
		String configFile = "/WEB-INF/hdapi_config.properties";
		Settings.init(context.getResourceAsStream(configFile));
	}

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
}
