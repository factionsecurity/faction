<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %><% boolean first=true;%>{ "data" : [<s:iterator value="combos">
<% if(first){ first=false;}else{ %>,<%}%>[ "<input class='remselect' type='checkbox' onclick='return false'/>",
"<s:property value="vuln.name" escapeJavaScript="true"/>" , 
"<s:property value="assessment.appId"/>\n<s:property value="assessment.name"/>", 
"<s:iterator value="assessment.assessor"><s:property value="fname" escapeJavaScript="true"/> <s:property value="lname" escapeJavaScript="true"/>\n</s:iterator>",
"<s:property value="vuln.tracking" escapeJavaScript="true"/>" , 
"<s:if test="isVer">Out for Verification</s:if>",
"<s:property value="vuln.overallStr" escapeJavaScript="true"/>" ,  
<s:if test="vuln.closed== null">
"<s:date name="vuln.opened"  format="MM/dd/yyyy"/>",
</s:if>
<s:else>
"<s:date name="vuln.opened"  format="MM/dd/yyyy"/>", 
</s:else>
"<s:date name="vuln.devClosed" format="MM/dd/yyyy"/>", 
"<s:date name="vuln.closed" format="MM/dd/yyyy"/>", 
{},
{ 
	"aid" :"<s:property value="assessment.id"/>", 
	"appId" :"<s:property value="assessment.appId" escapeJavaScript="true" />", 
	"vid" : "<s:property value="vuln.id"/>", 
	"dist" : "<s:property value="assessment.DistributionList" escapeJavaScript="true"/>", 
	"notes" : "<s:property value="assessment.AccessNotes"/>",
	"name" : "<s:property value="assessment.name" escapeJavaScript="true"/>",
	"vulnName" : "<s:property value="vuln.name" escapeJavaScript="true"/>",
	"tracking" : "<s:property value="vuln.tracking" escapeJavaScript="true"/>",
	"isVer" : ${isVer},
	"severity" : {
		"overall" :  "${vuln.overall}",
		"likelyhood" : "${vuln.likelyhood}",
		"impact" : "${vuln.impact}"
	},
	"reports": [<s:iterator value="reports" status="stat">
	<s:if test="!#stat.first">,</s:if>
	{	
		"name": "<s:property value="assessment.name" escapeJavaScript="true"/> - <s:property value="assessment.type.type" escapeJavaScript="true"/> <s:if test="retest == true">Retest </s:if>Report.docx", 
		"type": "<s:property value="assessment.type.type" escapeJavaScript="true"/> <s:if test="retest == true">Retest</s:if>", 
		"updated": "<s:date name="gentime" format="MM-dd-yyyy hh:mm:ss"/>", 
		"guid" : "<s:property value="filename"/>",
		"isRetest": <s:property value="retest"/>
	}</s:iterator>]
}]
</s:iterator>], 
"recordsTotal" : ${count}, 
"recordsFiltered" :  ${count} 
}
