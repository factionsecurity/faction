import 'jquery';
import 'jquery-ui';
import 'bootstrap';
import 'jquery-confirm';
import 'datatables.net';
import 'jquery-fileinput';
import 'datatables.net-bs'   ;
require('../scripts/fileupload/css/fileinput.css');
import '../scripts/fileupload/js/fileinput.min';
require('./checklist.css');
  
global.addQuestion = function addQuestion(el){
	  let data = encodeURIComponent($("#newQuest").val());
	  data="question="+ data;
	  data+="&checklist=" + $("#list").val();
	  data+="&_token="+global._token;
	  $.post("AddChecklistQuestion", data).done(function(resp){
		  global._token = resp.token;
		  if(typeof resp.message == 'undefined'){
			  let id = resp.id;
			  $("#questionTable").DataTable().row.add(
					  [ id, 
						  "<textarea class='form-control' style='min-width: 100%' id='q" + id +"'>"+$($("<div />").html($("#newQuest").val())).text()+"</textarea>", 
						  "<button class='btn btn-primary' onclick='saveQuestion("+ id + ")'><span class='fa fa-save'></span></button> <button class='btn btn-danger' onclick='deleteQuestion(this," + $("#list").val() +"," + id +")'><span class='fa fa-trash'></span></button>"]
			).draw();
			$("#newQuest").val("");
		  }else{
			  $.alert({
				  type:"red",
				  content: resp.message,
				  title:"Error",
			  });
		  }
	  });
  }

  
  global.deleteQuestion = function deleteQuestion(el, cid,id){
	  $.confirm({
		  title: "Are your sure?",
		   content: "Do you want to <b>DELETE</b> the question with: <br><pre width='100%'>" + $("<div />").text($("#q"+id).val()).html()+"</pre>",
		   buttons:{
			   yes:function(){
				   let data="checklist="+ cid;
				   data+="&listitem="+id;
				   data+="&_token=" + global._token;
				   $.post("RemoveChecklistQuestion",data).done(function(resp){
					   _token = resp.token;
					   if(resp.message){
						   $.alert(resp.message);
					   }else{
						   let row = $(el).parents("tr");
						   $("#questionTable").DataTable().row(row).remove().draw();
					   }
					   
				   });
			   },
               cancel: () => {return;}
		   }
	  });
  };

  global.saveQuestion = function saveQuestion(id){
	  $.confirm({
		  title: "Are your sure?",
		   content: "Do you want to update the question with: <br><pre width='100%'>" + $("<div />").text($("#q"+id).val()).html()+"</pre>",
		   buttons:{
			   yes:function(){
				   let data="question="+ encodeURIComponent($("#q"+id).val());
				   data+="&listitem="+id;
				   data+="&_token="+_token;
				   $.post("UpdateChecklistQuestion",data).done(function(resp){
					   alertMessage(resp,"Question Saved");
					   
				   });
			   }, 
               cancel: () =>{return;}
		   }
	  });
	  
  }

  $(function(){
	  $(".fa-question-circle").click(function(){
		  $.confirm({
			  title: "Help",
			  content: ("You can upload a checklist in the following format:" +
		  				"<br><pre>Question Id, Question<br>" +
		  				"100, Your Question Here<br>" +
		  				"101, Your Next Question Here</pre>" +
		  				"When Creating a new checklist item set the ID to 0 and Faction will create a unique "+
		  				"ID for the question. When downloading an existing Checklist Faction will match the IDs " +
		  				"and overwrite the question. <br>" +
		  				"<pre>Question Id, Question<br>" +
		  				"100, Update this Question in the DB<br>" +
		  				"0, New Question to add to the DB</pre><br>"
		  				)
		  });
		  
	  });
	  $("#questionTable").DataTable({
		  paging: false,
		  columnDefs:[
			  {"targets":0, "width":'10%'},
			  {
			      "targets": 2,
			      "searchable": false,
			      "orderable":false,
			      "width":'10%'
			    }
		  ]

	  });
	  $("#delete").click(function(){
		  $.confirm({
			  title:"Are you sure?",
			  type:"red",
			  content: "Are you sure you want to delete this checklist and all questions.",
			  buttons:{
				  "Yes, Delete it" : function(){
					  let data="checklist="+ $("#list").val();
					  data+="&_token=" + _token;
					  $.post("DeleteChecklist", data).done(function(resp){
						 alertRedirect(resp);
					  });
				  },
                  cancel: () =>{return;}
			  }
		  });
	  });
	  
	  $("#edit").click(function(){
		  $.confirm({
			  title: "Update List Name:",
			  content:"<input style='width:100%' id='listname' class='checklistName' value='" + $("<div />").text($("#list option:selected").text()).html() + "'/>",
			  buttons:{
				  "Update It":function(){
					  let data="checklist="+ $("#list").val();
					  data+="&name="+ encodeURIComponent($("#listname").val());
					  data+="&_token="+_token;
					  $.post("UpdateChecklist",data).done(function(resp){
						  alertMessage(resp,"Updated Sucessfully");
						  
					  });
					  
				  },
                  cancel: () =>{return;}
			  }
		  });
		  
	  });
	  
	  $("#list").on('change',function(){
		  
		  
		  $("#checklist_file").attr("checklistid",$(this).val());
		  $("#questionTable").DataTable().destroy();
		  $("#questionTable").DataTable({
			  "ajax": "GetCheckList?checklist="+$(this).val(),
			  paging: false,
			  columnDefs:[
				  {"targets":0, "width":'10%'},
				  {
				      "targets": 2,
				      "searchable": false,
				      "orderable":false,
				      "width":'10%'
				    }
			  ]
		  });
		  
		  $.post("GetTypes", "checklist=" + $(this).val() ).done(function(resp){
			  $("[id^=t]").prop('checked',false);
			  resp.data.forEach(function(id) {
				  $("#t"+id).prop('checked', true);
				});
			  //$("#t${id }").prop('checked', true);
		  });
		  
		  //$("#checklist_file").fileinput().destroy();
		 

	  });
	  
	  $("#checklist_file").fileinput({
	 		 overwriteInitial: false,
	 		 uploadUrl: "UploadChecklist",
	 		 uploadAsync: true,
	 		 minFileCount: 0,
	 		 maxFileCount: 1,
			 allowedFileExtensions : ['csv'],
			 uploadExtraData: function() { 
		            return {checklist: $("#list").val()};
		        },
		  });
	  
	  $("#export").click(function(){
		  $("#download").attr('src',"ExportChecklist?checklist=" +  $("#list").val());
		  
	  });
	  
	  $(".types").click(function(){
		  let data="checklist=" + $("#list").val();
		  data+="&type=" + $(this).attr("tid");
		  data+="&_token="+_token;
		  $.post("ToggleType",data).done(function(resp){
			  alertMessage(resp,"Updated Successfully");
			  
		  });
	  });
	  
	  
	  $("#add").click(function(){
		  $.confirm({
			 title: "Enter a Checklist Name",
			 content: "<input id='listname' style='width:100%' class='checklistName'></input>",
			 buttons:{
				 "Yes Add It":function(){
					 let data="name=" + $("#listname").val();
					 data+="&_token="+_token;
					 $.post("CreateChecklist", data).done(function(resp){
						alertRedirect(resp);
					 });
				 },
				 "cancel": function (){}
			 }
			  
		  });
		  
		  
	  });
	  
  });
  $('.add-question').on('click', (event) => {
	global.addQuestion(this);
  });