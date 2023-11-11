<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{ "users" : [ 
<%
boolean first =true;
%>
 <s:iterator value="assessors">
 	 <% if(first){first=false;}else{ %> , <% } %>
	 { "id" : <s:property value="id"/>, 
	 "name" : "<s:property value="fname"/> <s:property value="lname"/>", 
	 "team" : "<s:property value="team.TeamName"/>",
	 "count": <s:property value="VerificationCount"/>,
	 "ocount": <s:property value="OOOCount"/>,
	 "acount": <s:property value="assessmentCount"/>
	 }
 </s:iterator>
 ]}
