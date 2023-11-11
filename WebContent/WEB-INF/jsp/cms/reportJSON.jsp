<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
{ "title" : "${title}", "isFront" : ${front}, "content" : "${document}", "header" : "<s:property value="header"/>", "footer" : "${footer}"}