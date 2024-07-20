<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
   <link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
   <link href="../dist/css/jquery.autocomplete.css" media="all" rel="stylesheet" type="text/css" />
   <link href="../plugins/jquery-confirm/css/jquery-confirm.css" media="all" rel="stylesheet" type="text/css" />
	<link rel="stylesheet" href="../dist/css/Fuse.css">
	
<style>   
   
.fn-gantt .verificationClass {
	background-color: #e67e22 !important;
}
.fn-gantt .verificationClass .fn-label{
	color: white !important;

}
.fn-gantt .assessmentClass{
	background-color: #c0392b !important;
}
.fn-gantt .assessmentClass .fn-label{
	color: white !important;
}
.fn-gantt .oooClass{
	background-color: #27ae60 !important;
}
.fn-gantt .oooClass .fn-label{
	color: white !important;
}
label{
	text-overflow: ellipsis;
	 white-space: nowrap;
  overflow: hidden;
}

.breadcrumb {
	background-color: #030d1c !important;
}
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-random"></i>&nbsp;&nbsp;Schedule Assessment
      <small></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
	<bs:row>
	<bs:mco colsize="4">
	<s:if test="back=='assessment'">
		<ol class="breadcrumb">
			<li class="breadcrumb-item">
				<a href="Dashboard">Assessment Dashboard</a>
			</li>
			<li class="breadcrumb-item">
				<a href="Engagement?back=assessment">Create Assessment</a>
			</li>
		</ol>
	</s:if>
	</bs:mco>
	</bs:row>
  <div class="row">
<div class="col-md-12">
 <div class="nav-tabs-custom">
   <ul class="nav nav-tabs">
     <li class="active"><a href="#tab_1" data-toggle="tab">Assessments</a></li>
     <!--  <li><a href="#tab_2" data-toggle="tab">Verifications</a></li>-->
     <li><a href="#tab_3" data-toggle="tab">Search</a></li>
     <li><a href="#tab_4" data-toggle="tab">Upload</a></li>
   </ul>
   <div class="tab-content">
     <div class="tab-pane active" id="tab_1">
       <jsp:include page="AssessorSearch.jsp" />
     </div><!-- /.tab-pane -->
    <!--  div class="tab-pane" id="tab_2">
      
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="tab_3">
		<jsp:include page="EngagementSearch.jsp" />
     </div><!-- /.tab-pane -->
      <div class="tab-pane" id="tab_4">
		<jsp:include page="assessmentUpload.jsp" />
     </div><!-- /.tab-pane -->
   </div><!-- /.tab-content -->
 </div><!-- nav-tabs-custom -->
</div><!-- /.col -->
</div>
  <jsp:include page="../msgModal.jsp" />
  <jsp:include page="../footer.jsp" />
  <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js" integrity="sha512-qTXRIMyZIFb8iQcfjXWCO8+M5Tbc38Qi5WzdPOYZHIlZpzBHG3L3by84BBBOiRGiEb7KKtAOAs5qYdUiZiQNNQ==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>

  <link href='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.css' rel='stylesheet' />
  <script src='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js'></script>
  <script>

	let initialPreviewData = [];

	let initialPreviewConfigData = [];
	let finalized = false;
	let workflow="-1";
	let initialPreviewDownloadUrl = 'GetEngFile2?name={key}';
	let customFields = []
	let sDate= moment(new Date).format("MM-DD-YYYY")
	let eDate= moment(new Date).format("MM-DD-YYYY")
	let statName = "${defaultStatus}"
	let engName = ''
	let remName = ''
	let campName =''
	let assType = ''
	<s:iterator value="currentAssessment.CustomFields">
		customFields.push(${type.id});
	</s:iterator>
	</script>

    <script src="../dist/js/scheduling.js"></script>
  </body>
</html>