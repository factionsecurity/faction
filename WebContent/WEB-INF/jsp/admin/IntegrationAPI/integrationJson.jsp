<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>
{ "code" : "<s:property value="code" />", 
<s:set var = "first" value = "%{true}" />"inputs" : [<s:iterator value="inputs" var="input"><s:if test="#first"><s:set var = "first" value = "%{false}" /></s:if><s:else>,</s:else>"<s:property value="input"/>"</s:iterator>], 
<s:set var = "first" value = "%{true}" />"outputs": [<s:iterator value="outputs" var="output"><s:if test="#first"><s:set var = "first" value = "%{false}" /></s:if><s:else>,</s:else>"<s:property value="output"/>"</s:iterator>]}