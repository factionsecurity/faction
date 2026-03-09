<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
<link href="../dist/css/jquery.autocomplete.css" media="all" rel="stylesheet" type="text/css" />
<link href="../dist/css/throbber.css" media="all" rel="stylesheet" type="text/css" />
<link href="../plugins/jquery-confirm/css/jquery-confirm.css" media="all" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="../plugins/iCheck/all.css">

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <jsp:include page="AssessmentHeading.jsp" />
  <!-- Main content -->
  <section class="content">
  <div id="infobar" style="width:90%; margin-right:auto;margin-left:auto; display: none">
  <bs:row>

  <s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
    	 	<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
    	 	  <div class="col-sm-1">
				<div class="small-box" style="border-width:1px; border-style:solid;">
		            <div class="inner">
		              <h3>${counts.get(riskId)}</h3>
		              <p>${risk }</p>
		            </div>
		            <div class="icon">
		              <s:if test="risk.toLowerCase().startsWith('info') || risk.toLowerCase().startsWith('rec')">
		              	<i class="fa fa-info"></i>
		              </s:if>
		              <s:else>
		              	<i class="fa fa-bug"></i>
		              </s:else>
		            </div>
		             
		          </div>
		         </div>
			</s:if>
</s:iterator>
</bs:row>
</div> 	

<jsp:include page="AssessmentStats.jsp" />

<div class="row">
<div class="col-md-12">
 <div class="nav-tabs-custom">
   <ul class="nav nav-tabs" role="tablist">
   	<s:if test="%{#request.summaryActive == 'true'}">
     <li class="nav-item active" role="presentation"><a href="#Summary" data-toggle="tab" role="tab">Overview</a></li>
     <jsp:include page="AssessmentExtendedTabs.jsp"/>
     <li class="nav-item "><a href="VulnerabilityView#VulnView">Vulnerabilities</a></li>
     <li class="nav-item "><a href="VulnerabilityView#NoteView">Notes</a></li>
     <li class="nav-item "><a href="CheckList">Checklists</a></li>
     <li class="nav-item "><a href="#Finalize" data-toggle="tab"  >Finalize</a></li>
     <li class="nav-item "><a href="#History" data-toggle="tab"  >History</a></li>
     <li class="nav-item " ><a href="AuditLog" >Audit</a></li>
    
   	</s:if>
   	<s:if test="%{#request.vulnsActive == 'true'}">
     <li class="nav-item " role="presentation"><a href="Assessment#Summary" role="tab">Overview</a></li>
     <jsp:include page="AssessmentExtendedTabs.jsp"/>
     <li class="nav-item "><a href="#VulnView" data-toggle="tab" >Vulnerabilities</a></li>
     <li class="nav-item "><a href="#NoteView" data-toggle="tab" >Notes</a></li>
     <li class="nav-item "><a href="CheckList">Checklists</a></li>
     <li class="nav-item "><a href="Assessment#Finalize">Finalize</a></li>
     <li class="nav-item "><a href="Assessment#History">History</a></li>
     <li class="nav-item " ><a href="AuditLog" >Audit</a></li>
    </s:if>
    <s:if test="%{#request.vulnsActive != 'true' && #request.summaryActive != 'true' }">
     <li class="nav-item " role="presentation"><a href="Assessment#Summary" role="tab">Overview</a></li>
     <jsp:include page="AssessmentExtendedTabs.jsp"/>
     <li class="nav-item "><a href="VulnerabilityView#VulnView">Vulnerabilities</a></li>
     <li class="nav-item "><a href="VulnerabilityView#NoteView">Notes</a></li>
     <li class="nav-item <s:if test="%{#request.checklistActive == 'true'}">active</s:if>"><a href="CheckList">Checklists</a></li>
     <li class="nav-item "><a href="Assessment#Finalize">Finalize</a></li>
     <li class="nav-item "><a href="Assessment#History">History</a></li>
     <li class="nav-item <s:if test="%{#request.auditActive == 'true'}">active</s:if>" ><a href="AuditLog" >Audit</a></li>
    </s:if>
   </ul>
   <div class="tab-content">
   <script>
   $(function(){
	setTimeout( () => {
	   window.scrollTo({
		   top: 0,
		 });
   },100);
   });
   </script>