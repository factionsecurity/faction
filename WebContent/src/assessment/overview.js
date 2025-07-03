require('../scripts/fileupload/css/fileinput.css');
require('select2/dist/css/select2.min.css')
require('./overview.css');
require('../loading/css/jquery-loading.css');
import Editor from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import '../loading/js/jquery-loading';
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import 'select2';
import Chart from 'chart.js/auto';
import {FactionEditor} from '../utils/editor';



global._token = $("#_token")[0].value;
let assessmentId = $("#assessmentId")[0].value
console.log(assessmentId);
let editors = new FactionEditor(assessmentId);
let initialHTML={}



function alertMessage(resp, success) {
	if (typeof resp.message == "undefined")
		$.alert(
			{
				title: "SUCCESS!",
				type: "green",
				content: success,
				columnClass: 'small',
				autoClose: 'ok|1000',
				buttons: {
					ok: function() { }
				}
			}
		);
	else
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small',
				autoClose: 'ok|3000',
				buttons: {
					ok: function() { }
				}
			}
		);

	global._token = resp.token;
}

function showLoading(com) {
	$(com).loading({ overlay: true, base: 0.3 });
}
function clearLoading(com) {
	if ($(com).hasClass('js-loading'))
		$(com).loading({ destroy: true });
}

function alertRedirect(resp) {
	if (typeof resp.message == "undefined")
		window.location = window.location
	else
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small'
			}
		);

	global._token = resp.token;

}
function getData(resp) {
	global._token = resp.token;
	if (typeof resp.message == "undefined")
		return resp.data;
	else {
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small'
			}
		);
		return "error";
	}
}
function saveAllEditors(showLoadingScreen = false) {

	if (showLoadingScreen) {
		showLoading(".content");
	}
	let risk = editors.getEditorText('risk');
	let sum = editors.getEditorText('summary');
	let data = "riskAnalysis=" + encodeURIComponent(risk);
	data += "&summary=" + encodeURIComponent(sum);
	data += "&id=app" + $("#appid")[0].value
	data += "&update=true";
	data += "&_token=" + global._token;
	$.post("Assessment.action", data).done(function(resp) {
		$(".edited").each((a, b) => {
			b.innerHTML = ""
		})
		if (showLoadingScreen) {
			clearLoading(".content");
		}
		if (resp.result != "success") {
			$.alert(resp.message);
		}
		global._token = resp.token;
		$.get("summary/clear/lock").done();
	});

}

function saveEditor(type) {

	let edits = editors.getEditorText(type);
	let name = "";
	let data = "";
	if (type == "risk") {
		data += "riskAnalysis=" + encodeURIComponent(edits);
	}
	else if (type == "summary") {
		data += "summary=" + encodeURIComponent(edits);
	}
	data += "&id=app" + $("#appid")[0].value
	data += "&update=true";
	data += "&_token=" + global._token;
	$.post("Assessment.action", data).done(function(resp) {
		document.getElementById(`${type}_header`).innerHTML = ""
		if (resp.result != "success") {
			$.alert(resp.message);
		}
		global._token = resp.token;
		clearTimeout(clearLockTimeout[type]);
		clearLockTimeout[type] = setTimeout(() => {
			$.get(`summary/clear/lock?action=${type}`).done();
		}, 5000);
	});

}
let editorTimeout = {};
let clearLockTimeout = {};
function queueSave(type) {
	$.get(`summary/set/lock?action=${type}`).done((resp) => {
		if (resp.result == "success") {
			document.getElementById(`${type}_header`).innerHTML = "*"
			clearTimeout(editorTimeout[type]);
			clearTimeout(clearLockTimeout[type]);
			editorTimeout[type] = setTimeout(() => {
				saveEditor(type);
			}, 2000);
		}
	});

}

$(function() {

	global._token = $("#_token")[0].value;
	editors.createEditor("summary",true, () => {
		if (document.getElementById(`summary_header`).innerHTML == "") {
			queueSave("summary");
		}
	});
	editors.createEditor("risk", true, () => {
		if (document.getElementById(`risk_header`).innerHTML == "") {
			queueSave("risk");
		}
	});
	let initialNotes = editors.entityDecode($("#engagementnotes").html());
	editors.createReadOnly("engagementnotes", initialNotes);
	let errorMessageShown=false;
	setInterval(() => {
		$.get("summary/check/locks").done((resp) => {
			if(resp.result && resp.result == "error"){
				if(!errorMessageShown){
					errorMessageShown=true
					$.confirm({
						title: resp.message,
						content: 'Do you want to log in?',
						buttons: {
							login: ()=>{
								errorMessageShown=false;
								window.open("../", '_blank').focus();
							},
							cancel: ()=>{errorMessageShown=false;}
						}
					});
				}
				return;
			}
			if(resp.token){
				global._token = resp.token;	
			}
			["risk", "summary"].forEach(function(type) {
				if (resp[type] && resp[type].isLock) {
					
					$("#" + type).addClass("disabled")
					
					document.getElementById(`${type}_header`).innerHTML = `<i class="lockUser">Editing by ${resp[type].lockBy} ${resp[type].lockAt}</i>`
					editors.setEditorContents(type, resp[type].updatedText, true);
				} else {
					if (document.getElementById(`${type}_header`).innerHTML.indexOf("*") == -1) {
						document.getElementById(`${type}_header`).innerHTML = "";
					}
					$("#" + type).removeClass("disabled")
				}
			});
		}
		).catch( () =>{
			if(!errorMessageShown){
				errorMessageShown=true
				$.confirm({
					title: "Offline",
					content: 'You appear offline. Would you like to login?',
					buttons: {
						login: ()=>{
							errorMessageShown=false;
							 window.open("../", '_blank').focus();
						},
						cancel: ()=>{errorMessageShown=false;}
					}
				});
			}
		})

	}, 1000);

	$("#addList").click(function() {

		var id = $("#selectList").val();
		var data = "id=" + id;
		data += "&_token=" + global._token;
		$.post("AddCheckListToAssessment", data).done(function(resp) {
			alertRedirect(resp);

		});

	});

	function getPreviewConfig() {
		return Array.from($("[name=fileIds]")).map(el => {
			return {
				"caption": $(el).attr("fileName"),
				"width": "100px",
				"height": "100px",
				"key": $(el).attr("value"),
				"filename": $(el).attr("fileName"),
				"url": "DeleteEngFile?name=" + $(el).attr("fileName") + "&apid=" + $(el).attr("entityId") + "&delid=" + $(el).attr("uuid"),
				"filetype": $(el).attr("contentType"),
				"type": $(el).attr("fileExtType")
			};
		});
	}

	function getFileIds() {
		return Array.from($("[name=fileIds]")).map(el => { return "GetEngFile?name=" + $(el).attr("value"); });
	}

	$("#files").fileinput({
		overwriteInitial: false,
		uploadUrl: "UploadFile?apid=" + $("#appid")[0].value.replace("app", ""),

		uploadAsync: true,
		minFileCount: 0,
		maxFileCount: 5,
		allowedFileExtensions: ['msg', 'csv', 'jpg', 'gif', 'png', 'pdf', 'doc', 'xls', 'xlsx', 'docx', 'txt', 'bmp', 'jpeg', 'xml', 'zip', 'rar', 'tar', 'gzip', 'gz'],
		previewFileExtSettings: { // configure the logic for determining icon file extensions
			'doc': function(ext) {
				return ext.match(/(doc|docx)$/i);
			},
			'xls': function(ext) {
				return ext.match(/(xls|xlsx)$/i);
			},
			'ppt': function(ext) {
				return ext.match(/(ppt|pptx)$/i);
			},
			'zip': function(ext) {
				return ext.match(/(zip|rar|tar|gzip|gz)$/i);
			},
			'pdf': function(ext) {
				return ext.match(/(pdf)$/i);
			},
			'xml': function(ext) {
				return ext.match(/(xml)$/i);
			},
			'img': function(ext) {
				return ext.match(/(png|jpg|svg|jpeg|gif)$/i);
			},
			'txt': function(ext) {
				return ext.match(/(msg|csv|txt)$/i);
			}
		},
		preferIconicPreview: true,
		previewFileIconSettings: {
			'doc': '<i class="fa fa-file-word text-primary"></i>',
			'xls': '<i class="fa fa-file-excel text-success"></i>',
			'ppt': '<i class="fa fa-file-powerpoint text-warning"></i>',
			'pdf': '<i class="fa fa-file-pdf text-danger"></i>',
			'zip': '<i class="fa fa-file-archive text-info"></i>',
			'xml': '<i class="fa fa-file-code text-info"></i>',
			'img': '<i class="fa fa-file-image text-success"></i>',
			'txt': '<i class="fa fa-file-text text-info"></i>',
		},
		initialPreviewDownloadUrl: '../service/fileUpload?id={key}',
		allowedPreviewTypes: ['image'], // allow only preview of image & text files
		initialPreviewAsData: true, // defaults markup
		initialPreviewFileType: 'text',
		initialPreview: getFileIds(),
		initialPreviewConfig: getPreviewConfig(),
	}).on("filebatchselected", function(event, files) {
		$("#files").fileinput("upload");
	});



});

//<!-- Controls Section -->
$(function() {

	let checkStatus = {};
	$("#genreport").click(function() {
		$("#genreport").html("<div class='throbber-loader'>Loading…</div>");
		$(".reportLoading").loading({ overlay: true });
		$.get("GenReport?aid=" + $("#appid")[0].value, function(resp) {
			global._token = resp.token;
			clearInterval(checkStatus);
			checkStatus = setInterval(function() {
				$.get(`CheckStatus?aid=${$("#appid")[0].value}`).done(function(resp, _message, http) {
					if (http.status != 202) {
						const updatedDate = resp.message;
						$("#updatedDate").html(updatedDate);
						clearInterval(checkStatus);
						clearLoading($(".reportLoading")[0])
						$("#genreport").html("Generate Report");
						if (typeof $("#dlreport").attr('id') == 'undefined') {
							location.reload();
						}
					}
				});
			}, 2000);


		});
	});
	$("#dlreport").click(function() {
		var id = $(this).attr("rpt");
		var win = window.open('DownloadReport?aid=' + $("#appid")[0].value.replace("app", ""), '_blank');
	});
	$("#prsubmit").click(function() {
		$(".content").loading({ overlay: true, base: 0.3 });
		var data = "action=prsubmit";
		data += "&id=" + $("#appid")[0].value.replace("app", "");
		data += "&_token=" + global._token;
		$.post("SendToPR", data).done(function(resp) {
			global._token = resp.token;
			if (resp.result === "success") {
				$.alert({
					type: "green",
					title: 'Success!',
					content: "Assessment has been submitted for PR",
					buttons: {
						ok: function() { location.reload(); }
					}
				});

			} else {
				$(".content").loading({ destroy: true });
				$.alert({
					type: "red",
					title: 'Error!',
					content: resp.errors,
				});
			}

		});
	});
	$("#finalize").click(function() {
		$(".content").loading({ overlay: true, base: 0.3 });
		$.confirm({
			title: 'Are you sure!',
			content: "Once you finalize the assessment all vulnerabilities will be opened in the tracking system. And the assessment will drop out of your assesement queue.",
			buttons: {
				confirm: function() {
					$(".content").loading({ overlay: true, base: 0.3 });

					var data = "action=finalize";
					data += "&id=" + $("#appid")[0].value.replace("app", "");
					data += "&_token=" + global._token;
					$.post("Assessment", data).done(function(resp) {
						global._token = resp.token;
						if (resp.result === "success") {
							$.alert({
								type: "green",
								title: 'Success!',
								content: "Assessment has been finalized",
								buttons: {
									ok: function() { location.reload(); }
								}
							});
						} else {
							$(".content").loading({ destroy: true });
							var error = ""
							if (typeof resp.errors == 'undefined')
								error = "<br>" + resp.message;
							else {
								error = resp.errors
							}

							$.alert({
								type: "red",
								title: 'Alert!',
								content: error,
							});
						}
					});
				},
				cancel: function() { }
			}
		});

	});
});

//<!-- History section -->
$(function() {
	$('#history').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": true
	});

	var colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
	var boxCount = $("#infobar").find("div.row").find("[class^=col-sm]").length;
	var width = 100 / boxCount;
	$("#infobar").find("div.row").find("[class^=col-sm]").css("width", width + "%").css("min-width", "100px");
	var boxes = $("#infobar").find("[class=small-box]");
	var count = 9;
	boxes.each((index, box) => {
		$(box).css("border-color", colors[count]);
		$(box).css("color", colors[count--]);
	});


});
//   <!-- PR Options -->
$(function() {
	$("#showPR").click(function() {
		var prid = $(this).attr('prid');
		document.location = "TrackChanges?prid=" + prid;
	});

	$("#openICS").click(function() {
		document.getElementById('dlFrame').src = `Assessment?action=ics&id=${id}`;
	})

});

//  <!-- Template and Notes Search and Save -->
$(function() {
	$.ajaxSetup({ cache: false });
	$(".saveTemp").click(function() {
		var id = $(this).attr("for");
		var data = "term=" + $("#" + id).val();
		data += "&summary=" + encodeURIComponent(editors.getEditorText($("#" + id).attr("for")));
		if ($("#" + id).attr("for") == "step_description")
			data += "&exploit=true";
		data += "&_token=" + global._token;
		$.post("tempSave", data).done(function(resp) {
			alertMessage(resp, "Template Updated.");
		});
	});
	$(".deleteTemp").click(function() {
		var el = $(this).parent().parent().find("[id^=tempSearch]");


		var data = "tmpId=" + $(el).attr("tmpId");
		$.confirm({
			title: "Confirm?",
			content: "Are you sure you want to delete the template?",
			buttons: {
				confirm: function() {
					$.post("tempDelete", data).done(function(resp) {
						$(el).val("");
						$(el).attr("tmpId", "");
						alertMessage(resp, "Template Deleted.");
					});
				},
				cancel: function() { }
			}
		});


	});
	$(".saveTemplate").on('click', async (event) => {
		let type = $(event.currentTarget).attr("for");
		let selected = $(`#${type}Templates option:selected`);
		let selectedText = Array.from(selected).map((t) => t.innerHTML)
		let contentMessage = "";
		let buttons = {
			save: function() {
				selectedText = selectedText.join(",")
				if (selected.length == 0) {
					selectedText = $("#tempName").val();
				}
				let data = `term=${selectedText.trim()}`
				data += "&summary=" + encodeURIComponent(editors.getEditorText(type));
				data += `&type=${type}`;
				data += "&active=true"
				data += "&_token=" + global._token;
				$.post("tempSave", data).done(function(resp) {
					_token = resp.token;
					const template = resp.templates[0];
					if (!Array.from($(`#${type}Templates option`))
						.some((t) => $(t).val() == template.tmpId)) {
						let option = document.createElement("option");
						$(option).attr("global", "false");
						$(option).addClass("userTemplate");
						$(option).val(template.tmpId);
						$(option).html(template.title);
						$(`#${type}Templates`).append(option).trigger("change");
						$(option).on("dblclick", async (event)=>{
							await setUpListEvents(event)
						})
					}
					alertMessage(resp, "Template Updated.");
				});

			},
			cancel: function() { }
		}
		if (selectedText.length > 1) {
			$.confirm({
				title: "Error",
				content: "You can only select one template name to save.",
				buttons: {
					ok: function() { }
				}
			});
			return;

		} else if (selectedText.length == 0) {
			contentMessage = "Enter a Template name: <input id='tempName' class='form-control'></input>";
		} else {
			contentMessage = "Do you want to save the template <b>" + selectedText + "</b> or create a new template?<input id='updateTemplateName' type='hidden' value='" + selectedText + "'/>";
			buttons["new"] = function() {
				$(`#${type}Templates`).val(null).trigger('change');
				let saveButtons = $(".saveTemplate")
				for (let button of saveButtons) {
					if ($(button).attr('for') == type) {
						$(button).click();
					}
				}
			}
			const isGlobal = $(selected[0]).attr("global")
			if (isGlobal == "true") {
				buttons["new"]()
				return;
			}
		}
		$.confirm({
			title: "Create Template",
			content: contentMessage,
			buttons: buttons

		})

	});
	
	
	
	$(".deleteTemplate").on('click', async (event) => {
		let type = $(event.currentTarget).attr("for")
		let selected = Array.from($(`#${type}Templates option:selected`)).filter((t) => $(t).attr("global") != "true");
		if (selected.length == 0) {
			alertMessage({ message: "No Valid Templates to Delete" }, null)
			return;
		}
		let textData = [];
		const selectedText = selected.map((t) => t.innerHTML).join(",");

		$.confirm({
			title: "Confirm?",
			content: "Are you sure you want to delete these templates?<br><b>" + selectedText + "</b>",
			buttons: {
				confirm: async function() {
					let messages = []
					$(`#${type}Templates`).val(null).trigger('change');
					for await (const option of selected) {
						await $.post(`tempDelete`, `tmpId=${$(option).val()}`).done(function(resp) {
							_token = resp.token;
							messages.push(resp);
							option.remove();
						});
					}
					alertMessage(messages[0], "Templates Deleted.");
				},
				cancel: function() { }
			}
		});
	});
	async function setUpListEvents(event) {
		let id = event.target.parentElement.id
		let value = event.target.value;
		let type = "summary";
		if(id.indexOf('risk') != -1){
			type='risk'
		}
		await $.get('tempSearchDetail?tmpId=' + value)
			.done(function(data) {
				let template = data.templates[0].text;
				let text = editors.getEditorText(type) + "\n\n" + template;
				editors.setEditorContents(type, text, false);
			});
		
	}
	$(".globalTemplate, .userTemplate").on("dblclick", async (event)=>{
		await setUpListEvents(event)
	})
	
	$(".addTemplate").on('click', async (event) => {
		let type = $(event.currentTarget).attr("for")
		let selected = $(`#${type}Templates option:selected`);
		let textData = []
		for await (const option of selected) {
			await $.get('tempSearchDetail?tmpId=' + $(option).val())
				.done(function(data) {
					textData.push(data.templates[0].text);
				});
		}
		let text = textData.join("\n\n");
		$.confirm({
			title: "Confirm?",
			content: "Are you sure you want to Overwrite, Append, or Prepend the current text?",
			buttons: {
				overWrite: {
					text: "OverWrite",
					action: function() {
						editors.setEditorContents(type, text, false);
					}
				},
				prepend: {
					text: "Prepend",
					action: function() {
						text = text + "\n\n" + editors.getEditorText(type);
						editors.setEditorContents(type, text, false);
					}
				},
				append: {
					text: "Append",
					action: function() {
						text = editors.getEditorText(type) + "\n\n" + text;
						editors.setEditorContents(type, text, false);
					}
				},
				cancel: function() {

				}
			}

		});

	})
	$(".tempSearch").each(function(i, el) {
		var el = $(el)[0];
		var id = $(el).attr("id");


		$(el).autoComplete({
			minChars: 1,
			cacheLength: 0,
			source: function(term, response) {
				const type = $("#" + $(el).attr("id")).attr("for");
				$.ajaxSetup({ cache: false });
				$.getJSON(`tempSearch?term=${term}&type=${type}`,
					function(data) {
						global._token = data.token
						var tmps = data.templates;
						var list = [];
						for (i = 0; i < tmps.length; i++) {
							list[i] = tmps[i].tmpId + ": " + tmps[i].title

						}
						response(list);
					}
				);
			},
			onSelect: function(e, term, item) {
				var s = editors.getEditorText($(el).attr("for"));
				var tmpId = term.split(":")[0];
				$(el).val(term.split(":")[1].trim());
				$(el).attr("tmpId", tmpId);

				if ((s).trim() != "") {
					$.confirm({
						title: "Confirm?",
						content: "Are you sure you want to Overwrite, Append, or Prepend the current text?",
						buttons: {
							overWrite: {
								text: "OverWrite",
								action: function() {
									$.get('tempSearchDetail?tmpId=' + tmpId)
										.done(function(data) {
											let type = $(el).attr("for");
											let text = data.templates[0].text;
											editors.setEditorContents(type, text, false);
										});
								}

							},
							prepend: {
								text: "Prepend",
								action: function() {
									$.get('tempSearchDetail?tmpId=' + tmpId)
										.done(function(data) {
											var text = "<br />" + editors.getEditorText($(el).attr("for"));
											let type = $(el).attr("for");
											editors.setEditorContents(type, text, false);
										});
								}

							},
							append: {
								text: "Append",
								action: function() {
									$.get('tempSearchDetail?tmpId=' + tmpId)
										.done(function(data) {
											var text = editors.getEditorText($(el).attr("for")) + "<br />";
											let type = $(el).attr("for");
											editors.setEditorContents(type, text, false);
										});
								}

							},
							cancel: function() {

							}
						}

					});

				} else {
					$.get('tempSearchDetail?tmpId=' + tmpId)
						.done(function(data) {
							let type = $(el).attr("for");
							let text = data.templates[0].text;
							editors.setEditorContents(type, text, false);
						});
				}


				return false;
			}

		});
	});
});
//<!-- Click events for upper banners -->
$(function() {
	$(".small-box").click(function(el) {
		var p = $(this).find("p")[0];
		var filter = $(p).html();
		$('#vulntable').DataTable().column(4).search(filter).draw();
	});
	$("#removeFilter").click(function() {
		$('#vulntable').DataTable().search('')
			.columns().search('')
			.draw();
	});
});
$(function() {
	$(".select2").select2();
	$('[id^="cust"]').each((_index, el) => $(el).on('input', function (event) {
		let val = "";
		if (this.type == 'checkbox') {
			val = $(this).is(":checked");
		} else {
			val = $(this).val();
		}
		updateCustom(this.id);
	}));
	let pendingUpdate={}
	
	function updateCustom(cfid){
		let el = $(`#${cfid}`);
		$(`#${cfid}_header`).html("*");
		let val = "";
		if (el[0].type == 'checkbox') {
			val = $(el).is(":checked");
		} else {
			val = $(el).val();
		}
		let data = `cfid=${cfid.replace("cust","")}`;
		data += `&id=${id}`;
		data += `&cfValue=${val}`;
		data += "&_token=" + global._token;
		clearTimeout(pendingUpdate[cfid]);
		pendingUpdate[cfid] = setTimeout( () =>{
			$.post("UpdateAsmtCF", data).done(function(resp) {
				$(`#${cfid}_header`).html("");
				
				console.log("Updated");
			});
		}, 1000);
	}

});
$(function() {
	$("#uploadVulns").click(function() {
		$.confirm({
			title: 'Upload a XML Report',
			columnClass: 'large',
			content: "URL:ReportUploadView?id=${id}",
			buttons: {
				cancel: function() { console.log("closed"); this.close(); return 0; },
				close: function() { console.log("close"); }
			}
		});
	});
});

$(function() {

	var url = document.location.toString();
	if (url.match('#')) {
		$('.nav-tabs a[href="' + location.hash + '"]').tab('show');
	}
	$("a").click(evt => {
		if (evt.target.href.indexOf("#") != -1) {
			console.log("Here")
			location.href = evt.target.href
		}
		
	})
});