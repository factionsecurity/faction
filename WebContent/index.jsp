<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="IE=Edge"/> 
    <meta name="google-signin-client_id" content="656902358214-6qgdm5nsl3plmglgulardai5aph3dsmj.apps.googleusercontent.com">
</head>
<body>


	<link rel="stylesheet" href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet"
		href="${pageContext.request.contextPath}/dist/font-awesome-4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/dist/ionicons-2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/bootstrap/css/bootstrap.min.css">

	<link rel="stylesheet" href="${pageContext.request.contextPath}/start.css">

	<meta name="viewport"
		content="width=device-width, initial-scale=1, maximum-scale=1" />
	
		<div class="myform" style="padding: 50px 0">
			<div class="row">
				<div class="col-md-4">
				</div>
				<div class="col-md-6" style="background: white; border-radius: 10px;">
					<img src="${pageContext.request.contextPath}/faction-logo.png"  style="width: 100%; padding: 50px; margin: 0" alt="">
					<!-- Main Form -->
					<div class="login-form-1">
						<form class=""  method="POST">
							<div class="box-body">
								<div class="form-group">
									<label for="username" class="">User Name:</label>
										<input type="text" class="form-control" id="username" name="username"
											placeholder="">
								</div>
								<div class="form-group">
									<label for="password" class="">Password:</label>
										<input type="password" class="form-control" id="password" name="password"
											placeholder="">
								
								</div>

							</div>
							<!-- /.box-body -->
							<div class="box-footer">
								<button type="submit" class="btn btn-primary pull-right btn-lg" style="background-color: #a64ed0; border-color: #a64ed0;">Sign
									in</button>
							</div>
							<!-- /.box-footer -->
						</form>
						<br>
						<br>
						<br>
						<div style="color:white; float:right">
							<a href="reset.action" style="color:#030D1C"> Reset Your Password</a>
						</div>
						<br><br>
						<s:if test="useSSO">
									<button style="width:100%" class="btn btn-warning pull-right btn-lg" onClick="document.location='startOAuth'">SSO Sign
									in</button>
									</s:if>
						<s:if test="failed==true">
							<div id="errorMsg" class="alert alert-danger alert-dismissable" style="padding-top:10px; margin-top:20px">
								<h4>
									<i class="icon fa fa-ban"></i> <b>Alert!</b>
								</h4>
								${message }
							</div>
							<script src="plugins/jQuery/jQuery-2.1.4.min.js"
								type="text/javascript"></script>
							<script type="text/javascript">
								/*$(function() {
									$("#username").keyup(function() {
										$("#errorMsg").hide();
									});
								});*/
							</script>
						</s:if>
					</div>
				</div>
			</div>


			<!-- end:Main Form -->
		</div>


</body>
</html>
