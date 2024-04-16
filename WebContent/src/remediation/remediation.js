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
import 'bootstrap'
import 'jquery-ui';
import 'jquery-confirm';
import 'select2';
import * as moment from 'moment';
import 'bootstrap-daterangepicker';
import '../scripts/jquery.autocomplete.min';
import { marked } from 'marked';

let editorOptions = {
	codeMirror: CodeMirror,
	plugins: plugins,
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock', 'textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	height: 500
};
let editors = {
	notes: suneditor.create("notes", editorOptions),
	remNotes: suneditor.create("RemNotes", editorOptions),
	chSevNotes: suneditor.create("chSevNotes", editorOptions),
	nprodNotes: suneditor.create("nprodNotes", editorOptions),
	prodNotes: suneditor.create("prodNotes", editorOptions)
}
let calendar = null;
global.genCal = function genCal() {
	let cal = new FullCalendar.Calendar(document.getElementById("calendar"), {
		header: {
			left: 'prev,next today',
			center: 'title',
			right: 'month,agendaWeek,agendaDay'
		},
		buttonText: {
			today: 'today',
			month: 'month',
			week: 'week',
			day: 'day'
		}, editable: true,
		droppable: true, // this allows things to be dropped onto the calendar !!!
		eventDrop: function(_event, delta) { // this function is called when something is dropped
			console.log("dropped");
			thedelta = delta;
			let range = $("#reservation").val();
			let start = new Date(range.split(" to ")[0]);
			let end = new Date(range.split(" to ")[1]);
			start.setDate(start.getDate() + delta.asDays());
			end.setDate(end.getDate() + delta.asDays());
			let startStr = (start.getMonth() + 1) + "/" + start.getDate() + "/" + start.getFullYear();
			let endStr = (end.getMonth() + 1) + "/" + end.getDate() + "/" + end.getFullYear();
			$("#reservation").val(startStr + " to " + endStr);

		},
		eventResize: function(event, _jsEvent, _ui, _view) {
			thedelta = event;
			let range = $("#reservation").val();
			let start = new Date(range.split(" to ")[0]);
			let startStr = (start.getMonth() + 1) + "/" + start.getDate() + "/" + start.getFullYear();

			let endStr = (event.end.month() + 1) + "/" + (event.end.date() - 1) + "/" + event.end.year();
			$("#reservation").val(startStr + " to " + endStr);
		}

	});
	cal.render();
	return cal;
}
$(function() {
	if (vulnName != "") {
		setTimeout(function() {
			$($("#vulntable").DataTable().rows().data()).each(function(a, b) {
				if (searchId != -1 && b[11].vid == searchId) {
					$($("#vulntable").find("tbody").find("tr")[a]).trigger("click");

				}
			});
		}, 1000);

	}
	$(".col-md-1").css("min-width", "100px").css("text-overflow", "ellipsis").css("white-space", "nowrap").css("overflow", "hidden");

	$(".select2").select2();
	$('#reservation').daterangepicker({
		"locale": {

			"format": "MM/DD/YYYY",
			"separator": " to ",
			"applyLabel": "Apply",
			"cancelLabel": "Cancel",
			"fromLabel": "From",
			"toLabel": "To",
			"customRangeLabel": "Custom",
			"weekLabel": "W",

			"firstDay": 1
		},
		"weekStart": 5,
		"showWeekNumbers": true,
		"startDate": new Date()

	});
	$('#openDateCal').daterangepicker({ singleDatePicker: true});


	$("#addVerification").click(function() {
		let range = $("#reservation").val();
		let start = range.split(" to ")[0];
		let end = range.split(" to ")[1];
		let vid = $('#vulntable').DataTable().rows('.selected').data()[0][11].vid;
		if (vid == -1 || typeof vid == 'undefined') {
			$.alert({ title: "Error", content: "You Must Select a Vulnerablity" });
			return;
		}
		if (aid == -1) {
			$.alert({ title: "Error", content: "Something wrong with the applicaiton you selected" });
			return
		}
		if (start.trim() == "" || end.trim() == "") {
			$.alert({ title: "Error", content: "Your start and end dates are wrong." });
			return;
		}
		if ($("#remUser").val() == -1) {
			$.alert({ title: "Error", content: "Your remediation contact is missing." });
			return;
		}
		if ($("#engUser").val() == -1) {
			$.alert({ title: "Error", content: "Your engagement contact is missing." });
			return;
		}
		if ($("#assessors").val() == -1) {
			$.alert({ title: "Error", content: "Your assessor is missing." });
			return;
		}
		let vids = "";
		let checkAID = $('#vulntable').DataTable().rows('.selected').data()[0][11].aid;
		console.log("AID:" + checkAID);
		let canContinue = true;
		$.each($('#vulntable').DataTable().rows('.selected').data(), function(index, obj) {
			console.log(obj);
			vids += "&vulnids[" + index + "]=" + obj[11].vid;
			console.log(checkAID);
			if (obj[11].aid != checkAID) {
				$.alert({ title: "Error", content: "All Verifications must have the same application id." });
				canContinue = false;
				return;
			}


		});
		console.log(canContinue)
		if (!canContinue) {
			return;
		}

		//showLoading(".content");
		let notes = editors["notes"].getContents();
		let data = "action=create";
		//data+="&vulnId="+vid;
		//data+="&vulnId="+vids;
		data += vids;
		data += "&asmtId=" + checkAID;
		data += "&start=" + start;
		data += "&end=" + end;
		data += "&remId=" + $("#remUser").val();
		data += "&assessorId=" + $("#assessors").val();
		data += "&distro=" + $("#distlist").val();
		data += "&notes=" + encodeURIComponent(notes);
		console.log(data);
		$.post("Remediation", data).done(function(resp) {
			clearLoading(".content");
			if (resp.result == "success")
				$.alert({ type: "green", title: "Success!", content: "Verification has been assigned." });

			else {
				$.alert({ type: "red", title: "Error!", content: resp.message });
			}

		});
	});

	$('#reservation').change(function() {
		calendar.removeAllEvents();
		let range = $("#reservation").val();
		let sdate = range.split(" to ")[0];
		let edate = range.split(" to ")[1];
		let data = "sdate=" + sdate;
		data += "&edate=" + edate;
		data += "&action=dateSearch";
		//$('#calendar').fullCalendar('removeEvents', -1);
		//$('#calendar').fullCalendar('gotoDate', sdate);
		calendar.gotoDate(new Date(sdate));

		$.post("Remediation", data).done(function(resp) {
			console.log(resp);
			$("#assessors").html("");
			$("#assessors").append("<option class='asopt' value='-1'> - </option>");
			let ocText = "";
			for (let N in resp.users) {
				console.log(N);
				let d = resp.users[N];
				if (d.ocount != 0)
					ocText = " <span>[ OOO ]</span>";
				else {
					ocText = " <span>[ " + d.count + " Verifications ]</span>";
					if (d.count <= 0)
						ocText = " <span style='color:green'>[ Open ]</span>"
				}


				$("#assessors").append("<option class='asopt' value='" + d.id + "'>" + d.name + " - " + d.team + ocText + "</option>");
			}
			$("#assessors").select2('val', '-1');

		});

	});
	$("#assessors").change(function() {
		//$('#calendar').fullCalendar('removeEvents');
		calendar.removeAllEvents();
		//TODO:get assessments first and put on calendar


		let appid = $('#vulntable').DataTable().rows('.selected').data()[0][11].appId;
		let appname = $('#vulntable').DataTable().rows('.selected').data()[0][11].name;
		let originalEventObject = $(this).data('eventObject');
		let copiedEventObject = $.extend({}, originalEventObject);
		copiedEventObject.allDay = true;
		copiedEventObject.title = appid + " - " + appname;
		copiedEventObject.start = new Date(($("#reservation").val().split("to")[0].trim()));
		copiedEventObject.color = edit_color;
		copiedEventObject.id = "-1";
		let endTmp = $("#reservation").val().split(" to ")[1];
		let end = new Date(endTmp);
		end = end.setDate(end.getDate() + 1);
		copiedEventObject.end = end;
		//$('#calendar').fullCalendar('renderEvent', copiedEventObject, true);
		calendar.addEvent(copiedEventObject, true);
		$.post('../service/getVerifications', 'id=' + $(this).val()).done(function(adata) {
			//console.log(adata);
			let json = JSON.parse(adata);
			console.log(json);
			let N = json.count;
			for (let i = 0; i < N; i++) {
				let s = json.verifications[i][2];
				let e = json.verifications[i][4];
				let t = json.verifications[i][1] + " - " + json.verifications[i][0] + " - " + json.verifications[i][5];
				let aaid = json.verifications[i][3].replace('app');

				if (s != 'null' && e != 'null') {
					console.log(s + " " + e);
					let originalEventObject = $(this).data('eventObject');
					let copiedEventObject = $.extend({}, originalEventObject);
					copiedEventObject.allDay = true;
					copiedEventObject.title = t;
					copiedEventObject.start = new Date(s);
					copiedEventObject.color = ver_color;
					copiedEventObject.id = aaid;
					let end = new Date(e);
					end = end.setDate(end.getDate() + 1);

					copiedEventObject.end = end;
					copiedEventObject.editable = false;
					//$('#calendar').fullCalendar('renderEvent', copiedEventObject, true);
					calendar.addEvent(copiedEventObject, true);

				}
			}
			N = json.ocount;
			for (let i = 0; i < N; i++) {
				let s = json.ooo[i][2];
				let e = json.ooo[i][3];
				let t = json.ooo[i][0];
				let oid = "ooo" + json.ooo[i][0];
				if (s != 'null' && e != 'null') {
					let originalEventObject = $(this).data('eventObject');
					let copiedEventObject = $.extend({}, originalEventObject);
					copiedEventObject.allDay = true;
					copiedEventObject.title = t;
					copiedEventObject.start = new Date(s);
					copiedEventObject.color = ooo_color;
					copiedEventObject.id = oid;
					let end = new Date(e);
					end = end.setDate(end.getDate() + 1);

					copiedEventObject.end = end;
					copiedEventObject.editable = false;
					//$('#calendar').fullCalendar('renderEvent', copiedEventObject, true);
					calendar.addEvent(copiedEventObject, true);

				}
			}
		});
	});

});

let vid = -1;
let aid = -1;
let vName = "";
let appId = "";
$(function() {

	$('#vulntable').dataTable({
		"destroy": true,

		"paging": true,
		"pageLength": 15,
		"lengthChange": false,
		"searching": false,
		"ordering": true,
		"info": true,
		"autoWidth": true,
		"order": [[4, "desc"]]


	});

	$('#vulntable').DataTable().on('draw', function() {
		clearLoading(".content");
		clearLoading(".content");
	});

	function updateCheckbox(theParent, isChecked) {
		let checkbox = $(theParent).children()[0].firstChild
		let html = checkbox.outerHTML;
		checkbox.remove();
		let wrapper = document.createElement('div');
		wrapper.innerHTML = html;
		let newCheckbox = wrapper.firstChild;
		if (isChecked) newCheckbox.setAttribute("checked", true);
		else newCheckbox.removeAttribute("checked");
		$(theParent).children()[0].append(newCheckbox);


	}

	$("#search, #appid, #appname, #tracking").bind("click keypress", function(evt) {

		if (evt.currentTarget.type == "text" && evt.type == "click")
			return;
		if (evt.type == "keypress" && evt.which != 13)
			return;
		//showLoading(".content");
		console.log(evt);

		$("#controls").hide();
		let qs = "OpenVulns?action=get&appId=" + $("#appid").val() + "&appname=" + $("#appname").val() + "&tracking=" + $("#tracking").val();
		for (let riskId of levels) {
			if ($(`#levelbx${riskId}`).is(":checked"))
				qs += `&risk%5B${riskId}%5D=true`
			else
				qs += `&risk%5B${riskId}%5D=false`

		}
		if ($("#closedcbx").is(":checked")) {
			qs += "&closed=true"
		}
		if ($("#opencbx").is(":checked")) {
			qs += "&open=true"
		}

		$('#vulntable').dataTable({
			"destroy": true,

			"paging": true,
			"pageLength": 15,
			"lengthChange": false,
			"searching": false,
			"ordering": true,
			"info": true,
			"autoWidth": true,
			"order": [[4, "desc"]],
			serverSide: true,
			ajax: {
				"url": qs,
				"type": "POST"
			},
			columnDefs: [
				{
					targets: [1, 2],
					render: function(data, type, row) {
						return data.substr(0, 30) + "...";
					}
				},
				{
					targets: [6],
					render: function(data, type, row) {
						return updateColor(data);
					}
					
				}
			]

		}
		);



	});


	$('#vulntable tbody').on('click', 'tr', function(event) {

		let data = $('#vulntable').DataTable().row(this).data();
		let isver = data[11].isVer
		if (isver) {
			if (!$(this).hasClass('selected')) {
				$.alert({
					type: "orange",
					title: "Warning",
					content: "Unable to schedule a verificaiton for items out for verification."
				});
			}
		}
		if ($('#vulntable').DataTable().rows('.selected').data().length > 0) {

			let prevAID = $('#vulntable').DataTable().rows('.selected').data()[0][11].aid;

			let curAID = data[11].aid;
			if (prevAID != curAID) {
				$.alert({ title: "Error", content: "You cannot select a vulnerability from a different application assessment." });
				return;
			} else {

				$(this).toggleClass('selected');
				updateSelects();
				if ($(this).hasClass('selected')) {
					console.log("selected")
					updateCheckbox(this, true);
					//let cb = $(this).find("input[type='checkbox']");
					//$(cb).attr("checked", "true");
				} else {
					updateCheckbox(this, false);
					console.log("un-selected");
					//let cb = $(this).find("input[type='checkbox']");
					//$(cb).removeAttr("checked");
				}
			}
		} else {
			$(this).toggleClass('selected');
			if ($(this).hasClass('selected')) {
				console.log("selected")
				updateCheckbox(this, true);
				//let cb = $(this).find("input[type='checkbox']");
				//$(cb).attr("checked", "true");
			} else {
				updateCheckbox(this, false);
				console.log("un-selected");
				//let cb = $(this).find("input[type='checkbox']");
				//$(cb).removeAttr("checked");
			}
			updateSelects();
			//refreshNotes();
		}
		if ($('#vulntable').DataTable().rows('.selected').data().length == 0) {
			//$('#calendar').fullCalendar('removeEvents');
			calendar.removeAllEvents();
			$("#reservation").val("");
			$("#controls").hide();
			return;
		} else if ($('#vulntable').DataTable().rows('.selected').data().length == 1 && $("#controls").css("display") == "none") {
			$("#controls").show();
			calendar = genCal();
			//$("#appid1").val(data[0]);
			//$("#appname1").val(data[1]);
			$("#distlist").val(data[11].dist);
			vid = data[11].vid;
			aid = data[11].aid;
			vName = data[11].name;
			appId = data[11].appId;

			//$("#files").fileinput('destroy');
			//getFiles();	
			editors["notes"].setContents($("<div />").html(data[11].notes).text());
		}
		return false;
	});
});
//notes
function downloadFile(id) {
	$("#dl-" + id).click(function() {
		console.log("clicked");
		let id = $(this).attr("id").replace("dl-", "");
		document.getElementById('dlFrame').src = "../service/fileUpload?id=" + id;

	});
}

//actions
$(function() {
	$("#chStart").click(function() {
		let dtTmp = getSelectedStartDate();
		let cell = dtTmp.split("<")[0];
		let vdate = cell;
		$("#openDateCal").val(vdate);
		$("#changeDateModal").modal('show');

	});

	$("#chSev").click(function() {
		let sevMap = getSelectedSeverity();
		$("#sevModal").modal('show');
		$("#newSev").select2("val", `${sevMap.overall}`);
		$("#newImpact").select2("val", `${sevMap.impact}`);
		$("#newLike").select2("val", `${sevMap.likelyhood}`);
		$("#vulnName").html("<b>" + getSelectedAppName() + " - " + getSelectedVulnName() + "</b>");


	});
	$("#closeDev").click(function() {
		$("#nprodModal").modal('show');
	});
	$("#closeProd").click(function() {
		$("#prodModal").modal('show');
	});
	$("#noteSave").click(function() {
		let newNote = editors["remNotes"].getContents();
		let data = "action=insertNote";
		data += "&note=" + newNote;
		data += "&vulnId=" + $("#vuln_note_select").val();
		$.post("RemVulnData", data).done(function() {
			refreshNotes($("#vuln_note_select").val());
		});

	});
	$("#saveSev").click(function() {
		let newNote = editors["chSevNotes"].getContents();
		let data = "action=changeSev";
		data += "&note=" + newNote;
		data += "&vulnId=" + $("#vuln_note_select").val();
		data += "&severity=" + $("#newSev").val();
		data += "&impact=" + $("#newImpact").val();
		data += "&likelyhood=" + $("#newLike").val();
		$.post("RemVulnData", data).done(function() {
			refreshNotes($("#vuln_note_select").val());
			updateSelectedSeverity($("#newSev").val(), $("#newLike").val(), $("#newImpact").val())
			$("#sevModal").modal('hide');

		});


	});
	$("#saveNprod").click(function() {
		let newNote = editors['nprodNotes'].getContents();
		let data = "action=closeInDev";
		data += "&note=" + newNote;
		data += "&vulnId=" + $("#vuln_note_select").val();
		$.post("RemVulnData", data).done(function() {
			refreshNotes($("#vuln_note_select").val());
			$("#nprodModal").modal('hide');

		});
	});
	$("#saveProd").click(function() {
		let newNote = editors['prodNotes'].getContents();
		let data = "action=closeInProd";
		data += "&note=" + newNote;
		data += "&vulnId=" + $("#vuln_note_select").val();
		$.post("RemVulnData", data).done(function() {
			refreshNotes($("#vuln_note_select").val());
			$("#prodModal").modal('hide');

		});
	});
});
function refreshNotes(thevid) {
	$.get("RemVulnData?action=getNotes&vulnId=" + thevid).done(function(data) {
		let notes = data.notes;
		$("#noteHistory").html("");
		editors['remNotes'].setContents("");
		notes.forEach(function(note) {
			let decodedNote = $("<div/>").html(note.note).text();
			console.log(decodedNote);
			$("#noteHistory").append("<b><i class='fa fa-clock-o'></i> " + note.date + " - <i>" + note.creator + "</i></b>");
			if (note.gid != "nodelete")
				$("#noteHistory").append("&nbsp;&nbsp;<a class='delete' href='" + note.gid + "'>delete</a>");
			$("#noteHistory").append("<br> <div style='padding-left:20px' >" + decodedNote + "</div>");
			$("#noteHistory").append("<br><hr>");

		});
		$(".delete").click(function(event) {
			event.preventDefault();
			let gid = $(this).attr("href");
			let data = "action=delete";
			data += "&gid=" + gid;
			$.post("RemVulnData", data).done(function() {
				refreshNotes($("#vuln_note_select").val());
			});

		});
	});


}
function getFiles(theid) {
	$("#files").fileinput('destroy');
	$.get("RemVulnData?action=getFiles&vulnId=" + theid).done(function(data) {
		$("#files").unbind();
		$("#files").fileinput(data);
		downloadFile(theid);
	});

}
let dtTmp = null;
$(function() {
	$("#saveOpen").click(function() {
		const newStartDate = $("#openDateCal").val();
		const vulnId = $("#vuln_note_select").val();
		let data = "action=updateOpenDate";
		data += `&start=${newStartDate}`;
		data += `&vulnId=${vulnId}`;
		$.post("Remediation", data).done(function(resp) {
			updateSelectedStartDate(newStartDate)
			$("#changeDateModal").modal('hide');
		});


	});
});
function updateSelectedStartDate(newStartDate) {
	let oldStartDate = getSelectedStartDate()
	oldStartDate = oldStartDate.split("<")[0]
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	for (let i = 0; i < rows.length; i++) {
		if (rows[i][11].vid == $("#vuln_note_select").val()) {
			rows[i][7] = rows[i][7].replace(oldStartDate,`${newStartDate} `);
			$('#vulntable').DataTable().rows().invalidate();
			break;
		}
	}
}

function getSelectedStartDate() {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	for (let i = 0; i < rows.length; i++) {
		if (rows[i][11].vid == $("#vuln_note_select").val()) {
			return rows[i][7];
		}
	}
}

function getSelectedVulnName() {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	for (let i = 0; i < rows.length; i++) {
		if (rows[i][11].vid == $("#vuln_note_select").val()) {
			return rows[i][11].name;//changed
		}
	}
}
function getSelectedSeverity() {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	for (let i = 0; i < rows.length; i++) {
		if (rows[i][11].vid == $("#vuln_note_select").val()) {
			return rows[i][11].severity;
		}
	}
}
function updateSelectedSeverity(overall, likelyhood, impact) {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	for (let i = 0; i < rows.length; i++) {
		if (rows[i][11].vid == $("#vuln_note_select").val()) {
			let severity = rows[i][11].severity;
			severity.overall = overall;
			severity.likelyhood = likelyhood;
			severity.impact = impact;
			return;

		}
	}
}
function getSelectedAppName() {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	for (let i = 0; i < rows.length; i++) {
		if (rows[i][11].vid == $("#vuln_note_select").val()) {
			return rows[i][11].appId + " - " + rows[i][11].name;
		}
	}
}
function updateSelects() {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	if (rows.length > 0) {
		$("#vuln_note_select").html("");
		$("#vuln_history_select").html("");
		$.each(rows, function(index, obj) {
			let opt = "<option value='" + obj[11].vid + "'>" + obj[11].name + "</option>";
			$("#vuln_note_select").append(opt);
			$("#vuln_history_select").append(opt);
		});
		$("#vuln_note_select").val(rows[0][11].vid).trigger('change.select2');
		$("#vuln_history_select").val(rows[0][11].vid).trigger('change.select2');
		refreshNotes(rows[0][11].vid);
		getFiles(rows[0][11].vid)
	}



}
$(function() {
	$("#vuln_note_select").change(function(event) {

		let theid = $(this).val();
		$("#vuln_history_select").val(theid).trigger('change.select2');
		refreshNotes(theid);
		getFiles(theid);


	});
	$("#vuln_history_select").change(function(event) {

		let theid = $(this).val();
		$("#vuln_note_select").val(theid).trigger('change.select2');
		refreshNotes(theid);
		getFiles(theid);


	});
});



