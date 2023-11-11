<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<bs:row>
<bs:dt name="Set Dates For Retest" colsize="12" id="dates"></bs:dt>
<input id="hiddenid" value="${vid}" type="hidden"/>
</bs:row>
<bs:row>
<bs:box type="" title="Enter Creds and Scope">
<bs:mco colsize="12">
	<textarea id="notes" style="display:none"><s:property value="notes"/></textarea>
</bs:mco>
</bs:box>
</bs:row>



</body>
</html>