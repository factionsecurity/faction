<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet" href="../plugins/iCheck/all.css">

<style>
	.meta {
		font-weight: bold;
		margin-left: 30px;
	}
	
	#notes, #description, #recommendation, #failnotes, #details{
		background-color: white;
	}

</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			Verification for: <b><s:property
					value="verification.verificationItems[0].vulnerability.name" />&nbsp;-&nbsp;<s:property
					value="verification.assessment.name" /></b> <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="9">
				<bs:box type="info" 
					title="<i class='glyphicon  glyphicon-list-alt'></i>&nbsp;&nbsp;Vulnerability Info">
					<div style="height: 140px">
					<bs:row>
						<s:if test="verification.assessment.type.cvss40 || verification.assessment.type.cvss31">
							<bs:mco colsize="1" style="width: 150px">
								<div class="scoreBody" style="margin-top: 5px">
									<h3 class="scoreBox <s:property value="verification.verificationItems[0].vulnerability.overallStr"/>" id="score"><s:property value="verification.verificationItems[0].vulnerability.cvssScore"/></h3>
									<span class="severityBox <s:property value="verification.verificationItems[0].vulnerability.overallStr"/>" id="severity"><s:property value="verification.verificationItems[0].vulnerability.overallStr"/></span>
								</div>
							</bs:mco>
							<bs:mco colsize="10">
								<span class='meta'><u>Category:</u></span><br/>
								<span class='meta'><s:property value="verification.verificationItems[0].vulnerability.category.name"/></span><br/>
								<br/>
								<span class='meta'><u>CVSS Vector:</u></span><br/>
								<span class='meta'><s:property value="verification.verificationItems[0].vulnerability.cvssString"/></span><br/>
							</bs:mco>
						</s:if>
						<s:else>
							<bs:mco colsize="12">
								<bs:datatable
									columns="App Name, Overall, Impact, Likelyhood, Start, End, Report"
									classname="primary" id="">
									<tr>
										<td><s:property value="verification.assessment.appId" /> - <s:property value="verification.assessment.name" /></td>
										<td><s:property
												value="verification.verificationItems[0].vulnerability.overallStr" /></td>
										<td><s:property
												value="verification.verificationItems[0].vulnerability.impactStr" /></td>
										<td><s:property
												value="verification.verificationItems[0].vulnerability.likelyhoodStr" /></td>
										<td><s:date name="verification.start" format="MM/dd/yyyy" /></td>
										<td><s:date name="verification.end" format="MM/dd/yyyy" /></td>
										<td><bs:button color="info" size="md" colsize="12"
												text="<span class='fa fa-file'></span> Full Report" id="open"></bs:button></td>
									</tr>
								</bs:datatable>
							</bs:mco>
						</s:else>
					</bs:row>
					</div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="3">
				<bs:box type="info"
					title="<i class='glyphicon glyphicon-asterisk'></i> Retest Actions">
					<div style="height: 145px">
					<bs:row>
						<bs:mco colsize="12">
							<bs:row>
								<bs:mco colsize="6">
									<div class="form-group"
										style="font-size: xx-large; padding-left: 30px">
										<label> <input type="radio" name="r3"
											class="flat-green" value="1"> Pass <br /> <input
											type="radio" name="r3" class="flat-red" checked value="0">
											Fail
										</label>
									</div>
								</bs:mco>
								<bs:mco colsize="6">
									<bs:button color="primary" size="md" colsize="12"
										text="<span class='fa fa-check' ></span> Complete " id="save"></bs:button>
									<br>
									<br>
									<bs:button color="danger" size="md" colsize="12"
										text="<span class='fa fa-ban' ></span> Cancel" id="cancel"></bs:button>
									<br>
									<br>
								</bs:mco>

							</bs:row>
						</bs:mco>
						</div>
					</bs:row>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="info" title="<i class='fa fa-bug'></i> Scope">

					<bs:row>
						<bs:mco colsize="12">
							<div name="notes" id="notes" disabled>
					  <s:property value="verification.Notes" />
				  </div>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="warning"
					title="<i class='glyphicon  glyphicon-edit'></i>  Assessor Pass or Fail Notes">
					<div id="failnotes" name="failnotes"></div>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="9">
				<bs:box type="info" title="Vulnerability Description">
					<div id="description" name="description"><s:property
							value="verification.verificationItems[0].vulnerability.description" /></div>
				</bs:box>

				<bs:box type="info" title="Vulnerability Recommendation">
					<div id="recommendation" name="recommendation"><s:property
							value="verification.verificationItems[0].vulnerability.recommendation" /></div>
				</bs:box>

				<bs:box type="info" title="Vulnerability Details">
					<div id="details" name="details"><s:property
							value="verification.verificationItems[0].vulnerability.details" /></div>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="3">
				<bs:row>
					<bs:box type="info"
						title="<i class='glyphicon glyphicon-download'></i> Supporting Files">
						<bs:row>
							<bs:mco colsize="12">
								<input id="files" type="file" multiple name="file_data" />
							</bs:mco>
						</bs:row>
					</bs:box>
				</bs:row>
				<bs:row>
					<bs:box type="primary" title="Reports">
					<table class="table table-striped table-hover dataTable no-footer">
					<thead>
					<tr><th>Report Name</th><th>Created</th><td style="width:60px"></td></tr>
					</thead>
					<tbody id="reportTable">
					<s:iterator value="reports" var="r" >
					<s:if test="retest == true"><tr id="retestRow">
					<td>Retest Report</td>	
					</s:if>
					<s:else>
					<tr><td>Assessment Report</td>
					</s:else>
						<td><s:date name="gentime" format="MM-dd-yyyy hh:mm:ss"/></td>
						<td>
							<span class="vulnControl downloadReport" data-guid="<s:property value="filename"/>">
								<i class="fa fa-download"></i>
							</span>
						</td>
					</tr>
					</s:iterator>
					</tbody>
					</table>
					</bs:box>
				</bs:row>
			</bs:mco>
		</bs:row>

		<br>

		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/verification.js"></script>

		<script>
			const reportName = "${verification.assessment.finalReport.filename}";
			const vulnId = "${verification.verificationItems[0].vulnerability.id}";
			const verificationId = "${verification.id}";
			$(function() {
				$('input[type="radio"].flat-red').iCheck({
					radioClass : 'iradio_flat-red'
				});
				$('input[type="radio"].flat-green').iCheck({
					radioClass : 'iradio_flat-green'
				});
			});
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