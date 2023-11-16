require('suneditor/dist/css/suneditor.min.css');
require('jquery-fileinput/fileinput.css');
require('./overview.css');
require('../loading/css/jquery-loading.css');
import suneditor from 'suneditor';
import plugins from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
import '../loading/js/jquery-loading';
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-fileinput';
import 'jquery-ui';
import 'jquery-confirm';
import 'select2';
import '../scripts/jquery.autocomplete.min';
import { marked } from 'marked';

function getEditorText(name) {
	let html = editors[name].getContents();
	return Array.from($(html)).filter( a => a.innerHTML != "<br>").map( a => a.outerHTML).join("")
}


global.updateIntVal = function updateIntVal(element, elementName) {
	var rank = $(element).html();
	$("#" + elementName).val(rank);
	$("#" + elementName).attr("intVal", getIdFromValue(rank));
};

global.setIntVal = function setIntVal(value, el) {
	$("#" + el).val(value).trigger("change");
};

$(function() {

	$("#overall").select2()
	$("#likelyhood").select2()
	$("#impact").select2()
	
	$("#overall").on('change', (event) =>{
		let sev = event.target.value;
		$("#likelyhood").val(sev).trigger("change");
		$("#impact").val(sev).trigger("change");
		
	});
	$("#vulntable tr").on('click', function(event){
		const vulnid = $(this).data("vulnid");
		$(".selected").each( (_a,s) => $(s).removeClass("selected"))
		$(this).addClass("selected");
		EditVuln(vulnid);
		
		
	});
	$("#vulntable span[id^=deleteVuln]").each((_index, element) => {
		$(element).on('click', event => {
			var stepId = parseInt(event.currentTarget.id.replace("deleteVuln", ""));
			DeleteVuln(event.currentTarget, stepId);
		});
	});
	$("#deleteMulti").on("click", () => {
		deleteMulti();
	});
	$("#reasign").on("click", () => {
		reasign();
	});
});

var descriptions = {};
var testContent = "";
var editors = {};
var _token = $("#_token")[0].value;
var assesssmentId = $("#assessmentId")[0].value;
var disabled = $("#disable")[0].value;
var editorOptions = {
	codeMirror: CodeMirror,
	plugins: plugins,
	buttonList: [
		['undo', 'redo','fontSize', 'formatBlock','textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks','codeView'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	minHeight: 500,
	height: 'auto'
};
function updateColors() {
	var colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
	var boxCount = $("#infobar").find("div.row").find("[class^=col-sm]").length;
	var width = 100 / boxCount;
	$("#infobar").find("div.row").find("[class^=col-sm]").css("width", width + "%").css("min-width", "100px");
	var boxes = $("#infobar").find("[class=small-box]");
	var colorCount = 9;
	boxes.each((index, box) => {
		let risk = $(box).find("p")[0].innerText;
		$(`.severity:contains('${risk}')`).css("color", colors[colorCount]).css("font-weight", "bold");
		$(box).css("border-color", colors[colorCount]);
		//$(`.sev${risk}`).css("border-left", `5px solid ${colors[colorCount]}`)
		$(`.sev${risk}`).css("border-left-color", `${colors[colorCount]}`)
		$(box).css("color", colors[colorCount--]);
	});
}
function reasign() {
	var checkboxes = Array.from($("input[id^='ckl']")).filter(cb =>
		$(cb).is(':checked')
	);
	var movedid = $("#re_asmtid").val();
	var movedName = $("#re_asmtid option[value='" + movedid + "']").text();
	$.confirm({
		type: "red",
		title: "Are you sure?",
		content: "You will be moving " + checkboxes.length + " vulnerabilities to <br> <b>" + movedName + "</b>",
		buttons: {
			'yes, reassign': function() {
				var rows = ["id=" + movedid];
				$("#stepstable.table").dataTable().fnClearTable();
				checkboxes.forEach((element, index) => {
					var row = $(element).parents("tr");
					var id = $(element).attr("id").replace("ckl", "");
					rows.push("vulns[" + index + "]=" + id);
					$("#vulntable").DataTable().row(row).remove().draw();
				});
				rows.push("_token=" + _token);
				$.post('reassignVulns', rows.join("&")).done(function(resp) {
					alertMessage(resp, 'Vulns were successfully reassigned.');

				});


			},
			"no": function() { }
		}
	});
}
function saveChanges(type, isEditor=true) {
	
		let edits = "";
		if(isEditor){
			edits = getEditorText(type);
		}else{
			edits = $(`#${type}`).val();
		}
		var data = "vulnid=" + global.vulnid;
		data += `&${type}=${encodeURIComponent(edits)}`;
		let fields = [];
		for (let vulnId of vulnTypes) {
			let value = $(`#type${vulnId}`).val();
			fields.push(`{"typeid" : ${vulnId}, "value" : "${value}"}`);
		}
		data += '&cf=[' + fields.join(",") + "]";
		data += "&_token=" + global._token;
		$.post("updateVulnerability", data, function(resp) {
			document.getElementById(`${type}_header`).innerHTML=""
			if(resp.result != "success"){
				$.alert(resp.message);
			}
			global._token = resp.token;
			clearTimeout(clearLockTimeout[type]);
			clearLockTimeout[type] = setTimeout(() => {
			$.get(`ClearLock?action=${type}`).done();
			}, 5000);
			console.log("check for errors");
		});

}
let editorTimeout = {};
let clearLockTimeout = {};
function queueSave(type, isEditor=true) {
	$.get(`SetLock?action=${type}`).done( (resp) => {
		if(resp.result == "success"){
			document.getElementById(`${type}_header`).innerHTML="*"
			clearTimeout(editorTimeout[type]);
			clearTimeout(clearLockTimeout[type]);
			editorTimeout[type] = setTimeout(() => {
				saveChanges(type, isEditor);
			}, 2000);
		}
	});

}
(function() {
	updateColors();
	editors.description = suneditor.create("description", editorOptions);
	editors.recommendation = suneditor.create("recommendation", editorOptions);
	editors.details = suneditor.create("details", editorOptions);
	$('#vulntable').DataTable({
		"paging": false,
		"lengthChange": false,
		"searching": false,
		"ordering": true,
		"info": false,
		"autoWidth": true,
		"order": [[1, "desc"]],
		"columns": [
			{width: "10px"}, //checkbox
			null, //name
			{width: "10px"} //controls
			
		]
	});


	$.get("onGoingAssessments").done(function(resp) {

		$(resp).each(function(a, b) {
			var id = b.id;
			var name = b.name;
			var appid = b.appid;
			var type = b.type;
			$("#re_asmtid").append("<option value=" + id + " >Move Vulns to <b>" + appid + " " + name + " [" + type + "]</b></option>");
		});
		$('#re_asmtid').select2().select2('val', $('.select2 option:eq(0)').val());
	});


	$("#dtitle").autoComplete({
		minChars: 3,
		source: function(term, response) {
			$.getJSON('DefaultVulns?action=json&terms=' + term,
				function(data) {
					var vulns = data.vulns;
					var list = [];
					for (var i = 0; i < vulns.length; i++) {
						list[i] = vulns[i].vulnId + " :: " + vulns[i].name + " :: " + vulns[i].category;
					}
					response(list);
				}
			);
		},
		onSelect: function(e, term, item) {
			var d = getEditorText("description");
			var r = getEditorText("recommendation");
			var splits = term.split(" :: ");
			$("#dtitle").val(splits[1]);
			if ($("#title").val() == "")
				$("#title").val(splits[1]);
			$("#dcategory").val(splits[2]);
			var vulnid = splits[0];
			$("#dtitle").attr("intVal", vulnid);
			d = d.replace("<p><br></p>", "");
			r = r.replace("<p><br></p>", "");

			if ((r + d).trim() != "") {

				$.confirm({
					title: 'Are You Sure',
					type: 'orange',
					content: "Do you Want to OverWrite the Recommendation and Description with the default text for this vulnerablity.",
					buttons: {
						"yes": function() {
							$.get('DefaultVulns?action=getvuln&vulnId=' + vulnid)
								.done(function(data) {
									editors.description.setContents(marked.parse(b64DecodeUnicode(data.desc)));
									editors.recommendation.setContents(marked.parse(b64DecodeUnicode(data.rec)));
									setIntVal(data.likelyhood, 'likelyhood');
									setIntVal(data.impact, 'impact');
									setIntVal(data.overall, 'overall');
									$(data.cf).each(function(a, b) {
										$("#type" + b.typeid).val(b.value);
									});
								});

						},
						"cancel": function() { }
					}
				});

			} else {
				$.get('DefaultVulns?action=getvuln&vulnId=' + vulnid)
					.done(function(data) {
						editors.description.setContents(marked.parse(b64DecodeUnicode(data.desc)).replace(/\n/g, " "));
						editors.recommendation.setContents(marked.parse(b64DecodeUnicode(data.rec)).replace(/\n/g, " "));
						setIntVal(data.likelyhood, 'likelyhood');
						setIntVal(data.impact, 'impact');
						setIntVal(data.overall, 'overall');
						$(data.cf).each(function(a, b) {
							$("#type" + b.typeid).val(b.value);
						});
					});
			}
		}
	});
	
	
	


	$("#addVuln").click(function() {

		$("#dtitle").click(function() { $("#dtitle").val(""); $("#dcategory").val(""); });
		var isFeedPost = false;
		var desc = getEditorText("description");
		var rec = getEditorText("recommendation");
		var data = "id=" + assesssmentId;
		data += "&description=" + encodeURIComponent(desc);
		data += "&recommendation=" + encodeURIComponent(rec);
		data += "&title=";
		data += "&impact="
		data += "&likelyhood="
		data += "&overall="
		data += "&category="
		data += "&defaultTitle="
		data += "&feedMsg="
		let fields = [];
		for (let vulnId of vulnTypes) {
			let value = $(`#type${vulnId}`).val();
			fields.push(`{"typeid" : ${vulnId}, "value" : "${value}"}`);
		}
		data += '&cf=[' + fields.join(",") + "]";
		data += "&add2feed=false"
		data += "&action=add";
		data += "&_token=" + _token;
		$.post("AddVulnerability", data, function(resp) {

			var respData = getData(resp);
			if (respData != "error") {
				deleteVulnForm();
				$("#vulnForm").removeClass("disabled");
				//$("#vulntable").append(respData[0]);
				const row = $('#vulntable').DataTable().row.add($(respData[0])).draw().node()
				const vulnId = resp.vulnId
				$(row).on('click', event => {
					EditVuln(vulnId);
				});
				updateColors();
			} else if (text.Text == "WO-nnnn Name") {
				text.Text = "Maintenance";
			}

		});

	});
})();
function showLoading(com) {
	$(com).loading({ overlay: true, base: 0.3 });
}
function clearLoading(com) {
	if ($(com).hasClass('js-loading'))
		$(com).loading({ destroy: true });
}
function deleteMulti() {
	var checkboxes = Array.from($("input[id^='ckl']")).filter(cb =>
		$(cb).is(':checked')
	);

	$.confirm({
		type: "red",
		title: "Are you sure?",
		content: "Do you want to delete all " + checkboxes.length + " vulnerabilities?",
		buttons: {
			'yes, delete': function() {
				var rows = [];
				$("#stepstable.table").dataTable().fnClearTable();

				var checkboxes = $("input[id^='ckl']");
				Array.from(checkboxes).filter(cb =>
					$(cb).is(':checked')
				).forEach((element, index) => {
					var row = $(element).parents("tr");
					var id = $(element).attr("id").replace("ckl", "");
					rows.push("vulns[" + index + "]=" + id);
					$("#vulntable").DataTable().row(row).remove().draw();
				});

				rows.push("_token=" + _token);
				$.post('DeleteVulns', rows.join("&")).done(function(resp) {
					_token = resp.token;
					alertMessage(resp, "Vulnerabilties Deleted Successfully");
				});

			},
			"no": function() { }
		}
	});



}
function alertMessage(resp, success) {
	if (typeof resp.message == "undefined")
		$.alert(
			{
				title: "SUCCESS!",
				type: "green",
				content: success,
				columnClass: 'small'
			}
		);
	else
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small'
			}
		);

	_token = resp.token;
}
function deleteVulnForm() {
	editors.description.setContents("");
	editors.recommendation.setContents("");
	$("#title").val("");
	$("#impact").attr("intVal", "-1");
	$("#impact").val("").trigger("change");
	$("#likelyhood").val("").trigger("change");
	$("#overall").val("").trigger("change");
	$("#category").val("");
	$("#dtitle").attr("intVal", "-1");
	$("#dtitle").val("");
	$("#dcategory").attr("intVal", "-1");
	$("#dcategory").val("");
}


function disableAutoSave(){
	
	console.log("disnable autosave")
	editors.recommendation.onChange = function(){};
	editors.recommendation.onChange = function(){};
	editors.description.onChange = function(){};
	editors.description.onInput = function(){};
	editors.details.onChange = function(){};
	editors.details.onInput = function(){};
	$("#title").unbind('input');
}

function enableAutoSave(){
	console.log("enable autosave")
	editors.description.onInput = function(contents, core){
		queueSave("description");
	}
	editors.description.onChange = function(contents, core){
		queueSave("description");
	}
	editors.recommendation.onInput = function(contents, core){
		queueSave("recommendation");
	}
	editors.recommendation.onChange = function(contents, core){
		queueSave("recommendation");
	}
	editors.details.onInput = function(contents, core){
		queueSave("details");
	}
	editors.details.onChange = function(contents, core){
		queueSave("details");
	}
	$("#title").on('input', function(event){
		$(".selected").find(".vulnName")[0].innerHTML=$(this).val()
		queueSave("title", false);
	});
	
}


function EditVuln(id) {
	global.vulnid=id;
	disableAutoSave()
	$("#vulnForm").removeClass("disabled");
	$.get('AddVulnerability?vulnid=' + id + '&action=get').done(function(data) {

		$("#title").val($("<div/>").html(data.name).text());
		$("#dtitle").val($("<div/>").html(data.dfname).text());
		$("#dtitle").attr("intVal", data.dfvulnid);
		$("#dcategory").val($("<div/>").html(data.dfcat).text());
		$("#dcategory").attr("intVal", data.dfcatid);
		editors.description.setContents(b64DecodeUnicode(data.description));
		editors.recommendation.setContents(b64DecodeUnicode(data.recommendation));
		editors.details.setContents(b64DecodeUnicode(data.details));
		setIntVal(data.likelyhood, 'likelyhood');
		setIntVal(data.impact, 'impact');
		setIntVal(data.overall, 'overall');
		$(data.cf).each(function(a, b) {
			$("#type" + b.typeid).val(b.value);
		});
		enableAutoSave()
	});

}
function DeleteVuln(el, id) {
	var row = $(el).parents("tr");

	$.confirm({
		type: "red",
		title: "Are you sure?",
		content: "Do you want to delete " + $("#vulntable").DataTable().row(row).data()[3],
		buttons: {
			"yes, delete it": function() {
				var data = 'vulnid=' + id + '&action=delete';
				data += "&_token=" + _token;
				console.log(data);
				$.post('AddVulnerability', data).done(function(resp) {
					var isError = getData(resp);
					if (isError != "error") {
						$("#vulntable").DataTable().row(row).remove().draw();
						$("#stepstable.table").dataTable().fnClearTable();
					}
				});
			},
			cancel: function() { return 1; }
		}
	});


}

function getData(resp) {
	_token = resp.token;
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

function b64DecodeUnicode(str) {
	str = decodeURIComponent(str);
	return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
	}).join(''));
}


