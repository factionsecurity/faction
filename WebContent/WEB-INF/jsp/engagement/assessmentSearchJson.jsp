<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
 
{ "data" : [
<%
boolean first =true;
%>
 <s:iterator value="assessments">
 	 <% if(first){first=false;}else{ %> , <% } %>
	 ["<s:property value="appId" />", 
	 "<s:property value="name"/>", 
	 "<s:iterator value="assessor"><s:property value="fname" /> <s:property value="lname" />; </s:iterator>",
	 "<s:property value="type.type"/>", 
	 "<s:property value="campaign.name"/>", 
	 "<s:property value="start"/>",
	 "<s:property value="end"/>",
     "<s:property value="completed"/>",
     "<s:property value="status"/>",
     <s:if test="finalReport != null">
     "<a href='../service/Report.pdf?guid=${finalReport.filename}' >Report</a>",
     </s:if>
     <s:else>
     "",
     </s:else>
     "<button class='btn  btn-success btn-md' onClick='edit(${Id})' id='edit<s:property value="Id"/>'><span class='fa fa-edit'></span></button>&nbsp;&nbsp;<button class='btn  btn-danger btn-md' onClick='del(this,${Id})' id='del<s:property value="Id"/>'><span class='fa fa-trash'></span></button>"
	 ]
 </s:iterator>
 ], "recordsTotal" : ${count}, "recordsFiltered" :  ${count}}
