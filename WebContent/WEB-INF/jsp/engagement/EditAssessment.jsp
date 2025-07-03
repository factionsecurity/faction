<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
   <link rel="stylesheet" href="../plugins/daterangepicker/daterangepicker-bs3.css">
   <link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
   <link href="../dist/css/jquery.autocomplete.css" media="all" rel="stylesheet" type="text/css" />
   
<style>
.disabled-select {
   background-color:#d5d5d5;
   opacity:0.5;
   border-radius:3px;
   cursor:not-allowed;
   position:absolute;
   top:0;
   bottom:0;
   right:0;
   left:0;
}
.breadcrumb {
	background-color: #030d1c !important;
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      Edit Assessment
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
				<a href="AssessmentQueue">Assessment Queue</a>
			</li>
			<li class="breadcrumb-item">
				<a href="Assessment">Assessment</a>
			</li>
			<li class="breadcrumb-item">
				<a href="">Edit Assessment</a>
			</li>
		</ol>
	</s:if>
	<s:else>
		<ol class="breadcrumb">
			<li class="breadcrumb-item">
				<a href="Engagement">Assessment Scheduling</a>
			</li>
			<li class="breadcrumb-item">
				<a href="Engagement#tab_3">Assessment Search</a>
			</li>
			<li class="breadcrumb-item">
				<a href="">Edit Assessment</a>
			</li>
		</ol>
	</s:else>
	</bs:mco>
	</bs:row>
	<div class="row">
	<div class="col-md-12">
	 <div class="nav-tabs-custom">
	   <ul class="nav nav-tabs">
	     <li class="active"><a href="#tab_1" data-toggle="tab">Assessment</a></li>
	     <li><a href="#tab_2" data-toggle="tab">Audit</a></li>
	   </ul>
	   <div class="tab-content">
	     <div class="tab-pane active" id="tab_1">
	       <jsp:include page="AssessorSearch.jsp" />
	     </div><!-- /.tab-pane -->
	     <div class="tab-pane" id="tab_2">
	         <bs:datatable columns="Timestamp,Description,User" classname="" id="auditlog">
		     <s:iterator value="logs">
		     <tr><td>${timestamp }</td><td><s:property value="description"/></td><td><s:property value="user.fname"/> <s:property value="user.lname"/></td></tr>
		     </s:iterator>
		     </bs:datatable>
	     </div><!-- /.tab-pane -->
	   </div><!-- /.tab-content -->
	 </div><!-- nav-tabs-custom -->
	</div><!-- /.col -->
	</div>




	
  <jsp:include page="../footer.jsp" />
  <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js" integrity="sha512-qTXRIMyZIFb8iQcfjXWCO8+M5Tbc38Qi5WzdPOYZHIlZpzBHG3L3by84BBBOiRGiEb7KKtAOAs5qYdUiZiQNNQ==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>

  <link href='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.css' rel='stylesheet' />
  <script src='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js'></script>
  <script>

	let initialPreviewData = [
			<s:iterator value="files">
			"GetEngFile?name=<s:property value="uuid"/>",
			</s:iterator>
			];

	let initialPreviewConfigData = [
			<s:iterator value="files" status="stat">
				
			{ "caption": "<s:property value="name"/>", "width" : "160px", "height": "160px",  "key" : "<s:property value="uuid"/>", 
			"filename" : "<s:property value="name"/>", 
			"url" : "DeleteEngFile?name=<s:property value="name"/>&apid=<s:property value="entityId"/>&delid=<s:property value="uuid"/>",
			"filetype": "<s:property value="contentType"/>", 
			"type" : "<s:property value="fileExtType"/>"},
					
			</s:iterator>
			];
		let finalized = <s:property value="currentAssessment.finalized"/>
		let workflow = "<s:property value="currentAssessment.workflow"/>";
    	let engName="<s:property value="currentAssessment.engagement.Id"/>";
    	
       let remName="<s:property value="currentAssessment.remediation.Id"/>";
       let campName="<s:property value="currentAssessment.campaign.id"/>";
       let teamName="<s:property value="currentAssessment.assessor[0].team.id"/>";
       let assType="<s:property value="currentAssessment.type.id"/>";
       let statName="<s:property value="currentAssessment.status"/>";
       let aid="<s:property value="aid"/>";
	   let initialPreviewDownloadUrl = 'GetEngFile?name={key}';
	   let customFields = []
		let sDate="${startStr}";
		let eDate="${endStr}";
    	<s:iterator value="currentAssessment.CustomFields">
    		<s:if test="type.fieldType == 1 && value == 'true'"> $("#cust${type.id}").prop('checked', true);</s:if>
    		<s:elseif test="type.fieldType == 1 && value == 'false'"> $("#cust${type.id}").prop('checked', false);</s:elseif>
    		<s:elseif test="type.fieldType == 3"> $("#rtCust${type.id}").html(entityDecode("<s:property value="value"/>"))</s:elseif>
    		<s:else>$("#cust${type.id}").val("${value}");</s:else>
			customFields.push(${type.id});
    	</s:iterator>
	</script>
    <script src="../dist/js/scheduling.js"></script>
	<script>
   setTimeout(() => {
         
        
       // this needs to loop through users
       <s:iterator value="currentAssessment.assessor">
        $.post('../service/getAssessments','id=<s:property value="Id"/>').done(function(adata){
			let json = JSON.parse(adata);
			//console.log("Posted get Assessment");
			let N=json.count;
			for(let i=0;i<N; i++){
				//console.log("Posted get Assessment :" + i);
				let s=json.assessments[i][2];
				let e=json.assessments[i][4];
				let t=json.assessments[i][1] + " - " + json.assessments[i][0] + " - <s:property value="fname"/> <s:property value="lname"/>";
				let aid=json.assessments[i][3].replace('app', "");
				let currentTitle =  $("#appId").val() + " - " + $("#appName").val() + " - <s:property value="fname"/> <s:property value="lname"/>";
				//console.log(currentTitle);
				//console.log(t);
				if(s != 'null' && e != 'null'){
					///console.log(s + " " + e);
					
					var originalEventObject = $(this).data('eventObject');
					var copiedEventObject = $.extend({}, originalEventObject);
		            copiedEventObject.allDay=true; 
		            copiedEventObject.title=t;
		            copiedEventObject.start=new Date(s);
		            copiedEventObject.id=aid;
		            tmpdate = new Date(e);
		            tmpdate = tmpdate.setDate(tmpdate.getDate() + 1);
		            copiedEventObject.end=tmpdate;
		            if(currentTitle == t){
		            	copiedEventObject.editable=true;
			            copiedEventObject.color=edit_color;
		            }else{
			            copiedEventObject.editable=false;	
			            copiedEventObject.color=asmt_color;
					}
					try{
		        		calendar.addEvent(copiedEventObject, true);
					}catch(e){
						console.log(e)
						console.log(copiedEventObject)
					}
					
				}
			}
        	});
        </s:iterator>
	   
   }, 1000);

    </script>

  </body>
</html>