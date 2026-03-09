require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
require('daterangepicker/daterangepicker.css');
require('select2/dist/css/select2.min.css');
import Editor from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell'
import '@toast-ui/editor/dist/toastui-editor.css';
import 'tui-color-picker/dist/tui-color-picker.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
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
import 'daterangepicker';
import '../scripts/jquery.autocomplete.min';

function createEditor(id){
	return new Editor({
				el: document.querySelector(`#${id}`),
				previewStyle: 'vertical',
				height: 'auto',
				autofocus: false,
				plugins: [colorSyntax, tableMergedCell]
			});
}

let editors = {
	notes: createEditor("notes"), 
	remNotes: createEditor("RemNotes"),
	chSevNotes: createEditor("chSevNotes"),
	nprodNotes:createEditor("nprodNotes"),
	prodNotes: createEditor("prodNotes"),
	cancelVerNotes: createEditor("cancelVerNotes")
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
global.calendar = global.genCal();

function getSelectedVulnIds(){
	return Array.from($('[id^="ckl"]:checked').map( (x,y) => y.id.replace("ckl","")));
}
function getVuln(vid){
	//used for later
}

function updateForms(){
	const selected = $(".selected")
	vulnId = selected.data("vulnid");
	let verId = selected.data("verid")
	if(verId == ""){
		$("#closeVerControl").hide();
	}else{
		const start = selected.data("start");
		const end = selected.data("end");
		const assessor = selected.data("assessor");
		const remediation = selected.data("remediation");
		const distro = selected.data("distro");
		$("#reservation").val(`${start} to ${end}`);
		$("#remUser").val(remediation).change();
		$("#assessors").val(assessor).change();
		$("#distlist").val(distro);
		
		$("#closeVerControl").show();
		
		
	}
	const devclosed = selected.data("devclosed");
	const prodclosed = selected.data("prodclosed");
	if(devclosed != "" || prodclosed != ""){
		$("#reopenVulnControl").show();	
	}else{
		$("#reopenVulnControl").hide();	
		
	}
	refreshNotes(vulnId);
	getVuln(vulnId);
}

function setUpEventHandlers() {

	$("#vulntable tbody tr").on('click', function(event) {
		$(".selected").each((_a, s) => $(s).removeClass("selected"))
		$(this).addClass("selected");
		updateForms();
	});
}

$(function() {
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
	global.vulntable = $('#vulntable').dataTable({
			"paging": false,
			"lengthchange": false,
			"searching": true,
			language: { search: "" },
			"ordering": true,
			"info": false,
			"autowidth": false,
			"order": [[1, "desc"]],
			"columns": [
				{ width: "10px" }, //checkbox
				null, //name

			],
			columndefs: [
				{ orderable: false, targets: '_all' }
			]
		});
		$(window).on('resize', () => {
			global.vulntable.DataTable().columns.adjust();
		});
	setUpEventHandlers();
	refreshNotes(vulnId);
	getFiles(vulnId);
	updateForms();
	global.genCal = function genCal() {
	global.cal = new FullCalendar.Calendar(document.getElementById("calendar"), {
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
}
	
	
	$('#openDateCal').daterangepicker({ singleDatePicker: true});


	$("#addVerification").click(function() {
		let range = $("#reservation").val();
		let start = range.split(" to ")[0];
		let end = range.split(" to ")[1];
		let vid = vulnId 
		if (vid == -1 || typeof vid == 'undefined') {
			$.alert({ title: "Error", content: "You Must Select a Vulnerablity" });
			return;
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
		let vids = getSelectedVulnIds().map( (item, index) => {
			return "vulnids[" + (index) + "]=" + item;
		});
		console.log(vids);
		let notes = editors["notes"].getHTML();
		let data = "action=create";
		data += "&" + vids.join("&");
		data += "&asmtId=" + asmtId;
		data += "&start=" + start;
		data += "&end=" + end;
		data += "&remId=" + $("#remUser").val();
		data += "&assessorId=" + $("#assessors").val();
		data += "&distro=" + $("#distlist").val();
		data += "&notes=" + encodeURIComponent(notes);
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
		global.calendar.removeAllEvents();
		let range = $("#reservation").val();
		let sdate = range.split(" to ")[0];
		let edate = range.split(" to ")[1];
		let data = "sdate=" + sdate;
		data += "&edate=" + edate;
		data += "&action=dateSearch";
		global.calendar.gotoDate(new Date(sdate));

		$.post("Remediation", data).done(function(resp) {
			$("#assessors").html("");
			$("#assessors").append("<option class='asopt' value='-1'> - </option>");
			let ocText = "";
			for (let N in resp.users) {
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
		global.calendar.removeAllEvents();

		let originalEventObject = $(this).data('eventObject');
		let copiedEventObject = $.extend({}, originalEventObject);
		copiedEventObject.allDay = true;
		copiedEventObject.title = appId + " - " + appName;
		let start = new Date($("#reservation").val().split(" to ")[0].trim());
		copiedEventObject.start = start;
		copiedEventObject.color = edit_color;
		copiedEventObject.id = "-1";
		let end = new Date($("#reservation").val().split(" to ")[1].trim());
		end = new Date(end.setDate(end.getDate() + 1));
		copiedEventObject.end = end;
		global.calendar.addEvent(copiedEventObject, true);
		let userid = $(this).val();
		if(userid == null){
			userid=-1;
		}
		//increase range
		start = new Date(start.setDate(start.getDate() - 30));
		end = new Date(end.setDate(end.getDate() + 30));
		
		$.post('Calendar', `userid=${userid}&team=-1&start=${start.toLocaleDateString("en-US")}&end=${end.toLocaleDateString("en-US")}&action=search`).done(function(json) {
			for (let verification of json.verifications) {
				let s = verification.start;
				let e = verification.end;
				let t = verification.appid + " - " + verification.appname + " - " + verification.vuln;
				let status = verification.status;
				let aaid = verification.appid;

				if (s != 'null' && e != 'null' && status == "In Queue") {
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
					global.calendar.addEvent(copiedEventObject, true);

				}
			}
			for (let ooo of json.ooo) {
				let s = ooo.start;
				let e = ooo.end;
				let t = ooo.title;
				let oid = "ooo" + ooo.id;
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
					global.calendar.addEvent(copiedEventObject, true);

				}
			}
		});
	});

});


function downloadFile(id) {
	$("#dl-" + id).click(function() {
		let id = $(this).attr("id").replace("dl-", "");
		document.getElementById('dlFrame').src = "../service/fileUpload?id=" + id;

	});
}

function getSelectedSeverity(){
	return {
		overall: $(".selected").data("overall"),
		impact: $(".selected").data("impact"),
		likelyhood: $(".selected").data("likelyhood")
	}
	
}
//actions
$(function() {
	$(".downloadReport").on('click', function(event){
		const guid = $(this).data("guid");
		window.open(`DownloadReport?guid=${guid}`, "_blank");
	});
	let checkStatus = {};
	$(".genReport").click(function() {
		$("#retestRow").html("<td colspan='4'><div class='throbber-loader'>Loading…</div></td>");
		$.get("GenReport?retest=true&aid=" + asmtId, function(resp) {
			global._token = resp.token;
			clearInterval(checkStatus);
			checkStatus = setInterval(function() {
				$.get(`CheckStatus?aid=${asmtId}&retest=true`).done(function(resp, _message, http) {
					if (http.status != 202) {
						const updatedDate = resp.message;
						$("#updatedDate").html(updatedDate);
						clearInterval(checkStatus);
						window.location.href="#actions";
						window.location.reload();
					}
				});
			}, 2000);


		});
	});
	$("#chStart").click(function() {
		let dtTmp = $(".selected").data("opened");
		let cell = dtTmp.split("<")[0];
		let vdate = cell;
		$("#openDateCal").val(vdate);
		$("#changeDateModal").modal('show');

	});
	$("#reOpen").click(function() {
		let vulnName =$(".selected").data("vulnname");
		let vid = $(".selected").data("vulnid");
		let data = `vulnId=${vid}`;
		$.confirm({
			title: "Reopen vulnerability?",
			content: `Are you sure you want to reopen the vulnerability ${vulnName}`,
			buttons: {
				"reopen": ()=>{
					$.post("Reopen", data).done( (response) => {
						location.reload();
					});
				},
				"close": ()=>{}
			}
		})

	});
	$("#chSev").click(function() {
		let sevMap = getSelectedSeverity();
		$("#sevModal").modal('show');
		$("#newSev").select2("val", `${sevMap.overall}`);
		$("#newImpact").select2("val", `${sevMap.impact}`);
		$("#newLike").select2("val", `${sevMap.likelyhood}`);
	});
	$("#closeDev").click(function() {
		$("#nprodModal").modal('show');
	});
	$("#closeProd").click(function() {
		$("#prodModal").modal('show');
	});
	$("#noteSave").click(function() {
		let newNote = encodeURIComponent(editors["remNotes"].getHTML());
		let data = "action=insertNote";
		data += "&note=" + newNote;
		data += "&vulnId=" + vulnId;
		$.post("RemVulnData", data).done(function() {
			refreshNotes(vulnId);
		});

	});
	$("#saveSev").click(function() {
		let newNote = encodeURIComponent(editors["chSevNotes"].getHTML());
		let data = "action=changeSev";
		data += "&note=" + newNote;
		data += "&vulnId=" + vulnId;
		data += "&severity=" + $("#newSev").val();
		data += "&impact=" + $("#newImpact").val();
		data += "&likelyhood=" + $("#newLike").val();
		$.post("RemVulnData", data).done(function() {
			refreshNotes(vulnId);
			updateSelectedSeverity($("#newSev").val(), $("#newLike").val(), $("#newImpact").val())
			$("#sevModal").modal('hide');

		});


	});
	$("#saveNprod").click(function() {
		let newNote = encodeURIComponent(editors['nprodNotes'].getHTML());
		let data = "action=closeInDev";
		data += "&note=" + newNote;
		data += "&vulnId=" + vulnId;
		$.post("RemVulnData", data).done(function() {
			refreshNotes(vulnId);
			$("#nprodModal").modal('hide');

		});
	});
	$("#saveProd").click(function() {
		let newNote = encodeURIComponent(editors['prodNotes'].getHTML());
		let data = "action=closeInProd";
		data += "&note=" + newNote;
		data += "&vulnId=" + vulnId; 
		$.post("RemVulnData", data).done(function() {
			refreshNotes(vulnId);
			$("#prodModal").modal('hide');

		});
	});
	
	$("#closeVer").click(function() {
		editors.cancelVerNotes.reset()
		$("#closeVerModal").modal('show');

	});
	
	$("#closeVerBtn").click(function() {
		let newNote = editors.cancelVerNotes.getHTML();
		let verId = $(".selected").data("verid");
		let data = "action=closeVerification";
		data += `&note=${encodeURIComponent(newNote)}`;
		data += `&vulnId=${vulnId}`;
		data += `&verId=${verId}`;
		$.post("RemVulnData", data).done(function() {
			refreshNotes(vulnId);
			$("#closeVerModal").modal('hide');
			document.location.reload()

		});

	});
});
function refreshNotes(thevid) {
	let verId = $(".selected").data("verid");
	if(verId == ""){
		verId = -1
	}
	$.get("RemVulnData?action=getNotes&vulnId=" + thevid+"&verId="+verId).done(function(data) {
		let notes = data.notes;
		$("#noteHistory").html("");
		editors['remNotes'].hide();
		editors['remNotes'].setHTML("", false);
		editors['remNotes'].show(false);
		notes.forEach(function(note) {
			let decodedNote = $("<div/>").html(note.note).text();
			$("#noteHistory").append("<b><i class='fa fa-clock-o'></i> " + note.date + " - <i>" + note.creator + "</i></b>");
			if (note.gid != "nodelete")
				$("#noteHistory").append("&nbsp;&nbsp;<a class='delete' href='" + note.gid + "'>delete</a>");
			$("#noteHistory").append("<br> <div style='padding-left:20px' >" + decodedNote + "</div>");
			$("#noteHistory").append("<br><hr>");

		});
		if(notes.length == 0){
			$("#noteHistory").html("<i>No Comments</i>");
		}
		$(".delete").click(function(event) {
			event.preventDefault();
			let gid = $(this).attr("href");
			let data = "action=delete";
			data += "&gid=" + gid;
			$.post("RemVulnData", data).done(function() {
				refreshNotes(vulnId);
			});

		});
		let scope = data.scope;
			editors.notes.reset();	
			editors.notes.setHTML($("<div />").html(scope).text());
	});


}
function getFiles(vid) {
	$("#files").fileinput('destroy');
	$.get("RemVulnData?action=getFiles&vulnId=" + vid).done(function(data) {
		$("#files").unbind();
		$("#files").fileinput(data);
		downloadFile(vid);
	});

}
let dtTmp = null;
$(function() {
	$("#saveOpen").click(function() {
		const newStartDate = $("#openDateCal").val();
		let data = "action=updateOpenDate";
		data += `&start=${newStartDate}`;
		data += `&vulnId=${vulnId}`;
		$.post("Remediation", data).done(function(resp) {
			updateSelectedStartDate(newStartDate)
			$("#changeDateModal").modal('hide');
		});


	});
});