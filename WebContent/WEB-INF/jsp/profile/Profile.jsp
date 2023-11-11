<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />

<bs:row>
	<bs:mco colsize="6">
		<bs:row>
		<bs:inputgroup name="First Name:" colsize="6" id="profile_fname"><s:property value="user.fname"/></bs:inputgroup>
		<bs:inputgroup name="Last Name:" colsize="6" id="profile_lname"><s:property value="user.lname"/></bs:inputgroup>
		<bs:inputgroup name="Email:" colsize="12" id="profile_email"><s:property value="user.email"/></bs:inputgroup>
		<bs:inputgroup name="Enter Current Password to Update:" colsize="12" id="profile_password" password="true"></bs:inputgroup>
		</bs:row>
		<hr>
		<bs:row>
		<bs:mco colsize="12">Change your password?</bs:mco><br>
		<bs:inputgroup name="New Password:" colsize="12" id="profile_newpassword" password="true"></bs:inputgroup>
		<bs:inputgroup name="Confirm Password:" colsize="12" id="profile_confirm" password="true"></bs:inputgroup>
		</bs:row>
	</bs:mco>
	<!-- Profile Picture info -->
	<bs:mco colsize="6">
	<div class="col-md-12">
                <label class="control-label">Select Profile Image</label>
    			<input id="profile_image" type="file" name="profileImage" class="file-loading" data-show-upload="false" data-show-caption="true"/>
    </div>
	
	</bs:mco>
</bs:row>
<hr>
<bs:row>
<bs:inputgroup name="API Key (click to show): <a href='/faction/api-docs/' style='text-decoration: underline;'>Click here for API docs</a>" colsize="12" id="apiKey" password="true">${apiKey}</bs:inputgroup>
</bs:row>
<script src="../fileupload/js/fileinput.min.js" type="text/javascript"></script>
<script>

$(function () {
	  $("#apiKey").click(function(){
		  if($(this).attr('type')== 'password'){
			  $(this).attr('type', 'input');
		  }else{
			  $(this).attr('type', 'password'); 
		  }
	  });
	  $("#profile_image").fileinput({
		 overwriteInitial: true,
		 autoReplace: true,
		 maxFileCount: 1,
		 initialPreviewAsData: true,
		 uploadUrl: "Profile?action=imgUpload",
		 allowedFileExtensions : ['jpg','gif','png', 'jpeg'],
		 initialPreview: [
		 
			<s:if test="user.avatarGuid != null">
               		"../service/profileImage"
            </s:if>
            <s:else>
               		"../dist/img/default-avatar.png"
           </s:else>
			
	      ],
	  	initialPreviewConfig: [	{ "width" : "160px", "height": "160px", 
            "url" : "DeleteProfileImage",
            "type" : "image"}
	  	]
	 
	  });

});


</script>