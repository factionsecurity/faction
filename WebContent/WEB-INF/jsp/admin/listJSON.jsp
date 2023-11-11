<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:property value="question" escapeHtml="true" />
<%boolean first =true;%>
{"data":[
<s:iterator value="check.questions" var="q">
["${id }","<textarea class='form-control' style='min-width: 100%' id='q${id }'><s:property value='%{question.replace("\'"," ")}' escapeHtml="true" escapeJavaScript="true" /></textarea>","<button class='btn btn-primary' onclick='saveQuestion(${id })'><span class='fa fa-save'></span></button><button class='btn btn-danger' onclick='deleteQuestion(this,${check.id }, ${id })'><span class='fa fa-trash'></span></button>" ],
</s:iterator>
["Add New","<textarea class='form-control' style='min-width: 100%' id='newQuest'></textarea>","<button class='btn btn-success' onclick='addQuestion(this)'><span class='fa fa-plus'></span></button>"]
]}