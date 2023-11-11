<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<bs:row>
<bs:select name="Select Vulnerability" colsize="6" id="vuln_history_select">
</bs:select>
</bs:row>
<bs:datatable columns="Date,Type,Description,Action" classname="historyTable" id="historyTable"></bs:datatable>