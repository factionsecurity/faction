<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet"
	href="../plugins/daterangepicker/daterangepicker-bs3.css">
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<style>
.circle {
	border-radius: 50%;
	width: 30px;
	height: 30px;
	padding: 8px;
}

tr:hover {
	font-weight: bold;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-retweet"></i> Remediation Alerts <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:box type="info"
			title="Your Queue<br/><small></small>">
			<bs:row>
				<bs:mco colsize="1">
				</bs:mco>
				<bs:mco colsize="2">
					<input type='checkbox' id='showAll' ${checked}/>&nbsp;&nbsp;Show All Assigned</input>
				</bs:mco>
				<bs:mco colsize="2">
					<input type='checkbox' id='showAlmostDue' ${almostDueChecked}/>&nbsp;&nbsp;Show Almost Due</input>
				</bs:mco>
				<bs:mco colsize="2">
					<input type='checkbox' id='showPastDue' ${pastDueChecked}/>&nbsp;&nbsp;Show Past Due</input>
				</bs:mco>
				<bs:mco colsize="2">
					<input type='checkbox' id='showInRetest' ${inRetestChecked}/>&nbsp;&nbsp;Show Incomplete Retests</input>
				</bs:mco>
				<bs:mco colsize="2">
					<input type='checkbox' id='showCompletedRetest' ${completedRetesthecked}/>&nbsp;&nbsp;Show Completed Retests</input>
				</bs:mco>
			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					<bs:datatable id="queue"
						columns="Due Date,Opened,App Id,App Name,Description,Severity,Assessor,Type,Status"
						classname="primary">
						<s:iterator value="items">
							<tr data-vulnid="${vulnid}">
								<td><s:date name="due" format="MM/dd/yyyy" /></td>
								<td><s:date name="opened" format="MM/dd/yyyy" /></td>
								<td><s:property value="appid" /></td>
								<td><s:property value="appname" /></td>
								<td><s:property value="desc" /></td>
								<td><s:property value="severity" escapeHtml="false" /></td>
								<td><s:property value="assessor.fname" /> <s:property
										value="assessor.lname" /></td>
								<td><s:property value="type" escapeHtml="false" /></td>
								<td><s:property value="info" escapeHtml="false" /></td>
							</tr>
						</s:iterator>

					</bs:datatable>
				</bs:mco>
			</bs:row>



		</bs:box>
		<!-- Modals -->
		<bs:modal modalId="sevModal" saveId="cancelVer"
			title="Cancel Verification" color="red" closeText="Exit"
			saveText="Cancel Verification">
			<bs:row>
				<bs:mco colsize="12">
					<span style="font-size: large">Are you Sure you want to
						Cancel This Verification?</span>
				</bs:mco>
			</bs:row>
		</bs:modal>







		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/remediation_queue.js"></script>
		<script>
			let colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
			 <%int count = 9;%>
				<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
					<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
					$("td:contains('${risk}')").css("color", colors[<%=count--%>]).css("font-weight", "bold");

				</s:if>
			</s:iterator>
		</script>


		</body>
		</html>