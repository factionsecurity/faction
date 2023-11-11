<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
{ "response" : "<s:property value="message" />",
    "token" : "<s:property value="_token"/>"
}
