<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>
{
	"description" : "<s:property value="app.description"/>",
	"title" : "<s:property value="app.name"/>", 
	"version" : "<s:property value="app.version"/>", 
	"author" : "<s:property value="app.author"/>", 
	"url" : "<s:property value="app.url"/>", 
	"logo" : "<s:property value="app.base64Logo"/>",
	"configs" : [<s:iterator value="app.getHashMapConfig()" var="config" status="stat">
	{"<s:property value="key"/>": "<s:property value="value"/>"}<s:if test="!#stat.last">,</s:if>
	</s:iterator>]
}