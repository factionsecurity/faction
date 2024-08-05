<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
	<bs:mco colsize="6">
		<bs:row>
			<bs:select name="Select Vulnerability" colsize="12"
				id="vuln_note_select">
			</bs:select>
		</bs:row>
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
					<s:if test="(verForm && pass != null) || !verForm">
					<bs:button color="warning" size="md" colsize="3"
						text="Change Start Date" id="chStart"></bs:button>
					</s:if>
					<bs:button color="warning" size="md" colsize="3"
						text="Change Severity" id="chSev"></bs:button>
						
					<s:if test="(verForm && pass != null) || !verForm">
						<bs:button color="primary" size="md" colsize="3" text="Close in Dev"
							id="closeDev"></bs:button>
						<bs:button color="success" size="md" colsize="3" text="Close in Prod"
							id="closeProd"></bs:button>
					</s:if>
					<bs:button color="danger" size="md" colsize="3"
						text="Cancel Verification" id="closeVer"></bs:button>
				</bs:row> 
				</div>
				<br/>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="primary" title="Reports">
						<table class="table table-striped table-hover dataTable no-footer">
						<thead>
						<tr><th>Report Name</th><th>Type</th><th>Created</th><td style="width:60px"></td></tr>
						</thead>
						<tbody id="reportTable">
						<s:iterator value="reports" var="r" >
						<s:if test="retest == true"><tr id="retestRow"></s:if>
						<s:else>
						<tr>
						</s:else>
							<td><s:property value="appName"/> - <s:property value="appType"/> <s:if test="retest == true">Retest</s:if> Report.docx</td>
							<td><s:property value="appType"/> <s:if test="retest == true">Retest</s:if></td>
							<td><s:date name="gentime" format="MM-dd-yyyy hh:mm:ss"/></td>
							<td>
								<span class="vulnControl downloadReport" data-guid="<s:property value="filename"/>">
									<i class="fa fa-download"></i>
								</span>
								<s:if test="retest == true">
								<span class="vulnControl genReport" >
									<i class="fa fa-arrows-rotate"></i>
								</span>
								</s:if>
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
						</s:if>
						</tbody>
						</table>
						</bs:box>
					</bs:mco>
				</bs:row>
			</bs:mco>
		</bs:row>
	</bs:mco>
</bs:row>
	
	
	
	
	
	
	
	