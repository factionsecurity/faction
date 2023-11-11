<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
	<bs:select name="Select Vulnerability" colsize="6"
		id="vuln_note_select">
	</bs:select>
	<bs:mco colsize="6">
		<label>Actions:</label>
		<bs:row>
			<bs:button color="warning" size="md" colsize="3"
				text="Change Start Date" id="chStart"></bs:button>
			<bs:button color="warning" size="md" colsize="3"
				text="Change Severity" id="chSev"></bs:button>
			<bs:button color="primary" size="md" colsize="3" text="Close in Dev"
				id="closeDev"></bs:button>
			<bs:button color="success" size="md" colsize="3" text="Close in Prod"
				id="closeProd"></bs:button>
			<s:if test="VerForm">
			<bs:button color="danger" size="md" colsize="3"
				text="Cancel Verification" id="closeVer"></bs:button>
			</s:if>
		</bs:row>
	</bs:mco>
</bs:row>
<bs:row>
	<bs:mco colsize="6">
		<label>Vulnerability Notes:</label>
		<textarea id="RemNotes" name="RemNotes" rows="10" cols="80">             
 </textarea>
	</bs:mco>
</bs:row>
<br>
<bs:row>
	<bs:button color="success" size="md" colsize="3" text="Save"
		id="noteSave"></bs:button>
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