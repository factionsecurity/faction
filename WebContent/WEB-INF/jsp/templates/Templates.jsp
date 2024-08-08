
<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<link href="../dist/css/jquery.autocomplete.css" media="all"
	rel="stylesheet" type="text/css" />
<link href="../dist/css/throbber.css" media="all" rel="stylesheet"
	type="text/css" />
<link href="../plugins/jquery-confirm/css/jquery-confirm.css"
	media="all" rel="stylesheet" type="text/css" />
<style>
#templateEditor{
	background-color: white;
}
.disabled {
	opacity: 0.3;
	pointer-events: none;
}
</style>
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-pencil"></i> Assessment Templates
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">


		<div class="row">
			<div class="col-md-4">

				<div class="box box-primary">
					<div class="box-body">

						<div class="row">
							<div class="col-sm-4" style="margin-bottom: -30px; z-index: 1">
								<button class="btn btn-block btn-primary btn-sm"
									id="createTemplate">New Template</button>
							</div>
						</div>
						<table id="templateTable"
							class="table table-striped table-hover dataTable">
							<thead class="theader">
								<tr>
									<th></th>
									<th>Description</th>
									<th>Active</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
								<s:iterator value="templates">
									<tr id="template${id}">
										<td><s:property value="id" /></td>
										<td><b><s:property value="title" /></b><br/>
										<small><span><b>Type:</b> <s:property value="type" /></span>
										<span><b>By:</b> <s:property value="user.fname" /> <s:property
												value="user.lname" /></span>
										<span><b>On:</b> <s:date name="created" format="MM/dd/yyyy"/> </span></small>
										</td>
										<td><input type="checkbox" class="activeCheckBox" <s:if test="active">checked</s:if> /></td>
										<td>
						<span class="vulnControl vulnControl-delete"><i class="fa fa-trash" title="Delete Template"></i></span></td>
									</tr>
								</s:iterator>
							</tbody>
							<tfoot>
							</tfoot>
						</table>
					</div>
					<!-- /.box-body -->
				</div>
				<!-- /.box -->
			</div>
			<div class="col-md-8">
				<div class="col-md-12">

					<div id="editorContainer" class="box box-primary disabled">
						<div class="box-header">
						<h2><span id="templateName">&nbsp;</span><span id="edits"></span></h2> 
						<span id="saveTemplate" class="vulnControl pull-right" style="margin-top: -40px"><i class="fa fa-save" title="Save Template"></i></span>
						</div>
						<div class="box-body">
							<form>
							<div name="templateEditor" toolbar="Full" id="templateEditor"
								clickToEnable="false" >
							</div>
							</form>
						</div>
						<!-- /.box-body -->
					</div>
					<!-- /.box -->
				</div>
			</div>
		</div>



		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/templates.js"></script>

		</body>
		</html>