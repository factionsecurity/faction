<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>
{
	"disabled" : [<s:iterator value="disabledApps" status="stat">{ 
	"id" : "<s:property value="id"/>", 
	"title" : "<s:property value="name"/>", 
	"version" : "<s:property value="version"/>", 
	"author" : "<s:property value="author"/>", 
	"url" : "<s:property value="url"/>", 
	"logo" : "<s:property value="base64Logo"/>", 
	"enabled" : <s:property value="enabled"/>, 
	"description" : "<s:property value="description"/>"
}<s:if test="!#stat.last">,</s:if>
	</s:iterator>],
	"assessment" : [<s:iterator value="assessmentApps" status="stat">{ 
	"id" : "<s:property value="id"/>", 
	"title" : "<s:property value="name"/>", 
	"version" : "<s:property value="version"/>", 
	"author" : "<s:property value="author"/>", 
	"url" : "<s:property value="url"/>", 
	"logo" : "<s:property value="base64Logo"/>", 
	"enabled" : <s:property value="enabled"/>, 
	"description" : "<s:property value="description"/>"
}<s:if test="!#stat.last">,</s:if>
	</s:iterator>],
	"vulnerability" : [<s:iterator value="vulnerabilityApps" status="stat">{ 
	"id" : "<s:property value="id"/>", 
	"title" : "<s:property value="name"/>", 
	"version" : "<s:property value="version"/>", 
	"author" : "<s:property value="author"/>", 
	"url" : "<s:property value="url"/>", 
	"logo" : "<s:property value="base64Logo"/>", 
	"enabled" : <s:property value="enabled"/>, 
	"description" : "<s:property value="description"/>"
}<s:if test="!#stat.last">,</s:if>
	</s:iterator>],
	"verification" : [<s:iterator value="verificationApps" status="stat">{ 
	"id" : "<s:property value="id"/>", 
	"title" : "<s:property value="name"/>", 
	"version" : "<s:property value="version"/>", 
	"author" : "<s:property value="author"/>", 
	"url" : "<s:property value="url"/>", 
	"logo" : "<s:property value="base64Logo"/>", 
	"enabled" : <s:property value="enabled"/>, 
	"description" : "<s:property value="description"/>"
}<s:if test="!#stat.last">,</s:if>
	</s:iterator>],
	"inventory" : [<s:iterator value="inventoryApps" status="stat">{ 
	"id" : "<s:property value="id"/>", 
	"title" : "<s:property value="name"/>", 
	"version" : "<s:property value="version"/>", 
	"author" : "<s:property value="author"/>", 
	"url" : "<s:property value="url"/>", 
	"logo" : "<s:property value="base64Logo"/>", 
	"enabled" : <s:property value="enabled"/>, 
	"description" : "<s:property value="description"/>"
}<s:if test="!#stat.last">,</s:if>
	</s:iterator>]
}