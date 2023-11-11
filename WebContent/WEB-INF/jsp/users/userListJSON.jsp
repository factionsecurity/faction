<%@page import="org.apache.struts2.components.Include"%><%@ taglib prefix="s" uri="/struts-tags" %><%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>{ "users" : [ 
 <s:iterator value="users" status="stat">
	 { 
	 "username" : "<s:property value="username"/>",
	 "fname" : "<s:property value="fname"/>",
	 "lname" : "<s:property value="lname"/>",
	 "email": "<s:property value="email"/>"
	 }
	 <s:if test="!#stat.last">,</s:if>
 </s:iterator>
 ]
 }
