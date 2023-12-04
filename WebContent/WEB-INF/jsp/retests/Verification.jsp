<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet" href="../plugins/iCheck/all.css">

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
		<bs:box type="info"
			title="<i class='glyphicon  glyphicon-list-alt'></i> Vulnerability Info">
			<bs:row>
				<bs:mco colsize="12">
					<bs:datatable
						columns="App Id, App Name, Vuln Name, Overall, Impact, Likelyhood, start, end"
						classname="primary" id="">
						<tr>
							<td><s:property value="verification.assessment.appId" /></td>
							<td><s:property value="verification.assessment.name" /></td>
							<td><s:property
									value="verification.verificationItems[0].vulnerability.name" /></td>
							<td><s:property
									value="verification.verificationItems[0].vulnerability.overallStr" /></td>
							<td><s:property
									value="verification.verificationItems[0].vulnerability.impactStr" /></td>
							<td><s:property
									value="verification.verificationItems[0].vulnerability.likelyhoodStr" /></td>
							<td><s:property value="verification.start" /></td>
							<td><s:property value="verification.end" /></td>
						</tr>
					</bs:datatable>
				</bs:mco>
			</bs:row>
		</bs:box>
		<bs:row>
			<bs:mco colsize="6">

				<bs:box type="info"
					title="<i class='fa fa-bug'></i> Retest Notes">

					<bs:row>
						<bs:mco colsize="12">
							<textarea name="notes" id="notes" disabled>
					  <s:property value="verification.Notes" />
				  </textarea>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="3">
				<bs:box type="info"
					title="<i class='glyphicon glyphicon-download'></i> Supporting Files">
					<bs:row>
						<bs:mco colsize="12">
							<input id="files" type="file" multiple name="file_data"/>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="3">
				<bs:box type="info"
					title="<i class='glyphicon glyphicon-asterisk'></i> Retest Actions">
					<bs:row>
						<bs:mco colsize="12">
							<bs:row>
								<bs:mco colsize="6">
									<div class="form-group" style="font-size: xx-large; padding-left:30px">
										<label> <input type="radio" name="r3"
											class="flat-green" value="1"> Pass <br/><input
											type="radio" name="r3" class="flat-red" checked value="0">
											Fail
										</label>
									</div>
								</bs:mco>
								<bs:mco colsize="6">
									<bs:button color="info" size="md" colsize="12"
										text="<span class='fa fa-file'></span> Full Report" id="open"></bs:button>
									<br>
									<br>
									<bs:button color="primary" size="md" colsize="12"
										text="<span class='fa fa-check' ></span> Complete " id="save"></bs:button>
									<br>
									<br>
									<bs:button color="danger" size="md" colsize="12"
										text="<span class='fa fa-ban' ></span> Cancel" id="cancel"></bs:button>
									<br>
									<br>
									<!--<bs:button color="warning" size="md" colsize="6" text="Retest Report" id="retest"></bs:button>-->
								</bs:mco>

							</bs:row>
							<br>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
		</bs:row>

			<br>
			<bs:row>

				<bs:mco colsize="12">
					<bs:box type="warning" title="<i class='glyphicon  glyphicon-edit'></i> Pass or Fail Notes">
						<textarea id="failnotes" name="failnotes"></textarea>
					</bs:box>
				</bs:mco>
			</bs:row>
		<bs:box type="info" title="Exploit Steps">
			<s:iterator
				value="verification.verificationItems[0].vulnerability.steps"
				var="s">
				<h2>
					<u><b>Example <s:property value="#s.stepNum" />:
					</b></u>
				</h2>
				<hr>
				<s:property value="#s.description" escapeHtml="false" />
				<!--  <img src="../service/getImage?vulnid=<s:property value="verification.verificationItems[0].vulnerability.id"/>&stepId=<s:property value="#s.id"/>"/>-->
			</s:iterator>

		</bs:box>

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
		</script>

		</body>
		</html>