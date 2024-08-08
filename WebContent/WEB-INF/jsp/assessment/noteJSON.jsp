<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{ "name" : "<s:property value="note.name"/>",
"id" : "<s:property value="note.id"/>",
"note": "<s:property value="note.encodedNote"/>",
"createdBy" : "<s:property value="note.createdBy.fname"/> <s:property value="note.createdby.lname"/>",
"updatedBy" : "<s:property value="note.updatedBy.fname"/> <s:property value="note.updatedBy.lname"/>",
"createdAt" : "<s:date name="note.created" format="MM/dd/yyyy hh:mm:ss"/>",
"updatedAt" : "<s:date name="note.updated" format="MM/dd/yyyy hh:mm:ss"/>",
 "token" : "<s:property value="_token"/>"
}