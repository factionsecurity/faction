<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>
<%boolean first =true;%>{ "assessments" : [
<s:iterator value="assessments" var="asmt">
<s:iterator value="#asmt.assessor">
<% if(first){first=false;}else{ %> , <% } %>
	{ "id" : <s:property value="#asmt.id"/>,
	"appid" : "<s:property value="#asmt.appId"/>",
	"name" : "<s:property value="#asmt.name"/>",
	"userid" : "<s:property value="id"/>",
	"username" : "<s:property value="fname"/> <s:property value="lname"/>",
	"start" : "<s:date name="#asmt.start" format="MM/dd/yyyy"/>",
	"end" : "<s:date name="#asmt.end" format="MM/dd/yyyy"/>"
	}
</s:iterator>
</s:iterator>
],
"verifications" : [
<%first =true;%>
<s:iterator value="verifications">
<% if(first){first=false;}else{ %> , <% } %>
	{ "id" : <s:property value="id"/>,
	"appid" : "<s:property value="assessment.appId"/>",
	"appname" : "<s:property value="assessment.name"/>",
	"vuln" : "<s:property value="verificationItems[0].vulnerability.name"/>",
	"userid" : "<s:property value="assessor.id"/>",
	"username" : "<s:property value="assessor.fname"/> <s:property value="assessor.lname"/>",
	"start" : "<s:date name="start" format="MM/dd/yyyy"/>",
	"end" : "<s:date name="end" format="MM/dd/yyyy"/>",
	"status": "<s:property value="workflowStatus"/>"
	}
</s:iterator>

],
"ooo" : [
<%first =true;%>
<s:iterator value="ooo">
<% if(first){first=false;}else{ %> , <% } %>
	{ "id" : <s:property value="id"/>,
	"title" : "<s:property value="title"/>",
	"userid" : "<s:property value="user.id"/>",
	"username" : "<s:property value="user.fname"/> <s:property value="user.lname"/>",
	"start" : "<s:date name="start" format="MM/dd/yyyy"/>",
	"end" : "<s:date name="end" format="MM/dd/yyyy"/>"
	}
</s:iterator>

]
}
