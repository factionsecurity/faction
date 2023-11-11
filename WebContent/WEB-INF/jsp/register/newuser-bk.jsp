<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<body>
  
   
    <link rel="stylesheet" href="../dist/font-awesome-4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="../dist/css/AdminLTE.min.css">
    <link rel="stylesheet" href="../dist/css/skins/skin-blue.min.css">
    
	<link rel="stylesheet" href="../login.css">
	
<link href='http://fonts.googleapis.com/css?family=Varela+Round' rel='stylesheet' type='text/css'>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-validate/1.13.1/jquery.validate.min.js"></script>
 <script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script>


<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />

<!-- Where all the magic happens -->
<!-- LOGIN FORM -->
<center>
<div class="text-center" style="padding:50px 0">
	<div class="logo">Create a Password:</div>
	<!-- Main Form -->
	<div class="login-form-1" style="min-width:300px">
		<form id="login-form" class="text-left" method="POST">
			<div class="login-form-main-message"></div>
			<div class="main-login-form">

				<div class="login-group">
					
					<div class="alert alert-danger" id="messages"><a href="#" data-dismiss="alert"></a><s:property value="message"/></a></div>
					<input type="hidden" id="uid" value="<s:property value="uid"/>"/>
					
					
					<div class="form-group">
						<label for="username" class="sr-only">Username</label><br>
						<input type="text" class="form-control" id="username" name="susername" placeholder="" readonly="readonly" value="<s:property value="username"/>">
					</div>
					<div class="form-group">
						<label for="password" class="sr-only">Password</label><br>
						<input type="password" class="form-control" id="password" name="spassword" placeholder="enter new password">
					</div>
					<div class="form-group">
						<label for="confirm" class="sr-only">Confirm Password</label><br>
						<input type="password" class="form-control" id="confirm" name="confirm" placeholder="enter confirm password">
					</div>
				</div>
				<button type="submit" class="login-button" id="update"><i class="fa fa-chevron-right"></i></button>
			</div>
			
		</form>
	</div>
	<!-- end:Main Form -->
</div>
</center>
<script>
$(function(){
	if("<s:property value="message"/>" != ""){
		//$("#messages").show();
		$(".form-group, .logo, .login-button").attr("style", "display:none");

		
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
