<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{
"username" : "<s:property value="selectedUser.username"/>",
"fname" : "<s:property value="selectedUser.fname"/>",
"lname" : "<s:property value="selectedUser.lname"/>",
"email" : "<s:property value="selectedUser.email"/>",
"team" : "<s:property value="selectedUser.team.id"/>",
"authMethod" : "<s:property value="selectedUser.authMethod"/>",
"mgr" : <s:property value="selectedUser.permissions.manager"/>,
"eng" : <s:property value="selectedUser.permissions.engagement"/>,
"admin" : <s:property value="selectedUser.permissions.admin"/>,
"assessor" : <s:property value="selectedUser.permissions.assessor"/>,
"accesscontrol" : <s:property value="selectedUser.permissions.accessLevel"/>,
"rem" : <s:property value="selectedUser.permissions.remediation"/>,
"inactive" : <s:property value="selectedUser.inActive"/>,
"apikey" : "<s:property value="apiKey"/>"
}