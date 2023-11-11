<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<script src="../dist/js/dashboard.js" ></script>
   <link rel="stylesheet" href="../plugins/fullcalendar/fullcalendar.min.css">
   <link rel="stylesheet" href="../plugins/fullcalendar/fullcalendar.print.css" media="print">
   <link rel="stylesheet" href="../plugins/daterangepicker/daterangepicker-bs3.css">
   <link rel="stylesheet" href="../plugins/iCheck/all.css">
   <link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
   <style>
   .chSevTable td{
   	 padding:10px;
   }
   </style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-retweet"></i> Verification for <b><s:property value="vuln.name"/></b>
      <small></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
    <bs:box type="info" title="Edit Verification">
	<div class="row" id="controls">
	<div class="col-md-12">
	</div><!-- /.col -->
	</div>
  	</bs:box>
  	 <jsp:include page="../footer.jsp"></jsp:include>
      <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="../plugins/fastclick/fastclick.min.js"></script>
    <script src="../dist/js/moment.js"></script>
    <script src="../plugins/fullcalendar/fullcalendar.min.js"></script>
    <script src="../plugins/daterangepicker/daterangepicker.js"></script>
     <script src="../plugins/select2/select2.full.min.js"></script>
    <script src="//cdn.ckeditor.com/4.15.1/standard/ckeditor.js"></script>
     <script src="../fileupload/js/fileinput.min.js" type="text/javascript"></script>
    <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <script src="../plugins/iCheck/icheck.min.js"></script>
</body>
</html>