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
		EditVuln(vulnid);
		
		
	});

	$("#vulntable tr[id^=show]").each((_index, element) => {
		$(element).on('click', event => {
			var stepId = parseInt(event.currentTarget.id.replace("showSteps", ""));
			ShowSteps(event.currentTarget, stepId);
		});
	});
	$("#vulntable span[id^=vulnID]").each((_index, element) => {
		$(element).on('click', event => {
			var stepId = parseInt(event.currentTarget.id.replace("vulnID", ""));
			EditVuln(stepId);
		});
	});
	$("#vulntable span[id^=svulnID]").each((_index, element) => {
		$(element).on('click', event => {
			var stepId = parseInt(event.currentTarget.id.replace("svulnID", ""));
			AddDetail(event.currentTarget, stepId);
		});
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
		$(`.sev${risk}`).css("border-left", `5px solid ${colors[colorCount]}`)
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
function saveEditor(type) {
	
	let edits = getEditorText(type);
	data += `${type}=${encodeURIComponent(edits)}`;
	data += "&id=app" + $("#appid")[0].value
	data += "&update=true";
	data += "&_token=" + global._token;
	$.post("Assessment.action", data).done(function(resp) {
		document.getElementById(`${type}_header`).innerHTML=""
		if(resp.result != "success"){
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
	$.get(`SetLock?action=${type}`).done( (resp) => {
		if(resp.result == "success"){
			document.getElementById(`${type}_header`).innerHTML="*"
			clearTimeout(editorTimeout[type]);
			clearTimeout(clearLockTimeout[type]);
			editorTimeout[type] = setTimeout(() => {
				saveEditor(type);
			}, 2000);
		}
	});

}
(function() {
	updateColors();
	editors.vulnDescription = suneditor.create("description", editorOptions);
	editors.vulnDescription.onInput = function(contents, core){
		queueSave("vulnDescription");
	}
	editors.vulnDescription.onChange = function(contents, core){
		queueSave("vulnDescription");
	}
	editors.vulnRecommendation = suneditor.create("recommendation", editorOptions);
	editors.vulnRecommendation.onInput = function(contents, core){
		queueSave("vulnRecommendation");
	}
	editors.vulnRecommendation.onChange = function(contents, core){
		queueSave("vulnRecommendation");
	}
	editors.stepDescription = suneditor.create("step_description", editorOptions);
	editors.stepDescription.onInput = function(contents, core){
		queueSave("stepDescription");
	}
	editors.stepDescription.onChange = function(contents, core){
		queueSave("stepDescription");
	}
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

	$(".close").click(function() {
		deleteVulnForm();
	});
	$("button:contains('Close')").click(function() {
		deleteVulnForm();
	});
	$(".step-close").click(function() {
		clearStepForm();
	});
	$("#clearVuln").click(function() {
		deleteVulnForm();
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
			var d = getEditorText("vulnDescription");
			var r = getEditorText("vulnRecommendation");
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
									editors.vulnDescription.setContents(marked.parse(b64DecodeUnicode(data.desc)));
									editors.vulnRecommendation.setContents(marked.parse(b64DecodeUnicode(data.rec)));
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
						editors.vulnDescription.setContents(marked.parse(b64DecodeUnicode(data.desc)).replace(/\n/g, " "));
						editors.vulnRecommendation.setContents(marked.parse(b64DecodeUnicode(data.rec)).replace(/\n/g, " "));
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
		$('#vulnModal').modal('show');

		$("#dtitle").click(function() { $("#dtitle").val(""); $("#dcategory").val(""); });


		$("#saveVuln2").show();
		$("#saveVuln3").show();
		$("#saveVuln, #saveVuln1, #saveVuln2, #saveVuln3").unbind();
		$("#saveVuln, #saveVuln1, #saveVuln2, #saveVuln3").click(function(event) {
			var isFeedPost = false;
			if ($("#isFeedPost").is(':checked'))
				isFeedPost = true;
			var desc = getEditorText("vulnDescription");
			var rec = getEditorText("vulnRecommendation");
			var data = "id=" + assesssmentId;
			data += "&description=" + encodeURIComponent(desc);
			data += "&recommendation=" + encodeURIComponent(rec);
			data += "&title=" + $("#title").val();
			data += "&impact=" + $("#impact").val()
			data += "&likelyhood=" + $("#likelyhood").val()
			data += "&overall=" + $("#overall").val()
			data += "&category=" + $("#dcategory").attr("intVal");
			data += "&defaultTitle=" + $("#dtitle").attr("intVal");
			data += "&feedMsg=" + $("#feedMsg").val();
			let fields = [];
			for (let vulnId of vulnTypes) {
				let value = $(`#type${vulnId}`).val();
				fields.push(`{"typeid" : ${vulnId}, "value" : "${value}"}`);
			}
			data += '&cf=[' + fields.join(",") + "]";
			data += "&add2feed=" + isFeedPost;
			data += "&action=add";
			data += "&_token=" + _token;
			$.post("AddVulnerability", data, function(resp) {

				var respData = getData(resp);
				if (respData != "error") {
					deleteVulnForm();
					$('#vulnModal').modal('hide');
					//$("#vulntable").append(respData[0]);
					const row = $('#vulntable').DataTable().row.add($(respData[0])).draw().node()
					const editBtn = $(row).find("[id^=vulnID]")
					const stepsBtn = $(row).find("[id^=svulnID]")
					const deleteBtn = $(row).find("[id^=delete]")
					console.log(editBtn)
					$(editBtn[0]).on('click', event => {
						const vulnId = parseInt(event.currentTarget.id.replace("vulnID", ""));
						EditVuln(vulnId);
					});
					$(stepsBtn[0]).on('click', event => {
						const vulnId = parseInt(event.currentTarget.id.replace("svulnID", ""));
						AddDetail(event.currentTarget, vulnId)
					});
					$(deleteBtn[0]).on('click', event => {
						const vulnId = parseInt(event.currentTarget.id.replace("deleteVuln", ""));
						DeleteVuln(event.currentTarget, vulnId)
					});
					$(row).on('click', event => {
						const stepId = parseInt(event.currentTarget.id.replace("showSteps", ""));
						ShowSteps(event.currentTarget, stepId);
					});

					updateColors();
					if(event.target.id == "saveVuln2" || event.target.id == "saveVuln3"){
						const vulnId = stepsBtn[0].id.replace("svulnID", "");
						AddDetail(stepsBtn[0], vulnId);
					}
				} else if (text.Text == "WO-nnnn Name") {
					text.Text = "Maintenance";
				}

			});


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
	editors.vulnDescription.setContents("");
	editors.vulnRecommendation.setContents("");
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


function EditVuln(id) {
	$.get('AddVulnerability?vulnid=' + id + '&action=get').done(function(data) {

		$("#title").val($("<div/>").html(data.name).text());
		$("#dtitle").val($("<div/>").html(data.dfname).text());
		$("#dtitle").attr("intVal", data.dfvulnid);
		$("#dcategory").val($("<div/>").html(data.dfcat).text());
		$("#dcategory").attr("intVal", data.dfcatid);
		editors.vulnDescription.setContents(b64DecodeUnicode(data.description));
		editors.vulnRecommendation.setContents(b64DecodeUnicode(data.recommendation));
		editors.stepDescription.setContents(b64DecodeUnicode(data.details));
		setIntVal(data.likelyhood, 'likelyhood');
		setIntVal(data.impact, 'impact');
		setIntVal(data.overall, 'overall');
		$(data.cf).each(function(a, b) {
			$("#type" + b.typeid).val(b.value);
		});
	});
	$("#saveVuln").click(function() {
		var desc = getEditorText("vulnDescription");
		var rec = getEditorText("vulnRecommendation");
		var data = "vulnid=" + id;
		data += "&description=" + encodeURIComponent(desc);
		data += "&recommendation=" + encodeURIComponent(rec);
		data += "&title=" + $("#title").val();
		data += "&impact=" + $("#impact").val()
		data += "&likelyhood=" + $("#likelyhood").val()
		data += "&overall=" + $("#overall").val()
		data += "&category=" + $("#category").attr("intVal");
		data += "&defaultTitle=" + $("#dtitle").attr("intVal");
		data += "&defaultCategory=" + $("#dcategory").attr("intVal");
		let fields = [];
		for (let vulnId of vulnTypes) {
			let value = $(`#type${vulnId}`).val();
			fields.push(`{"typeid" : ${vulnId}, "value" : "${value}"}`);
		}
		data += '&cf=[' + fields.join(",") + "]";
		data += "&action=update";
		data += "&_token=" + _token;
		$.post("AddVulnerability", data, function(resp) {
			console.log("check for errors");
			deleteVulnForm();
			location.reload();
		});


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
function ShowSteps(el, id) {
	showLoading("#stepstable");
	if (el != -1) {
		$("#vulntable").find("tbody").find("tr").each(function(index, tr) { $(tr).removeClass('selected'); });
		var row = $("#vulntable").DataTable().row(el).data();
		var title = "Showing Details for " + row[1] + " " + row[2] + ":" + row[3];
		$("#vulnTitle").html(title);
		$(el).toggleClass('selected');
	}
	var data = 'vulnid=' + id + '&action=get';
	$.post('AddStep', data).done(function(response) {
		clearLoading("#stepstable");
		getData(response);
		$('#stepstable.table').dataTable().fnClearTable();
		$("#stepstable").DataTable().destroy();
		var st = $("#stepstable").DataTable({
			"lengthChange": false,
			"columns": [
				{ "width": "8%" },
				null,
				{ "width": "5%" },
				{ "width": "5%", "contentPadding": "0px" },
				{ "width": "5%" },
				{ "width": "5%" },
			]
		});
		var steps = response.steps;
		for (var i = 0; i < steps.length; i++) {
			var disabledStr = "";
			if (disabled == "true") disabledStr = "disabled";
			var edit = `<button 
                class="btn btn-block btn-primary btn-xs" style="width:100px;" 
                hasImage="${steps[i].hasImage}" 
                id="editStep${steps[i].stepId}" 
                ${disabledStr}>
                <i class="fa fa-edit"></i> Edit
                </button>`;
			var del = `<button 
                class="btn btn-block btn-primary btn-xs" style="width:100px;" 
                index="${i}" 
                id="deleteStep${steps[i].stepId}|${steps[i].vulnId}"
                ${disabledStr}>
                <i class="fa fa-trash"></i> Delete
                </button>`;
			var up = `<div class="btn-group" 
                style="width:75px">
                <button class="btn btn-default btn-primary btn-xs" 
                style="color:white" index="${i}" 
                id="upStep${steps[i].stepId}|${steps[i].vulnId}" >
                <span class="glyphicon glyphicon-triangle-top"></span>
                </button>`;
			var dn = `<button 
                class="btn btn-default btn-primary btn-xs" 
                style="color:white" 
                index="${i}" 
                id="dnStep${steps[i].stepId}|${steps[i].vulnId}" 
                ${disabledStr}>
                <span class="glyphicon glyphicon-triangle-bottom"></span>
                </button>
                `;

			/*st.row.add([steps[i].vulnId, $('<div/>').text(
					replaceHTMLChars(
							atob(
									decodeURIComponent(steps[i].name)
									)
				   )
			  ).html(), steps[i].order, up + dn, edit, del]).draw( false );*/
			st.row.add([steps[i].order, decodeRow(steps[i].name, steps[i].stepId), steps[i].vulnId, up + dn, edit, del]).draw(false);
		}
		hookSteps(id);
	});


}
function decodeRow(data, stepid) {
	var decoded = b64DecodeUnicode(data);
	descriptions[stepid] = decoded;

	decoded = $("<div>" + decoded + "</div>").text();

	var el = "<div id='desc" + stepid + "' style='width:500px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" + $('<div/>').text(decoded).html() + "<div>";
	return el;
}
function AddDetail(el, vulnid) {
	var vulnName = $($(el).parent().parent().children()[2]).html();
	editors.stepDescription.setContents("");
	$('#stepModal').modal('show');

	$("#stepTitle").html("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[ " + vulnName + " ]");
	//clearStepForm();
	$("#stepVulnId").val(vulnid);
	$("#stepAction").val("add");


}
function hookSteps(vulnid) {
	$("[id^=editStep]").unbind();
	$("[id^=editStep]").click(function() {
		//var hasImage=$(this).attr('hasImage');
		var stepId = $(this).attr("id").replace("editStep", "");
		var vulnId = $($(this).parent().parent().children()[2]).html();
		var vulnName = $($("td:contains('" + vulnId + "')").parent().children()[3]).html();
		$('#details').modal('show');
		$("#stepTitle").html("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[ " + vulnName + " ]");
		//clearStepForm();
		$("#stepVulnId").val(vulnId);
		$("#stepAction").val("edit");
		$("#stepId").val(stepId);


		editors.stepDescription.setContents(descriptions[stepId]);

	});
	hookupdn();
	hookdel();
}
function hookupdn() {
	$("[id^=upStep]").unbind();
	$("[id^=dnStep]").unbind();
	$("[id^=upStep]").click(function() {
		var stepId = $(this).attr("id").replace("upStep", "").split("|")[0];
		var vulnId = $(this).attr("id").replace("upStep", "").split("|")[1];
		var index = parseInt($(this).attr("index"));
		var qs = "vulnid=" + vulnId + "&stepId=" + stepId;
		qs += "&_token=" + _token;
		$.post("OrderUp", qs).done(function(resp) {
			var isError = getData(resp);
			if (isError != "error") {

				moveUp(index);
				hookSteps(vulnId);
			}
		});



	});
	$("[id^=dnStep]").click(function() {
		var stepId = $(this).attr("id").replace("dnStep", "").split("|")[0];
		var vulnId = $(this).attr("id").replace("dnStep", "").split("|")[1];
		var index = parseInt($(this).attr("index"));
		var qs = "vulnid=" + vulnId + "&stepId=" + stepId;
		qs += "&_token=" + _token;
		$.post("OrderDn", qs).done(function(resp) {
			var isError = getData(resp);
			if (isError != "error") {
				moveDn(index);
				hookSteps(vulnId);
			}
		});

	});
}
function hookdel() {
	$("[id^=deleteStep]").unbind();
	$("[id^=deleteStep]").click(function() {
		var stepId = $(this).attr("id").replace("deleteStep", "").split("|")[0];
		var vulnId = $(this).attr("id").replace("deleteStep", "").split("|")[1];
		var index = parseInt($(this).attr("index"));
		$.confirm({
			title: "Are your sure?",
			type: "red",
			content: "Are you sure you want to delete this step?",
			buttons: {
				"Yes, I'm sure": function() {
					var qs = "vulnid=" + vulnId + "&stepId=" + stepId + "&action=delete";
					qs += "&_token=" + _token;
					$.post("AddStep", qs).done(function(resp) {
						var isError = getData(resp);
						if (isError != "error") {
							delStep(index, vulnId);
							hookSteps(vulnId);
						}
					});
				},
				cancel: function() { return 0; }
			}
		});


	});

}
function clearStepForm() {
	editors.stepDescription.setContents("");
	$("#stepVulnId").val("");
	$("#stepAction").val("");
	$("#stepId").val("");
	$('#image').fileinput('destroy');
	$('#image').val("");

}
$("#saveStep, #saveStep2").click(function( event ) {
	var step = getEditorText("stepDescription");
	var div = $("<div/>").html(step);
	console.log(div.html());
	div.find("pre").each(function(i, el) {
		var newHTML = $(el).html().replace(/\n/g, "<br/>");
		$(el).html(newHTML);
	});
	var data = "action=" + $("#stepAction").val();
	data += "&editor6=" + encodeURIComponent(div.html());
	data += "&stepId=" + $("#stepId").val();
	data += "&vulnid=" + $("#stepVulnId").val();
	data += "&_token=" + _token;
	$.post("AddStep", data).done(function(resp) {
		_token = resp.token;
		var isError = getData(resp);
		if (isError != "error") {
			ShowSteps(-1, $("#stepVulnId").val());
			$('#stepModal').modal('hide');
			$("#detailCbx"+$("#stepVulnId").val()).attr("checked","true");
			if(event.target.id =="saveStep2"){
				let vulnid = $("#stepVulnId").val();
				AddDetail($("#svulnID"+vulnid),vulnid);
			}
		}
	});

	//$("#stepForm").submit();

});
function delStep(index, vulnid) {
	var datatable = $('#stepstable.table').dataTable();
	var data = datatable.fnGetData();
	var size = data.length;
	datatable.fnClearTable();
	if (index + 1 == size) {
		data.splice(index, 1);
	} else {
		for (var i = index + 1; i < size; i++) {

			data[i][0] = data[i][0] - 1;
			data[i][3] = data[i][3].replace(/index="[0-9]{1,3}"/g, "index=\"" + (i - 1) + "\"");
			data[i][5] = data[i][5].replace(/index="[0-9]{1,3}"/g, "index=\"" + (i - 1) + "\"");
			data[i - 1] = data[i];

		}
		data.splice(size - 1, 1);
	}
	if (data.length != 0)
		datatable.fnAddData(data);
	else{
			$("#detailCbx"+vulnid).removeAttr("checked");
	}


}
function moveUp(index) {

	if ((index - 1) >= 0) {
		var datatable = $('#stepstable.table').dataTable();
		var data = datatable.fnGetData();
		datatable.fnClearTable();
		data[index][0] = data[index][0] - 1;
		data[index - 1][0] = data[index - 1][0] + 1;
		data[index][3] = data[index][3].replace(/index="[0-9]{1,3}"/g, "index=\"" + (index - 1) + "\"");
		data[index][5] = data[index][5].replace(/index="[0-9]{1,3}"/g, "index=\"" + (index - 1) + "\"");
		data[index - 1][3] = data[index - 1][3].replace(/index="[0-9]{1,3}"/g, "index=\"" + index + "\"");
		data[index - 1][5] = data[index - 1][5].replace(/index="[0-9]{1,3}"/g, "index=\"" + index + "\"");
		var hold = data[index - 1];
		data[index - 1] = data[index];
		data[index] = hold;
		console.log("THis might not be working");
		datatable.fnAddData(data);

	}
}

function moveDn(index) {

	if ((index + 1) >= 0) {
		var datatable = $('#stepstable.table').dataTable();
		var data = datatable.fnGetData();

		if (index + 1 < data.length) {
			datatable.fnClearTable();
			data[index][0] = data[index][0] + 1;
			data[index + 1][0] = data[index + 1][0] - 1;
			data[index][3] = data[index][3].replace(/index="[0-9]{1,3}"/g, "index=\"" + (index + 1) + "\"");
			data[index][5] = data[index][5].replace(/index="[0-9]{1,3}"/g, "index=\"" + (index + 1) + "\"");
			data[index + 1][3] = data[index + 1][3].replace(/index="[0-9]{1,3}"/g, "index=\"" + (index) + "\"");
			data[index + 1][5] = data[index + 1][5].replace(/index="[0-9]{1,3}"/g, "index=\"" + index + "\"");
			var hold = data[index + 1];
			data[index + 1] = data[index];
			data[index] = hold;
			console.log("This might not be working");
			datatable.fnAddData(data);
		}

	}
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

	$("#uploadVulns").click(function() {
		$.confirm({
			type: "green",
			title: 'Upload a XML Report',
			columnClass: 'large',
			content: "URL:ReportUploadView?id=" + assesssmentId,
			buttons: { cancel: function() { this.close(); return 0; } }
		});


	});
});
