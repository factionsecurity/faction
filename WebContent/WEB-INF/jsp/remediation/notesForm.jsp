<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
	<bs:mco colsize="6">
		<bs:row>
			<bs:mco colsize="12">
				<label>Comment:</label>
				<div id="RemNotes" name="RemNotes">             
		 		</div>
			</bs:mco>
		</bs:row>
		<br/>
		<bs:row>
			<bs:button color="success" size="md" colsize="3" text="Submit"
				id="noteSave"></bs:button>
		</bs:row>
		<br/>
		<bs:row>
			<bs:mco colsize="12">
				<bs:box type="primary" title="Note history">
					<div id="noteHistory"></div>
				</bs:box>
			</bs:mco>
		</bs:row>
	</bs:mco>
	<bs:mco colsize="6">
		<bs:row>
			<bs:mco colsize="12">
				<label>Actions:</label>
				<div class="btn-actions">
				<bs:row>
					<table class="controlTable">
					<s:if test="(verForm && pass != null) || !verForm">
					<tr id="changeStartControl"><td> <a id="chStart"><i class="fa fa-calendar"></i> Change Start Date</a></td></tr>
					</s:if>
					<tr><td> <a id="chSev"><i class="fa fa-bolt"></i> Change Severity</a></td></tr>
					<s:if test="!verForm">
					<tr id="reopenVulnControl"><td> <a id="reOpen"><i class="fa fa-bug"></i> Reopen Vulnerability </a></td></tr>
					</s:if>
					<s:if test="(verForm && pass != null) || !verForm">
					<tr><td> <a id="closeDev"><i class="fa fa-code"></i> Close in Development</a></td></tr>
					<tr><td> <a id="closeProd"><i class="fa fa-server"></i> Close in Production</a></td></tr>
					</s:if>
					<tr id="closeVerControl"><td> <a id="closeVer"><i class="fa fa-circle-xmark"></i> Cancel or Close Retest</a></td></tr>
					</table>
				</bs:row> 
				</div>
				<br/>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="primary" title="Reports">
						<table class="table table-striped table-hover dataTable no-footer">
						<thead>
						<tr><th>Report Name</th><th>Type</th><th>Created</th><th></th></tr>
						</thead>
						<tbody id="reportTable">
						<s:iterator value="reports" var="r" >
						<s:if test="retest == true"><tr id="retestRow"></s:if>
						<s:else>
						<tr>
						</s:else>
							<td style="vertical-align:top"><s:property value="appName"/> - <s:property value="appType"/> <s:if test="retest == true">Retest</s:if> Report</td>
							<td style="vertical-align:top"><s:property value="appType"/> <s:if test="retest == true">Retest</s:if></td>
							<td style="vertical-align:top"><s:date name="gentime" format="MM-dd-yyyy hh:mm:ss"/></td>
							<td style="vertical-align:top">
								<div style="display:inline-block; text-align:center;">
								<s:if test="#r.variantCount > 1">
									<div class="btn-group">
										<button type="button" class="btn btn-primary dropdown-toggle" style="margin-top:.25em;" data-toggle="dropdown">
											Download <span class="caret"></span>
										</button>
										<ul class="dropdown-menu" style="background-color:#192338; border-color:#0f1a2b;">
											<li><a href="DownloadReport?guid=<s:property value="#r.filename"/>&format=docx" target="_blank" rel="noopener noreferrer" style="color:#fff;">Word (.docx)</a></li>
											<li><a href="DownloadReport?guid=<s:property value="#r.filename"/>&format=pdf" target="_blank" rel="noopener noreferrer" style="color:#fff;">PDF</a></li>
											<li><a href="DownloadReport?guid=<s:property value="#r.filename"/>&format=encryptedpdf" target="_blank" rel="noopener noreferrer" style="color:#fff;">Encrypted PDF</a></li>
										</ul>
									</div>
								</s:if>
								<s:else>
									<a class="vulnControl" href="DownloadReport?guid=<s:property value="#r.filename"/>" target="_blank" rel="noopener noreferrer">
										<i class="fa fa-download"></i>
									</a>
								</s:else>
								<s:if test="retest == true">
								<span class="vulnControl genReport" style="display:block; margin-top:20px;">
									<i class="fa fa-arrows-rotate"></i>
								</span>
								</s:if>
								</div>
							</td>
						</tr>
						</s:iterator>
						<s:if test="reports.size == 1">
						<tr id="retestRow" >
							<td></td>
							<td><a class="genReport">Generate a Retest Report</a></td>
							<td></td>
							<td>
								<span class="vulnControl genReport" >
									<i class="fa fa-arrows-rotate"></i>
								</span>
							</td>
						</tr>
						</s:if>
						</tbody>
						</table>
						<s:if test="reportPassword != null && reportPassword != ''">
						<div style="margin-top:10px; text-align:right;">
							<input type="password" id="remReportPasswordField" value="<s:property value="reportPassword"/>" readonly style="display:inline-block; width:auto; min-width:33ch; vertical-align:middle; font-size:inherit; background-color:#030d1c; color:#fff; padding:.3em;">
							<a id="toggleRemReportPassword" class="btn btn-default btn-sm" style="cursor:pointer; vertical-align:top; margin:0; padding:.5em .8em;"><i class="fa fa-eye"></i></a>
							<div><small class="text-muted">Report encryption password</small></div>
						</div>
						</s:if>
						</bs:box>
					</bs:mco>
				</bs:row>
			</bs:mco>
		</bs:row>
	</bs:mco>
</bs:row>

<script>
$(function() {
	$("#toggleRemReportPassword").click(function() {
		var field = $("#remReportPasswordField");
		var icon = $(this).find("i");
		if (field.attr("type") === "password") {
			field.attr("type", "text");
			icon.removeClass("fa-eye").addClass("fa-eye-slash");
		} else {
			field.attr("type", "password");
			icon.removeClass("fa-eye-slash").addClass("fa-eye");
		}
	});
});
</script>

	
	