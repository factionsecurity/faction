<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
</head>
<body>


	<link rel="stylesheet" href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet"
		href="${pageContext.request.contextPath}/dist/font-awesome-4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/dist/ionicons-2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.min.css">

	<link rel="stylesheet" href="${pageContext.request.contextPath}/start.css">


	<meta name="viewport"
		content="width=device-width, initial-scale=1, maximum-scale=1" />
	
		<div class="myform" style="padding: 50px 0">
			<div class="row">
				<div class="col-md-2">
					<img style="margin-top:90px; width:100%" src="${pageContext.request.contextPath}/tri-logo.png" alt="">
				</div>
				<div class="col-md-10">
					<div class="logo">ERROR! 
					<div class="alert alert-danger" id="messages" ><a href="#" data-dismiss="alert">
					<s:property value="exception"/><s:property value="_message"/></a></div>
					</div>
				</div>
			</div>
			<div class="row">
					<div class="col-md-12" style="height:500px">
					<textarea class="alert alert-danger" style="width:100%; height:500px; font-size:x-small" spellcheck="false"><s:property value="exceptionStack"/></textarea>
					</div>
			</div>
		</div>
			

		</body>
</html>
			

