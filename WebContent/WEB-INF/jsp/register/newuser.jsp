<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    
    
	<link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet"
		href="../dist/font-awesome-4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">

	<link rel="stylesheet" href="../start.css">
	


	<meta name="viewport"
		content="width=device-width, initial-scale=1, maximum-scale=1" />

</head>
<body>	
<div class="myform" style="padding: 50px 0">
			<div class="row">
				<div class="col-md-4">
				</div>
				<div class="col-md-6"  style="background: white; border-radius: 10px;padding:30px;">
					<img src="${pageContext.request.contextPath}/faction-logo.png"  style="width: 100%; padding: 50px; margin: 0" alt="">
	<!-- Main Form -->
	<div class="login-form-1">
		<form id="login-form"  method="POST">
			<div class="box-body">
				<div class="row">
					<div class="login-group col-md-12" style="text-align: center"><h3>Change Password</h3></div>
					<div class="alert alert-danger" id="messages" style="display:none; margin-top:200px"><a href="#" data-dismiss="alert"></a><s:property value="message"/>
					</div>
					<input type="hidden" id="uid" value="<s:property value="uid"/>"/>
					
					
					<div class="form-group">
						<label for="username" >Username</label><br>
						<input type="text" class="form-control" id="username" name="username" placeholder="" readonly="readonly" value="<s:property value="username"/>">
					</div>
					<div class="form-group">
						<label for="password" >Password</label><br>
						<input type="password" class="form-control" id="password" name="password" placeholder="enter new password">
					</div>
					<div class="form-group">
						<label for="confirm" >Confirm Password</label><br>
						<input type="password" class="form-control" id="confirm" name="confirm" placeholder="enter confirm password">
					</div>
				</div>
			</div>
			<!-- /.box-body -->
			<div class="box-footer">
					
					<button type="submit" class="btn btn-success pull-right btn-lg" id="update" >Update</button>
			</div>
				<!-- /.box-footer -->
		</form>
	</div>
			
	<s:if test="failed==true">
							<div id="errorMsg" class="alert alert-danger alert-dismissable">
								<h4>
									<i class="icon fa fa-ban"></i> Alert!
								</h4>
								User Name and/or Password is invalid
							</div>
							<script src="plugins/jQuery/jQuery-2.1.4.min.js"
								type="text/javascript"></script>
							<script type="text/javascript">
								$(function() {
									$("#username").keyup(function() {
										$("#errorMsg").hide();
									});
								});
							</script>
						</s:if>
					</div>


			<!-- end:Main Form -->

<script>
$(function(){
	if("<s:property value="message"/>" != ""){
		$("#messages").show();
		$(".form-group, .logo, .btn").attr("style", "display:none");

		
	}
	$("#update").click(function(event){
		event.preventDefault();
	data="uid=" + $("#uid").val();
	data+="&password=" + $("#password").val();
	data+="&confirm=" + $("#confirm").val();
	$("#messages").hide();
	$.post("Register", data).done(function(resp){
		console.log(resp);
		if(resp.message == ""){
			document.location="../";
		}else{
			$("#messages").html(resp.message);
			$("#messages").show();
			
		}
		
		
	});
	
	});
	
	$("input[type=password]").click(function(){
		$(this).attr("placeholder", "");
		
	});
});

</script>
</body>
</html>
