<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Main Index</title>	
	<c:url value="/resources/css/styles.css" var="cssURL" />  
    <link rel="stylesheet" type="text/css" media="screen" href="${cssURL}" />  	
</head>
<body>
 <header>
  <nav>
   <ul>
    <li>
     <a href="<%=request.getContextPath()%>/DeviceTest">Test Device<!--<span class="caret"></span>--></a>     
    </li>
    <li>
     <a href="<%=request.getContextPath()%>/FetchArchiveTest">Test Fetch Archive</a>
    </li>
    <li>
     <a href="<%=request.getContextPath()%>/SiteDetectionTest">Test Site Detection</a>
    </li>
    <li>
     <a href="<%=request.getContextPath()%>/LocalTest">Test Local</a>
    </li>
   </ul>			
  </nav>
 </header>
 <section id="main_section">