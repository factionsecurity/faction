<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{ "name" : "<s:property value="getSafeJSON(current.name)"/>",
"description" : "<s:property value="description"/>",
"recommendation" : "<s:property value="recommendation"/>",
"details" : "<s:property value="details"/>",
"likelyhood" : "<s:property value="current.likelyhood"/>",
"impact" : "<s:property value="current.impact"/>",
"overall" : "<s:property value="current.overall"/>",
"cf" : [<s:iterator value="current.CustomFields" status="stat"><s:if test="#stat.index!=0">,</s:if>{ "id" : ${id}, "typeid" : ${type.id}, "value" : "${value}"}</s:iterator>],
"dfname" : "<s:property value="current.defaultVuln.name" escapeJavaScript="true"/>",
"dfvulnid" : "<s:property value="current.defaultVuln.id"/>",
"dfcat" : "<s:property value="current.defaultVuln.category.name" escapeJavaScript="true"/>",
"dfcatid" : "<s:property value="current.defaultVuln.category.id"/>",
"catid" : "<s:property value="current.category.id"/>",
"cvssScore" : "<s:property value="current.cvssScore" escapeJavaScript="true"/>",
"cvssString" : "<s:property value="current.cvssString" escapeJavaScript="true"/>",
"section" : "<s:property value="current.section" escapeJavaScript="true"/>",
"sectionPretty" : "<s:property value="current.sectionPretty" escapeJavaScript="true"/>"
}


