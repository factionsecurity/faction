<%@page import="org.apache.struts2.components.Include"%><%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%><%@ taglib prefix="s" uri="/struts-tags" %>{
	"message" : "<s:property value="message" escapeHtml="false"/><s:property value="_message" escapeHtml="false"/>",
	"token" : "<s:property value="_token"/>"
}
