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
	background-color: #192339;
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
	.bootstrap-switch .bootstrap-switch-handle-off.bootstrap-switch-primary
	{
	color: #fff;
	background: #a64ed0;
}

.active{
	background-color: #FFFFFF22 !important;
}
.extensionDescription{
	min-height: 500px;
	padding: 30px;
	
}
.disabled{
	opacity: 0.0;
    pointer-events: none;
}
.appLogo{
  border-radius: 25px;
  height: 150px;
  background-color: white;
  margin-top:25px;
}
.appLogo-small{
  border-radius: 5px;
  height: 50px;
  margin-left: 30px;
  background-color: white;
}
.appAuthor{
font-weight: normal;
}
.appDescription{
 padding-top: 50px;
 font-size: large;
 padding-left: 20px;
}
.appStoreButton{
}

#appLoading{
	display:none;
}
</style>
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="fa fa-rocket"></i> Installed Extensions
		</h1>
  <button class="btn btn-success pull-right dashboardButton" id="installExtension"><i class="fa fa-upload"></i>&nbsp;&nbsp;Install Extension</button>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="6">
				<bs:mco colsize="12">
					<bs:box type="primary" title="<i class='glyphicon glyphicon-th-list'></i>&nbsp;&nbsp;&nbsp;Assessment Extensions <small>Drag to change order of operations. Executes from top to bottom.</small>">
						<ul id="assessmentExtensions" class="list-group">
						</ul>
					</bs:box>
				</bs:mco>
				<bs:mco colsize="12">
					<bs:box type="primary" title="<i class='fa fa-bug'></i>&nbsp;&nbsp;&nbsp;Vulnerability Extensions <small>Drag to change order of operations. Executes from top to bottom.</small>">
						<ul id="vulnerabilityExtensions" class="list-group">
						</ul>
					</bs:box>
				</bs:mco>
				<bs:mco colsize="12">
					<bs:box type="primary" title="<i class='glyphicon glyphicon-ok'></i>&nbsp;&nbsp;&nbsp;Verification Extensions <small>Drag to change order of operations. Executes from top to bottom.</small>">
						<ul id="verificationExtensions" class="list-group">
						</ul>
					</bs:box>
				</bs:mco>
				<bs:mco colsize="12">
					<bs:box type="primary"
						title="<i class='glyphicon glyphicon-search'></i>&nbsp;&nbsp;&nbsp;Application Inventory Extensions <small>Drag to change order of operations. Executes from top to bottom.</small>">
						<ul id="inventoryExtensions" class="list-group">
						</ul>
					</bs:box>
				</bs:mco>
				<bs:mco colsize="12">
					<bs:box type="primary" title="<i class='glyphicon glyphicon-ban-circle'></i>&nbsp;&nbsp;&nbsp;Disabled Extensions">
						<ul id="disabledExtensions" class="list-group">
						</ul>
					</bs:box>
				</bs:mco>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Extension Description" id="appLoading">
					<div id="loadingbox" class="extensionDescription js-loading">
					<bs:row>
						<bs:mco colsize="9">
						</bs:mco>
						<bs:mco colsize="3">
  							<button class="btn btn-primary" ><i class="fa fa-upload"></i>&nbsp;&nbsp;Install Update</button>
  						</bs:mco>
  					</bs:row>
					<bs:row>
						<bs:mco colsize="2" style="min-width: 200px"><img src="../app-default.png" class="appLogo"/></bs:mco>
						<bs:mco colsize="8">
						<h1 >Loading Extension <br/><small>Version: 0.0</small></h1>
							By: <span class="appAuthor">...</span> <br/>
							Link: ...<br/>
							Hash: ...<br/>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="12">
						<div class="appDescription">
						</div>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="12">
						</bs:mco>
					</bs:row>
					<bs:row>
						<br/>
						<bs:mco colsize="2">
  						</bs:mco>
  					</bs:row>
					</div>
					<script>
					</script>
				</bs:box>
				<bs:box type="primary disabled" title="Extension Description" id="appBox">
					<div class="extensionDescription">
					<bs:row>
						<bs:mco colsize="9">
						</bs:mco>
						<bs:mco colsize="3">
  							<button class="btn btn-primary" id="installUpdate"><i class="fa fa-upload"></i>&nbsp;&nbsp;Install Update</button>
  						</bs:mco>
  					</bs:row>
					<bs:row>
						<bs:mco colsize="2" style="min-width: 200px"><img id="appLogo" src="../app-default.png" class="appLogo"/></bs:mco>
						<bs:mco colsize="8">
						<h1 id="appTitle">Example Extension <br/><small>Version: 1.0</small></h1>
							By: <span id="appAuthor" class="appAuthor">Jane Doe</span> <br/>
							Link: <a href="" id="appURL">www.factionsecurity.com</a><br/>
							Hash: <span id="appHash" ></span> <br/>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="12">
						<div id="appDescription" class="appDescription">
						Extension Information will be displayed here.
						</div>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="12">
						<div id="appConfigs" class="appConfigs">
						<bs:row>
						</bs:row>
						</div>
						</bs:mco>
					</bs:row>
					<bs:row>
						<br/>
						<bs:mco colsize="2">
  							<button class="btn btn-primary" id="saveConfigs"><i class="fa fa-save"></i>&nbsp;&nbsp;Save</button>
  						</bs:mco>
  					</bs:row>
					</div>
				
				</bs:box>
			</bs:mco>
		</bs:row>

		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/appstore.js"></script>

		</body>
		</html>