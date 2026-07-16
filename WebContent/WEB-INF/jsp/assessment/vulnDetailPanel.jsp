<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="vuln-detail-title">
	<h4 style="margin-top:0;"><s:property value="detailVuln.name"/></h4>
	<span class="label label-default"><s:property value="detailVuln.tracking"/></span>
	<span class="label label-primary"><s:property value="detailVuln.overallStr"/></span>
</div>

<table class="table table-condensed vuln-detail-meta" style="margin-top:15px;">
	<tr><th style="width:35%;">Impact</th><td><s:property value="detailVuln.impactStr"/></td></tr>
	<tr><th>Likelihood</th><td><s:property value="detailVuln.likelyhoodStr"/></td></tr>
	<s:if test="detailVuln.category != null">
		<tr><th>Category</th><td><s:property value="detailVuln.category.name"/></td></tr>
	</s:if>
	<s:elseif test="detailVuln.defaultVuln != null && detailVuln.defaultVuln.category != null">
		<tr><th>Category</th><td><s:property value="detailVuln.defaultVuln.category.name"/></td></tr>
	</s:elseif>
	<tr><th>Section</th><td><s:property value="detailVuln.sectionPretty"/></td></tr>
	<s:if test="detailVuln.cvssScore != null && detailVuln.cvssScore != ''">
		<tr><th>CVSS Score</th><td><s:property value="detailVuln.cvssScore"/></td></tr>
	</s:if>
	<s:if test="detailVuln.cvssString != null && detailVuln.cvssString != ''">
		<tr><th>CVSS Vector</th><td><s:property value="detailVuln.cvssString"/></td></tr>
	</s:if>
	<s:if test="detailVuln.opened != null">
		<tr><th>Opened</th><td><s:date name="detailVuln.opened" format="MM/dd/yyyy"/></td></tr>
	</s:if>
	<s:if test="detailVuln.closed != null">
		<tr><th>Closed</th><td><s:date name="detailVuln.closed" format="MM/dd/yyyy"/></td></tr>
	</s:if>
	<s:if test="detailVuln.devClosed != null">
		<tr><th>Dev Closed</th><td><s:date name="detailVuln.devClosed" format="MM/dd/yyyy"/></td></tr>
	</s:if>
	<s:if test="detailVuln.stagingClosed != null">
		<tr><th>Staging Closed</th><td><s:date name="detailVuln.stagingClosed" format="MM/dd/yyyy"/></td></tr>
	</s:if>
	<s:if test="detailAssessment != null">
		<tr><th>Assessment</th><td>[<s:property value="detailAssessment.appId"/>] <s:property value="detailAssessment.name"/></td></tr>
		<s:if test="detailAssessment.finalReport != null">
			<tr><th>Report</th><td><a href="../portal/DownloadReport?guid=<s:property value="detailAssessment.finalReport.filename"/>" target="_blank">Download Report</a></td></tr>
		</s:if>
	</s:if>
</table>

<s:if test="detailVuln.description != null && detailVuln.description != ''">
	<h5 class="vuln-detail-section-title">Description</h5>
	<div class="vuln-detail-content"><s:property value="detailVuln.description" escapeHtml="false"/></div>
</s:if>

<s:if test="detailVuln.recommendation != null && detailVuln.recommendation != ''">
	<h5 class="vuln-detail-section-title">Recommendation</h5>
	<div class="vuln-detail-content"><s:property value="detailVuln.recommendation" escapeHtml="false"/></div>
</s:if>

<s:if test="detailVuln.details != null && detailVuln.details != ''">
	<h5 class="vuln-detail-section-title">Details</h5>
	<div class="vuln-detail-content"><s:property value="detailVuln.details" escapeHtml="false"/></div>
</s:if>

<s:if test="detailVuln.customFields != null && detailVuln.customFields.size() > 0">
	<h5 class="vuln-detail-section-title">Custom Fields</h5>
	<table class="table table-condensed">
		<s:iterator value="detailVuln.customFields">
			<tr>
				<th style="width:35%;"><s:property value="type.key"/></th>
				<td><s:property value="value"/></td>
			</tr>
		</s:iterator>
	</table>
</s:if>
