
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

$(function() {
	let editorOptions = {
		codeMirror: CodeMirror,
		plugins: plugins,
		buttonList: [
			['undo', 'redo', 'font', 'fontSize', 'formatBlock', 'textStyle'],
			['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
			['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
			['link', 'image', 'fullScreen', 'showBlocks', 'codeView' ],

		],
		defaultStyle: 'font-family: arial; font-size: 18px',
		height: 500
	};

	$(".select2").select2();
	$("#remUser").select2("val", defaultRemId);
	$("#assessors").select2("val", defaultAssessorId);

	let opt = `<option value='${defaultVulnId}'>${defaultVulnName}</option>`;
	$("#vuln_note_select").append(opt);
	$("#vuln_note_select").val(`${defaultVulnId}`).trigger('change.select2');

	$("#vuln_note_select").select2({ disabled: 'readonly' });

	let notes = suneditor.create("notes", editorOptions);
	let remNotes = suneditor.create("RemNotes", editorOptions);
	let chSevNotes = suneditor.create("chSevNotes", editorOptions);
	let nprodNotes = suneditor.create("nprodNotes", editorOptions);
	let prodNotes = suneditor.create("prodNotes", editorOptions);
	let verNotes = suneditor.create("verNotes", editorOptions);

	$("#files").fileinput({
		overwriteInitial: false,
		uploadUrl: "../service/fileUpload?category=verification&vid=" + defaultVulnId,
		uploadAsync: true,
		minFileCount: 0,
		maxFileCount: 5,
		allowedFileExtensions: ['jpg', 'gif', 'png', 'pdf', 'doc', 'xls', 'xlsx', 'docx', 'txt', 'csv', 'bmp', 'jpeg', 'xml', 'zip', 'rar', 'tar', 'gzip', 'tar.gz'],
		initialPreview: filesPreview,
		initialPreviewConfig: previewConfig
	}).on("filebatchselected", function(event, files) {
    	$("#files").fileinput("upload");
	});

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

	let calendar = new FullCalendar.Calendar(document.getElementById("calendar"), {
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
		eventDrop: function(event, delta) { // this function is called when something is dropped
			thedelta = delta;
			range = $("#reservation").val();
			start = new Date(range.split("  to ")[0]);
			end = new Date(range.split(" to ")[1]);
			start.setDate(start.getDate() + delta.asDays());
			end.setDate(end.getDate() + delta.asDays());
			startStr = (start.getMonth() + 1) + "/" + start.getDate() + "/" + start.getFullYear();
			endStr = (end.getMonth() + 1) + "/" + end.getDate() + "/" + end.getFullYear();
			$("#reservation").val(startStr + " - " + endStr);

		},
		eventResize: function(event, jsEvent, ui, view) {
			thedelta = event;
			range = $("#reservation").val();
			start = new Date(range.split("  to ")[0]);
			startStr = (start.getMonth() + 1) + "/" + start.getDate() + "/" + start.getFullYear();

			endStr = (event.end.month() + 1) + "/" + (event.end.date() - 1) + "/" + event.end.year();
			$("#reservation").val(startStr + " to " + endStr);
		}

	});
	calendar.render();
	let TMPresVal = $("#reservation").val();

	if (TMPresVal != "") {
		let one = TMPresVal.split(" to ")[0];
		calendar.gotoDate(new Date(one));
	}


	$("#addVerification").click(function() {
		let range = $("#reservation").val();
		let start = range.split(" to ")[0];
		let end = range.split(" to ")[1];
		if (defaultSearchId == -1) {
			alert("You Must Select a Vulnerablity");
			return;
		}
		if (start.trim() == "" || end.trim() == "") {
			alert("Your start and end dates are wrong.");
			return;
		}
		if ($("#remUser").val() == -1) {
			alert("Your remediation contact is missing.");
			return;
		}
		if ($("#engUser").val() == -1) {
			alert("Your engagement contact is missing.");
			return;
		}
		if ($("#assessors").val() == -1) {
			alert("Your assessor is missing.");
			return;
		}

		let noteData = notes.getContents();
		let data = "action=update";
		data += `&verId=${defaultSearchId}`;
		data += "&start=" + start;
		data += "&end=" + end;
		data += "&remId=" + $("#remUser").val();
		data += "&assessorId=" + $("#assessors").val();
		data += "&distro=" + $("#distlist").val();
		data += "&notes=" + encodeURIComponent(noteData);
		$.post("Remediation", data).done(function() {
			$.alert({ title: "Success", content: "Verificaiton Updated Successfully." })

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
		calendar.removeAllEvents();
		calendar.gotoDate(new Date(sdate));
		$.post("Remediation", data).done(function(resp) {
			$("#assessors").html("");
			$("#assessors").append("<option class='asopt' value='-1'> - </option>");
			for (let N in resp.users) {
				let d = resp.users[N];
				let ocText = ""
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
		calendar.removeAllEvents();
		//TODO:get assessments first and put on calendar
		let originalEventObject = $(this).data('eventObject');
		let copiedEventObject = $.extend({}, originalEventObject);
		copiedEventObject.allDay = true;
		let originalTitle = `${defaultAppId} - ${defaultAppName} - ${defaultVulnName}`;
		copiedEventObject.title = originalTitle;
		copiedEventObject.start = new Date(($("#reservation").val().split("to")[0].trim()));
		//copiedEventObject.color="#993333";
		copiedEventObject.color = "#95a5a6";
		copiedEventObject.id = "-1";
		let endTmp = $("#reservation").val().split(" to ")[1];
		let end = new Date(endTmp);
		end = end.setDate(end.getDate() + 1);
		copiedEventObject.end = end;
		calendar.addEvent(copiedEventObject, true);
		let id = $(this).val();

		if (id == null || typeof id == 'undefined') {
			return;
		}

		$.post('../service/getVerifications', 'id=' + $(this).val()).done(function(adata) {
			let json = JSON.parse(adata);
			let N = json.count;
			for (let i = 0; i < N; i++) {
				let s = json.verifications[i][2];
				let e = json.verifications[i][4];
				let t = json.verifications[i][1] + " - " + json.verifications[i][0] + " - " + json.verifications[i][5];
				let aaid = json.verifications[i][3].replace('app');

				if (s != 'null' && e != 'null' && t != originalTitle) {
					let originalEventObject = $(this).data('eventObject');
					let copiedEventObject = $.extend({}, originalEventObject);
					copiedEventObject.allDay = true;
					copiedEventObject.title = t;
					copiedEventObject.start = new Date(s);
					copiedEventObject.color = "#339966";
					copiedEventObject.id = aaid;
					let end = new Date(e);
					end = end.setDate(end.getDate() + 1);

					copiedEventObject.end = end;

					copiedEventObject.editable = false;
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
					copiedEventObject.color = "#FF0099";
					copiedEventObject.id = oid;
					let end = new Date(e);
					end = end.setDate(end.getDate() + 1);

					copiedEventObject.end = end;
					copiedEventObject.editable = false;
					calendar.addEvent(copiedEventObject, true);

				}
			}
		});
	});
	function prepopulateCalendar() {
		calendar.removeAllEvents();

		//TODO:get assessments first and put on calendar
		let originalEventObject = $(this).data('eventObject');
		let copiedEventObject = $.extend({}, originalEventObject);
		copiedEventObject.allDay = true;
		let originalTitle = `${defaultAppId} - ${defaultAppName} - ${defaultVulnName}`;
		copiedEventObject.title = originalTitle;
		copiedEventObject.start = new Date(($("#reservation").val().split("to")[0].trim()));
		//copiedEventObject.color="#993333";
		copiedEventObject.color = "#95a5a6";
		copiedEventObject.id = "-1";
		let endTmp = $("#reservation").val().split(" to ")[1];
		let end = new Date(endTmp);
		end = end.setDate(end.getDate() + 1);
		copiedEventObject.end = end;
		calendar.addEvent(copiedEventObject, true);
		$.post('../service/getVerifications', `id=${defaultSearchId}`).done(function(adata) {
			let json = JSON.parse(adata);
			let N = json.count;
			for (let i = 0; i < N; i++) {
				let s = json.verifications[i][2];
				let e = json.verifications[i][4];
				let t = json.verifications[i][1] + " - " + json.verifications[i][0] + " - " + json.verifications[i][5];
				let aaid = json.verifications[i][3].replace('app');

				if (s != 'null' && e != 'null' && t != originalTitle) {
					let originalEventObject = $(this).data('eventObject');
					let copiedEventObject = $.extend({}, originalEventObject);
					copiedEventObject.allDay = true;
					copiedEventObject.title = t;
					copiedEventObject.start = new Date(s);
					copiedEventObject.color = "#339966";
					copiedEventObject.id = aaid;
					let end = new Date(e);
					end = end.setDate(end.getDate() + 1);

					copiedEventObject.end = end;
					copiedEventObject.editable = false;
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
					copiedEventObject.color = "#FF0099";
					copiedEventObject.id = oid;
					let end = new Date(e);
					end = end.setDate(end.getDate() + 1);

					copiedEventObject.end = end;
					copiedEventObject.editable = false;
					calendar.addEvent(copiedEventObject, true);

				}
			}
		});
	}
	prepopulateCalendar();
	/*
		 vid="<s:property value="vulnId"/>";
		 vName = "";
		 appId="<s:property value="appId"/>";
		 verId="<s:property value="searchId"/>"; */
	$('#vulntable').DataTable();


	$("#search").click(function() {
		$("#controls").hide();
		let qs = "OpenVulns?action=get&appId=" + $("#appid").val() + "&appname=" + $("#appname").val()
		if ($("#critcbx").is(":checked")) {
			qs += "&crit=true"
		}
		if ($("#highcbx").is(":checked")) {
			qs += "&high=true"
		}
		if ($("#medcbx").is(":checked")) {
			qs += "&med=true"
		}
		if ($("#lowcbx").is(":checked")) {
			qs += "&low=true"
		}
		if ($("#reccbx").is(":checked")) {
			qs += "&rec=true"
		}
		if ($("#infocbx").is(":checked")) {
			qs += "&info=true"
		}
		$.get(qs).done(function(data) {
			$('#vulntable').DataTable().clear().draw();
			data.forEach(function(d) {
				$('#vulntable').DataTable().row.add(d).draw(false);
			});
		});
	});
	/* TODO: Can Remove if icheck is not required
		$('input[type="checkbox"].minimal, input[type="radio"].minimal').iCheck({
			checkboxClass: 'icheckbox_minimal-blue',
			radioClass: 'iradio_minimal-blue'
		});*/

	$('#vulntable tbody').on('click', 'tr', function() {
		if ($(this).hasClass('selected')) {
			$(this).removeClass('selected');
			$("#controls").hide();

		}
		else {

			let data = $('#vulntable').DataTable().row(this).data();

			if (typeof (data) == "undefined")
				return;
			$("#vulntable").DataTable().$('tr.selected').removeClass('selected');
			$(this).addClass('selected');
			$("#controls").show();
			$("#appid1").val(data[0]);
			$("#appname1").val(data[1]);
			$("#distlist").val(data[9].dist);
			defaultVulnId = data[9].vid;
			//let aid = data[9].aid;
			defaultVulnName = data[4];
			defaultAppId = data[0];
			//get notes
			refreshNotes();
			//set up files
			$("#files").unbind();
			$("#files").fileinput({
				overwriteInitial: false,
				uploadUrl: "../service/fileUpload?category=verification&vid=" + defaultVulnId,
				uploadAsync: true,
				minFileCount: 0,
				maxFileCount: 5,
				allowedFileExtensions: ['jpg', 'gif', 'png', 'pdf', 'doc', 'xls', 'xlsx', 'docx', 'txt', 'csv', 'bmp', 'jpeg', 'xml', 'zip', 'rar', 'tar', 'gzip', 'tar.gz'],

			});
			notes.setContents($("<div />").html(data[9].notes).text());
		}
	});
	/*** Notes section ***/
	//CKEDITOR.replace('RemNotes');
	$("[id^=dl-]").click(function() {
		let id = $(this).attr("id").replace("dl-", "");
		document.getElementById('dlFrame').src = "../service/fileUpload?category=verification&id=" + id;

	});


	/*** Actions  ***/
	$("#historyTable").DataTable();
	
	$("#chStart").click(function() {
		//vopen=$(this).attr("id");
		//vopen=vopen.replace("open","");
		dtTmp = $('#vulntable').DataTable().cell($(el).parent());
		let cell = dtTmp.data();
		cell = cell.split("<")[0];
		let vdate = cell;
		$("#openDateCal").val(vdate);
		$("#changeDateModal").modal('show');

	});

	$("#closeVer").click(function() {
		$("#closeVerModal").modal('show');

	});
	$("#chSev").click(function() {
		$("#sevModal").modal('show');
		$("#newSev").select2("val", defaultOverall);
		$("#newImpact").select2("val", defaultImpact);
		$("#newLike").select2("val", defaultLikelyhood);
		$("#vulnName").html(`<b>${defaultAppId} - ${defaultVulnName}</b>`);


	});
	$("#closeDev").click(function() {
		$("#nprodModal").modal('show');
		//CKEDITOR.replace("nprodNotes");
	});
	$("#closeProd").click(function() {
		$("#prodModal").modal('show');
		//CKEDITOR.replace("nprodNotes");
	});
	$("#noteSave").click(function() {
		let newNote = remNotes.getContents();
		let data = "action=insertNote";
		data += `&note=${newNote}`;
		data += `&vulnId=${defaultVulnId}`;
		$.post("RemVulnData", data).done(function() {
			refreshNotes();
		});

	});
	//change severity
	$("#saveSev").click(function() {
		let newNote = chSevNotes.getContents();
		let data = "action=changeSev";
		data += `&note=${newNote}`;
		data += `&vulnId=${defaultVulnId}`;
		data += "&severity=" + $("#newSev").val();
		data += "&impact=" + $("#newImpact").val();
		data += "&likelyhood=" + $("#newLike").val();
		$.post("RemVulnData", data).done(function() {
			refreshNotes();
			$("#sevModal").modal('hide');

		});


	});
	//save non-prod
	$("#saveNprod").click(function() {
		let newNote = nprodNotes.getContents();
		let data = "action=closeInDev";
		data += `&note=${newNote}`;
		data += `&vulnId=${defaultVulnId}`;
		data += `&verId=${defaultSearchId}`;
		$.post("RemVulnData", data).done(function() {
			refreshNotes();
			$("#nprodModal").modal('hide');

		});
	});
	//save in prod
	$("#saveProd").click(function() {
		let newNote = prodNotes.getContents();
		let data = "action=closeInProd";
		data += `&note=${newNote}`;
		data += `&vulnId=${defaultVulnId}`;
		data += `&verId=${defaultSearchId}`;
		$.post("RemVulnData", data).done(function() {
			refreshNotes();
			$("#prodModal").modal('hide');
			document.location = "RemediationQueue";

		});
	});
	//cancel the verification
	$("#closeVerBtn").click(function() {
		let newNote = verNotes.getContents();
		let data = "action=closeVerification";
		data += `&note=${newNote}`;
		data += `&vulnId=${defaultVulnId}`;
		data += `&verId=${defaultSearchId}`;
		$.post("RemVulnData", data).done(function() {
			refreshNotes();
			$("#closeVerModal").modal('hide');
			document.location = "RemediationQueue";

		});

	});
	refreshNotes();
	/*CKEDITOR.replace("nprodNotes");
	CKEDITOR.replace("prodNotes");
	CKEDITOR.replace("chSevNotes");
	CKEDITOR.replace("verNotes");*/

	function refreshNotes() {
		$.get(`RemVulnData?action=getNotes&vulnId=${defaultVulnId}`).done(function(data) {
			let notes = data.notes;
			$("#noteHistory").html("");
			remNotes.setContents("");
			notes.forEach(function(note) {
				let decodedNote = $("<div/>").html(note.note).text();
				$("#noteHistory").append(`<b><i class='fa fa-clock-o'></i>${note.date} - <i>${note.creator}</i></b> 
			<a class='delete' href='${note.gid}'>delete</a><br> <div style='padding-left:20px' >${decodedNote}</div>`);
				$("#noteHistory").append("<br><hr>");

			});
			$(".delete").click(function(event) {
				event.preventDefault();
				let gid = $(this).attr("href");
				let data = "action=delete";
				data += `&gid=${gid}`;
				$.post("RemVulnData", data).done(function() {
					refreshNotes();
				});

			});
		});
	}
});

