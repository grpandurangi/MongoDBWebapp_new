<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>	    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script>window['adrum-start-time'] = new Date().getTime();</script><script src="/adrum.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Persons Management</title>
<link rel="stylesheet" type="text/css" href="style.css" media="all">
<style>
table,th,td {
	border: 1px solid black;
}
</style>
</head>
<body>
<center class="elegant-aero">
	<H1>Custom People Manage Systems!!!!</H1>
	<hr/>
	<a href="addPersonPage.do">Add Person</a><br/>
	<a href="viewPersonPage.do">View Persons</a><br/>
	<a href="searchPersonPage.do">Search Person</a><br/>
	<!-- <a href="updateCountryPage.do">Edit Country</a><br/> -->
	<a href="pageNotFound.do">404 Page not found Error</a><br/>
	<a href="serverError.do">Server Error</a><br/>
	<br/>
	
	<c:if test="${requestScope.success ne null}">
		<strong style="color: green;"><c:out
				value="${requestScope.success}"></c:out></strong>
	</c:if>
	
</center>
</body>
</html>
