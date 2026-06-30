<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<style>
.history-row { cursor: pointer; }
.vuln-detail-overlay {
	position: fixed; top: 0; left: 0; width: 100%; height: 100%;
	background: rgba(0,0,0,0.4); z-index: 1040; display: none;
}
.vuln-detail-panel {
	position: fixed; top: 0; right: 0; height: 100%;
	width: 640px; max-width: 95%;
	background: #fff; z-index: 1050;
	box-shadow: -2px 0 12px rgba(0,0,0,0.3);
	transform: translateX(100%); transition: transform .25s ease;
	display: flex; flex-direction: column;
}
.vuln-detail-panel.open { transform: translateX(0); }
.vuln-detail-panel .vuln-detail-topbar {
	padding: 12px 18px; border-bottom: 1px solid rgba(255,255,255,0.15);
	display: flex; justify-content: space-between; align-items: center;
	background: #225080;
}
.vuln-detail-panel .vuln-detail-topbar h4 { margin: 0; font-size: 18px; color: #fff; }
.vuln-detail-panel .vuln-detail-topbar .btn-box-tool { color: #fff; }
.vuln-detail-panel .vuln-detail-body {
	padding: 18px; overflow-y: auto; flex: 1;
	background: #0f1522; color: #c9d3e0;
}
.vuln-detail-panel .vuln-detail-body a { color: #6fb0ff; }
.vuln-detail-panel .vuln-detail-section-title {
	margin-top: 20px; padding-bottom: 5px; border-bottom: 1px solid rgba(255,255,255,0.15);
	font-weight: bold; color: #fff;
}
.vuln-detail-panel .vuln-detail-content { word-wrap: break-word; }
.vuln-detail-panel .vuln-detail-content img { max-width: 100%; height: auto; }
.vuln-detail-panel .vuln-detail-body table { color: #c9d3e0; }
.vuln-detail-panel .vuln-detail-body table td,
.vuln-detail-panel .vuln-detail-body table th { border-color: rgba(255,255,255,0.12); }
.vuln-detail-panel .vuln-detail-meta th { color: #8a97a8; }
</style>

<bs:row>
	<bs:mco colsize="12">
		<bs:box type="primary" title="Assessment History">

			<s:if test="assessment.completed == null">
				<div class="row" style="margin-bottom:15px;">
					<div class="col-md-12">
						<button type="button" id="addOpenFindings" class="btn btn-primary">
							<i class="fa fa-plus"></i> Add Open Findings to this Assessment
						</button>
					</div>
				</div>
			</s:if>

			<div class="nav-tabs-custom">
			<ul class="nav nav-tabs" role="tablist">
				<li class="nav-item active" role="presentation"><a href="#OpenFindings" data-toggle="tab" role="tab">Open Vulnerabilities (<s:property value="openHistory.size()"/>)</a></li>
				<li class="nav-item" role="presentation"><a href="#ClosedFindings" data-toggle="tab" role="tab">Closed Vulnerabilities (<s:property value="closedHistory.size()"/>)</a></li>
			</ul>

			<div class="tab-content">

				<div class="tab-pane active" id="OpenFindings">
					<table id="openHistory" class="table table-striped table-hover dataTable">
						<thead class="theader">
							<tr>
								<th>Opened</th>
								<th>Vuln</th>
								<th>Severity</th>
								<th>Assessor</th>
								<th>Report</th>
								<th>Action</th>
							</tr>
						</thead>
						<tbody>
							<s:iterator value="openHistory">
								<tr class="history-row" data-vulnid="<s:property value="id"/>">
									<td><s:date name="opened" format="MM/dd/yyyy"/></td>
									<td><s:property value="vuln"/></td>
									<td><s:property value="severity"/></td>
									<td><s:property value="assessor"/></td>
									<td>
										<s:if test="report != null && report != ''">
											<a href="../portal/DownloadReport?guid=<s:property value="report"/>" target="_blank">Download Report</a>
										</s:if>
									</td>
									<td>
										<s:if test="assessment.completed == null">
											<s:if test="alreadyAdded">
												<span class="text-muted"><i class="fa fa-check"></i> Added</span>
											</s:if>
											<s:else>
												<button type="button" class="btn btn-xs btn-primary add-finding-btn" data-vulnid="<s:property value="id"/>">
													<i class="fa fa-plus"></i> Add to Assessment
												</button>
											</s:else>
										</s:if>
									</td>
								</tr>
							</s:iterator>
						</tbody>
					</table>
				</div>

				<div class="tab-pane" id="ClosedFindings">
					<table id="closedHistory" class="table table-striped table-hover dataTable">
						<thead class="theader">
							<tr>
								<th>Opened</th>
								<th>Closed</th>
								<th>Vuln</th>
								<th>Severity</th>
								<th>Assessor</th>
								<th>Report</th>
								<th>Action</th>
							</tr>
						</thead>
						<tbody>
							<s:iterator value="closedHistory">
								<tr class="history-row" data-vulnid="<s:property value="id"/>">
									<td><s:date name="opened" format="MM/dd/yyyy"/></td>
									<td><s:date name="closed" format="MM/dd/yyyy"/></td>
									<td><s:property value="vuln"/></td>
									<td><s:property value="severity"/></td>
									<td><s:property value="assessor"/></td>
									<td>
										<s:if test="report != null && report != ''">
											<a href="../portal/DownloadReport?guid=<s:property value="report"/>" target="_blank">Download Report</a>
										</s:if>
									</td>
									<td>
										<s:if test="assessment.completed == null">
											<s:if test="alreadyAdded">
												<span class="text-muted"><i class="fa fa-check"></i> Added</span>
											</s:if>
											<s:else>
												<button type="button" class="btn btn-xs btn-primary add-finding-btn" data-vulnid="<s:property value="id"/>">
													<i class="fa fa-plus"></i> Add to Assessment
												</button>
											</s:else>
										</s:if>
									</td>
								</tr>
							</s:iterator>
						</tbody>
					</table>
				</div>

			</div>
			</div>
		</bs:box>
	</bs:mco>
</bs:row>

<!-- Slide-out panel showing full vulnerability details for the clicked history row -->
<div id="vulnDetailOverlay" class="vuln-detail-overlay"></div>
<div id="vulnDetailPanel" class="vuln-detail-panel">
	<div class="vuln-detail-topbar">
		<h4>Vulnerability Details</h4>
		<button type="button" id="vulnDetailClose" class="btn btn-box-tool" aria-label="Close">
			<i class="fa fa-times fa-lg"></i>
		</button>
	</div>
	<div id="vulnDetailBody" class="vuln-detail-body"></div>
</div>
