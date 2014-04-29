package com.handsetdetection.test;

import hdapi3.HD3;
import hdapi3.HD3Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class LocalTest
 */
@WebServlet("/LocalTest")
public class LocalTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LocalTest() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private JsonElement normal = null;
	private JsonElement mobile = null;

	private void setupData() {
		normal = HD3Util.parseJson(getServletContext().getResourceAsStream(
				"/WEB-INF/normal_test_data.json"));
		mobile = HD3Util.parseJson(getServletContext().getResourceAsStream(
				"/WEB-INF/mobile_test_data.json"));
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Utility.initHDAPISettings(this.getServletContext());
		setupData();
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		HashMap<String, String> headers = Utility.getRequestHeaders(request);

		HD3 hd = new HD3();
		hd.setup(headers, request.getRemoteAddr(), request.getRequestURI());
		int normalCount = 0;
		int mobileCount = 0;
		int totalCount = 0;
		if (normal != null && normal.isJsonArray()) {
			JsonArray normals = (JsonArray) normal;
			Iterator<JsonElement> iter = normals.iterator();
			while (iter.hasNext()) {
				JsonElement item = iter.next();
				if (item.isJsonObject()) {
					JsonObject obj = (JsonObject) item;
					Set<Map.Entry<String, JsonElement>> entries = obj
							.entrySet();
					Iterator<Map.Entry<String, JsonElement>> iter2 = entries
							.iterator();
					while (iter2.hasNext()) {
						Map.Entry<String, JsonElement> item2 = iter2.next();
						hd.addDetectVar(item2.getKey(), item2.getValue()
								.getAsString());
					}
					if (hd.siteDetect()) {
						out.println(totalCount + " "
								+ obj.get("user-agent").getAsString() + " "
								+ hd.getReply() + "<br/><br/>");
						mobileCount++;
					} else {
						normalCount++;
					}
					totalCount++;
				}
			}
		} else {
			out.println("Invalid test data in file /WEB-INF/normal_test_data.json");
		}
		out.println("Expecting all detections to be other : Got " + mobileCount
				+ " Mobiles and " + normalCount + " Others<br/>");
		normalCount = 0;
		mobileCount = 0;
		totalCount = 0;
		if (mobile != null && mobile.isJsonArray()) {
			JsonArray mobiles = (JsonArray) mobile;
			Iterator<JsonElement> iter = mobiles.iterator();
			while (iter.hasNext()) {
				JsonElement item = iter.next();
				if (item.isJsonObject()) {
					JsonObject obj = (JsonObject) item;
					Set<Map.Entry<String, JsonElement>> entries = obj
							.entrySet();
					Iterator<Map.Entry<String, JsonElement>> iter2 = entries
							.iterator();
					while (iter2.hasNext()) {
						Map.Entry<String, JsonElement> item2 = iter2.next();
						hd.addDetectVar(item2.getKey(), item2.getValue()
								.getAsString());
					}
					if (hd.siteDetect()) {
						mobileCount++;
					} else {
						out.println(totalCount + " "
								+ obj.get("user-agent").getAsString() + " "
								+ hd.getReply() + "<br/><br/>");
						normalCount++;
					}
					totalCount++;
				}
			}
		} else {
			out.println("Invalid test data in file /WEB-INF/mobile_test_data.json");
		}
		out.println("Expecting all detections to be mobile : Got "
				+ mobileCount + " Mobiles and " + normalCount + " Others<br/>");
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
