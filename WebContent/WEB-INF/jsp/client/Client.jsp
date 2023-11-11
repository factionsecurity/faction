<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
<link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet"
		href="../dist/font-awesome-4.4.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
	<link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="../plugins/select2/select2.min.css">
    <link rel="stylesheet" href="../bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="../dist/font-awesome-4.4.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="../dist/ionicons-2.0.1/css/ionicons.min.css">
    <link rel="stylesheet" href="../dist/css/skins/skin-blue.min.css">
    <link rel="stylesheet" href="../plugins/datatables/jquery.dataTables.css">
    <link rel="stylesheet" href="../plugins/jquery-confirm/css/jquery-confirm.css">
    <link rel="stylesheet" href="../plugins/daterangepicker/daterangepicker-bs3.css">
    <link rel="stylesheet" href="../dist/css/Fuse.css">

</head>
<body>
   <style>
    	body{
    	background: #367fa9;
    	}
    	.content-wrapper{
    	margin-left:100px;
    	margin-right:100px;
    	}
    	.main-footer{
    	margin-left:100px;
    	margin-right:100px;
    	}
    	.jconfirm{
    		z-index:1000 important!;
    	}
    	.daterangepicker.dropdown-menu{
    		z-index:199999999;
    	}
    	#refresh{
			float:right;
    	}
    </style>
	<div class="wrapper">
		<header class="main-header">
        <!-- Header Navbar -->
        <nav class="navbar navbar-static-top" role="navigation">
        </nav>
        </header>
        <!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
	  <!-- Content Header (Page header) -->
	  <section class="content-header">
	    <h1>
	      <i class="fa fa-bug"></i> Remediation and Retest Queue <button class="btn btn-success" id="refresh">Refresh</button>
	      <small></small>
	    </h1>
	  </section>

  <!-- Main content -->
  <section class="content">
	
		<div class="myform" style="padding: 50px 0">
			<bs:row>
			<bs:mco colsize="12">
				<bs:box type="danger" title="Findings">
					<bs:row>
					<bs:mco colsize="12">
						<bs:datatable columns="Id,TrackingId,Status,Assign" classname="" id="issues" >
						<s:iterator value="vitems">
							<tr><td>${v.id}</td><td>${v.tracking }</td><td>${status}</td><td width="50px"><button class="btn btn-warning assign" hiddenid="${v.id}">Schedule Retest</button></td></tr>
						</s:iterator>
						</bs:datatable>
					</bs:mco>
					</bs:row>
				
				</bs:box>
			</bs:mco>
			</bs:row>


			<!-- end:Main Form -->
		</div>
		</section>
</div>
<footer class="main-footer">
        <!-- To the right -->
        <div class="pull-right hidden-xs">
           Version 1.8 Beta
        </div>
        <!-- Default to the left -->
        <strong>Copyright &copy; 2016 <a href="https://www.fusesoftsecurity.com">FuseSoft</a>.</strong> All rights reserved.
      </footer>
</div>

<!-- jQuery 2.1.4 -->
    <script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script>
    <!-- Bootstrap 3.3.5 -->
    <script src="../bootstrap/js/bootstrap.min.js"></script>
    <script src="../dist/js/app.js"></script>
    <script src="../plugins/jquery-confirm/js/jquery-confirm.js" type="text/javascript"></script>
     <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <script src="../dist/js/moment.js"></script>
    <script src="../plugins/daterangepicker/daterangepicker.js"></script>
    <script src="//cdn.ckeditor.com/4.15.1/standard/ckeditor.js"></script>
    
<script>
var notes="<s:property value="notes"/>";
$(function(){
	$("#refresh").click(function(){
		console.log("test");
		var newForm = $("<form/>").attr("action","ClientPortal").attr("method","POST").attr("id","refreshForm");
		var key = $("<input/>").attr("name","accessKey").val("${accessKey}");
		var noteData = $("<input/>").attr("name","notes").val(notes);
		$(newForm).append(key);
		$(newForm).append(noteData);
		$("body").append(newForm);
		$("#refreshForm").submit();
	});
	$(".assign").click(function(){
		vid = $(this).attr("hiddenid");
		$.confirm({
			columnClass: 'col-md-8',
			content:function () {
		        var self = this;
		        data="modal=true";
		        data+="&accessKey=${accessKey}";
		        data+="&vid="+vid;
		        data+="&notes=" + notes;
		        return $.post("ClientPortal", data).done(function (response) {
		        	self.setContent(response);
		        	
		  			setTimeout(function(){
		  				$("#notes").show();
		  				$("#dates").daterangepicker();
		        		CKEDITOR.config.contentsCss=CKEDITOR.getUrl("../service/rd_styles.css");
		        	  	CKEDITOR.config.disableNativeSpellChecker = false;
		        		CKEDITOR.replace('notes', {customConfig : 'ckeditor_config.js', toolbar: 'Full'});
		  			}, 1000);

		        })}, //'url:ClientPortal?modal=true&accessKey=${accessKey}&vid='+vid,
				
			title: "Assign Verification",
			confirm: function(){
				data="accessKey=${accessKey}"
				data+="&vid="+$("#hiddenid").val();
				data+="&start=" + $("#dates").val().split("-")[0].trim();
				data+="&end=" + $("#dates").val().split("-")[1].trim();
				notes = encodeURIComponent(CKEDITOR.instances.notes.getData());
				data+="&notes=" + notes;
				console.log(data);
				$.post("ClientPortal",data).done(function(){
					var newForm = $("<form/>").attr("action","ClientPortal").attr("method","POST").attr("id", "reloadForm");
					var key = $("<input/>").attr("name","accessKey").val("${accessKey}");
					var noteData = $("<input/>").attr("name","notes").val(notes);
					$(newForm).append(key);
					$(newForm).append(noteData);
					$("body").append(newForm);
					$("#reloadForm").submit();
				});
			}
		})
	});
	
});

</script>
		


</body>
</html>
