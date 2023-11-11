<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<bs:row>
<bs:select name="Select Vulnerability" colsize="6" id="vuln_action_select">
</bs:select>
</bs:row>
<bs:row>
<!--<s:if test="VerForm">-->
	<bs:button color="danger" size="md" colsize="3" text="Cancel Verification" id="closeVer"></bs:button>
<!--</s:if>-->
<bs:button color="warning" size="md" colsize="3" text="Change Severity" id="chSev"></bs:button>
<bs:button color="primary" size="md" colsize="3" text="Close in Dev" id="closeDev"></bs:button>
<bs:button color="success" size="md" colsize="3" text="Close in Prod" id="closeProd"></bs:button>
</bs:row>
<div style="height:500px"></div>

