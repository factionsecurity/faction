<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
<link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet"
		href="../dist/font-awesome-4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="../plugins/select2/select2.min.css">
    <link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="../dist/font-awesome-4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="../dist/css/skins/skin-blue.min.css">
    <link rel="stylesheet" href="../plugins/datatables/jquery.dataTables.css">
    <link rel="stylesheet" href="../plugins/jquery-confirm/css/jquery-confirm.css">
    <link rel="stylesheet" href="../plugins/daterangepicker/daterangepicker-bs3.css">
    <link rel="stylesheet" href="../dist/css/Fuse.css">

</head>
<body>
   <style>
    	body{
    	background: #367fa9;
    	}
    	.content-wrapper{
    	margin-left:100px;
    	margin-right:100px;
    	}
    	.main-footer{
    	margin-left:100px;
    	margin-right:100px;
    	}
    	.jconfirm{
    		z-index:1000 important!;
    	}
    	.daterangepicker.dropdown-menu{
    		z-index:199999999;
    	}
    </style>
	<div class="wrapper">
		<header class="main-header">
        <!-- Header Navbar -->
        <nav class="navbar navbar-static-top" role="navigation">
        </nav>
        </header>
        <!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
	  <!-- Content Header (Page header) -->
	  <section class="content-header">
	    <h1>
	      <i class="fa fa-bug"></i> Enter Your Token:
	      <small></small>
	    </h1>
	  </section>

  <!-- Main content -->
  <section class="content">
	
		<div class="myform" style="padding: 50px 0">
			<bs:row>
			<form action="ClientPortal" method="POST" id="login">
			<bs:inputgroup name="Token" colsize="12" id="accessKey" htmlname="accessKey"></bs:inputgroup>
			</form>
			</bs:row>
			<br>
			<bs:row>
			<bs:button color="success" size="md" colsize="2" text="Submit" id=""></bs:button>
			</bs:row>


			<!-- end:Main Form -->
		</div>
		</section>
</div>
<footer class="main-footer">
        <!-- To the right -->
        <div class="pull-right hidden-xs">
          Version 0.1 Beta
        </div>
        <!-- Default to the left -->
        <strong>Copyright &copy; 2016 <a href="https://www.fusesoftsecurity.com">FuseSoft</a>.</strong> All rights reserved.
      </footer>
</div>

<!-- jQuery 2.1.4 -->
    <script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script>
    <!-- Bootstrap 3.3.5 -->
    <script src="../bootstrap/js/bootstrap.min.js"></script>
    <script src="../dist/js/app.js"></script>
    <script src="../plugins/jquery-confirm/js/jquery-confirm.js" type="text/javascript"></script>
     <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <script src="../dist/js/moment.js"></script>
    <script src="../plugins/daterangepicker/daterangepicker.js"></script>
    
<script>
$(function(){
	$("button").click(function(){
		$("#login").submit();
		
	});
	
});

</script>
		


</body>
</html>
