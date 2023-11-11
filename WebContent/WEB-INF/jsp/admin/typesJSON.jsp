<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%boolean first =true;%>
{"data":[
<s:iterator value="check.types" var="id">
<% if(first){first=false;}else{ %> , <% } %>${id }
</s:iterator>
]}