<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>	
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script>window['adrum-start-time'] = new Date().getTime();</script><script src="/adrum.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Custom Persons Manage Page</title>
<link rel="stylesheet" type="text/css" href="style.css" media="all">
<style>
table,th,td {
	border: 1px solid black;
}
.backButton {
    margin-top: -35px;
    margin-left: 48%;
}
</style>
<script type="text/javascript">
function goHome(form) {
	form.action = "home.do";
	form.method = "get";
	form.submit();
}
</script>
</head>
<body>
<center class="elegant-aero">
	<h1>Custom People Manage Systems!!!!<span>Please fill all the texts in the fields.</span></h1>
	<hr/>
	<H2>Enter the name of the person and country he/she lives in:</H2>
	
	<%-- Person Add/Edit logic --%>
	<c:if test="${requestScope.error ne null}">
		<strong style="color: red;"><c:out
				value="${requestScope.error}"></c:out></strong>
	</c:if>
	<c:if test="${requestScope.success ne null}">
		<strong style="color: green;"><c:out
				value="${requestScope.success}"></c:out></strong>
	</c:if>
	<c:url value="/addPerson.do" var="addURL"></c:url>
	
	<%-- Add Request --%>
	<c:if test="${requestScope.person eq null}">
		<form action='<c:out value="${addURL}"></c:out>' method="post" id="addPerson">
			<label><span>Person Name: </span><input type="text" name="name" placeholder="Full Name"></label><br>
			<label><span>Country Name: </span><input type="text" name="country" placeholder="Country Name"></label><br> 
			<input type="submit" class="button" value="Add Person">
			<input type="button" class="button backButton" value="Back" onclick="goHome(this.form);">
		</form>
	</c:if>
</center>
</body>
</html>