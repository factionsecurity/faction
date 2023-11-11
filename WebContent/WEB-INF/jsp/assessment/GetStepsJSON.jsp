<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%
boolean first =true;
%>
{ "steps" : [
<s:iterator value="steps">
<% if(first){first=false;}else{ %> , <% } %>
	{ "vulnId" : <s:property value="vulnid"/>,
	"name" : "<s:property value="descBase64"/>",
	"order" : <s:property value="stepNum"/>,
	"stepId" : <s:property value="id"/>,
	"hasImage" : false}
</s:iterator>
],
"token" : "${_token}"
}
