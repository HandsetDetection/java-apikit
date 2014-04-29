package com.handsetdetection.test;

import hdapi3.HD3;
import hdapi3.Settings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SitesTest
 */
@WebServlet("/DeviceTest")
public class DeviceTest extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public DeviceTest() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @throws IOException 
	 * @see Servlet#init(ServletConfig)
	 */
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		ServletContext context = this.getServletContext();
		String configFile = "/WEB-INF/hdapi_config.properties";
		try {
			Settings.init(context.getResourceAsStream(configFile));
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
				
		PrintWriter out = response.getWriter();

		HD3 hd3 = new HD3();
		System.out.println(hd3.getUsername());
/*				
		out.println("<h1>Test Vendors</h1><code>");		
		if(hd3.deviceVendors()) {
			out.println(hd3.getReply().toString());
		} else {
			out.println(hd3.getError());
		}
		out.println("</code>");
		
		out.println("<h1>Nokia Models</h1><code>");
		if (hd3.deviceModels("Nokia")) {
			out.println(hd3.getReply().toString());
		} else {
			out.println(hd3.getError());
		}
		out.println("</code>");
		
		out.println("<h1>Nokia N95 Properties</h1><code>");
		if (hd3.deviceView("Nokia", "N95")) {
			out.println(hd3.getReply().toString());
		} else {
			out.println(hd3.getError());
		}
		out.println("</code>");

		out.println("<h1>Handsets with CDMA Support</h1><code>");
		if (hd3.deviceWhatHas("network", "CDMA")) {
			out.println(hd3.getReply().toString());
		} else {
			out.println(hd3.getError());
		}
		out.println("</code>"); */

		
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
