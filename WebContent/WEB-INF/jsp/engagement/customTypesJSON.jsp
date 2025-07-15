<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>[<%boolean firstType =true;%>
<s:iterator value="types"><% if(firstType){firstType=false;}else{ %> , <% } %>
	{
		"id": <s:property value="id" />,
		"name": "<s:property value="key" />",
		"variable": "<s:property value="variable" />",
		"type": <s:property value="type" />,
		"readonly": <s:property value="readonly" />,
		"defaultValue": "<s:property value="defaultValue" />",
		"fieldType": <s:property value="fieldType" />
	}
</s:iterator>
]