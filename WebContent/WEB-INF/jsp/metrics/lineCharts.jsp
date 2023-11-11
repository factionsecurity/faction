<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
	<bs:mco colsize="12">
		<bs:inputgroup name="Search Application ID:" colsize="2" id="appId"></bs:inputgroup>
		<bs:inputgroup name="Search Application Name:" colsize="2" id="appName"></bs:inputgroup>
		<bs:inputgroup name="Search Campaign Name:" colsize="2" id="campName"></bs:inputgroup>
		<bs:inputgroup name="Search User Name:" colsize="2" id="userName"></bs:inputgroup>
	</bs:mco>
</bs:row>
<hr>
<bs:row>
<bs:mco colsize="4"></bs:mco>
<bs:mco colsize="4"><div id="nodateMessage" style="margin-top:30px; font-size: large"> Search to display chart. </div></bs:mco>
<bs:mco colsize="4"></bs:mco>
</bs:row>
<bs:row>
	<bs:mco colsize="12">
		
		<canvas id="myChart" width="400" height="100"></canvas>
	</bs:mco>
</bs:row>
<bs:row>
<bs:mco colsize="12">
<bs:datatable columns=",Start,AppId,Name,Campaign,Users,End,Report" classname="" id="appDetails">

</bs:datatable>
</bs:mco>
</bs:row>


