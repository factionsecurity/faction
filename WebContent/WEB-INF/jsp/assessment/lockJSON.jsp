<%@page import="org.apache.struts2.components.Include"%><%@ taglib prefix="s" uri="/struts-tags" %><%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
{
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
}