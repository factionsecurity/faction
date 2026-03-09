<%@page import="org.apache.struts2.components.Include"%><%@ taglib prefix="s" uri="/struts-tags" %><%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
{<s:iterator value="allLocks" status="rowStatus">
"${lockedField}": { "lockedBy": "${lockedBy.fname} ${lockedBy.lname}", 
"lockedAt" : "<s:date name="lockedAt" format="MM/dd/yyyy HH:mm:ss"/>",
"isLocked" : true,
"updatedText" : "${updatedText}"
}
<s:if test="!#rowStatus.last">,</s:if>
</s:iterator>
}