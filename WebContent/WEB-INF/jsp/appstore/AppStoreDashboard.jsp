<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<style>
.circle {
	border-radius: 50%;
	padding: 3px;
}

.list-group-item {
	background-color: #192339 !important;
	border-top: 0px;
	border-left: 0px;
	border-right: 0px;
	border-bottom: 1px solid #607d8b;
	border-top: 1px solid #607d8b;
	border-radius: 0px;
}

.list-group-item:last-child {
	margin-bottom: 0;
	border-bottom-right-radius: 0px;
	border-bottom-left-radius: 0px;
}

.list-group-item:first-child {
	border-top-left-radius: 0px;
	border-top-right-radius: 0px;
}

.handle {
	margin: 0;
	position: absolute;
	top: 50%;
	-ms-transform: translateY(-50%);
	transform: translateY(-50%);
}

.handle-container {
	height: 65px;
}
.bootstrap-switch .bootstrap-switch-handle-on.bootstrap-switch-primary,
.bootstrap-switch .bootstrap-switch-handle-off.bootstrap-switch-primary {
  color: #fff;
  background: #a64ed0;
}
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="fa fa-rocket"></i> App Store

		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Installed Assessment Extensions">
					<ul id="assessmentExtensions" class="list-group">
					</ul>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Installed Vulnerability Extensions">
					<ul id="vulnerabilityExtensions" class="list-group">
					</ul>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Installed Verification Extensions">
					<ul id="verificationExtensions" class="list-group">
					</ul>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Installed Application Inventory Extensions">
					<ul id="inventoryExtensions" class="list-group">
					</ul>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Disabled Extensions">
					<ul id="disabledExtensions" class="list-group">
					</ul>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Install Custom Apps">
					<div class="form-group">
						<label>Upload Extension</label> <input id="appFile" type="file"
							multiple name="file_data" />
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>

		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/appstore.js"></script>

		</body>
		</html>