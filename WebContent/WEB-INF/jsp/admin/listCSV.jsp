<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="text/csv; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %><% response.setHeader("Content-Disposition","inline; filename=checklist.csv"); %>"Question ID",Question
<s:iterator value="check.questions">${id },<s:property value="question" escapeCsv="true" escapeHtml="false"/>
</s:iterator>
