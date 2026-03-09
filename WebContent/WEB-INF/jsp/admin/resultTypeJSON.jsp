<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%boolean first =true;%>
{
	"id": <s:property value="resultType.id" />,
	"name": "<s:property value="resultType.key" />",
	"variable": "<s:property value="resultType.variable" />",
	"type": <s:property value="resultType.type" />,
	"readonly": <s:property value="resultType.readonly" />,
	"defaultValue": "<s:property value="resultType.defaultValue" />",
	"fieldType": <s:property value="resultType.fieldType" />,
	"asmtTypes": [
	<s:iterator value="resultType.assessmentTypes" var="asmtType"> <% if(first){first=false;}else{ %> , <% } %>${asmtType.id }</s:iterator>
	]
}