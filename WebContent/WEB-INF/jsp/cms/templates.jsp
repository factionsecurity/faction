<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<style>
.page {
cursor: pointer;
}
.page:hover{
	 font-weight: bold;
}
.css{
width:100%;
height: 700px;
}
</style>
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-book"></i> Report Templates
      <small><a href="http://blog.fusesoftsecurity.com/p/report-designer-tags.html" target="_blank"></a></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  <bs:row>
  <bs:mco colsize="12">
  <<bs:datatable columns="Template Name,Team" classname="" id="" >
  <s:iterator var="reports">
  		<tr id="rpt_${id }"><td>${name }</td><td>${team.teamName}</td></tr>
  </s:iterator>
  </bs:datatable>
  </bs:mco>
  </bs:row>
  	
  
  <jsp:include page="../footer.jsp" />
	<script src="../dist/js/cms.js" charset="utf-8"></script>
  </body>
</html>