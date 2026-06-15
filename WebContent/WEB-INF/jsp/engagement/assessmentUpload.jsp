<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
	     <bs:mco colsize="12">
	     <div class="form-group">
	     	<label>Upload Assessments</label>
	    	<input id="assessmentFiles" type="file"  name="file_data"/>
	     </div>
	     </bs:mco>
</bs:row>
<bs:row>
<bs:mco colsize="12">
 <a href="../assessments.csv">Download Template</a>
</bs:mco>
 </bs:row>

<!-- Results: assessments successfully added from the uploaded CSV(s) -->
<bs:row>
	<bs:mco colsize="12">
		<div id="uploadAddedWrap" style="display:none; margin-top:15px;">
			<h4>Added Assessments <span id="uploadAddedCount" class="badge bg-green"></span></h4>
			<table class="table table-bordered table-striped">
				<thead>
					<tr>
						<th>App ID</th>
						<th>Name</th>
						<th>Edit</th>
					</tr>
				</thead>
				<tbody id="uploadAddedBody"></tbody>
			</table>
		</div>
	</bs:mco>
</bs:row>

<!-- Results: assessments added but with non-fatal issues to fix (e.g. unknown assessor) -->
<bs:row>
	<bs:mco colsize="12">
		<div id="uploadWarningsWrap" style="display:none; margin-top:15px;">
			<h4 class="text-yellow">Warnings <span id="uploadWarningsCount" class="badge bg-yellow"></span></h4>
			<table class="table table-bordered table-striped">
				<thead>
					<tr>
						<th>Row</th>
						<th>App ID</th>
						<th>Name</th>
						<th>Issue</th>
					</tr>
				</thead>
				<tbody id="uploadWarningsBody"></tbody>
			</table>
		</div>
	</bs:mco>
</bs:row>

<!-- Results: rows from the CSV(s) that could not be processed -->
<bs:row>
	<bs:mco colsize="12">
		<div id="uploadErrorsWrap" style="display:none; margin-top:15px;">
			<h4 class="text-red">Upload Errors <span id="uploadErrorsCount" class="badge bg-red"></span></h4>
			<table class="table table-bordered table-striped">
				<thead>
					<tr>
						<th>Row</th>
						<th>App ID</th>
						<th>Name</th>
						<th>Error</th>
					</tr>
				</thead>
				<tbody id="uploadErrorsBody"></tbody>
			</table>
		</div>
	</bs:mco>
</bs:row>
