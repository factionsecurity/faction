<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>
{
	"message" : "<s:property value="message"/>",
	"title" : "<s:property value="app.name"/>", 
	"version" : "<s:property value="app.version"/>", 
	"author" : "<s:property value="app.author"/>", 
	"url" : "<s:property value="app.url"/>", 
	"logo" : "<s:property value="app.base64Logo"/>"
}