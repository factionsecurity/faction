<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %><%boolean first =true;%>{ "vulns" : [
<s:iterator value="vulnerabilities" var="v">
<% if(first){first=false;}else{ %> , <% } %>
	{ "vulnId" : <s:property value="id"/>,
	"name" : "<s:property value="name"/>",
	"category" : "<s:property value="category.name"/>"
	}
</s:iterator>
]}
