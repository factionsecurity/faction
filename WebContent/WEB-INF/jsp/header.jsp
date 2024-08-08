<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <title><s:property value="_title1"/> <s:property value="_title2"/></title>
   
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
 	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" integrity="sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwEsw9c2QQeAIftl+Vegovlnee1c9QX4TctnWMn13TZye+giMm8e2LwA==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="../dist/css/skins/skin-blue.min.css">
    <link rel="stylesheet" href="../plugins/datatables/jquery.dataTables.css">
    <link rel="stylesheet" href="../plugins/jquery-confirm/css/jquery-confirm.css">
    <link rel="stylesheet" href="../plugins/loading/css/jquery-loading.css">
    <link rel="stylesheet" href="../dist/css/Fuse.css">
    
     <!-- jQuery 2.1.4 -->
    <script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script> 
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js" ></script>
    <script src="../dist/js/fuse.js"></script>
    <script src="../dist/js/main.js"></script>
    <style>
    .icon-img{
    	height: 30px;
    }
    </style>

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body class="hold-transition skin-blue sidebar-mini <s:property value="MENUOPTION"/>">
  <s:hidden value="%{_token}" name="_token"></s:hidden>
    <div class="wrapper">

      <!-- Main Header -->
      <header class="main-header">

        <!-- Logo -->
        <a href="../" class="logo">
          <!-- mini logo for sidebar mini 50x50 pixels -->
          <span class="logo-mini"><img class="icon-img" src="../tri-logo.png" /></span>
          <!-- logo for regular state and mobile devices -->
          <span class="logo-lg"><img class="icon-img" src="../tri-logo.png" />&nbsp;&nbsp;<b>${_title1}</b> ${_title2}</span>
        </a>

        <!-- Header Navbar -->
        <nav class="navbar navbar-static-top" role="navigation">
          <!-- Sidebar toggle button-->
          <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
            <span class="sr-only">Toggle navigation</span>
          </a>
          
          <!-- Assessment Menu -->
          <div class="navbar-custom-menu">
            <ul class="nav navbar-nav">
              <!-- Messages: style can be found in dropdown.less-->
              <li class="dropdown notifications-menu">
                <!-- Menu toggle button -->
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                  <i class="glyphicon glyphicon-th-list"></i>
                  <span class="label label-success assessmentCount"></span>
                </a>
                <ul class="dropdown-menu" id="assessmentWidget">
                 
                </ul>
              </li><!-- /.messages-menu -->

              <!-- Verification Menu -->
              <li class="dropdown notifications-menu">
                <!-- Menu toggle button -->
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                  <i class="glyphicon glyphicon-ok"></i>
                  <span class="label label-warning verificationCount"></span>
                </a>
                <ul class="dropdown-menu" id="verificationWidget">
                 
                </ul>
              </li>
              <!-- User Account Menu -->
              <li class="dropdown user user-menu">
                <!-- Menu Toggle Button -->
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
					<i class="glyphicon glyphicon-user"></i>
                </a>
                <ul class="dropdown-menu">

                
                  <!-- Menu Body -->
                  <li class="user-body">
                    <div class="col-xs-6 text-center">
                      <a href="#">User:<br><s:property value="sessionUser.fname"/> <s:property value="sessionUser.lname"/></a>
                    </div>
                    <div class="col-xs-6 text-center">
                      <a href="#">Last Login: <s:date name="sessionUser.lastLogin" format="MM/dd/yyyy hh:mm:ss"/></a>
                    </div>
                  </li>
                  <!-- Menu Footer-->
                  <li class="user-footer">
                    <div class="pull-left">
                      <button class="btn btn-default btn-flat" id="Profile">Profile</button>
                    </div>
                    <div class="pull-right">
                      <a href="../service/logout" class="btn btn-default btn-flat">Sign out</a>
                    </div>
                  </li>
                </ul>
              </li>
              <!-- Control Sidebar Toggle Button -->
              <!--  <li>
                <a href="#" data-toggle="control-sidebar"><i class="fa fa-gears"></i></a>
              </li> -->
            </ul>
          </div>
        </nav>
      </header>
     
      <!-- Left side column. contains the logo and sidebar -->
      <aside class="main-sidebar">

        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">

          <!-- Sidebar user panel (optional) -->
          <div class="user-panel">
          </div>
			<style>
			/*.sidebar-menu > li > a{
				font-size: large;
				font-weight: normal;
			}*/
			
			
			.sidebar-menu > li > a > .label{
				font-size: 9px;
				top:2px;
				left:25px;
				position: absolute;
				z-index:100;
				padding: 2px 3px;
				font-style: normal;
			
			}
			</style>
          <!-- Sidebar Menu -->
          <ul class="sidebar-menu">
            

             <s:if test="acassessor || acmanager">
            	<li class="<s:property value="activeDB"/>"><a href="Dashboard"><i class="glyphicon glyphicon-dashboard"></i> <span>Dashboard</span></a></li>
            </s:if>
            <s:if test="acassessor || acmanager">
            	<li class="<s:property value="activeAQ"/>">
            			
            			<a href="AssessmentQueue"><i class="label bg-green assessmentCount"></i><i class="glyphicon glyphicon-th-list"></i>
            			<span>Assessments</span></a>
            		
            	</li>
            </s:if>
            <s:if test="(acassessor || acmanager) && !tier.equals('consultant')">
            	<li class="<s:property value="activeVerification"/>">
            		<a href="Verifications"><i class="label bg-orange verificationCount"></i><i class="glyphicon glyphicon-ok"></i> 
            		<span>Retests</span></a>
            		</li>
            </s:if>
            <s:if test="(acassessor || acmanager) && tier.equals('consultant')">
            	<li class="<s:property value="activeVerification"/>">
            		<a href="Upgrade"><i class="label bg-purple">&nbsp;<div class='fa fa-arrow-up'>&nbsp;</div></i><i class="glyphicon glyphicon-ok"></i> 
            		<span>Retests</span></a>
            		</li>
            </s:if>
           <s:if test="prEnabled && (acassessor || acmanager) && !tier.equals('consultant')">
            	<li class="<s:property value="activePR"/>">
            	<a href="PeerReview"><i class="label bg-purple prCount"></i><i class="glyphicon glyphicon-eye-open"></i>
            	<span>Peer Review</span></a></li>
            </s:if>
           <s:if test="(acassessor || acmanager) && tier.equals('consultant')">
            	<li class="<s:property value="activePR"/>">
            	<a href="Upgrade"><i class="label bg-purple">&nbsp;<div class='fa fa-arrow-up'>&nbsp;</div></i><i class="glyphicon glyphicon-eye-open"></i>
            	<span>Peer Review</span></a></li>
            </s:if>
            <s:if test="feedEnabled && (acassessor || acmanager)">
            	<li class="<s:property value="activeLF"/>"><a href="LiveFeed"><i class="glyphicon glyphicon-flash"></i> <span>Live Feed</span></a></li>
            </s:if>
            <s:if test="acengagement || acmanager">
            	<li class="<s:property value="activeEngagement"/>"><a href="Engagement"><i class="glyphicon glyphicon-calendar"></i> <span>Scheduling</span></a></li>
            </s:if>
            <s:if test="acremediation && !tier.equals('consultant')">
            	 <li class="treeview <s:property value="activeRem"/> <s:property value="activeRemSearch"/>">
            	 <a href="#"><i class="glyphicon glyphicon-retweet"></i> <span>Remediation</span> <i class="fa fa-angle-left pull-right"></i></a>
            	 	<ul class="treeview-menu">
            	 	<li class="<s:property value="activeRem"/>"><a href="RemediationQueue"><i class="glyphicon glyphicon-retweet"></i> <span>Queue</span></a></li>
            		<li class="<s:property value="activeRemSearch"/>"><a href="Remediation"><i class="glyphicon glyphicon-search"></i> <span>Search</span></a></li>
            		</ul>
            	</li>
             </s:if>
            <s:if test="acremediation && tier.equals('consultant')">
            	 <li class="treeview <s:property value="activeRem"/> <s:property value="activeRemSearch"/>">
            	 <a href="#"><i class="label bg-purple">&nbsp;<div class='fa fa-arrow-up'>&nbsp;</div></i><i class="glyphicon glyphicon-retweet"></i> <span>Remediation</span> <i class="fa fa-angle-left pull-right"></i></a>
            	 	<ul class="treeview-menu">
            	 	<li class="<s:property value="activeRem"/>"><a href="Upgrade"><i class="label bg-purple">&nbsp;<div class='fa fa-arrow-up'>&nbsp;</div></i><i class="glyphicon glyphicon-retweet"></i> <span>Queue</span></a></li>
            		<li class="<s:property value="activeRemSearch"/>"><a href="Upgrade"><i class="label bg-purple">&nbsp;<div class='fa fa-arrow-up'>&nbsp;</div></i><i class="glyphicon glyphicon-search"></i> <span>Search</span></a></li>
            		</ul>
            	</li>
             </s:if>
            <s:if test="acassessor || acremediation || acmanager || acengagement || acadmin">
            	<li class="<s:property value="activeCal"/>"><a href="Calendar"><i class="fa fa-people-group"></i> <span>Team Calendar</span></a></li>
            </s:if>
            <s:if test="acassessor || acremediation || acmanager || acengagement || acadmin">
            	<li class="<s:property value="activeMetrics"/>"><a href="Metrics"><i class="glyphicon glyphicon-signal"></i> <span>Metrics</span></a></li>
            </s:if>
            <s:if test="acmanager || acadmin || acengagement">
            <li class="treeview <s:property value="activeAppStore"/><s:property value="activeChecklist"/><s:property value="activeUsers"/><s:property value="activeOptions"/>">
              <a href="#"><i class="glyphicon glyphicon-cog"></i> <span>Admin</span> <i class="fa fa-angle-left pull-right"></i></a>
              <ul class="treeview-menu">
              	<s:if test="acadmin == true ">
                <li class="<s:property value="activeUsers"/>"><a href="Users">Users</a></li>
                </s:if>
                <s:if test="acmanager || acadmin || acengagement">
                <li class="<s:property value="activeOptions"/>" ><a href="Options">Settings</a></li>
                </s:if>
                <s:if test="acmanager || acadmin">
                <li class="<s:property value="activeChecklist"/>"><a href="Checklists">Checklists</a></li>
                </s:if>
                <s:if test="acadmin && appStoreEnabled">
                <li class="<s:property value="activeAppStore"/>"><a href="AppStoreDashboard">App Store</a></li>
                </s:if>
                <s:elseif test="acadmin">
                <li class="<s:property value="activeAppStore"/>"><a href="Upgrade"><i class="label bg-purple">&nbsp;<div class='fa fa-arrow-up'>&nbsp;</div></i>App Store</a></li>
                </s:elseif>
              </ul>
            </li>
            </s:if>
            <li class="treeview <s:property value="activeVulns"/><s:property value="activeCms"/><s:property value="activeTemplates"/>">
              <a href="#"><i class="glyphicon glyphicon-book"></i> <span>Templates</span> <i class="fa fa-angle-left pull-right"></i></a>
              <ul class="treeview-menu">
                <li class="<s:property value="activeTemplates"/>" ><a href="Templates">Assessment Templates</a></li>
                <li class="<s:property value="activeCms"/>" ><a href="cms">Report Designer</a></li>
                <li class="<s:property value="activeVulns"/>"><a href="DefaultVulns">Default Vulnerabilities</a></li>
              </ul>
            </li>
          </ul><!-- /.sidebar-menu -->
        </section>
        <!-- /.sidebar -->
      </aside>
      

     

         