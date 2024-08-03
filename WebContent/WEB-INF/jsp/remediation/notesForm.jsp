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
				<label>Vulnerability Notes:</label>
				<div id="RemNotes" name="RemNotes" rows="10" cols="80">             
		 </div>
			</bs:mco>
		</bs:row>
		<br/>
		<bs:row>
			<bs:button color="success" size="md" colsize="3" text="Save"
				id="noteSave"></bs:button>
		</bs:row>
	</bs:mco>
	<bs:mco colsize="6">
		<bs:row>
			<bs:mco colsize="12">
				<label>Actions:</label>
				<bs:row>
					<bs:button color="warning" size="md" colsize="3"
						text="Change Start Date" id="chStart"></bs:button>
					<bs:button color="warning" size="md" colsize="3"
						text="Change Severity" id="chSev"></bs:button>
						
					<s:if test="(verForm && pass != null) || !verForm">
						<bs:button color="primary" size="md" colsize="3" text="Close in Dev"
							id="closeDev"></bs:button>
						<bs:button color="success" size="md" colsize="3" text="Close in Prod"
							id="closeProd"></bs:button>
					</s:if>
				</bs:row>
				<br/>
				<bs:row>
					<bs:button color="info" size="md" colsize="3"
						text="Generate Retest Report" id="genRetest"></bs:button>
					<bs:button color="info" size="md" colsize="3"
						text="Download Retest Report" id="downloadRetest"></bs:button>
					<s:if test="VerForm">
					<bs:button color="danger" size="md" colsize="3"
						text="Cancel Verification" id="closeVer"></bs:button>
					</s:if>
				</bs:row>
					<br>
					<hr>
					<br>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="primary" title="Reports">
						<table class="table table-striped table-hover dataTable no-footer">
						<tr><th>Report Name</th><th>Type</th><th>Created</th></tr>
						<s:iterator value="reports" var="r" >
						<tr>
							<td><s:property value="appId"/> - <s:property value="appName"/> Report.docx</td>
							<td><s:property value="appType"/> <s:if test="retest == true">Retest</s:if></td>
							<td><s:date name="gentime" format="MM-dd-yyyy hh:mm:ss"/></td>
						</tr>
						</s:iterator>
						</table>
						</bs:box>
					</bs:mco>
				</bs:row>
					<br>
					<hr>
					<br>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="primary" title="Note history">
							<div id="noteHistory"></div>
						</bs:box>
					</bs:mco>
				</bs:row>
			</bs:mco>
		</bs:row>
	</bs:mco>
</bs:row>
	
	
	
	
	
	
	
	