<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<link href="../dist/css/Fuse.css" media="all" rel="stylesheet"
	type="text/css" />
<style>
#notes,#RemNotes,#chSevNotes,#nprodNotes,#prodNotes{
	background-color: white;
}
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

.daterangepicker td.off{
	background-color: #192339 !important;
}
.daterangepicker .calendar-table{
	border:none;
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

#noteHistory table {
    font-family: Arial, Helvetica, sans-serif;
    border-collapse: collapse;
    width: 100%;
}
#noteHistory td, #noteHistory th {
    border: 0.3px solid #acb9ca;
    padding: 2px;
  	padding-left: 8px;
}
#noteHistory td div {
   word-break: break-all !important;
}
#noteHistory th {
  white-space: nowrap !important;
  background-color: #afbfcf;
  display: table-cell;
  vertical-align: inherit;
  font-weight: normal;
  color: #030d1c;
}

.btn-actions .btn{
margin-top: 20px;
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
		</bs:box>
	</section>
</div>



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