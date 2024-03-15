<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
{
	"vulnId" : <s:property value="vulnId"/>,
	"name" : "<s:property value="name"/>",
	"desc" : "<s:property value="description"/>",
	"rec" : "<s:property value="recommendation"/>",
	"likelyhood" : <s:property value="likelyhood"/>,
	"impact" : <s:property value="impact"/>,
	"category" : <s:property value="category"/>, 
	"overall" : <s:property value="overall"/>,
	"cvss31String" : "<s:property value="cvss31String"/>",
	"cvss40String" : "<s:property value="cvss40String"/>",
	"cf" : [<s:iterator value="fields" status="stat"><s:if test="#stat.index!=0">,</s:if>{ "id" : ${id}, "typeid" : ${type.id}, "value" : "${value}"}</s:iterator>]
}
