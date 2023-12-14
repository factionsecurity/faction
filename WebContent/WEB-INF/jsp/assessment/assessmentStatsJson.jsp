<%@page import="org.apache.struts2.components.Include"%><%@ taglib prefix="s" uri="/struts-tags" %><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
{
	"data" : {
		"colors": [<s:iterator value="colors" status="stat" var="color">
    	"<s:property value="color" />"<s:if test="!#stat.last">,</s:if></s:iterator>
		],
		"severityNames": [<s:iterator value="vulnMap" status="stat">
		"<s:property value="key"/>"<s:if test="!#stat.last">,</s:if></s:iterator>
		],
    	"vulns": [<s:iterator value="vulnMap" status="stat">
    	{"severity":"<s:property value="key"/>", "count": <s:property value="value"/>}<s:if test="!#stat.last">,</s:if></s:iterator>
    	],
    	"categories": [<s:iterator value="catMap" status="stat">
    	{"category":"<s:property value="key"/>", "count": <s:property value="value"/>}<s:if test="!#stat.last">,</s:if></s:iterator>
    	]
   }
   
}