<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Custom Persons Manage Page</title>
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
	<c:if test="${requestScope.error ne null}">
		<strong style="color: red;"><c:out
				value="${requestScope.error}"></c:out></strong>
	</c:if>
	<c:if test="${requestScope.success ne null}">
		<strong style="color: green;"><c:out
				value="${requestScope.success}"></c:out></strong>
	</c:if>
	<%-- Update Request --%>
	<form action="updateCountry.do" method="post">
		
		<label><span>Country: </span><input type="text" name="country"	placeholder="Enter Country name"></label><br>
		
		<input type="submit" class="button" value="Update Country">
	</form>
	
<br/><br/>
	<form action='home.do' method="get">
		<input type="submit" class="button" value="Back">
	</form>
</center>
</body>
</html>