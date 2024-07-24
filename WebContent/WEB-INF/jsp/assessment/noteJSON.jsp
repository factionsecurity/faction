<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{ "name" : "<s:property value="note.name"/>",
"id" : "<s:property value="note.id"/>",
"note": "<s:property value="note.note"/>",
"first" : "<s:property value="note.createdBy.fname"/>",
"last" : "<s:property value="note.createdBy.lname"/>",
 "token" : "<s:property value="_token"/>"
}