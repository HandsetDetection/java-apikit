package main.java.container.hd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import api.hd.HD;
import api.hd.Config;

/**
 * Servlet implementation class SiteDetectionTest
 */
//@WebServlet("/SiteDetectionTest")
public class SiteDetectionTest extends TestServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SiteDetectionTest() {
        super();
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
			Config.init(context.getResourceAsStream(configFile));
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		HD hd = new HD(context.getResourceAsStream("hdapi_config.properties"));
		if (hd.isUseLocal()) {
			hd.deviceFetchArchive();
		}

		out.println("<h1>Simple Detection - Using your web browser standard headers (expect NotFound)</h1><p>");
		HashMap<String, String> headers = Utility.getRequestHeaders(request);
		hd.setup(headers, request.getRemoteAddr(), request.getRequestURI());
		if (hd.deviceDetect(null)) {
			out.println(hd.getReply().toString());
		} else {
			out.println(hd.getError());
		}
		out.println("</p>");

		out.println("<h1>Simple Detection - Setting Headers for an N95</h1><p>");
		hd.addDetectVar("user-agent", "NokiaN95");
		hd.addDetectVar("x-wap-profile",
				"http://nds1.nds.nokia.com/uaprof/NN95-1r100.xml");
		if (hd.deviceDetect(null)) {
			out.println(hd.getReply().toString());
		} else {
			out.println(hd.getError());
		}
		out.println("</p>");

		out.println("<h1>Simple Detection - Passing a different ip address</h1><p>");
		hd.addDetectVar("ipaddress", "64.34.165.180");
		hd.addDetectVar("options", "geoip,hd_specs");
		if (hd.deviceDetect(null)) {
			out.println(hd.getReply().toString());
		} else {
			out.println(hd.getError());
		}
		out.println("</p>");

		out.println("<h1>Simple Detection - Getting legacy options and current information</h1><p>");
		hd.addDetectVar("options", "hd_specs,legacy,product_info,display");
		if (hd.deviceDetect(null)) {
			out.println(hd.getReply().toString());
		} else {
			out.println(hd.getError());
		}
		out.println("</p>");

		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
