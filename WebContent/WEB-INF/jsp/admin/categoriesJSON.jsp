<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>[<s:iterator value="categories" status="stat">
{ "name": "${name}", "id": ${id }}<s:if test="!#stat.last">,</s:if></s:iterator>
]