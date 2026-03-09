<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<link href="../dist/css/Fuse.css" media="all" rel="stylesheet"
	type="text/css" />
<style>
.chSevTable td {
	padding: 10px;
}

#notes, #RemNotes, #nprodNotes, #prodNotes, #cancelVerNotes, #chSevNotes {
background-color: white;
}

.nav-tabs-custom .form-control {
	background-color: #192338 !important;
}

.remediationSelect {
	background-color: #192338 !important;
}

.select2-container .select2-selection--single .select2-selection__rendered
	{
	background-color: #192338 !important;
	border-color: #192338 !important;
}

.select2-container--default .select2-selection--single,
	.select2-selection .select2-selection--single {
	background-color: #192338 !important;
	border-color: #192338 !important;
}

.select2-results {
	border: 1px solid #030D1C;
	border-width: 0px 1px 1px 1px;
}

.btn{
margin-top: 20px;
}
.infoTable td{
	padding-left: 10px;
	padding-right: 10px;
}

.controlTable{
	border:0px !important;
	margin-left: 20px;
}
.controlTable a{
	color: #b8c7ce;
}
.controlTable td:first-child {
	border-left-width: 0px;
	border-left-style: solid;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-retweet"></i> Verification for <b><s:property
					value="vuln.name" /></b> <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:box type="info" title="Edit Verification">
			<div class="row" >
				<div class="col-md-12">
					<table class="infoTable">
				<tr><td>	<b>Vulnerability Name:</b></td><td> <s:property value="vuln.name"/></td></tr>
				<tr><td>	<b>Assessment Id:</b></td><td> <s:property value="appId"/></td></tr>
				<tr><td>	<b>Assessment Name:</b></td><td> <s:property value="appName"/></td></tr>
				<tr><td>	<b>Tracking:</b></td><td> <s:property value="vuln.tracking"/></td></tr>
				<tr><td>	<b>Opened:</b></td><td> <span id="opened"><s:date name="vuln.opened" format="MM/dd/yyyy"/></span></td></tr>
				<tr><td>	<b>Severity:</b> </td><td><s:property value="vuln.getOverallStr()"/></td></tr>
				<tr><td>	<b>Status: </b></td><td> <s:property value="badges" escapeHtml="false"/></td></tr>
					</table>
					<br/>
					<br/>
					<a href="RemediationSchedule?vulnId=${vuln.id}">Show All In Assessment</a>
				</div>
			</div>
			<div class="row" >
				<div class="col-md-12">
				<br/>
				</div>
			</div>
			<div class="row" id="controls">
				<div class="col-md-12">
					<div class="nav-tabs-custom">
						<ul class="nav nav-tabs">
							<li class="active"><a href="#info" data-toggle="tab">Verification
									Info</a></li>
							<li><a href="#actions" data-toggle="tab">Notes/Actions</a></li>
						</ul>
						<div class="tab-content">
							<div class="tab-pane active" id="info">
								<jsp:include page="verificationForm.jsp" />
							</div>
							<!-- /.tab-pane -->
							<div class="tab-pane" id="actions">
								<jsp:include page="notesForm.jsp" />
							</div>
							<!-- /.tab-pane -->
							<!-- /.tab-pane -->
						</div>
						<!-- /.tab-content -->
					</div>
					<!-- nav-tabs-custom -->
				</div>
				<!-- /.col -->
			</div>

		</bs:box>


		<!-- Modals -->
<!-- Begin Change Date Modal -->

<div id="changeDateModal" class="modal fade" tabindex="-1" role="dialog">
	<div class="modal-dialog">
		<div class="modal-content bg-red">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				<h4 class="modal-title">Change The Start Date</h4>
			</div>
			<div class="modal-body">
				<bs:row>
					<bs:dt name="Opened Date" colsize="12" id="openDateCal"></bs:dt>
				</bs:row>

			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<button type="button" class="btn btn-primary" id="saveOpen">Save
					changes</button>
			</div>
		</div>
	</div>
</div>
		<bs:modal modalId="sevModal" saveId="saveSev" title="Change Severity"
			color="red" closeText="Cancel" saveText="Save Severity">
			<bs:row>
				<bs:mco colsize="12">
					<span style="font-size: large">Change Severity of <i
						id="vulnname"><s:property value="vuln.name" /></i> to :</i></span>
				</bs:mco>
			</bs:row>
			<bs:row>

				<bs:select name="Severity:" colsize="4" id="newSev">
					<s:iterator value="levels" begin="9" end="0" step="-1"
						status="stat">
						<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
							<option value="${riskId }">${risk}</option>
						</s:if>
					</s:iterator>
				</bs:select>

				<bs:select name="Impact:" colsize="4" id="newImpact">
					<s:iterator value="levels" begin="9" end="0" step="-1"
						status="stat">
						<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
							<option value="${riskId }">${risk}</option>
						</s:if>
					</s:iterator>
				</bs:select>

				<bs:select name="Likelyhood:" colsize="4" id="newLike">
					<s:iterator value="levels" begin="9" end="0" step="-1"
						status="stat">
						<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
							<option value="${riskId }">${risk}</option>
						</s:if>
					</s:iterator>
				</bs:select>
			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					<b>Add Notes:</b>
					<div id="chSevNotes" name="chSevNotes"></div>
				</bs:mco>
			</bs:row>

		</bs:modal>


		<bs:modal modalId="nprodModal" saveId="saveNprod"
			title="Close in Development" color="red" closeText="Cancel"
			saveText="Close in Development">
			<bs:row>
				<bs:mco colsize="12">
					<h3>
						Are you sure you want to close this finding in the <b
							style="color: #F39C12">Development Environment</b>?
					</h3>
					<br>
					<i>This will <b>not</b> fully mark the item as remediated until
						it is closed in the production environment.
					</i>
					<br>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					<b>Add Notes:</b>
					<div id="nprodNotes" name="nprodNotes"></div>
				</bs:mco>
			</bs:row>
		</bs:modal>


		<bs:modal modalId="prodModal" saveId="saveProd" title="Close in Prod"
			color="red">
			<bs:row>
				<bs:mco colsize="12">
					<h3>
						Are you sure you want to close this finding in the <b
							style="color: red">Production Environment</b>?
					</h3>
					<br>
					<i>This will fully close the item and record the item as fully
						remediated.</i>
					<br>
					<br>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					<b>Add Notes:</b>
					<div id="prodNotes" name="nprodNotes"></div>
				</bs:mco>
			</bs:row>
		</bs:modal>


		<bs:modal modalId="closeVerModal" saveId="closeVerBtn"
			title="Close/Cancel the Verification" color="red">
			<bs:row>
				<bs:mco colsize="12">
					<h3>Are you sure you want to cancel the verification?</h3>
					<br>
					<i>This will remove the verification from the both the
						assessor's queue and the remediation queue.</i>
					<br>
					<br>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					<b>Add Notes:</b>
					<div id="cancelVerNotes" name="cancelVerNotes"></div>
				</bs:mco>
			</bs:row>
		</bs:modal>

		<jsp:include page="../footer.jsp"></jsp:include>
		<script
			src='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js'></script>
		<script>
			let defaultOverall = "<s:property value="vuln.overall"/>";
			let defaultLikelyhood = "<s:property value="vuln.likelyhood"/>";
			let defaultImpact = "<s:property value="vuln.impact"/>";

			let defaultRemId = "<s:property value="remId"/>";
			let defaultAssessorId = "<s:property value="asId"/>";
			let defaultSearchId = "<s:property value="searchId"/>";
			let defaultVulnId = "<s:property value="vulnId"/>";
			let defaultVulnName = "<s:property value="vuln.name"/>";

			let defaultAppId = "<s:property value="appId"/>";
			let defaultVerId = "<s:property value="searchId"/>";
			let defaultAppName = "<s:property value="appName"/>";
			let defaultAssessmentId = "<s:property value="vuln.assessmentId"/>";
			let filesPreview = [
			<s:iterator value="files">
				<s:if test="contentType.contains('image')">
					'<img src="../service/fileUpload?id=${uuid}" class="file-preview-image" style="width:auto;height:auto;max-width:100%;max-height:100%;""/>',
		  		</s:if>
				<s:elseif test="contentType.contains('text')">
					'<textarea class="file-preview-text" title="${name}" style="width:auto;height:160px;max-width:100%;max-height:100%;">"<s:property value="fileStrJs" escapeJavaScript="true" /></textarea>',
		  		</s:elseif>
			  	<s:else>
					'<embed src="../service/fileUpload?id=${uuid}" type="${contentType}" class="file-preview-data" style="width:auto;height:auto;max-width:100%;max-height:100%;"></embed>',
	  			</s:else>
			</s:iterator>
			]
			let previewConfig = [
			<s:iterator value="files">
				{ 	"caption": "${name}",
					"width": "100%", 
					"url" : "../service/fileUpload?delId=${uuid}",
					"downloadUrl": "../service/fileUpload?id=${uuid}",
					"key" : 1
				},
			</s:iterator>
			]
	
		</script>
		<script src="../dist/js/verification_edit.js"></script>
		<link
			href='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.css'
			rel='stylesheet' />
		<script
			src='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js'></script>
		</body>
		</html>