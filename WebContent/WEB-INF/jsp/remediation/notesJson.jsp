<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%boolean isFirst=true; %>
{ "notes" : [
<s:iterator value="notes">
<%if(isFirst){ isFirst=false;}else {%>,<%} %>
{"date" : "<s:date name="created" format="yyyy-MM-dd hh:mm:ss"/>", "gid" : "<s:property value="uuid"/>",  "note" : "<s:property value="note" escapeJavaScript="true"/>", "creator" : "<s:property value="creatorObj.fname"/> <s:property value="creatorObj.lname"/>"}
</s:iterator>
]}