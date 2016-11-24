<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>	
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script>window['adrum-start-time'] = new Date().getTime();</script><script src="/adrum.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Custom Persons Manage Page</title>
<style>
table,th,td {
	border: 1px solid black;
}
</style>
</head>
<body>
        <H1>Custom People Manage Systems!!!!</H1>
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
	<c:url value="/editPerson.do" var="editURL"></c:url>

	<%-- Edit Request --%>
	<c:if test="${requestScope.person ne null}">
		<form action='<c:out value="${editURL}"></c:out>' method="post">
			ID: <input type="text" value="${requestScope.person.id}"
				readonly="readonly" name="id"><br>Person Name: <input
				type="text" value="${requestScope.person.name}" name="name"><br>
			Country: <input type="text" value="${requestScope.person.country}"
				name="country"><br> <input type="submit"
				value="Edit Person">
		</form>
	</c:if>

</body>
</html>
