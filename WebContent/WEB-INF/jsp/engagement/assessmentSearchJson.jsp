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
     "<s:if test="completed==null">Open</s:if><s:else>Completed</s:else>",
	 "<s:iterator value="assessor"><s:property value="fname" /> <s:property value="lname" />; </s:iterator>",
	 "<s:property value="type.type"/>", 
	 "<s:property value="campaign.name"/>", 
	 "<s:date name="start" format="MM/dd/yyyy"/>",
	 "<s:date name="end" format="MM/dd/yyyy" />",
     "<s:date name="completed" format="MM/dd/yyyy"/>",
     <s:if test="finalReport != null">
     "<a href='../service/Report.pdf?guid=${finalReport.filename}' >Report</a>",
     </s:if>
     <s:else>
     "",
     </s:else>
     "<span class='vulnControl vulnControl-delete'><i class='fa fa-trash'></i></span>",
     "<s:property value="id"/>"
	 ]
 </s:iterator>
 ], "recordsTotal" : ${count}, "recordsFiltered" :  ${count}}
