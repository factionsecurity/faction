
require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
//require('bootstrap/dist/css/bootstrap.css');
import suneditor from 'suneditor';
import {font, fontColor, hiliteColor, link, fontSize, align, image, imageGallery, list, formatBlock, table, blockquote } from 'suneditor/src/plugins';
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
import { marked } from 'marked';

let editorOptions = {
    codeMirror: CodeMirror,
    plugins: [font, fontColor, hiliteColor, link, fontSize, image, align, imageGallery, list, formatBlock, table, blockquote],
    buttonList : [
		['undo', 'redo','fontSize', 'formatBlock','textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks','codeView'],
    ],
    defaultStyle: 'font-family: arial; font-size: 18px',
    height: 500
};
global.editors = {
    description: suneditor.create("description", editorOptions),
    recommendation: suneditor.create("recommendation", editorOptions)
};

    global.editVuln = function editVuln(id){
        
    	$.get('DefaultVulns?vulnId='+id+'&action=getvuln').done(function(data){
            global.editors.description.setContents(marked.parse(b64DecodeUnicode(data.desc)));
            global.editors.recommendation.setContents(marked.parse(b64DecodeUnicode(data.rec)));
		    
		  	$("#title").val($("<div/>").html(data.name).text());
		  	setIntVal( data.impact, "impact");
		  	setIntVal( data.likelyhood, "likelyhood");
		  	setIntVal( data.overall, "overall");
		  	$(data.cf).each(function(a,b){
		  		$("#type"+ b.typeid).val(b.value);
		  	});
		  	$("#catNameSelect").val(data.category).trigger('change');
		  	$("#vulnModal").modal('show');
		  	$("#saveVuln").unbind();
		  	$("#saveVuln").click(function(){
			    let desc = global.editors.description.getContents();
			    let rec = global.editors.recommendation.getContents();
			  	let postData="description=" + encodeURIComponent(desc);
			  	postData+="&recommendation=" + encodeURIComponent(rec);
			  	postData+="&name=" + $("#title").val();
			  	postData+="&impact=" + $("#impact").val()
			  	postData+="&likelyhood=" + $("#likelyhood").val()
			  	postData+="&overall=" + $("#overall").val()
			  	postData+="&category=" + $("#catNameSelect").select2("val");
			  	postData+="&vulnId="+ id;
                let fields = [];
                for(let vulnId of vulnTypes){
                    fields.push(`{"id" : ${vulnId}, "text" : "' + $("#type${vulnId}").val() + '"}`);
                }
    	        postData+='&cf=[' + fields.join(",") + "]";
			  	postData+="&action=savevuln";
			  	postData+="&_token=" + _token;
				$.post("DefaultVulns", postData, function(resp){
					alertRedirect(resp);
					});
	  		});
		  	
  		});
    };

global.deleteVuln = function deleteVuln(id){
    	$.confirm({
    		title: "Are you sure?",
    		content: "This change cannot be recovered. If the vulnerability is assigned to an assessment it cannot be deleted but can be make inactive.",
    		buttons: {
    			"Yes Delete it": function(){
    				let data='vulnId='+id+'&action=delvuln';
    				data+="&_token=" + _token;
    				$.get('DefaultVulns', data).done(function(resp){
    					if(resp.message){
    						if(resp.message == 'Failed CSRF Token'){
    							$.alert({
    								title: "Error",
    								content: resp.message,
    								type: red
    							});
    						}else{
	    						$.confirm({
	    							title: "Error",
	    							content: data.message,
	    							buttons: {
	    								"Make Inactive" : function(){
	    									data="vulnId="+id;
	    									data+="&_token=" + _token;
	    									$.post("DeActivate",data).done(function(resp2){
	    										alertRedirect(resp2);
	    									});
	    								},
	    								cancel:function(){return;}
	    							}
	    						});
    						}
    					}else{
    		        		document.location.reload();
    					}
    		    	});
    			},
    			cancel: function(){return;}
    		}
    	});
    	
    }
    global.editCat = function editCat(el, id){
    	var catName = $($($($(el).parent()).parent()).find("td")[0]).text();
    	console.log(catName);
    	$.confirm({
    		title: "Edit Category",
    		content: "<input style='width:100%' id='updatedCatName' value='" + catName + "'></input>",
    		buttons: {
    			"Update It": function(){
    				let data="catId="+id;
    				data+="&name=" + $("#updatedCatName").val();
    				data+="&_token=" + _token;
    				$.post("editCat", data).done(function(resp){
    					alertRedirect(resp);
    				});
    			},
    		cancel:function(){return; }
    		}
    	});
    };
    global.deleteCat = function deleteCat(id){
    	$.confirm({
    		title: "Are you sure?",
    		content: "This change cannot be recovered. If the category is assigned to an vulnerability it cannot be deleted but can be make inactive.",
    		buttons: {
    			"Yes Delete it": function(){
    				let data='catId='+id+'&action=delCat';
    				data+="&_token=" + _token;
			    	$.post('DefaultVulns',data).done(function(resp){
			    		alertRedirect(resp);
			    	
			    	});
    			},
    			cancel:function(){return;}
    		}
    	});
    	
    };
    global.toggleVuln = function toggleVuln(id,state){
    	let data="vulnId=" + id;
    	data+="&_token=" + _token;
    	if(!state){
	    	$.post("ReActivate", data).done(function(resp){
	    		alertRedirect(resp);
	    	});
    	}else{
	    	$.post("DeActivate", data).done(function(resp){
	    		alertRedirect(resp);
	    	});
    	}
    };
   
    $(function () {
    	$(".select2").select2();
    	
    	$("#overall").on('change', (event) => {
			let sev = event.target.value;
			$("#impact").val(sev).trigger("change");
			$("#likelyhood").val(sev).trigger("change");
			
		})

    	$('#catTable').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": true,
            "columns": [ null, {"width": "10%" } ]         
          });
    	let vulnTable=$('#vulnTable').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": true,
            "columns": [ null, {"width": "10%"},{"width": "5%"}, {"width": "10%" }],
            "columnDefs": [
				{
					"targets" : [4,5],
					"visible" : false,
					"searchable": true
				}
			]
          });
    	vulnTable.search( 'active' ).draw();
        $("#addCat").click(function(){
			name=$("#catname").val();
			let data="action=addcat&name="+name;
			data+="&_token=" + _token;
			$.post("DefaultVulns",data).done(function(resp){
				alertRedirect(resp);
				});
            });
        $("#addVuln").click(function(){
			$("#vulnModal").modal('show');
        });
        $("#saveVuln").click(function(){
			console.log("saving Vuln")
            let desc = global.editors.description.getContents();
            let rec = global.editors.recommendation.getContents();
		  	let data="description=" + encodeURIComponent(desc);
		  	data+="&recommendation=" + encodeURIComponent(rec);
		  	data+="&name=" + $("#title").val();
		  	data+="&impact=" + $("#impact").val();
		  	data+="&likelyhood=" + $("#likelyhood").val()
		  	data+="&overall=" + $("#overall").select2("val")
		  	data+="&category=" + $("#catNameSelect").select2("val");
			console.log(data)
			console.log($("#likelyhood"))
            let fields = [];
            for(let vulnId of vulnTypes){
                fields.push(`{"id" : ${vulnId}, "text" : "' + $("#type${vulnId}").val() + '"}`);
            }
            data+='&cf=[' + fields.join(",") + "]";
		  	data+="&action=addvuln";
		  	data+="&_token=" + _token;
			$.post("DefaultVulns", data, function(resp){
				alertRedirect(resp);
				});
  		});
       
        $("[id^=updateRisk]").click(function(){
        	let id = $(this).attr("id");
        	id = id.replace("updateRisk","");
        	let data="action=updateRisk";
        	data+="&riskId="+id;
        	data+="&riskName="+$("#riskName"+id).val();
        	data+="&_token=" + _token;
        	$.post("DefaultVulns", data).done(function(resp){
        		alertRedirect(resp);
        	});
        });

        
        $("#saveDates").click(function(){
        	let data="action=updateDates";
        	data+="&_token=" + _token;
            for(let i=0;i<10;i++){
            	data+="&duedate["+i+"]=" + $("#due_"+i).val();
            	data+="&warndate["+i+"]=" + $("#warn_"+i).val();
            }
			$.post('DefaultVulns', data).done(function(resp){
				alertRedirect(resp);
				});

        });
       
        $("#importDB").click(function(){
        	$.confirm({
        		title:"Are you sure?",
        		content: "This will create categories and several vulnerabilities in the system. This may be hard to reverse.",
        		buttons : {
        			"Yes I Want This!": function(){
        				$.confirm({
        					title: "Are you really REALLY sure?",
        					content: "Last chance to back out...",
        					buttons:{
        						"Yup I know what i'm doing.":function(){
        							$("#vulnTable").loading({overlay: true, base: 0.3});
        	        	        	$("#catTable").loading({overlay: true, base: 0.3});
        	        	        	let data="action=importVDB";
        	        	        	data+="&_token=" + _token;
        	        	        	$.post("DefaultVulns", data).done(function(resp){
        	        	        		alertRedirect(resp);
        	        	        	});
        						},
        						"Nope": function(){return; }
        					}
        				});
        				
        			},
        			"No Thanks": function(){ return;}
        		}
        	});
        	
        });
        
        $('input[type=radio][name=verOption]').change(function() {
        	let data="verOption=" +  $(this).val();
        	data+="&_token=" + _token;
        	$.post("VerificationSetting", data).done(function(resp){
        		alertMessage(resp, "Setting Updated");
        	});
        });
    });
    
	  function updateIntVal(par, el){
		  let rank = $(par).html();
		  //console.log(par);
		  $("#"+el).val(rank);
		  //console.log(rank);
		  $("#"+el).attr("intVal", getIdFromValue(rank));
	  }
	  function setIntVal(value, el){
    	 $("#"+el).val(value).trigger("change");
		  //$("#"+el).attr("intVal", value);
		  //$("#"+el).val(getValueFromId(""+value));
	  }

   
    $(function(){
    	$("#downloadVulns").click(function(){
    		document.location="GetVulnsCSV";
    	});
    });
function b64DecodeUnicode(str) {
    str=decodeURIComponent(str);
    return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
} 