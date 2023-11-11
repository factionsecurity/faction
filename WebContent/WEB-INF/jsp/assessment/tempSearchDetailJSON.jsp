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
	{ "text" : "<s:property value="text" escapeJavaScript="true" escapeHtml="false"/>"
	}
	
</s:iterator>
]}
