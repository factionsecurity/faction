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
<s:if test="isSummaryLockedbyAnotherUser() && (isRiskLockedbyAnotherUser() || isRiskLockedbyAnotherUser())">
,
</s:if>
<s:if test="isRiskLockedbyAnotherUser()">
"risk" : { "isLock" : <s:property value="assessment.isRiskLock()" />,
  "lockBy" : "<s:property value="assessment.getRiskLockBy().fname"/> <s:property value="assessment.getRiskLockBy().lname"/>",
  "lockAt" : "<s:date name="assessment.getRiskLockAt()" format="MM/dd/yyyy HH:mm:ss"/>",
  "updatedText" : "<s:property value="assessment.getRiskAnalysis()"/>"
}
</s:if>

<s:if test="isNotesLockedbyAnotherUser() && isRiskLockedbyAnotherUser())">
,
</s:if>
<s:if test="isNotesLockedbyAnotherUser()">
"notes": { "isLock" : <s:property value="assessment.isNotesLock()" />,
  "lockBy" : "<s:property value="assessment.getNotesLockBy().fname"/> <s:property value="assessment.getNotesLockBy().lname"/>",
  "lockAt" : "<s:date name="assessment.getNotesLockAt()" format="MM/dd/yyyy HH:mm:ss"/>",
  "updatedText" : "<s:property value="assessment.getNotes()"/>"
}
</s:if>
<s:if test="lockedVulns">
"vulns": [
	<s:iterator value="lockedVulns" status="stats">
	{ "id": "<s:property value="id"/>", 
	  "islock" : <s:property value="desc_lock" />,
	  "lockby" : "<s:property value="desc_locked_by.fname"/> <s:property value="desc_locked_by.lname"/>",
	  "lockat" : "<s:date name="desc_lock_time" format="mm/dd/yyyy hh:mm:ss"/>"
	}<s:if test="!#stats.last">,</s:if>
	</s:iterator>
],
"current": [
	<s:iterator value="currentVulns" status="stats">
	{ "id": "<s:property value="id"/>", 
	  "title" : "<s:property value="name" />",
	  "category" : "<s:property value="getCategory().getName()"/>",
	  "severityName" : "<s:property value="overallStr"/>",
	  "severity" : <s:if test="assessment.type.cvss31 || assessment.type.cvss40">"<s:property value="cvssScore"/>" </s:if><s:else><s:property value="overall"/></s:else>
	}<s:if test="!#stats.last">,</s:if>
	</s:iterator>
]
</s:if>
}