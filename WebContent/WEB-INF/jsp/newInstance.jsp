<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    
	<link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet"
		href="dist/font-awesome-4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="dist/ionicons-2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">

	<link rel="stylesheet" href="start.css">
	


	<meta name="viewport"
		content="width=device-width, initial-scale=1, maximum-scale=1" />

</head>
<body>


	
		<div class="myform" style="padding: 50px 0">
			<div class="row">
				<div class="col-lg-2">
				</div>
				<div class="col-lg-8"  style="background: white; border-radius: 10px;padding:30px;">
					<img src="${pageContext.request.contextPath}/faction-logo.png"  style="width: 100%; padding: 50px; margin: 0" alt="">
	<!-- Main Form -->
	<div class="login-form-1">
		<form id="login-form"  method="POST">
			
			<div class="box-body">
				<s:if test="message != ''">
				<div class="row">
				<div class="col-md-12">
				<div id="errorMsg" class="alert alert-danger alert-dismissable">
								<h4>
									<i class="icon fa fa-ban"></i> Alert!
								</h4>
								${message}
							</div>
				</div>
				</div>
				</s:if>
				<div class="row">
					<div class="login-group col-md-12" style="text-align: center"><h3>Finish Setting Up Your Account</h3></div>
				</div>
				<div class="row"><hr/></div>
				<div class="row">
					<div class="login-group col-md-6">
						<div class="form-group">
							<label for="adminUsername" class="">Admin Username *</label><br>
							<input type="text" class="form-control" id="adminUsername" name="adminUsername" placeholder="username"/>
						</div>
						<div class="form-group">
							<label for="adminPassword" class="">Password *</label><br>
							<input type="password" class="form-control" id="adminPassword" name="adminPassword" placeholder="password"/>
						</div>
						<div class="form-group">
							<label for="confirm" class="">Confirm Password *</label><br>
							<input type="password" class="form-control" id="confirm" name="confirm" placeholder="password"/>
						</div>
						<div class="form-group">
							<label for="first" class="">First Name *</label><br>
							<input type="text" class="form-control" id="first" name="first" placeholder="first"/>
						</div>
						<div class="form-group">
							<label for="last" class="">Last Name *</label><br>
							<input type="text" class="form-control" id="last" name="last" placeholder="last"/>
						</div>
						<div class="form-group">
							<label for="email" class="">Email *</label><br>
							<input type="text" class="form-control" id="eamil" name="email" placeholder="email">
						</div>
						</div>
						<div class="login-group col-md-6">
							<div class="form-group">
								<label for="team" >Create a Team Name (Optional)</label><br>
								<input type="text" class="form-control" id="team" name="team" placeholder="Hacking Team">
							</div>
							
						</div>
					</div>
					<input type="hidden" value="create" name="action" id="action"/>
				</div>
				
			</div>
			<div class="box-footer">
				<button type="submit" class="btn btn-success pull-right btn-lg">Save</button>
			</div>
		</form>
		</div>
			
		
		</body>
</html>
			

