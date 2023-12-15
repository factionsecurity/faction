<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<s:set var="height" value="'200px'"/>
<bs:row>
<bs:mco colsize="8">
<canvas id="vulnStats" style="height: ${height}"></canvas>
</bs:mco>
<bs:mco colsize="2">
</bs:mco>
<bs:mco colsize="2">
<s:hidden value="%{id}" id="assessmentId"></s:hidden>
<div style="height: ${height}; width: ${height}; ">
<canvas id="catStats" ></canvas>
</div>
</bs:mco>
</bs:row>
<bs:row>
&nbsp;
</bs:row>
<script src="../dist/js/assessment_stats.js"></script>
