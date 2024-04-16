<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %><% boolean first=true;%>{ "data" : [<s:iterator value="combos">
<% if(first){ first=false;}else{ %>,<%}%>[ "<input class='remselect' type='checkbox' onclick='return false'/>",
"<s:property value="assessment.appId"/>\n<s:property value="assessment.name"/>", 
"<s:iterator value="assessment.assessor"><s:property value="fname"/> <s:property value="lname"/>\n</s:iterator>",
"<s:property value="vuln.tracking"/>" , 
"<s:if test="isVer">Out for Verification</s:if>",
"<s:property value="vuln.name"/>" , 
"<s:property value="vuln.overallStr"/>" ,  
<s:if test="vuln.closed== null">
"<s:date name="vuln.opened"  format="MM/dd/yyyy"/> <i class='fa fa-edit' onclick='changeDate(this)'/>", 
</s:if>
<s:else>
"<s:date name="vuln.opened"  format="MM/dd/yyyy"/>", 
</s:else>
"<s:date name="vuln.devClosed" format="MM/dd/yyyy"/>", 
"<s:date name="vuln.closed" format="MM/dd/yyyy"/>", 
{},
{ 
	"aid" :"<s:property value="assessment.id"/>", 
	"appId" :"<s:property value="assessment.appId"/>", 
	"vid" : "<s:property value="vuln.id"/>", 
	"dist" : "<s:property value="assessment.DistributionList"/>", 
	"notes" : "<s:property value="assessment.AccessNotes"/>",
	"name" : "<s:property value="assessment.name"/>",
	"isVer" : ${isVer},
	"severity" : {
		"overall" :  "${vuln.overall}",
		"likelyhood" : "${vuln.likelyhood}",
		"impact" : "${vuln.impact}"
	}
}]
</s:iterator>], 
"recordsTotal" : ${count}, 
"recordsFiltered" :  ${count} 
}
