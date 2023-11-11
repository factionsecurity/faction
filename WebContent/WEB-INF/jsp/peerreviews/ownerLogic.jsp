<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<script>
	//console.log("ownerlogic");
	function updateVulnEditors(){
		$.each($("[id^=vuln_]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(id.indexOf("notes") == -1){
    			if (CKEDITOR.instances[$(obj).attr("id")]){
    				console.log("destory");
    				CKEDITOR.instances[$(obj).attr("id")].destroy();
    			}
    			
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config_track.js', toolbar: 'Fuller'});
    		}
    	});
	}
	function updateStepEditors(){
		$.each($("[id^=step]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(id.indexOf("notes") == -1){
    			if (CKEDITOR.instances[$(obj).attr("id")]){
    				console.log("destory");
    				CKEDITOR.instances[$(obj).attr("id")].destroy();
    			}
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config_track.js', toolbar: 'Fuller'});
    		}
    		
    	});
	}
    $(function(){ 	
    	
    	$("input[type=radio]").iCheck({
	  		radioClass: 'iradio_square-blue'
	  	   
	  	  });
    	
    	CKEDITOR.config.contentsCss=CKEDITOR.getUrl("../service/rd_styles.css");
    	//CKEDITOR.config.height = '150mm';
    	CKEDITOR.config.height=450;
        CKEDITOR.replace('appsum', {customConfig : 'ckeditor_config_track.js', toolbar: 'Fuller'});
        
        
    	CKEDITOR.replace("risk", {customConfig : 'ckeditor_config_track.js', toolbar: 'Fuller'});
    	$('a[href="#tab_2"]').click(function(){
    		//firefox hack since cannot track changes on a collapsed ckeditor
    		updateVulnEditors();
    	});
    	$('a[href="#tab_3"]').click(function(){
    		//firefox hack since cannot track changes on a collapsed ckeditor
    		updateStepEditors();
    	});
    	
    	CKEDITOR.replace('risk_notes', {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    	$("#risk_notes").attr("readonly","readonly");
    	CKEDITOR.replace('appsum_notes', {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    	
    	$("#appsum_notes").attr("readonly","readonly")
    	$.each($("[id^=vuln_]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(id.indexOf("notes") != -1){
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    			$(obj).attr("readonly","readonly")
    		}
    		
    	});
    	$.each($("[id^=step]"), function(id, obj){ 
    		var id = $(obj).attr("id");
    		if(id.indexOf("notes") != -1){
    			CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'Min'});
    			$(obj).attr("readonly","readonly")
    		}
    		
    	});
    	
    	var lite = CKEDITOR.config.lite|| {};
        CKEDITOR.config.lite = lite;
        lite.userName="${user.fname} ${user.lname}";
    	
    	$(".save").click(function(){
    		
    		data="action=save";
    		data+="&prid=${prid}";
    		data+="&risk=" + encodeURIComponent(CKEDITOR.instances.risk.getData());
    		data+="&summary=" + encodeURIComponent(CKEDITOR.instances.appsum.getData());
    		data+="&risk_notes=" + encodeURIComponent(CKEDITOR.instances.risk_notes.getData());
    		data+="&sum_notes=" + encodeURIComponent(CKEDITOR.instances.appsum_notes.getData());
    		updateVulnEditors();
    		updateStepEditors();
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
        	});
    		//console.log(data);
    		$.post("TrackChanges",data).done(function(){
    			console.log("done");
    		})
    	});
    	$(".closeit").click(function(){
    		$.confirm({
        	    title: 'Confirm!',
        	    content: 'Unsaved changes will be lost!',
        	    buttons:{
	        	    confirm: function(){
	        	    	 window.history.back();
	        	    },
	        	    cancel: function(){
	        	    }
        	    }
        	        
        	    });
    		
    	});
    	$(".complete").click(function(){
    		data="action=complete";
    		data+="&prid=${prid}";
    		data+="&risk=" + encodeURIComponent(CKEDITOR.instances.risk.getData());
    		data+="&summary=" + encodeURIComponent(CKEDITOR.instances.appsum.getData());
    		data+="&risk_notes=" + encodeURIComponent(CKEDITOR.instances.risk_notes.getData());
    		data+="&sum_notes=" + encodeURIComponent(CKEDITOR.instances.appsum_notes.getData());
    		updateVulnEditors();
    		updateStepEditors();
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
    		
    		$.post("TrackChanges",data).done(function(resp){
    			console.log("done");
    			if(resp.result == "success"){
    				document.location="Assessment?id=app${asmt.id}#tab_3";
    			}else{
    				var errors = resp.errors;
    				var bullets = "<ul>";
    				$.each(errors, function(id,error){
    					bullets+="<li>"+ error+"</li>";
    				});
    				bullets+="</ul>";
    			    $.alert({
    			        title: 'Alert!',
    			        content: bullets,
    			    });
    			}
    		})
    	});
    	
    	setup(2000);
    	$(".nav-tabs").click(function(){
    		console.log("resize");
    		setup(500);
    		
    	});
    	
    	//CKEDITOR.config.height = '150mm';
    	
    	
    });
    function setup(time){
    	setTimeout(function(){ 
    		console.log("loaded");
    		var height = 550;
    		CKEDITOR.instances['appsum'].resize("100%", height, false);
        	CKEDITOR.instances['appsum_notes'].resize("100%", height, false);
        	CKEDITOR.instances['risk'].resize("100%", height, false);
        	CKEDITOR.instances['risk_notes'].resize("100%", height, false);
        	$.each($("[id^=vuln_]"), function(id, obj){ 
        		var id = $(obj).attr("id");
        		
        		CKEDITOR.instances[id].resize("100%", height, false);
        		//console.log(id);
        		
        	});
        	$.each($("[id^=step]"), function(id, obj){ 
        		var id = $(obj).attr("id");
        		
        		CKEDITOR.instances[id].resize("100%", height, false);
        		//console.log(id);
        		
        	});
    	}, time);
    }
    </script>