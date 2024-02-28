<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet"
	href="../plugins/fullcalendar/fullcalendar.min.css">
<link rel="stylesheet"
	href="../plugins/fullcalendar/fullcalendar.print.css" media="print">
<link rel="stylesheet"
	href="../plugins/daterangepicker/daterangepicker-bs3.css">
<link rel="stylesheet" href="../plugins/datepicker/datepicker3.css">
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<link href="../dist/css/Fuse.css" media="all" rel="stylesheet"
	type="text/css" />
<style>
.chSevTable td {
	padding: 10px;
}

.smallbtn {
	font-size: small;
	padding: 0px;
	padding-left: 2px;
	padding-right: 2px;
	margin-top: -5px;
}

.daterangepicker {
	z-index: 1151 !important;
	background-color: #030D1C !important;
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
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-retweet"></i> Remediation <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:box type="info"
			title="Search Vulnerabilities and Add them to a Verification">
			<jsp:include page="OpenVulns.jsp"></jsp:include>
			<div class="row" id="controls" style="display: none">
				<div class="col-md-12">
					<div class="nav-tabs-custom">
						<ul class="nav nav-tabs">
							<li class="active"><a href="#tab_1" data-toggle="tab">Submit
									for Verification</a></li>
							<li><a href="#tab_2" data-toggle="tab">Notes/Actions</a></li>
						</ul>
						<div class="tab-content">
							<div class="tab-pane active" id="tab_1">
								<jsp:include page="verificationForm.jsp" />
							</div>
							<!-- /.tab-pane -->
							<div class="tab-pane" id="tab_2">
								<jsp:include page="notesForm.jsp" />
							</div>
							<!-- /.tab-pane -->
						</div>
						<!-- /.tab-pane -->
					</div>
					<!-- /.tab-content -->
				</div>
				<!-- nav-tabs-custom -->
			</div>
			<!-- /.col -->
		</bs:box>
	</section>
</div>



<!-- Modals -->
<bs:modal modalId="sevModal" saveId="saveSev" title="Change Severity"
	color="red" closeText="Cancel" saveText="Save Severity">
	<bs:row>
		<bs:mco colsize="12">
			<span style="font-size: large">Change Severity of <i
				id="vulnName"></i> to :</i></span>
		</bs:mco>
	</bs:row>
	<bs:row>
		<bs:select name="Severity:" colsize="4" id="newSev"
			cssClass="remediationSelect">
			<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
				<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
					<option value="${riskId }">${risk}</option>
				</s:if>
			</s:iterator>
		</bs:select>

		<bs:select name="Impact:" colsize="4" id="newImpact">
			<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
				<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
					<option value="${riskId }">${risk}</option>
				</s:if>
			</s:iterator>
		</bs:select>

		<bs:select name="Likelyhood:" colsize="4" id="newLike">
			<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
				<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
					<option value="${riskId }">${risk}</option>
				</s:if>
			</s:iterator>
		</bs:select>

	</bs:row>
	<bs:row>
		<bs:mco colsize="12">
			<b>Add Notes:</b>
			<textarea id="chSevNotes" name="chSevNotes"></textarea>
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
			<textarea id="nprodNotes" name="nprodNotes"></textarea>
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
			<textarea id="prodNotes" name="nprodNotes"></textarea>
		</bs:mco>
	</bs:row>
</bs:modal>

<jsp:include page="../footer.jsp" />

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
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<!-- /.modal -->
<!-- End Change Date modal -->
<script>
let levels=[];
<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
		<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
		levels.push(${riskId});
	</s:if>
</s:iterator>
</script>
<link
	href='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.css'
	rel='stylesheet' />
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js'></script>
<script src="../dist/js/remediation.js"></script>
<script>

let searchId = -1;
let vulnName = "${vulnName }";
<s:if test="asmtId != null && asmtId > 0">
	$(function(){
		setTimeout( () => {
			searchId=<s:property value="searchId"/>;
			let asmtId=<s:property value="asmtId"/>;
			if(searchId > -1){
				$("#appid").val(asmtId);
				$("#search").trigger("click");
			}
		},100);
		
	});
</s:if>
function updateColor( severity){
let colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
 <%int count = 9;%>
	<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
		<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
			if("${risk}" == severity){
				return "<b style='color: " + colors[<%=count--%>] +"'>" + severity + "</b>";
			}
	</s:if>
</s:iterator>
}
</script>


</body>
</html>