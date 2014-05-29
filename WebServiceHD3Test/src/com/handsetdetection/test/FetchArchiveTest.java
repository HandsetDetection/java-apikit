package com.handsetdetection.test;

import hdapi3.HD3;
import hdapi3.Settings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FetchArchiveTest
 */
@WebServlet("/FetchArchiveTest")
public class FetchArchiveTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FetchArchiveTest() {
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
			Settings.init(context.getResourceAsStream(configFile));
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		PrintWriter out = response.getWriter();
		HD3 hd3 = new HD3();
		
		out.println("<h1>Test complete!</h1>");
		long start = System.currentTimeMillis();	
		if (hd3.siteFetchArchive()) {
			int size = hd3.getRawReply().length;
			out.println("<p>fetchArchive : " + size + " bytes read</p>");
		} else {
			int size = hd3.getRawReply().length;
			out.println("</p>Problem Fetching Archive. "
					+ hd3.getReply().toString() + "  : " + size
					+ "bytes read</p>");
		}
		long elapsedTime = System.currentTimeMillis() - start;		
		float epalsedSec = elapsedTime/1000F;
		out.println("Done.<br/>");
		out.println("Elapsed time: " + epalsedSec + "ms");
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
