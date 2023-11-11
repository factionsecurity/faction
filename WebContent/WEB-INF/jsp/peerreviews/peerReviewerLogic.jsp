<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/latest/css/font-awesome.min.css">
<script src="https://cdn.jsdelivr.net/simplemde/latest/simplemde.min.js"></script>
<script>
	var cksfirst=true;
	var ckvfirst=true;
	//console.log("prlogic");
	function updateVulnEditors(){
		$.each($("[id^=vuln_]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		console.log(id);
    		if (CKEDITOR.instances[$(obj).attr("id")]){
				console.log("destory");
				CKEDITOR.instances[$(obj).attr("id")].destroy();
			}
    		if(id.indexOf("notes") == -1){
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config_track.js', toolbar: 'Full'});
    		}else{
    			console.log("notes");
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    		}
    	});
	}
	function updateStepEditors(){
		$.each($("[id^=step]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		console.log(id);
    		if (CKEDITOR.instances[$(obj).attr("id")]){
				console.log("destory");
				CKEDITOR.instances[$(obj).attr("id")].destroy();
			}
    		if(id.indexOf("notes") == -1){
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config_track.js', toolbar: 'Full'});
    		}else{
    			console.log("notes");
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    		}
    		
    	});
	}
    $(function(){ 	
    	
    	$("input[type=radio]").iCheck({
	  		radioClass: 'iradio_square-blue'
	  	   
	  	  });
    	
    	CKEDITOR.config.contentsCss=CKEDITOR.getUrl("../service/rd_styles.css");
    CKEDITOR.config.height = '150mm';
       CKEDITOR.replace('appsum', {customConfig : 'ckeditor_config_track.js', toolbar: 'Full'});
      //var simplemde = new SimpleMDE({ element: document.getElementById("appsum") });
        
    	CKEDITOR.replace("risk", {customConfig : 'ckeditor_config_track.js', toolbar: 'Full'});
    	$.each($("[id^=vuln_]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(!id.includes("notes"))
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config_track.js', toolbar: 'Full'});
    	});
    	$.each($("[id^=step]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(!id.includes("notes"))
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config_track.js', toolbar: 'Full'});
    		
    	});
    	
    	$('a[href="#tab_2"]').click(function(){
    		//firefox hack since cannot track changes on a collapsed ckeditor
    		if(ckvfirst){
	    		updateVulnEditors();
	    		ckvfirst=false;
	    		}
    	});
    	$('a[href="#tab_3"]').click(function(){
    		//firefox hack since cannot track changes on a collapsed ckeditor
    		if(cksfirst){
	    		updateStepEditors();
	    		cksfirst=false;
    		}
    	});
    	
    	CKEDITOR.replace('risk_notes', {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    	CKEDITOR.replace('appsum_notes', {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    	$.each($("[id^=vuln_]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(id.indexOf("notes") != -1)
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    		
    	});
    	$.each($("[id^=step]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(id.indexOf("notes") != -1)
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    		
    	});
    	
    	var lite = CKEDITOR.config.lite|| {};
        CKEDITOR.config.lite = lite;
        lite.userName="${user.fname} ${user.lname}";
        $(".closeit").click(function(){
        	$.confirm({
        	    title: 'Confirm!',
        	    content: 'Unsaved changes will be lost!',
        	    confirm: function(){
        	    	document.location="PeerReview";
        	    },
        	    cancel: function(){
        	        
        	    }});
    		
    	});
    	
    	$(".save, .complete").click(function(){
    		data="action=save";
    		if($(this).hasClass("complete"))
    			data="action=completePR";
    		data+="&prid=${prid}";
    		data+="&risk=" + encodeURIComponent(CKEDITOR.instances.risk.getData());
    		data+="&summary=" + encodeURIComponent(CKEDITOR.instances.appsum.getData());
    		data+="&risk_notes=" + encodeURIComponent(CKEDITOR.instances.risk_notes.getData());
    		data+="&sum_notes=" + encodeURIComponent(CKEDITOR.instances.appsum_notes.getData());
    		if(cksfirst){
    			updateStepEditors();
    		}
    		if(ckvfirst){
    			updateVulnEditors();
    		}
    		var index =0;
    		$.each($("[id^=vuln_]"), function(id, obj){ 
        		var id = $(obj).attr("id");
        		data+="&" + id + "="+encodeURIComponent(CKEDITOR.instances[id].getData());
        	});
    		$.each($("[id^=step]"), function(id, obj){ 
        		var id = $(obj).attr("id");
        		var step = CKEDITOR.instances[id].getData();
        		//replaces br inside pre that gets removed from ckeditor
        		var div = $("<div/>").html(step);
      		  	console.log(div.html());
      		  	div.find("pre").each(function(i,el){
      			  var newHTML = $(el).html().replace(/\n/g,"<br/>");
      			  $(el).html(newHTML);
      		  	});
        		data+="&" + id + "="+encodeURIComponent(div.html());
        		//data+="&" + id + "="+encodeURIComponent(CKEDITOR.instances[id].getData());
        	});
    		console.log(data);
    		$.post("TrackChanges",data).done(function(resp){
    			
    			if(resp.result == "success"){
    				$.alert({
    					title: "Saved",
    					content: "Peer Review Has been Saved."
    				});
    				console.log("saved");
    			}else if(resp.result == "complete"){
    				console.log("complete");
    				document.location="PeerReview";
    			}
    		})
    	});
    	
    	
    });
    </script>
