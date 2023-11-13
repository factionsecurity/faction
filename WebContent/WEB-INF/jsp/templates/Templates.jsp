
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
			<div class="col-md-6">

				<div class="box box-primary">
					<div class="box-body">

						<div class="row">
							<div class="col-sm-3" style="margin-bottom: -30px; z-index: 1">
								<button class="btn btn-block btn-primary btn-sm"
									id="addTemplate">Add Template</button>
							</div>
						</div>
						<table id="templateTable"
							class="table table-striped table-hover dataTable">
							<thead class="theader">
								<tr>
									<th>id</th>
									<th>Title</th>
									<th>Type</th>
									<th>Created</th>
									<th>Active</th>
								</tr>
							</thead>
							<tbody>
								<s:iterator value="templates">
									<tr id="template${id}">
										<td><s:property value="id" /></td>
										<td><s:property value="title" /></td>
										<td><s:property value="type" />
										<td><s:property value="user.fname" /> <s:property
												value="user.lname" /></td>
										<td><input type="checkbox" /></td>
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
			<div class="col-md-6">
				<div class="col-md-12">

					<div class="box box-primary">
						<div class="box-body">
							<form>
							<bs:editor name="templateEditor" toolbar="Full" id="templateEditor"
								clickToEnable="false" >
							</bs:editor>
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