
require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
//require('bootstrap/dist/css/bootstrap.css');
import suneditor from 'suneditor';
import plugins from 'suneditor/src/plugins';
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
import CVSS from "@turingpointde/cvss.js";

let fromMarkdown = {
	name: 'fromMarkdown',
	display: 'command',
	title: 'Convert Markdown',
	buttonClass: '',
	innerHTML: '<i class="fa-brands fa-markdown" style="color:lightgray"></i>',
	add: function(core, targetElement) {
		core.context.fromMarkdown = {
			targetButton: targetElement,
			preElement: null
		}
	},
	active: function(element) {
		if (element) {
			this.util.addClass(this.context.fromMarkdown.targetButton.firstChild, 'mdEnabled');
			this.context.fromMarkdown.preElement = element;
			return true;
		} else {
			this.util.removeClass(this.context.fromMarkdown.targetButton.firstChild, 'mdEnabled');
			this.context.fromMarkdown.preElement = null;
		}
		return false;
	},
	action: function() {
		let selected = this.getSelectedElements();
		const md = selected.reduce((acc, item) => acc + item.innerText + "\n", "");
		const html = marked.parse(md);
		const div = document.createElement("div");
		div.innerHTML = html;
		const parent = selected[0].parentNode;
		parent.insertBefore(div, selected[0]);
		for (let i = 0; i < selected.length; i++) {
			selected[i].remove();
		}
		this.history.push(true)
		}
};

plugins['fromMarkdown'] = fromMarkdown;
let editorOptions = {
    codeMirror: CodeMirror,
    plugins: plugins,
    buttonList : [
		['undo', 'redo','fontSize', 'formatBlock','textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks','fromMarkdown'],
    ],
    defaultStyle: 'font-family: arial; font-size: 18px',
    minHeight: 500,
    height: "auto"
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
		  	$("#cvss31Score").val(data.cvss31Score);
		  	$("#cvss40Score").val(data.cvss40Score);
		  	$("#cvss31String").val(data.cvss31String);
		  	$("#cvss40String").val(data.cvss40String);
		  	$(data.cf).each(function(a,b){
		  		$("#type"+ b.typeid).val(b.value);
		  	});
		  	$("#catNameSelect").val(data.category).trigger('change');
            $("#vulnModal").modal({	show: true,
            						keyboard: false,
            						backdrop: 'static'});
            setUpCVSSModal();
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
			  	postData+="&cvss31Score=" + $("#cvss31Score").val()
			  	postData+="&cvss40Score=" + $("#cvss40Score").val()
			  	postData+="&cvss31String=" + $("#cvss31String").val()
			  	postData+="&cvss40String=" + $("#cvss40String").val()
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
            $("#vulnModal").modal({	show: true,
            						keyboard: false,
            						backdrop: 'static'});
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
		  	data+="&cvss31String=" + $("#cvss31String").val();
		  	data+="&cvss40String=" + $("#cvss40String").val();
		  	data+="&cvss31Score=" + $("#cvss31Score").val();
		  	data+="&cvss40Score=" + $("#cvss40Score").val();
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



function setUpCVSSModal(){
	
		$("#cvss31Calc #cvss40Calc").on("click", function(){
			const modalId = $(this).id;
			let cvssURL = "url:CVSS"
			let title = "CVSS 3.1"
			if(modalId.indexOf("40Modal") != -1){
				cvssURL = "url:CVSS40"
				title = "CVSS 4.0"
			}
			$.confirm({
				title: title,
				content: cvssURL,
				columnClass: 'col-md-12',
				onContentReady: () => {
					let vectorString = $("#cvssString").val();
					if(vectorString.trim() != ""){
						let vector = this.createCVSSObject(vectorString);
						let severity = vector.getRating();
						let score = vector.getScore();
						$("#modalScore").addClass(severity);
						$("#modalScore").html(score);
						$("#modalSeverity").addClass(severity);
						$("#modalSeverity").html(severity);
						let vObj = vector.getVectorObject();
						setTimeout( () => {
							$(`#av_${vObj['AV'].toLowerCase()}`).click()
							$(`#ac_${vObj['AC'].toLowerCase()}`).click()
							$(`#pr_${vObj['PR'].toLowerCase()}`).click()
							$(`#ui_${vObj['UI'].toLowerCase()}`).click()
							
							$(`#ei_${vObj['E'].toLowerCase()}`).click()
							
							$(`#cr_${vObj['CR'].toLowerCase()}`).click()
							$(`#ir_${vObj['IR'].toLowerCase()}`).click()
							$(`#ar_${vObj['AR'].toLowerCase()}`).click()
							
							$(`#mav_${vObj['MAV'].toLowerCase()}`).click()
							$(`#mac_${vObj['MAC'].toLowerCase()}`).click()
							$(`#mpr_${vObj['MPR'].toLowerCase()}`).click()
							$(`#mui_${vObj['MUI'].toLowerCase()}`).click()
							
							
							if(!is40){
								$(`#s_${vObj['S'].toLowerCase()}`).click()
								$(`#c_${vObj['C'].toLowerCase()}`).click()
								$(`#i_${vObj['I'].toLowerCase()}`).click()
								$(`#a_${vObj['A'].toLowerCase()}`).click()
								
								
								$(`#e_${vObj['E'].toLowerCase()}`).click()
								$(`#rl_${vObj['RL'].toLowerCase()}`).click()
								$(`#rc_${vObj['RC'].toLowerCase()}`).click()
								
								$(`#ms_${vObj['MS'].toLowerCase()}`).click()
								$(`#mc_${vObj['MC'].toLowerCase()}`).click()
								$(`#mi_${vObj['MI'].toLowerCase()}`).click()
								$(`#ma_${vObj['MA'].toLowerCase()}`).click()
							}else{
								$(`#at_${vObj['AT'].toLowerCase()}`).click()
								$(`#vc_${vObj['VC'].toLowerCase()}`).click()
								$(`#vi_${vObj['VI'].toLowerCase()}`).click()
								$(`#va_${vObj['VA'].toLowerCase()}`).click()
								$(`#sc_${vObj['SC'].toLowerCase()}`).click()
								$(`#si_${vObj['SI'].toLowerCase()}`).click()
								$(`#sa_${vObj['SA'].toLowerCase()}`).click()
								
								
								
								$(`#mvc_${vObj['MVC'].toLowerCase()}`).click()
								$(`#mvi_${vObj['MVI'].toLowerCase()}`).click()
								$(`#mva_${vObj['MVA'].toLowerCase()}`).click()
								
								
								$(`#msc_${vObj['MSC'].toLowerCase()}`).click()
								$(`#msi_${vObj['MSI'].toLowerCase()}`).click()
								$(`#msa_${vObj['MSA'].toLowerCase()}`).click()
								
								
								$(`#mat_${vObj['MAT'].toLowerCase()}`).click()
								
								
								
							}
							
						},100);
					}

					$('.vector').on("click", function(event) {
						let el = this;
						$(el).parent().children().each((_index, e) => {
							$(e).removeClass("activeVector");
						});
						$(el).addClass("activeVector");
						setTimeout(() => {
							let av = $("input[name='attackVector']:checked").val()
							let ac = $("input[name='attackComplexity']:checked").val()
							let pr = $("input[name='privileges']:checked").val()
							let ui = $("input[name='userInteraction']:checked").val()
							
							let ei = $("input[name='ei']:checked").val() || "X"
							let cr = $("input[name='cr']:checked").val() || "X"
							let ir = $("input[name='ir']:checked").val() || "X"
							let ar = $("input[name='ar']:checked").val() || "X"
							let mav = $("input[name='mav']:checked").val() || "X"
							let mac = $("input[name='mac']:checked").val() || "X"
							let mpr = $("input[name='mpr']:checked").val() || "X"
							let mui = $("input[name='mui']:checked").val() || "X"
							
							let commonVector ={
									AV: av,
									AC: ac,
									PR: pr,
									UI: ui, 
									EI: ei, 
									CR: cr, 
									IR: ir, 
									AR: ar, 
									MAV: mav, 
									MAC: mac, 
									MPR: mpr, 
									MUI: mui 
							}
							let cvssVector = {}
							let score = 0.0;
							let severity = "None";
							let cvssString="";
									
							
							if(!is40){
								let c = $("input[name='confidentiality']:checked").val()
								let i = $("input[name='integrity']:checked").val()
								let a = $("input[name='availability']:checked").val()
								let s = $("input[name='scope']:checked").val()
								
								let e = $("input[name='e']:checked").val() || "X"
								let rl = $("input[name='rl']:checked").val() || "X"
								let rc = $("input[name='rc']:checked").val() || "X"
								let ms = $("input[name='ms']:checked").val() || "X"
								let mc = $("input[name='mc']:checked").val() || "X"
								let mi = $("input[name='mi']:checked").val() || "X"
								let ma = $("input[name='ma']:checked").val() || "X"
								let cvss31Vector = {
									CVSS: "3.1", 
									C: c, 
									I: i, 
									A: a, 
									S: s, 
									E: e, 
									RL: rl, 
									RC: rc, 
									MS: ms, 
									MC: mc, 
									MI: mi, 
									MA: ma	
								}
								cvssVector = {
									...cvss31Vector, 
									...commonVector
								}
								
								Object.keys(cvssVector).forEach( (a, _i) =>{
									if(cvssVector[a] == "X"){
										delete cvssVector[a];
									}
								});
									
								let vector = CVSS(cvssVector);
								severity = vector.getRating();
								score = vector.getScore();
								if(vector.getTemporalScore() >0 && vector.getEnvironmentalScore() == 0){
									score = vector.getTemporalScore();
									severity = vector.getTemporalRating();
								}
								else if(vector.getEnvironmentalScore() > 0 ){
									score = vector.getEnvironmentalScore() 
									severity = vector.getEnvironmentalRating();
								}
								$("#modalCVSSString").val(vector.getCleanVectorString());
								
							}else{
								let at = $("input[name='at']:checked").val() || "X"
								let vc = $("input[name='vc']:checked").val() || "X"
								let vi = $("input[name='vi']:checked").val() || "X"
								let va = $("input[name='va']:checked").val() || "X"
								let sc = $("input[name='sc']:checked").val() || "X"
								let si = $("input[name='si']:checked").val() || "X"
								let sa = $("input[name='sa']:checked").val() || "X"
								
								
								let mvc = $("input[name='mvc']:checked").val() || "X"
								let mvi = $("input[name='mvi']:checked").val() || "X"
								let mva = $("input[name='mva']:checked").val() || "X"
								
								
								let msc = $("input[name='msc']:checked").val() || "X"
								let msi = $("input[name='msi']:checked").val() || "X"
								let msa = $("input[name='msa']:checked").val() || "X"
								
								let mat = $("input[name='mat']:checked").val() || "X"
								
							}
							
							["Critical", "High", "Medium", "Low", "None"].forEach((a, b) => {
								$("#modalScore").removeClass(a);
								$("#modalSeverity").removeClass(a);
							});
							$("#modalScore").addClass(severity);
							$("#modalScore").html(score);
							$("#modalSeverity").addClass(severity);
							$("#modalSeverity").html(severity);
						}, 200);

					});
				},
				buttons: {
					save: () =>{
						let cvssString = $("#modalCVSSString").val();
						$("#cvssString").val(cvssString).trigger("change")

					},
					cancel: () => { }
				}
			})

		});
		
	}
	setUpCVSSModal();