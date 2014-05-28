package com.handsetdetection.test;

import hdapi3.HD3;
import hdapi3.HD3Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class LocalTest
 */
@WebServlet("/LocalTest")
public class LocalTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private InputStream file;
       
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
		this.file =  getServletContext().getResourceAsStream("/WEB-INF/headers.txt");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Utility.initHDAPISettings(this.getServletContext());
		setupData();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		this.header(out);
		HashMap<String, String> headers = Utility.getRequestHeaders(request);		
		// Same instance of the object
		singleNewHD3(out, headers, request);		
		out.close();
	}
	
	private void singleNewHD3(PrintWriter out, HashMap<String, String> headers, HttpServletRequest request) {				
		int totalCount = 0;				
		long start = System.currentTimeMillis();						
		InputStreamReader isr = new InputStreamReader(this.file);
		BufferedReader reader = new BufferedReader(isr);	  
		String text = "";		
		try {
			while ((text = reader.readLine()) != null) {				
				String[] string_headers = text.split("\\|", -1);
				String userAgent = string_headers[0];
				String profile = string_headers[1];
				for(int j = 0; j < 10; j++)  {
					out.println("<tr>");
					HD3 hd = new HD3();
					hd.setup(headers, request.getRemoteAddr(), request.getRequestURI());
					hd.addDetectVar(userAgent, profile);
					if(hd.siteDetect()) {					
						try {
							JSONObject json = new JSONObject(hd.getReply().toString());							
							out.println("<td>" + totalCount + "</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_vendor") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_model") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_platform") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_platform_version") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_browser") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_browser_version") +"</td>");
							out.println("<td>"+userAgent+"</td>");
						} catch (JSONException e) {
							e.printStackTrace();
						}					
					} else {
						out.println("<td>" + totalCount + "</td>");
						out.println("<td colspan='7'> Got nothing for "+userAgent+"</td>");
					}
					totalCount++;
					out.println("</tr>");
				}
			}
		} catch (IOException e1) {			
			e1.printStackTrace();
		} finally {
			try {
				if(isr != null) isr.close();
				reader.close();				
			} catch (IOException e1) {			
				e1.printStackTrace();
			}		
		}
		out.println("</table>");								 	   
		long elapsed = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsed/1000F;
		int dps = (int) ((int) totalCount / elapsedTimeSec);							
		long elapsedTime = System.currentTimeMillis() - start;		
		float epalsedSec = elapsedTime/1000F;			
		out.println("<h1>Test Complete</h1>");
		out.println("<h3>Elapsed time: "+epalsedSec+"ms, Total detections: "+totalCount+", Detections per second: "+dps+"</h3>");			    	    
		try {
			file.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	private void multipleSameHD3(PrintWriter out, HashMap<String, String> headers, HttpServletRequest request) {		
		HD3 hd = new HD3();
		hd.setup(headers, request.getRemoteAddr(), request.getRequestURI());		
		int totalCount = 0;				
		long start = System.currentTimeMillis();	
		InputStreamReader isr = new InputStreamReader(this.file);
		BufferedReader reader = new BufferedReader(isr);	  		
		String text = "";		
		try {
			while ((text = reader.readLine()) != null) {				
				String[] string_headers = text.split("\\|", -1);
				String userAgent = string_headers[0];
				String profile = string_headers[1];
				for(int j = 0; j < 10; j++)  {		
					out.println("<tr>");
					hd.addDetectVar(userAgent, profile);
					if(hd.siteDetect()) {					
						try {
							JSONObject json = new JSONObject(hd.getReply().toString());							
							out.println("<td>" + totalCount + "</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_vendor") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_model") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_platform") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_platform_version") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_browser") +"</td>");
							out.println("<td>"+ json.getJSONObject("hd_specs").get("general_browser_version") +"</td>");
							out.println("<td>"+userAgent+"</td>");
						} catch (JSONException e) {
							e.printStackTrace();
						}					
					} else {
						out.println("<td>" + totalCount + "</td>");
						out.println("<td colspan='7'> Got nothing for</td>");
					}
					totalCount++;
					out.println("</tr>");					
				}
			}
		} catch (IOException e1) {			
			e1.printStackTrace();
		} finally {
			try {
				if(isr != null) isr.close();
				reader.close();				
			} catch (IOException e1) {			
				e1.printStackTrace();
			}		
		}
		out.println("</table>");								 	   
		long elapsed = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsed/1000F;
		int dps = (int) ((int) totalCount / elapsedTimeSec);							
		long elapsedTime = System.currentTimeMillis() - start;		
		float epalsedSec = elapsedTime/1000F;				
		out.println("<h1>Test Complete</h1>");
		out.println("<h3>Elapsed time: "+epalsedSec+"ms, Total detections: "+totalCount+", Detections per second: "+dps+"</h3>");			    	    
		try {
			file.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	private void header(PrintWriter out) {
		out.println("<table style='font-size:12px'><tr><th>Count</th><th>Vendor</th><th>Model</th><th>Platform</th><th>Platform Version</th><th>Browser</th><th>Browser Version</th><th>HTTP Headers</th></tr>");
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
