<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<!-- jQuery 2.1.4 -->
<script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script>
<style>
.aPostClass{
	display:none;
}
</style>


<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<span class="glyphicon glyphicon-flash"></span>Live Feed <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
	<bs:row>
	<bs:mco colsize="6">
		<div class="nav-tabs-custom">
		   <ul class="nav nav-tabs">
		     <li class="active"><a href="#Everyone" data-toggle="tab">Everyone <span id="EveryCount" class="label label-success"></span></a></li>
		     <li><a href="#Team" data-toggle="tab">Team <span id="TeamCount" class="label label-success"></span></a></li>
		     <li><a href="#Assessments" data-toggle="tab">Assessments <span id="AsmtCount" class="label label-success"></span></a></li>
		   </ul>
		   <div class="tab-content" style="background-color:#F2F3F4">
		     <div class="tab-pane active" id="Everyone">
			     <bs:row>
		      	<bs:mco colsize="8">
		      	<div class="updates1" style="display:none">
			      	<h3>
			      	<button class="btn btn-success refresh">
			      		<i class="glyphicon glyphicon-refresh"></i> 
			      	</button>&nbsp;
			      	<span id="everyMsg"></span>
			      	</h3>
		      	</div>
		      	</bs:mco>
		      	<bs:mco colsize="4">
		      	<h3>
		      	<button class="pull-right btn btn-primary newPost" type="every">New Post</button>
		      	</h3>
		      	</bs:mco></bs:row><br>
		     	<s:if test="everyFeed == null || everyFeed.size==0">
					<h3><center>Nothing to Read Right Now :( </center></h3>
				</s:if>
		     	<s:iterator value="everyFeed.values()" var="every">
					<bs:feed id="${guid}" feed="${every}" user="${user}"></bs:feed>
			 	</s:iterator>
				
		     </div><!-- /.tab-pane -->
		     <div class="tab-pane" id="Team">
		     	<bs:row>
		      	<bs:mco colsize="8">
		      	<div class="updates2" style="display:none">
			      	<h3>
			      	<button class="btn btn-success refresh">
			      		<i class="glyphicon glyphicon-refresh"></i> 
			      	</button>&nbsp;
			      	<span id="teamMsg"></span>
			      	</h3>
		      	</div>
		      	</bs:mco>
		      	<bs:mco colsize="4">
		      	<h3>
		      	<button class="pull-right btn btn-info newPost" type="team">New Post</button>
		      	</h3>
		      	</bs:mco></bs:row><br>
		        <s:if test="teamFeed == null || teamFeed.size==0">
					<h3><center>Nothing to Read Right Now :( </center></h3>
				</s:if>
		     	<s:iterator value="teamFeed.values()" var="team">
					<bs:feed id="${team.guid}" feed="${team}" user="${user}"></bs:feed>
			 	</s:iterator>
		     </div><!-- /.tab-pane -->
		      <div class="tab-pane" id="Assessments" >
		      	<bs:row>
		      	<bs:mco colsize="8">
		      	<div class="updates3" style="display:none">
			      	<h3>
			      	<button class="btn btn-success refresh">
			      		<i class="glyphicon glyphicon-refresh"></i> 
			      	</button>&nbsp;
			      	<span id="asmtMsg"></span>
			      	</h3>
		      	</div>
		      	</bs:mco>
		      	<bs:mco colsize="4">
		      	<h3>
		      	<button class="pull-right btn btn-success newPost" type="assessment">New Post</button>
		      	</h3>
		      	</bs:mco></bs:row><br>
		     	<s:if test="asmtFeed == null || asmtFeed.size==0">
					<h3><center>Nothing to Read Right Now :( </center></h3>
				</s:if>
		     	<s:iterator value="asmtFeed.values()" var="asmt">
					<bs:feed id="${asmt.guid}" feed="${asmt}" user="${user}"></bs:feed>
			 	</s:iterator>
		     </div><!-- /.tab-pane -->
		     
		    
		   </div><!-- /.tab-content -->
	 	</div><!-- nav-tabs-custom -->
	</bs:mco>
	</bs:row>
<!-- New Post Modal -->	
<bs:modal modalId="modal" saveId="saveComment" title="New Post">
  <bs:row>
  <div class="aPostClass">
	  <bs:select name="Post to Assessment:" colsize="12" id="aPost">
	  	<s:iterator value="assessments">
	  		<option value="${id }">${appId} - ${name}</option>
	  	</s:iterator>
	  </bs:select>
  </div>
  <bs:mco colsize="12">
  	<textarea id="newPostComment" name="newPostComment">
  	</textarea>
  </bs:mco>
  </bs:row>
  </bs:modal>
	
	
	<jsp:include page="../footer.jsp" />

		<!-- DataTables -->
		<script src="../plugins/datatables/jquery.dataTables.min.js"
			type="text/javascript"></script>
		<script src="../plugins/datatables/dataTables.bootstrap.min.js"
			type="text/javascript"></script>
		<!-- SlimScroll -->
		<script src="../plugins/slimScroll/jquery.slimscroll.min.js"
			type="text/javascript"></script>
		<!-- FastClick -->
		<script src="../plugins/fastclick/fastclick.min.js"
			type="text/javascript"></script>
		<script src="../dist/js/moment.js" type="text/javascript"></script>
		<script src="../plugins/fullcalendar/fullcalendar.min.js"
			type="text/javascript"></script>
		<script src="../plugins/daterangepicker/daterangepicker.js"
			type="text/javascript"></script>
		<script src="../plugins/select2/select2.full.min.js"
			type="text/javascript"></script>
			<script src="//cdn.ckeditor.com/4.15.1/standard/ckeditor.js"></script>

			
			
<script>
$(function(){
	
	 var url = document.location.toString();
	  if (url.match('#')) {
		  hash=document.location.hash;
		  hash = hash.replace("#","");
		  splits = hash.split("_");
		  if(splits.length == 1 && !splits[0].startsWith("id")){
	      	$('.nav-tabs a[href=#'+splits[0]+']').tab('show');
		  }else if (splits.length ==2 && splits[1].startsWith("id")){
			id=splits[1].replace("id=","");
			$("#cm_"+id).toggle();
			if(splits[0] != ""){
				$('.nav-tabs a[href=#'+splits[0]+']').tab('show');
			}
		  }
	  } 
	  $('.nav-tabs a').click(function(){ 
			hash = $(this).attr('href');
			window.location.hash=hash;
   	  });
	  
	CKEDITOR.replace('newPostComment', {customConfig : 'ckeditor_config.js', toolbar: 'Full'});
	$("[id^='comment_']").keypress(function(e) {
		
	    if(e.which == 13) {
	    	e.preventDefault();
	    	hash=document.location.hash;
	    	splits=hash.split("_");
	    	hash=splits[0] + "_id=" + $(this).attr("rid");
	    	document.location.hash=hash;
	    	id = $(this).attr("id");
	    	id=id.replace("comment_", "");
	        var data="action=comment";
	        data+="&comment=" + $(this).val();
	        data+="&feedid=" + id;
	        
	        $.post("LiveFeed", data).done(function(resp){
	        	location.reload();
	        });
	    }
	});
	$("[id^='like_']").click(function(e) {

    	id = $(this).attr("id");
    	id=id.replace("like_", "");
        var data="action=like";
        data+="&feedid=" + id;
        
        $.post("LiveFeed", data).done(function(resp){
        	location.reload();
        });
	    
	});
	$("[id^='delcomment_']").click(function(e) {
    	id = $(this).attr("id");
    	id=id.replace("delcomment_", "");
        var data="action=delcomment";
        data+="&feedid=" + id;
        
        
        $.confirm({
		    title: 'Are you sure!',
		    content: 'Confirm you want to delete the comment!',
		    confirm: function(){
		    	$.post("LiveFeed", data).done(function(resp){
		        	location.reload();
		        });
		    }
		});
       
	});
	$("[id^='delpost_']").click(function(e) {
		id = $(this).attr("id");
    	id=id.replace("delpost_", "");
        var data="action=delpost";
        data+="&feedid=" + id;
        
		$.confirm({
		    title: 'Are you sure!',
		    content: 'Confirm you want to delete the post!',
		    confirm: function(){
		    	$.post("LiveFeed", data).done(function(resp){
		        	location.reload();
		        });
		    }
		});
    	
	});
	
	
	$(".refresh").click(function(){
		location.reload();
	});
	
	$(".newPost").click(function(){
		var type = $(this).attr("type");
		if(type == "assessment"){
			$(".aPostClass").show();
		}else{
			$(".aPostClass").hide();
		}
		$("#saveComment").attr("type", type);
		$("#modal").modal("show");
	});
	$("#saveComment").click(function(){
		var type = $(this).attr("type");
		data="action=newpost";
		data+="&type=" + type;
		if(type == "assessment")
			data+="&asmtId=" + $("#aPost").val();
		data+="&comment=" + encodeURIComponent(CKEDITOR.instances.newPostComment.getData());
		$.post("LiveFeed",data).done(function(resp){
			
			location.reload();
		});
	});
	
	/*setTimeout(function(){
		var __hash = document.location.hash;
		var __splits = __hash.split("_");
		if(__splits.length > 1){
			__id=__splits[1];
			__id=__id.replace("id=","");
			$("#cm_"+__id).toggle();
		}
		
		
	},1000);*/
	
	
	
	
});
setInterval(function() {
	
	  checkNew();
	  
	}, 10000);
	
function reload(){
	location.reload();
}	

function checkNew(){
	$.get("LiveFeed?action=check").done(function(resp){
		
		var tc = resp.teamNew;
		var ac = resp.asmtNew;
		var ec = resp.everyNew;
		
		
		if(ac != 0 ){
			$(".updates3").show();
			$("#AsmtCount").html(ac);
			if(ac == 1){
				$("#asmtMsg").html("There is " + ac + " new post.");
			}else{
				$("#asmtMsg").html("There are " + ac + " new posts.");
			}
		}
		if(ec != 0 ){
			$(".updates1").show();
			$("#EveryCount").html(ec);
			if(ac == 1){
				$("#everyMsg").html("There is " + ec + " new post.");
			}else{
				$("#everyMsg").html("There are " + ec + " new posts.");
			}
		}
		if(tc != 0 ){
			$(".updates2").show();
			$("#TeamCount").html(tc);
			if(ac == 1){
				$("#teamMsg").html("There is " + tc + " new post.");
			}else{
				$("#teamMsg").html("There are " + tc + " new posts.");
			}
		}
		
	}).error(function(){console.log("Error")});
}
</script>

</body>
</html>
	