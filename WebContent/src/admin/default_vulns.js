
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
import 'datatables.net-bs';
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import 'select2';
import '../scripts/jquery.autocomplete.min';
import { marked } from 'marked';
import CVSS from '../cvss'

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
};

plugins['fromMarkdown'] = fromMarkdown;
let editorOptions = {
	codeMirror: CodeMirror,
	plugins: plugins,
	buttonList: [
		['undo', 'redo', 'fontSize', 'formatBlock', 'textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks', 'fromMarkdown', 'codeView'],
	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	minHeight: 500,
	height: "auto"
};
global.editors = {
	description: suneditor.create("description", {...editorOptions}),
	recommendation: suneditor.create("recommendation", {...editorOptions})
};
global.editors.description.onChange = function(contents, core) {
	console.log(contents)
	if (!contents.endsWith("</p>")) {
		global.editors.description.setContents(contents + "<p><br></p>");
	}
}
global.editors.recommendation.onChange = function(contents, core) {
	if (!contents.endsWith("</p>")) {
		global.editors.remediation.setContents(contents + "<p><br></p>");
	}
}

global.editVuln = function editVuln(id) {

	$.get('DefaultVulns?vulnId=' + id + '&action=getvuln').done(function(data) {
		global.editors.description.setContents(marked.parse(b64DecodeUnicode(data.desc)));
		global.editors.recommendation.setContents(marked.parse(b64DecodeUnicode(data.rec)));
		global.editors.description.core.history.reset();
		global.editors.recommendation.core.history.push(true);

		$("#title").val($("<div/>").html(data.name).text());
		setIntVal(data.impact, "impact");
		setIntVal(data.likelyhood, "likelyhood");
		setIntVal(data.overall, "overall");
		$("#cvss31Score").val(data.cvss31Score);
		$("#cvss40Score").val(data.cvss40Score);
		$("#cvss31String").val(data.cvss31String);
		$("#cvss40String").val(data.cvss40String);
		$(data.cf).each(function(a, b) {
			$("#type" + b.typeid).val(b.value);
		});
		$("#catNameSelect").val(data.category).trigger('change');
		$("#vulnModal").modal({
			show: true,
			keyboard: false,
			backdrop: 'static'
		});
		$("#saveVuln").unbind();
		$(".saveVuln").click(function() {
			let desc = global.editors.description.getContents();
			let rec = global.editors.recommendation.getContents();
			let postData = "description=" + encodeURIComponent(desc);
			postData += "&recommendation=" + encodeURIComponent(rec);
			postData += "&name=" + $("#title").val();
			postData += "&impact=" + $("#impact").val()
			postData += "&likelyhood=" + $("#likelyhood").val()
			postData += "&overall=" + $("#overall").val()
			postData += "&cvss31Score=" + $("#cvss31Score").val()
			postData += "&cvss40Score=" + $("#cvss40Score").val()
			postData += "&cvss31String=" + $("#cvss31String").val()
			postData += "&cvss40String=" + $("#cvss40String").val()
			postData += "&category=" + $("#catNameSelect").select2("val");
			postData += "&vulnId=" + id;
			let fields = [];
			for (let vulnId of vulnTypes) {
				let val = $(`#type${vulnId}`).val();
				if(`${val}` == "undefined"){
					val=""
				}
				fields.push(`{"typeid" : ${vulnId}, "value" : "${val}"}`);
			}
			postData += '&cf=[' + fields.join(",") + "]";
			postData += "&action=savevuln";
			postData += "&_token=" + _token;
			$.post("DefaultVulns", postData, function(resp) {
				$(`#vuln_title_${id}`).html($("#title").val());
				let severityName = $("#overall option:selected").text();
				$(`#vuln_sev_${id}`).html(severityName);
				$("#vulnModal").modal('hide');
			});
		});

	});
};

global.deleteVuln = function deleteVuln(id) {
	$.confirm({
		title: "Are you sure?",
		content: "This change cannot be recovered. If the vulnerability is assigned to an assessment it cannot be deleted but can be make inactive.",
		buttons: {
			"Yes Delete it": function() {
				let data = 'vulnId=' + id + '&action=delvuln';
				data += "&_token=" + _token;
				$.get('DefaultVulns', data).done(function(resp) {
					if (resp.message) {
						if (resp.message == 'Failed CSRF Token') {
							$.alert({
								title: "Error",
								content: resp.message,
								type: red
							});
						} else {
							$.confirm({
								title: "Error",
								content: data.message,
								buttons: {
									"Make Inactive": function() {
										data = "vulnId=" + id;
										data += "&_token=" + _token;
										$.post("DeActivate", data).done(function(resp2) {
											alertRedirect(resp2);
										});
									},
									cancel: function() { return; }
								}
							});
						}
					} else {
						$(`#vuln_title_${id}`).parent().remove()
					}
				});
			},
			cancel: function() { return; }
		}
	});

}
global.editCat = function editCat(el, id) {
	var catName = $($($($(el).parent()).parent()).find("td")[0]).text();
	console.log(catName);
	$.confirm({
		title: "Edit Category",
		content: "<input style='width:100%' id='updatedCatName' value='" + catName + "'></input>",
		buttons: {
			"Update It": function() {
				let data = "catId=" + id;
				data += "&name=" + $("#updatedCatName").val();
				data += "&_token=" + _token;
				$.post("editCat", data).done(function(resp) {
					alertRedirect(resp);
				});
			},
			cancel: function() { return; }
		}
	});
};
global.deleteCat = function deleteCat(id) {
	$.confirm({
		title: "Are you sure?",
		content: "This change cannot be recovered. If the category is assigned to an vulnerability it cannot be deleted but can be make inactive.",
		buttons: {
			"Yes Delete it": function() {
				let data = 'catId=' + id + '&action=delCat';
				data += "&_token=" + _token;
				$.post('DefaultVulns', data).done(function(resp) {
					alertRedirect(resp);

				});
			},
			cancel: function() { return; }
		}
	});

};
global.toggleVuln = function toggleVuln(id, state) {
	let data = "vulnId=" + id;
	data += "&_token=" + _token;
	if (!state) {
		$.post("ReActivate", data).done(function(resp) {
			alertRedirect(resp);
		});
	} else {
		$.post("DeActivate", data).done(function(resp) {
			alertRedirect(resp);
		});
	}
};

$(function() {
	$(".select2").select2();

	/*$("#overall").on('change', (event) => {
		let sev = event.target.value;
		$("#impact").val(sev).trigger("change");
		$("#likelyhood").val(sev).trigger("change");

	})*/

	$('#catTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": true,
		"columns": [null, { "width": "10%" }]
	});
	let vulnTable = $('#vulnTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": true,
		"columns": [null, { "width": "10%" }, { "width": "5%" }, { "width": "10%" }],
		"columnDefs": [
			{
				"targets": [4, 5],
				"visible": false,
				"searchable": true
			}
		]
	});
	vulnTable.search('active').draw();
	$("#addCat").click(function() {
		name = $("#catname").val();
		let data = "action=addcat&name=" + name;
		data += "&_token=" + _token;
		$.post("DefaultVulns", data).done(function(resp) {
			alertRedirect(resp);
		});
	});
	$("#addVuln").click(function() {
		
		let desc = global.editors.description.setContents("");
		let rec = global.editors.recommendation.setContents("");
		global.editors.description.core.history.reset();
		global.editors.recommendation.core.history.push(true);
		$("#title").val("");
		$("#impact").val(0).trigger('change')
		$("#likelyhood").val(0).trigger('change')
		$("#overall").val(0).trigger('change')
		$("#catNameSelect").val(0).trigger('change')
		$("#cvss31String").val('');
		$("#cvss40String").val('');
		$("#cvss31Score").val('');
		$("#cvss40Score").val('');
		$("#vulnModal").modal({
			show: true,
			keyboard: false,
			backdrop: 'static'
		});
		$(".saveVuln").unbind();
		$(".saveVuln").click(function() {
			let desc = global.editors.description.getContents();
			let rec = global.editors.recommendation.getContents();
			let data = "description=" + encodeURIComponent(desc);
			data += "&recommendation=" + encodeURIComponent(rec);
			data += "&name=" + $("#title").val();
			data += "&impact=" + $("#impact").val()
			data += "&likelyhood=" + $("#likelyhood").val()
			data += "&overall=" + $("#overall").val()
			data += "&category=" + ($("#catNameSelect").val() == null ?  "": $("#catNameSelect").val())
			data += "&cvss31String=" + $("#cvss31String").val();
			data += "&cvss40String=" + $("#cvss40String").val();
			data += "&cvss31Score=" + $("#cvss31Score").val();
			data += "&cvss40Score=" + $("#cvss40Score").val();
			let fields = [];
			for (let vulnId of vulnTypes) {
					let val = $(`#type${vulnId}`).val();
					if(`${val}` == "undefined"){
						val=""
					}
				fields.push(`{"typeid" : ${vulnId}, "value" : "${val}"}`);
			}
			data += '&cf=[' + fields.join(",") + "]";
			data += "&action=addvuln";
			data += "&_token=" + _token;
			$.post("DefaultVulns", data, function(resp) {
				alertRedirect(resp);
			});
		});
	});

	$("[id^=updateRisk]").click(function() {
		let id = $(this).attr("id");
		id = id.replace("updateRisk", "");
		let data = "action=updateRisk";
		data += "&riskId=" + id;
		data += "&riskName=" + $("#riskName" + id).val();
		data += "&_token=" + _token;
		$.post("DefaultVulns", data).done(function(resp) {
			alertRedirect(resp);
		});
	});


	$("#saveDates").click(function() {
		let data = "action=updateDates";
		data += "&_token=" + _token;
		for (let i = 0; i < 10; i++) {
			data += "&duedate[" + i + "]=" + $("#due_" + i).val();
			data += "&warndate[" + i + "]=" + $("#warn_" + i).val();
		}
		$.post('DefaultVulns', data).done(function(resp) {
			alertRedirect(resp);
		});

	});

	$("#importDB").click(function() {
		$.confirm({
			title: "Are you sure?",
			content: "This will create categories and several vulnerabilities in the system. This may be hard to reverse.",
			buttons: {
				"Yes I Want This!": function() {
					$.confirm({
						title: "Are you really REALLY sure?",
						content: "Last chance to back out...",
						buttons: {
							"Yup I know what i'm doing.": function() {
								$("#vulnTable").loading({ overlay: true, base: 0.3 });
								$("#catTable").loading({ overlay: true, base: 0.3 });
								let data = "action=importVDB";
								data += "&_token=" + _token;
								$.post("DefaultVulns", data).done(function(resp) {
									alertRedirect(resp);
								});
							},
							"Nope": function() { return; }
						}
					});

				},
				"No Thanks": function() { return; }
			}
		});

	});

	$('input[type=radio][name=verOption]').change(function() {
		let data = "verOption=" + $(this).val();
		data += "&_token=" + _token;
		$.post("VerificationSetting", data).done(function(resp) {
			alertMessage(resp, "Setting Updated");
		});
	});
});

function updateIntVal(par, el) {
	let rank = $(par).html();
	//console.log(par);
	$("#" + el).val(rank);
	//console.log(rank);
	$("#" + el).attr("intVal", getIdFromValue(rank));
}
function setIntVal(value, el) {
	$("#" + el).val(value).trigger("change");
	//$("#"+el).attr("intVal", value);
	//$("#"+el).val(getValueFromId(""+value));
}


$(function() {
	$("#downloadVulns").click(function() {
		document.location = "GetVulnsCSV";
	});
	let cvss31 = new CVSS("", false);

	cvss31.setUpCVSSModal("cvss31Calc", "cvss31String", (vector, score) => {
		$("#cvss31String").val(vector).trigger("change")
		$("#cvss31Score").val(score).trigger("change")
		const overall = cvss31.convertCVSSSeverity(cvss31.getCVSSSeverity(score));
		$("#overall").val(overall).trigger('change');
		
	});
	let cvss40 = new CVSS("", true);
	cvss40.setUpCVSSModal("cvss40Calc", "cvss40String", (vector, score) => {
		$("#cvss40String").val(vector).trigger("change")
		$("#cvss40Score").val(score).trigger("change")
		const overall = cvss40.convertCVSSSeverity(cvss40.getCVSSSeverity(score));
		$("#overall").val(overall).trigger('change');
	});
});
function b64DecodeUnicode(str) {
	str = decodeURIComponent(str);
	return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
	}).join(''));
}

