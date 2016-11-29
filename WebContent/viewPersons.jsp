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
	
	<form action='home.do' method="get">
		<input type="submit" class="button" value="Back">
	</form>
	<%-- Persons List Logic --%>
	<c:if test="${not empty requestScope.persons}">
		<table>
			<tbody>
				<tr>
					<th>ID</th>
					<th>Name</th>
					<th>Country</th>
					<th>Edit</th>
					<th>Delete</th>
				</tr>
				<c:forEach items="${requestScope.persons}" var="person">
					<c:url value="/editPersonPage.do" var="editURL">
						<c:param name="id" value="${person.id}"></c:param>
					</c:url>
					<c:url value="/deletePerson.do" var="deleteURL">
						<c:param name="id" value="${person.id}"></c:param>
					</c:url>
					<tr>
						<td><c:out value="${person.id}"></c:out></td>
						<td><c:out value="${person.name}"></c:out></td>
						<td><c:out value="${person.country}"></c:out></td>
						<td><a
							href='<c:out value="${editURL}" escapeXml="true"></c:out>'>Edit</a></td>
						<td><a
							href='<c:out value="${deleteURL}" escapeXml="true"></c:out>'>Delete</a></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:if>

	
</center>
</body>
</html>