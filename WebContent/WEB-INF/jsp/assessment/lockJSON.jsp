<%@page import="org.apache.struts2.components.Include"%><%@ taglib prefix="s" uri="/struts-tags" %><%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
{
 "token": "<s:property value="currentToken"/>",
<s:if test="isSummaryLockedbyAnotherUser()">
"summary": { "isLock" : <s:property value="assessment.isSummaryLock()" />,
  "lockBy" : "<s:property value="assessment.getSummaryLockBy().fname"/> <s:property value="assessment.getSummaryLockBy().lname"/>",
  "lockAt" : "<s:date name="assessment.getSummaryLockAt()" format="MM/dd/yyyy HH:mm:ss"/>",
  "updatedText" : "<s:property value="assessment.getSummary()"/>"
}
</s:if>
<s:if test="isSummaryLockedbyAnotherUser() && isRiskLockedbyAnotherUser()">
,
</s:if>
<s:if test="isRiskLockedbyAnotherUser()">
"risk" : { "isLock" : <s:property value="assessment.isRiskLock()" />,
  "lockBy" : "<s:property value="assessment.getRiskLockBy().fname" escapeJavaScript="true"/> <s:property value="assessment.getRiskLockBy().lname" escapeJavaScript="true"/>",
  "lockAt" : "<s:date name="assessment.getRiskLockAt()" format="MM/dd/yyyy HH:mm:ss"/>",
  "updatedText" : "<s:property value="assessment.getRiskAnalysis()"/>"
}
</s:if>
<s:if test="lockedVulns">
"vulns": [
	<s:iterator value="lockedVulns" status="stats">
	{ "id": "<s:property value="id"/>", 
	  "islock" : <s:property value="desc_lock" />,
	  "lockby" : "<s:property value="desc_locked_by.fname" escapeJavaScript="true"/> <s:property value="desc_locked_by.lname" escapeJavaScript="true"/>",
	  "lockat" : "<s:date name="desc_lock_time" format="mm/dd/yyyy hh:mm:ss"/>"
	}<s:if test="!#stats.last">,</s:if>
	</s:iterator>
],
"current": [
	<s:iterator value="currentVulns" status="stats">
	{ "id": "<s:property value="id"/>", 
	  "title" : "<s:property value='getSafeJSON(name)'/>",
	  "category" : "<s:property value="getCategory().getName()" escapeJavaScript="true"/>",
	  "severityName" : "<s:property value="overallStr" escapeJavaScript="true"/>",
	  "severity" : <s:if test="assessment.type.cvss31 || assessment.type.cvss40">"<s:property value="cvssScore" escapeJavaScript="true"/>" </s:if><s:else><s:property value="overall" escapeJavaScript="true"/></s:else>
	}<s:if test="!#stats.last">,</s:if>
	</s:iterator>
],
"notes": [
	<s:iterator value="lockedNotes" status="stats">
	{ "id": "<s:property value="id"/>", 
	  "islock" : <s:property value="noteLocked" />,
	  "lockby" : "<s:property value="noteLockedBy.fname" escapeJavaScript="true"/> <s:property value="noteLockedBy.lname" escapeJavaScript="true"/>",
	  "lockat" : "<s:date name="noteLockedAt" format="mm/dd/yyyy hh:mm:ss"/>"
	}<s:if test="!#stats.last">,</s:if>
	</s:iterator>
]
</s:if>
}