
require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
//require('bootstrap/dist/css/bootstrap.css');
import suneditor from 'suneditor';
import {font, fontColor, fontSize, align, image, imageGallery, list, formatBlock, table, blockquote } from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
import '../loading/js/jquery-loading';
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs'   ;
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import 'select2';
import '../scripts/jquery.autocomplete.min';

    global.deleteStatus = function deleteStatus(status){
    	let data = "status=" +status;
		data +="&_token=" + _token;
		$.post("deleteStatus",data).done(function(resp){
			alertRedirect(resp);
		});
    };

   	$(function(){
        $("input").each((index,el) => {if(el.type == "checkbox"){$(el).addClass("icheckbox_minimal-blue");}});
        $("#cfType").select2();
        $("#cfFieldType").select2();
   	
   		$('.statuscheck').on('click', function(event){
            $(".statuscheck").each( (index, el)  => { $(el).removeAttr("checked");});
   			console.log($(this).closest('td').attr("status"));
   			let data="status=" + $(this).closest('td').attr("status");
   			data+="&_token="+ _token;
   			$.post("setDefaultStatus",data).done(function(resp){
   				alertMessage(resp, "Status Updated");
   			});
	
   		});
   		
   		$("#addstatus").click(function(){
   			let data = "status=" +$("#asmtStatus").val();
   			data +="&_token=" + _token;
   			$.post("createStatus",data).done(function(resp){
   				alertRedirect(resp);
   			});
   		});
   		
        var editorOptions = {
            codeMirror: CodeMirror,
            plugins: [font, fontColor, fontSize, image, align, imageGallery, list, formatBlock, table, blockquote],
            buttonList : [
            ['undo', 'redo', 'font', 'fontSize', 'formatBlock'],
            ['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
            ['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
            ['link', 'image', 'fullScreen', 'showBlocks', 'codeView', 'preview'],
            
            ],
            defaultStyle: 'font-family: arial; font-size: 18px',
            height: 500
        };
        var emailSignature = suneditor.create("emailSignature", editorOptions);

		$("#type").DataTable(
             {"paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false}
		);
		$("#campaign").DataTable(
			
             {"paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false}
		);
		$("#addType").click(function(){
			$.confirm({
				escapeKey: true,
    			backgroundDismiss: false,
				title: 'Add an Assessment Type',
				content: `
				<div class="col-md-12">
					<div class="row">
						<div class="form-group">
							<label title="The name of the assessment type">Type Name</label><input type="text" placeholder="Assessment Type Name" class="form-control pull-right" id="typeName" value="">
						</div>
					</div>
					<div class="row">
						<br>
						<div class="form-group">
						<label title="The Risk Ranking System to be used for this assessment type">Risk Ranking System <i class="fa-solid fa-question"></i></label>
						<select id="riskType" style="width:100%">
							  <option value="0">Native (default)</option>
							  <option value="1">CVSS 3.1</option>
							  <option value="2">CVSS 4.0</option>
						  </select>
						</div>
					</div>
				</div>
				`,
				onContentReady: function () {
					console.log("content loaded")
					$("#riskType").select2();
				},
				buttons: {
					cancel: () => {},
					save: () => {
						let name = $("#typeName").val();
						let data="action=addType";
						data+="&name=" + name;
						data+="&riskType=" + $("#riskType").val();
						data+="&_token=" + _token;
						$.post("Options",data).done(function(resp){
							alertRedirect(resp);
						});
					}
				}
    		});
			
			
			
			
		});
		
		
		
		$("#addCampaign").click(function(){
				$.confirm({
					title: "Add New Campaign",
					content: 
						`<div class="col-md-12">
							<div class="row">
								<div class="form-group">
									<label title="Enter a Campaign Name">Campaign Name</label><input type="text" placeholder="Campaign Name" class="form-control pull-right" id="campaignName" value="">
								</div>
							</div>
						</div>`,
					buttons: {
						"Add" : function(){
							let name = $("#campaignName").val();
							let data="action=addCampaign";
							data+="&name=" + name;
							data+="&_token=" + _token;
							$.post("Options",data).done(function(resp){
								alertRedirect(resp);
							});
						},
						cancel:function(){return;}
					}
				});
		});
		
		$("#testEmail").click(function(){
			$.confirm({
				title: "Send Test Email",
				theme: "black",
				content : "<div class=row><div class=col-md-3><label for=to >Send TO:</label></div><div class=col-md-9><input name=to style='color:black; width:100%' type=text id=to /></div></div>",
				buttons: {
					confirm : function (){
						
						let ssl=$("#isSSL").is(":checked");
						let tls=$("#isTLS").is(":checked");
						let auth=$("#isAuth").is(":checked");
						let data="action=test";
						data+="&server=" + $("#emailServer").val();
						data+="&type="+ $("#emailProto").val();
						data+="&username="+ $("#emailName").val();
						data+="&fromaddress="+ $("#fromAddress").val();
						data+="&port="+ $("#emailPort").val();
						data+="&password="+ encodeURIComponent($("#emailPass").val());
						data+="&prefix="+ $("#emailPrefix").val();
						data+="&signature="+ encodeURIComponent(emailSignature.getContents());
						data+="&sslischecked="+ssl;
						data+="&tlsischecked="+tls;
						data+="&authischecked="+auth;
						data+="&to="+$("#to").val();
						data+="&_token=" + _token;
						$.post("Options", data).done(function (resp){
							alertRedirect(resp);
						});
					}
				}
			});
			
		});
		$("#saveEmail").click(function(){
			let ssl=$("#isSSL").is(":checked");
			let tls=$("#isTLS").is(":checked");
			let auth=$("#isAuth").is(":checked");
			let data="action=emailSettings";
			data+="&server=" + $("#emailServer").val();
			data+="&type="+ $("#emailProto").val();
			data+="&username="+ $("#emailName").val();
			data+="&fromaddress="+ $("#fromAddress").val();
			data+="&port="+ $("#emailPort").val();
			data+="&password="+ encodeURIComponent($("#emailPass").val());
			data+="&prefix="+ $("#emailPrefix").val();
			data+="&signature="+ encodeURIComponent(emailSignature.getContents());
			data+="&sslischecked="+ssl;
			data+="&tlsischecked="+tls;
			data+="&authischecked="+auth;
			data+="&_token=" + _token;
			$.post("Options",data).done(function(resp){
				alertRedirect(resp);
			});
		});
		
		$("#saveOAuth").click(function(){
			let data="server=" + $("#ssoserver").val();
			data+="&username=" + $("#ssousername").val();
			if($("#isSSO").val() == "on")
				data+="&useSSO=true";
			else
				data+="&useSSO=false";
			data+="&clientid=" + $("#clientid").val();
			data+="&secret=" + $("#secret").val();
			data+="&profile=" + $("#profile").val();
			data+="&tokenURL=" + $("#tokenurl").val();
			data+="&userURL=" + $("#userurl").val();
			data+="&_token=" + _token;
			$.post("UpdateSSO",data).done(function(resp){
				alertMessage(resp,"SSO Config Updated Successfully");
			});
		});
		
		


   });
   	
   	
	global.editType = function editType(el, typeId){
		let typeName = $($($($(el).parent()).parent()).find("td")[0]).text();
		let riskName = $($($($(el).parent()).parent()).find("td")[1]).text();
		let isNative=true;
		let isCvss31=false;
		let isCvss40=false;
		switch(riskName){
			case "CVSS 3.1": isNative=isCvss40=false; isCvss31=true;break;
			case "CVSS 4.0": isNative=isCvss31=false; isCvss40=true;break;
			default: isCvss31=isCvss40=false; isNative=true;
		}
		
		$.confirm({
			title: "Editing Assessment Type",
			content: 
				`<div class="col-md-12">
					<div class="row">
						<div class="form-group">
							<label title="The name of the assessment type">Type Name</label><input type="text" placeholder="Assessment Type Name" class="form-control pull-right" id="typeName" value="${typeName}">
						</div>
					</div>
					<div class="row">
						<br>
						<div class="form-group">
						<label title="The Risk Ranking System to be used for this assessment type">Risk Ranking System <i class="fa-solid fa-question"></i></label>
						<select id="riskType" style="width:100%">
							  <option value="0" ${isNative?'selected':''}>Native (default)</option>
							  <option value="1" ${isCvss31?'selected':''}>CVSS 3.1</option>
							  <option value="2" ${isCvss40?'selected':''}>CVSS 4.0</option>
						  </select>
						</div>
					</div>
				</div>`,
			onContentReady: function () {
				$("#riskType").select2();
			},
			buttons: {
				"Yes Update" : function(){
					
					let data="id=" + typeId;
					data+="&name=" + $("#typeName").val();
					data+="&riskType=" + $("#riskType").val();
					data+="&_token=" + _token;
					$.post("editType",data).done(function(resp){
						alertRedirect(resp);
					});
				},
				cancel:function(){return;}
			}
		});
	};

	global.delType = function delType(el, id){
		
		$.confirm({
			title: "Are you Sure?",
			content: "Deleting this <b>Assessment Type</b> is not reversable.",
			buttons: {
				"Yes Delete" : function(){
					
					let data="action=delType";
					data+="&id=" + id;
					data+="&_token=" + _token;
					$.post("Options",data).done(function(resp){
						alertRedirect(resp);
					});
				},
				cancel:function(){return;}
			}
		});
		

	};
	global.editCampaign = function editCampaign(el, typeId){
		var campName = $($($($(el).parent()).parent()).find("td")[0]).text();
		
		$.confirm({
			title: "Editing Campaign Type",
			content: 
				`<div class="col-md-12">
					<div class="row">
						<div class="form-group">
							<label title="Enter a Campaign Name">Campaign Name</label>
							<input type="text" placeholder="Campaign Name" 
								class="form-control pull-right" id="editCampName" value="${campName}">
						</div>
					</div>
				</div>`,
			buttons: {
				"Yes Update" : function(){
					let data="id=" + typeId;
					data+="&name=" + $("#editCampName").val();
					data+="&_token=" + _token;
					$.post("editCamp",data).done(function(resp){
						alertRedirect(resp);
					});
				},
				cancel:function(){return; }
			}
		});
	};
	
	//$("[id^=delCampaign]").click(
	global.delCampaign = function delCampaign(el, id){
		//var id = $(this).attr("id").replace("delCampaign","");
		$.confirm({
			title: "Are you Sure?",
			content: "Deleting this <b>Campaign</b> is not reversable.",
			buttons: {
				"Yes Delete" : function(){
					
					let data="action=delCampaign";
					data+="&id=" + id;
					data+="&_token=" + _token;
					$.post("Options",data).done(function(resp){
						alertRedirect(resp);
					});
				},
				cancel:function(){return; }
			}

		});
	};
    
    $(function(){
    	$("#addCF").click(function(){
			$.confirm({
				escapeKey: true,
    			backgroundDismiss: false,
				title: 'Add Custom Field',
				content: `
				<div class="col-md-12">
					<div class="row">
						<div class="form-group">
							<label title="This is the name that is shown in UI">Field Display Name</label><input type="text" placeholder="" class="form-control pull-right" id="cfName" value="">
						</div>
					</div>
					<div class="row">
						<div class="form-group">
							<label title="This variable will be populated in reports">Variable Name (No Spaces) <i class="fa-solid fa-question"></i></label><input type="text" placeholder="" class="form-control pull-right" id="cfVar" value="">
						</div>
					</div>
					<div class="row">
						<div class="form-group">
							<label title="The Default Value that will be populated in the UI">Default Value (Optional) <i class="fa-solid fa-question"></i></label><input type="text" placeholder="" class="form-control pull-right" id="cfDefault" value="">
						</div>
					</div>
					<div class="row">
						<br>
						<div class="form-group">
						<label title="Strings will display as input boxes, Boolean will be checkboxes, and Lists will be dropdowns">Data Type <i class="fa-solid fa-question"></i></label>
						<select id="cfFieldType" style="width:100%">
							  <option value="0">String</option>
							  <option value="1">Boolean</option>
							  <option value="2">List</option>
						  </select>
						</div>
					</div>
					<div class="row">
						<div class="form-group">
						  <label title="The location where the field is valid">Applied to <i class="fa-solid fa-question"></i></label>
						  <select id="cfType" style="width:100%">
							  <option value="0">Assessment</option>
							  <option value="1">Vulnerability</option>
						  </select>
						</div>
					</div>
					<div class="row">
						<div class="form-group">
							<label title="Read Only fields cannot be edited in assessments">Read Only <i class="fa-solid fa-question"></i></label><br/>
							<input type="checkbox" id="readonly" class="icheckbox_minimal-blue">
						</div>
					</div>
				</div>
				`,
				onContentReady: function () {
					console.log("content loaded")
					$("#cfType").select2();
					$("#cfFieldType").select2();
				},
				buttons: {
					cancel: () => {},
					save: () => {
						let data="cftype=" + $("#cfType").val();
						data+="&cfname="+$("#cfName").val();
						data+="&readonly="+$("#readonly").is(":checked");
						data+="&cfvar="+$("#cfVar").val();
						data+="&cffieldtype="+$("#cfFieldType").val();
						data+="&cfdefault="+$("#cfDefault").val();
						data+="&_token=" + _token;
						$.post("CreateCF", data).done(function(resp){
							if(resp.message){
    							alertMessage(resp,"Custom Field Updated");
							}else{
								alertRedirect(resp);
							}
						});
					}
				}
    		});
    		
    	});
    	$(".updCF").click(function(){
    		let cfid = $(this).attr("for");
    		let variable = $("#var" + cfid).val();
    		let defaultVal = $("#default" + cfid).val();
    		let text = $("#key"+cfid).val();
    		let data = "cfid=" + cfid;
    		data+="&cfname=" + text;
    		data+="&cfvar=" + variable;
    		data+="&cfdefault=" + defaultVal;
    		data+="&readonly="+$("#ro"+cfid).is(":checked");
    		data+="&_token=" + _token;
    		$.post("UpdateCF",data).done(function(resp){
    			alertMessage(resp,"Custom Field Updated");
    			
    		});
    		
    	});
    	$(".delCF").click(function(){
    		var cfid = $(this).attr("for");
    		$.confirm({
				title: "Are you Sure?",
				content: "Deleting this <b>Custom Field</b> is not reversable.",
				buttons: {
					"Yes Delete" : function(){
						
			    		
			    		let data = "cfid=" + cfid;
			    		data+="&_token=" + _token;
			    		$.post("DeleteCF",data).done(function(resp){
			    			alertRedirect(resp);
			    		});
					},
				cancel: function(){return;}
			}
    		});

    	});
    	
    	$("#prEnabled").on("change",function(){
    		
    		let data="prChecked=" + $("#prEnabled").is(":checked");
    		data+="&_token=" + _token;
    		$.post("updatePrConfig", data).done(function(resp){
				if(resp.message){
					$("#prEnabled").prop("checked", false)
				}
    			alertMessage(resp,"PR Config Updated");
				
    		});
    	});
    	$("#prSelfReview").on("change",function(){
    		
    		let data="selfPeerReview=" + $("#prSelfReview").is(":checked");
    		data+="&_token=" + _token;
    		$.post("updatePrConfig", data).done(function(resp){
				if(resp.message){
					$("#selfPeerReview").prop("checked", false)
				}
    			alertMessage(resp,"PR Config Updated");
				
    		});
    	});
    	$("#feedEnabled").on("change",function(){
    	
    		let data="feedChecked=" + $("#feedEnabled").is(":checked");
    	    data+="&_token=" + _token;
    		$.post("updateFeedConfig", data).done(function(resp){
    			alertMessage(resp,"Feed Setting Updated");
    			
    		});
    	});
    	$("#randEnabled").on("change",function(){
        	
    		let data="randChecked=" + $("#randEnabled").is(":checked");
    		data+="&_token=" + _token;
    		$.post("updateRandConfig", data).done(function(resp){
    			alertMessage(resp,"Application ID Behaviour Updated");
    			
    		});
    	});
    	
    	$("#updateTitles").click(function(){
    		let data="title[0]=" + $("#title1").val();
    		data+="&title[1]=" + $("#title2").val();
    		data+="&_token=" + _token;
    		$.post("updateTitles", data).done(function(resp){
    			alertMessage(resp,"Titles Updated");
    			
    		});
    		
    	});
   	});