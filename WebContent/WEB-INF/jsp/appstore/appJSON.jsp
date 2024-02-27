<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>
{
	"uuid": "<s:property value="app.uuid"/>",
	"description" : "<s:property value="app.description"/>",
	"title" : "<s:property value="app.name"/>", 
	"hash" : "<s:property value="app.hash"/>", 
	"version" : "<s:property value="app.version"/>", 
	"author" : "<s:property value="app.author"/>", 
	"url" : "<s:property value="app.url"/>", 
	"logo" : "<s:property value="app.base64Logo"/>",
	"configs" : [<s:iterator value="app.getJSONConfig().keySet()" var="config" status="stat"><s:set var="type" value="app.getJSONConfig().get(#config).get('type')"/>
	{"<s:property value="config"/>": {"value":"<s:if test="#type == 'password'"></s:if><s:else><s:property value="app.getJSONConfig().get(#config).get('value')"/></s:else>", "type": "<s:property value="#type"/>"}}<s:if test="!#stat.last">,</s:if>
	</s:iterator>]
}