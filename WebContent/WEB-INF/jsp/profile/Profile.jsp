O
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
<bs:inputgroup name="API Key (click to show): <a href='../api-docs/' style='text-decoration: underline;'>Click here for API docs</a>" colsize="12" id="apiKey" password="true">${apiKey}</bs:inputgroup>
</bs:row>
<hr>
<!--<bs:row>
    <bs:mco colsize="12">
        <h4><i class="fa fa-github"></i> GitHub Copilot Token</h4>
        <p class="text-muted">Add your personal GitHub token to enable GitHub Copilot AI features. Your token is encrypted and used only for requests made under your account.</p>
        <div class="form-group">
            <label>
                GitHub Token:
                <s:if test="user.hasCopilotToken()">
                    <span class="label label-success" style="margin-left:6px;"><i class="fa fa-check"></i> Token saved</span>
                </s:if>
                <s:else>
                    <span class="label label-warning" style="margin-left:6px;">Not set</span>
                </s:else>
            </label>
            <div class="input-group">
                <input type="password" class="form-control" id="copilotToken" placeholder="github_pat_... (leave blank to clear)">
                <span class="input-group-btn">
                    <button class="btn btn-default" type="button" id="toggleCopilotToken">
                        <i class="fa fa-eye"></i>
                    </button>
                </span>
            </div>
            <p class="help-block">Requires a GitHub Personal Access Token with Copilot access. Never pre-populated for security.</p>
        </div>
        <button type="button" class="btn btn-primary" id="saveCopilotTokenBtn">
            <i class="fa fa-save"></i> Save Copilot Token
        </button>
        <span id="copilotTokenResult" style="margin-left:10px;"></span>
    </bs:mco>
</bs:row>-->
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

	  $("#toggleCopilotToken").click(function(){
		  var input = $("#copilotToken");
		  var isHidden = input.attr('type') === 'password';
		  input.attr('type', isHidden ? 'text' : 'password');
		  $(this).find('i').toggleClass('fa-eye fa-eye-slash');
	  });

	  $("#saveCopilotTokenBtn").click(function(){
		  var btn = $(this);
		  btn.prop('disabled', true).html('<i class="fa fa-spinner fa-spin"></i> Saving...');
		  $('#copilotTokenResult').text('').removeClass('text-success text-danger');

		  $.post('SaveCopilotToken', {
			  copilotToken: $('#copilotToken').val(),
			  '_token': $('[name="_token"]').val()
		  })
		  .done(function(data){
			  if(data.result === 'success'){
				  $('#copilotTokenResult').text('Saved successfully').addClass('text-success');
				  $('#copilotToken').val('');
			  } else {
				  $('#copilotTokenResult').text(data.message || 'Failed to save').addClass('text-danger');
			  }
		  })
		  .fail(function(){
			  $('#copilotTokenResult').text('Request failed').addClass('text-danger');
		  })
		  .always(function(){
			  btn.prop('disabled', false).html('<i class="fa fa-save"></i> Save Copilot Token');
		  });
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