<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%
boolean first =true;
%>
{ "templates" : [
<s:iterator value="boilers">
<% if(first){first=false;}else{ %> , <% } %>
	{ "tmpId" : <s:property value="id"/>,
	"title" : "<s:property value="title"/>",
	"type" : "<s:property value="type"/>",
	"user": "<s:property value="user.fname"/> <s:property value="user.lname"/>",
	"created": "<s:date name="created" format="dd/MM/yyyy"/>",
	"active": <s:property value="active"/>
	}
	
</s:iterator>
],
"token" : "${_token}"
}
