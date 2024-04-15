require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('./overview.css');
require('../loading/css/jquery-loading.css');
import suneditor from 'suneditor';
import colorPicker from 'suneditor/src/plugins/modules/_colorPicker';
import plugins from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
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
import { marked } from 'marked';
import Chart from 'chart.js/auto';
import TurndownService from 'turndown'
let html2md = new TurndownService()



global._token = $("#_token")[0].value;
var editors = {
	risk: {},
	summary: {},
	notes: {}
};


plugins.table.createCells = function(nodeName, cnt, returnElement) {
	nodeName = nodeName.toLowerCase();

	if (!returnElement) {
		let cellsHTML = '';
		while (cnt > 0) {
			cellsHTML += '<' + nodeName + '>&nbsp;</' + nodeName + '>';
			cnt--;
		}
		return cellsHTML;
	} else {
		const cell = this.util.createElement(nodeName);
		cell.innerHTML = '&nbsp;';
		return cell;
	}
}

global.editors = editors;
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
		const div = document.createElement("p");
		div.innerHTML = html;
		const parent = selected[0].parentNode;
		parent.insertBefore(div, selected[0]);
		for (let i = 0; i < selected.length; i++) {
			selected[i].remove();
		}
		this.history.push(true)
	}
}
plugins['fromMarkdown'] = fromMarkdown;
let toMarkdown = {
	name: 'toMarkdown',
	display: 'command',
	title: 'Convert To Markdown',
	buttonClass: '',
	innerHTML: '<i class="fa-brands fa-markdown" style="color:lightgray; transform: rotate(180deg);"></i>',
	add: function(core, targetElement) {
		core.context.toMarkdown = {
			targetButton: targetElement,
			preElement: null
		}
	},
	active: function(element) {
		if (element) {
			this.util.addClass(this.context.toMarkdown.targetButton.firstChild, 'mdEnabled');
			this.context.toMarkdown.preElement = element;
			return true;
		} else {
			this.util.removeClass(this.context.toMarkdown.targetButton.firstChild, 'mdEnabled');
			this.context.toMarkdown.preElement = null;
		}
		return false;
	},
	action: function() {
		let selected = this.getSelectedElements();
		const html = selected.reduce((acc, item) => acc + item.outerHTML + "", "");
		const md = html2md.turndown(html);
		const div = document.createElement("div");
		div.innerHTML = `<pre>${md}</pre>`;
		const parent = selected[0].parentNode;
		parent.insertBefore(div, selected[0]);
		for (let i = 0; i < selected.length; i++) {
			selected[i].remove();
		}
	}
}
plugins['fromMarkdown'] = fromMarkdown;
var editorOptions = {
	codeMirror: CodeMirror,
	plugins: plugins,
	buttonList: [
		['undo', 'redo', 'fontSize', 'formatBlock', 'textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks', 'fromMarkdown', 'codeView'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	height: "auto",
	minHeight: 500
};
let engagementOptions = {
	defaultStyle: 'font-family: arial; font-size: 18px',
	buttonList: [],
	height: 500
}

function setEditorText_bk(id, data) {
	if (typeof editors[id] == 'undefined') {
		$('#' + id).html(data);
	} else {
		editors[id].setContents(data);
	}
}
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

function getEditorText(name) {
	let html = editors[name].getContents(true);
	return Array.from($(html)).filter(a => a.innerHTML != "<br>").map(a => a.outerHTML).join("")
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
	let risk = getEditorText('risk');
	let sum = getEditorText('summary');
	let notes = getEditorText('notes');
	let data = "riskAnalysis=" + encodeURIComponent(risk);
	data += "&summary=" + encodeURIComponent(sum);
	data += "&notes=" + encodeURIComponent(notes);
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
		$.get("ClearLock").done();
	});

}

function saveEditor(type) {

	let edits = getEditorText(type);
	let data = "";
	if (type == "notes") {
		data += "notes=" + encodeURIComponent(edits);
	}
	else if (type == "risk") {
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
			$.get(`ClearLock?action=${type}`).done();
		}, 5000);
	});

}
let editorTimeout = {};
let clearLockTimeout = {};
function queueSave(type) {
	$.get(`SetLock?action=${type}`).done((resp) => {
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
function b64DecodeUnicode(str) {
	str = decodeURIComponent(str);
	return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
	}).join(''));
}
function setEditorContents(contents, editor, isEncoded) {
	if (isEncoded) {
		contents = b64DecodeUnicode(contents)
	}
	//This fixes issuen with images that makes it hard to edit
	if (contents.endsWith("</div>") || contents.endsWith("</table>")) {
		editors[editor].setContents(contents + "<p><br></p>");
	}else{
		editors[editor].setContents(contents);
	}
}
$(function() {
	global._token = $("#_token")[0].value;
	editors.summary = suneditor.create("summary", {...editorOptions});
	editors.summary.onInput = function(contents, core) {
		queueSave("summary");
	}
	editors.summary.onChange = function(contents, core) {
		if (document.getElementById(`summary_header`).innerHTML == "") {
			queueSave("summary");
		}
		if (!contents.endsWith("</p>")) {
			editors.summary.setContents(contents + "<p><br></p>");
		}
	}

	editors.risk = suneditor.create("riskAnalysis", {...editorOptions});
	editors.risk.onInput = function(contents, core) {
		queueSave("risk");
	}
	editors.risk.onChange = function(contents, core) {
		//setEditorContents(contents, 'risk', false);
		if (document.getElementById(`risk_header`).innerHTML == "") {
			queueSave("risk");
		}
		if (!contents.endsWith("</p>")) {
			editors.risk.setContents(contents + "<p><br></p>");
		}
	}
	editors.notes = suneditor.create("notes", {...editorOptions});
	editors.notes.onInput = function(contents, core) {
		queueSave("notes");
	}
	editors.notes.onChange = function(contents, core) {
		//setEditorContents(contents, 'notes', false);
		if (document.getElementById(`notes_header`).innerHTML == "") {
			queueSave("notes");
		}
		if (!contents.endsWith("</p>")) {
			editors.notes.setContents(contents + "<p><br></p>");
		}
	}
	suneditor.create("engagmentnotes", engagementOptions);
	let errorMessageShown=false;
	setInterval(() => {
		$.get("CheckLocks").done((resp) => {
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
			["notes", "risk", "summary"].forEach(function(type) {
				if (resp[type] && resp[type].isLock) {
					editors[type].core.context.element.wysiwygFrame.classList.add("disabled");
					document.getElementById(`${type}_header`).innerHTML = `<i class="lockUser">Editing by ${resp[type].lockBy} ${resp[type].lockAt}</i>`
					editors[type].disabled();
					setEditorContents(resp[type].updatedText, type, true);
				} else {
					editors[type].enable();
					if (document.getElementById(`${type}_header`).innerHTML.indexOf("*") == -1) {
						document.getElementById(`${type}_header`).innerHTML = "";
					}
					editors[type].core.context.element.wysiwygFrame.classList.remove("disabled");
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



	$(".saveIt").click(function() {
		saveAllEditors(true);

	});
});

//<!-- Controls Section -->
$(function() {

	let checkStatus = {};
	$("#genreport").click(function() {
		$("#genreport").html("<div class='throbber-loader'>Loading…</div>");
		$(".reportLoading").loading({ overlay: true });
		$.get("Assessment?action=genreport&id=" + $("#appid")[0].value, function(resp) {
			global._token = resp.token;
			clearInterval(checkStatus);
			checkStatus = setInterval(function() {
				$.get("CheckStatus").done(function(resp, _message, http) {
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
		var win = window.open('../service/Report.pdf?id=' + $("#appid")[0].value.replace("app", ""), '_blank');
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

//  <!-- Template Search and Save -->
$(function() {
	$.ajaxSetup({ cache: false });
	$(".saveTemp").click(function() {
		var id = $(this).attr("for");
		var data = "term=" + $("#" + id).val();
		data += "&summary=" + encodeURIComponent(getEditorText($("#" + id).attr("for")));
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
				data += "&summary=" + encodeURIComponent(getEditorText(type));
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
			contentMessage = "Do you want to save the template <b>" + selectedText + "</b> or create a new template?<input id='updateTemplateName' type='hidden' value=" + selectedText + "'/>";
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
			title: "Save Template",
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
	$(".globalTemplate, .userTemplate").on("dblclick", async (event)=>{
		let id = event.target.parentElement.id
		let value = event.target.value;
		let type = "summary";
		if(id.indexOf('risk') != -1){
			type='risk'
		}
		await $.get('tempSearchDetail?tmpId=' + value)
			.done(function(data) {
				let template = data.templates[0].text;
				let text = getEditorText(type) + "\n" + template;
				setEditorContents(text, type, false);
			});
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
		let text = textData.join("\n");
		$.confirm({
			title: "Confirm?",
			content: "Are you sure you want to Overwrite, Append, or Prepend the current text?",
			buttons: {
				overWrite: {
					text: "OverWrite",
					action: function() {
						setEditorContents(text, type, false);
					}
				},
				prepend: {
					text: "Prepend",
					action: function() {
						text = text + "\n" + getEditorText(type);
						setEditorContents(text, type, false);
					}
				},
				append: {
					text: "Append",
					action: function() {
						text = getEditorText(type) + "\n" + text;
						setEditorContents(text, type, false);
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
				var s = getEditorText($(el).attr("for"));
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
											setEditorContents(text, type, false);
										});
								}

							},
							prepend: {
								text: "Prepend",
								action: function() {
									$.get('tempSearchDetail?tmpId=' + tmpId)
										.done(function(data) {
											var text = "<br />" + getEditorText($(el).attr("for"));
											let type = $(el).attr("for");
											setEditorContents(text, type, false);
										});
								}

							},
							append: {
								text: "Append",
								action: function() {
									$.get('tempSearchDetail?tmpId=' + tmpId)
										.done(function(data) {
											var text = getEditorText($(el).attr("for")) + "<br />";
											let type = $(el).attr("for");
											setEditorContents(text, type, false);
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
							setEditorContents(text, type, false);
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
	$(".updateCF").click(function() {
		let cfid = $(this).attr("for");
		let el = $("#cust" + cfid);
		let val = "";
		if (el[0].type == 'checkbox') {
			val = $(el).is(":checked");
		} else {
			val = $(el).val();
		}
		let data = `cfid=${cfid}`;
		data += `&id=${id}`;
		data += `&cfValue=${val}`;
		data += "&_token=" + global._token;
		$.post("UpdateAsmtCF", data).done(function(resp) {
			alertMessage(resp, "Parameter Updated.");
		});
	});

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
		if (evt.target.href.indexOf("tab_3") != -1) {
			location.href = "#tab_3"
		} else if (evt.target.href.indexOf("tab_1") != -1) {
			location.href = "#tab_1"
		}
	})
});