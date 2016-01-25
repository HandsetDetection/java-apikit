package main.java.container.hd;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import api.hd.HD;
import api.hd.Config;

/**
 * Servlet implementation class FetchArchiveTest
 */
//@WebServlet("/FetchArchiveTest")
public class FetchArchiveTest extends TestServlet {
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
		//response.setCharacterEncoding("UTF-8");
		
		PrintWriter out = response.getWriter();
		HD hd = new HD(Utility.getBytesFromIS(context.getResourceAsStream("hdapi_config.properties")));
		
		out.println("<h1>Test complete!</h1>");
		long start = System.currentTimeMillis();	
		if (hd.deviceFetchArchive()) {
			int size = hd.getRawReply().length;
			out.println("<p>fetchArchive : " + size + " bytes read</p>");
		} else {
			int size = null != hd.getRawReply() ? hd.getRawReply().length : 0;						
			out.println("</p>Problem Fetching Archive. "
					+ hd.getReply().toString() + "  : " + size
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
