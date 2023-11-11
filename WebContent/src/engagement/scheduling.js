require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('./scheduling.css');
import suneditor from 'suneditor';
import { font, fontColor, fontSize, align, image, imageGallery, list, formatBlock, table, blockquote } from 'suneditor/src/plugins';
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
import * as moment from 'moment';
import 'bootstrap-daterangepicker';
import 'select2';
import 'jquery.gantt';

let assessors = {};
let editors = {
	notes: {}
};
global._token = $("#_token")[0].value;
let editorOptions = {
	codeMirror: CodeMirror,
	plugins: [font, fontColor, fontSize, image, align, imageGallery, list, formatBlock, table, blockquote],
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks', 'codeView', 'preview'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	height: 500,
	width: "100%"
};
global.calendar = {};
$(function() {
	global.readonly_select = function readonly_select(objs, action) {
		if (action === true) {
			objs.prepend('<div class="disabled-select"></div>');
			$(".select2-selection__arrow").remove();
		} else
			$(".disabled-select", objs).remove();
	}

	//getAssessors();
	calendar = new FullCalendar.Calendar(document.getElementById("calendar"), {
		//$('#calendar').fullCalendar({
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
		eventDrop: function(event, _delta) { // this function is called when something is dropped
			let range = $("#reservation").val();
			let start = new Date(range.split(" to ")[0]);
			let end = new Date(range.split(" to ")[1]);
			start.setDate(start.getDate() + event.delta.days);
			end.setDate(end.getDate() + event.delta.days);
			let startStr = (start.getMonth() + 1) + "/" + start.getDate() + "/" + start.getFullYear();
			let endStr = (end.getMonth() + 1) + "/" + end.getDate() + "/" + end.getFullYear();
			$("#reservation").val(startStr + " to " + endStr);

		},
		eventResize: function(event, _jsEvent, _ui, _view) {
			let range = $("#reservation").val();
			let start = new Date(range.split(" to ")[0]);
			let end = new Date(range.split(" to ")[1]);
			start.setDate(start.getDate() + event.startDelta.days);
			end.setDate(end.getDate() + event.endDelta.days);
			let startStr = (start.getMonth() + 1) + "/" + start.getDate() + "/" + start.getFullYear();
			let endStr = (end.getMonth() + 1) + "/" + end.getDate() + "/" + end.getFullYear();
			$("#reservation").val(startStr + " to " + endStr);
		}

	});
	calendar.render();


	$("#searchList").on('click', () => {
		let term = $("#searchTerm").val();
		$("#assessors").html("");
		for (let key in assessors) {
			if (assessors[key]['name'].toLowerCase().indexOf(term.toLowerCase()) != -1) {
				$("#assessors").append(assessors[key]['html']);
			}
		}


	});
	$("#clearList").on('click', () => {

		$("#assessors").html("");
		$("#searchTerm").val("");
		for (let key in assessors) {
			$("#assessors").append(assessors[key]['html']);
		}

	});

	$("#files").fileinput({
		overwriteInitial: false,
		uploadUrl: "UploadFile",
		//uploadUrl: "../service/fileUpload",
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
				return ext.match(/(zip|rar|tar|gzip|gz|7z)$/i);
			},
			'txt': function(ext) {
				return ext.match(/(txt|ini|csv|java|php|js|css)$/i);
			},
			'pdf': function(ext) {
				return ext.match(/(pdf)$/i);
			}
		},
		preferIconicPreview: true,
		previewFileIconSettings: {
			'doc': '<i class="fa fa-file-word-o text-primary"></i>',
			'xls': '<i class="fa fa-file-excel-o text-success"></i>',
			'ppt': '<i class="fa fa-file-powerpoint-o text-danger"></i>',
			'pdf': '<i class="fa fa-file-pdf-o text-danger"></i>',
			'zip': '<i class="fa fa-file-archive-o text-muted"></i>',
		},
		initialPreviewDownloadUrl: initialPreviewDownloadUrl,
		allowedPreviewTypes: ['image'], // allow only preview of image & text files
		initialPreviewAsData: true, // defaults markup
		initialPreviewFileType: 'text',
		initialPreview: initialPreviewData || [],
		initialPreviewConfig: initialPreviewConfigData || [],
	});

	$("#assessmentFiles").fileinput({
		overwriteInitial: false,
		uploadUrl: "../service/uploadAssessment",
		uploadAsync: true,
		minFileCount: 0,
		maxFileCount: 5,
		allowedFileExtensions: ['csv']
	});

	let TMPresVal = $("#reservation").val().trim();
	if (TMPresVal != "" && TMPresVal != "to" && TMPresVal != "-") {
		let one = TMPresVal.split("to")[0].trim();
		calendar.gotoDate(new Date(one));
	}


	/* let url = document.location.toString();
		   if (url.match('#')) {
			   $('.nav-tabs a[href=#'+url.split('#')[1]+']').tab('show') ;
		   } 
		   $('.nav-tabs a').on('click', ()=>{ 
				 let hash = $(this).attr('href');
				 window.location.hash=hash;
			   });*/

	$(".select2").select2();
	/*$("#engName").select2('val', engName);
	$("#remName").select2('val', remName);
	$("#campName").select2('val', campName);
	$("#assType").select2('val', assType);
	$("#statName").select2('val', statName);*/
	if(engName != '') $("#engName").val(engName).trigger("change")
	if(remName != '') $("#remName").val(remName).trigger("change");
	if(campName != '') $("#campName").val(campName).trigger("change");
	if(assType != '') $("#assType").val(assType).trigger("change");
	if(statName != '') $("#statName").val(statName).trigger("change");
	getAssessors();
	if (finalized) {
		readonly_select($(".select2"), true);
	}
	editors.notes = suneditor.create("notes", editorOptions);
	if(!finalized){

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
				"firstDay": 1,
			},
			"weekStart": 5,
			"showWeekNumbers": true,
			"startDate": moment(`${sDate}`,'MM-DD-YYYY'),
			"endDate": moment(`${eDate}`,'MM-DD-YYYY')

		});
		//$("#reservation").val(`${sDate} to ${eDate}`);
		getAssessors();
	}
	$('#reservation, #teamName').on('change', function() {
		getAssessors();
		let value3 = $("#assessorListSelect option");
		calendar.removeAllEvents();

		outputdata = [];
		let ids = "";
		let users = "";
		value3.each(function(option) {
			ids += $(this).val() + ",";
			users += $(this).text().replace(/[.*]/, "") + ";"

		});
		ids = ids.substring(0, ids.length - 1);
		users = users.substring(0, users.length - 1);

		updateCalendar(users, ids);

	});

	$("#AddAssessment").on('click', () => {
		checkForms();
	});

});
function checkForms() {
	let issues = "";
	if ($("#appId").val() == null || $("#appId").val().trim() == "") {
		//issues += "<li>Missing an Application ID.</li>";
		let rand = Math.floor(Math.random() * 1000000) + 1000;
		$("#appId").val(rand);
	}
	if ($("#appName").val() == null || $("#appName").val().trim() == "") {
		issues += "<li>Missing an Application Name.</li>";
	}
	if ($("#remName").val() == null || $("#remName").val().trim() == "" || $("#remId").val() == "-1") {
		issues += "<li>Missing a Remediation Contact.</li>";
	}
	if ($("#engName").val() == null || $("#engName").val().trim() == "" || $("#engId").val() == "-1") {
		issues += "<li>Missing an Engagement Contact.</li>";
	}
	if ($("#assType").val() == null || $("#assType").val().trim() == "" || $("#assType").val() == "-1") {
		issues += "<li>Missing an Assessment Type.</li>";
	}

	let dates = $("#reservation").val().split("to");
	if (dates[0].trim() == "" || dates[1].trim() == "") {
		issues += "<li>Missing Start and End Dates</li>";
	}
	if ($("#distlist").val() == null || $("#distlist").val().trim() != "") {
		let reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		let emails = $("#distlist").val().split(";");
		let tests = true;
		$(emails).each(function(a, b) {
			if (!reg.test(b.trim())) {
				tests = false;
			}
		});
		if (!tests) {
			issues += "<li>Invalid Distribution List</li>";
		}

	}


	/*
	if($("#assessorListSelect option").size()==0){
		issues += "<li>Missing an Assessor.</li>";
	}*/

	if (issues != "") {
		$.alert({
			type: "red",
			title: 'Incomplete Entry!',
			content: 'You have the following Errors.<br><ul>' + issues + '</ul>',
		});
		//return false;
	} else {
		let optional = [];
		let index = 0;


		if (getEditorText("notes").trim() == "") {
			optional[index++] = "Do you want to add Assessment Notes?";
		}
		if ($("#distlist").val().trim() == "") {
			optional[index++] = "Do you want to add to the Distribution List?";
		}

		if ($("#campName").val().trim() == "" || $("#campName").val().trim() == "-1") {
			optional[index++] = "Do you want to add to a Campaign?";
		}
		if ($("#assessorListSelect option").length == 0) {
			optional[index] = "<li>Missing an Assessor.</li>";
		}
		confirmAndPostIt(optional, 0, optional.length);

	}

	//return false;

}

function confirmAndPostIt(messages, index, size) {
	if (size == 0)
		messages[0] = "Ready to Schedule?";
	let cButton = "Proceed Anyway";
	if (size == 0)
		cButton = "Save It";
	$.confirm({
		type: "yellow",
		title: 'Are you sure?',
		content: messages[index],
		buttons: {
			confirm: {
				text: cButton,
				btnClass: 'btn-orange',
				action: function(confirm) {
					$(".content").loading({ overlay: true, base: 0.3 });

					if (index == size - 1 || size == 0) {

						let data = "appid=" + $("#appId").val();
						data += "&appName=" + $("#appName").val();
						data += "&remId=" + $("#remName").val();
						data += "&engId=" + $("#engName").val();
						data += "&type=" + $("#assType").val();
						data += "&statusName=" + $("#statName").val();
						let value3 = $("#assessorListSelect option");

						index = 0;
						value3.each(function(option) {
							data += "&assessorId[" + (index++) + "]=" + $(this).val() + "";
							console.log(data);
						});
						data += "&notes=" + encodeURIComponent(getEditorText("notes"));
						data += "&distro=" + $("#distlist").val();
						data += "&campId=" + $("#campName").val();
						data += "&sdate=" + $("#reservation").val().split("to")[0].trim();
						console.log("fix iterator");
						let fields = [];
						for (let id of customFields) {
							fields.push(`{"id" : ${id}, "text" : "' + $("#cust${id}").val() + '"}`);
						}
						/*
						<s:iterator value="custom" status="incr">
						<s:if test="#incr.index != 0">data+=",";</s:if>
						data+='{"id" : ${id}, "text" : "' + $("#cust${id}").val() + '"}';
						</s:iterator>*/
						data += '&cf=[' + fields.join(",") + "]";

						let endTmp = $("#reservation").val().split("to")[1];
						let end = new Date(endTmp);
						end = new Date(end.setDate(end.getDate() + 1));

						let eform = (end.getMonth() + 1) + "/" + (end.getDate() - 1) + "/" + end.getFullYear();
						if ((end.getDate() - 1) == 0 && end.getMonth() == 13) {
							eform = "1/1/" + end.getFullYear() + 1;
						} else if ((end.getDate() - 1) == 0) {
							eform = (end.getMonth() + 1) + "/1/" + end.getFullYear();
						}
						data += "&edate=" + eform;
						if (location.pathname.indexOf("EditAssessment") != -1) {
							data += "&action=update";
							data += "&aid=" + aid;
						} else {
							data += "&action=createAssessment";
						}
						data += "&_token=" + global._token;
						$.post(location.pathname, data).done(function() {
							location.reload();
						});
					} else {
						confirmAndPostIt(messages, index + 1, size);
					}
				}
			},
			cancel: function() { return; }
		}
	});
}


let invData = {};
global.edit = function edit(id) {
	document.location = "EditAssessment?action=get&aid=" + id;
}
let sample;
global.del = function del(el, id) {
	$.confirm({
		title: "Are you sure?",
		content: "Deleting this assessment is permenant and cannot be restored.</br>Are your sure?",
		buttons: {
			"yes delete": function() {
				let td = $(el).parent();
				let tr = $(td).parent();
				$('#searchResults').DataTable().row(tr).remove().draw();

				$.get("Engagement?action=delete&appid=" + id + "&_token=" + global._token).done(function(data) {
					global._token = data.token;
					if (data.result == "success") {
						let row = $(el).parents("tr");
						$('#searchResults').DataTable().row(row).remove().draw();
					} else {
						$.confirm({ title: data.message || data.response, content: data.message || data.response });
					}
				});
			},
			cancel: function() { return; }
		}
	});

}
$(function() {
	//This is to set the dataTable on the second tab to full width	
	$("ul.nav li").click(() => { $("#searchResults").css("width", "100%") });

	$('#searchResults').DataTable({
		"destroy": true,

		"paging": true,
		"pageLength": 10,
		"lengthChange": false,
		"searching": false,
		"ordering": true,
		"info": true,
		//"autoWidth": true,
		"order": [[4, "desc"]],
		serverSide: true,
		ajax: {
			"url": "Engagement?appid=&assessorId=&engId=&appName=&statusName=&action=search&max=10",
			"type": "POST"
		}


	});

	$("#searchBtn, #search_appid, #search_appname, #statusSearch").bind("click keypress", function(evt) {
		if (evt.currentTarget.type == "text" && evt.type == "click")
			return;
		if (evt.type == "keypress" && evt.which != 13)
			return;
		let appid = $("#search_appid").val();
		let assessor = $("#search_assessorid").val();
		let engagement = $("#search_engagementid").val();
		let appname = $("#search_appname").val();
		let statname = $("#statusSearch").val();
		let data = "appid=" + appid;
		data += "&assessorId=" + assessor;
		data += "&engId=" + engagement;
		data += "&appName=" + appname;
		data += "&statusName=" + statname;
		data += "&action=search";
		data += "&max=10";

		$('#searchResults').DataTable({
			"destroy": true,

			"paging": true,
			"pageLength": 10,
			"lengthChange": false,
			"searching": false,
			"ordering": true,
			"info": true,
			"autoWidth": true,
			"order": [[4, "desc"]],
			serverSide: true,
			ajax: {
				"url": "Engagement?" + data,
				"type": "POST"
			}

		}
		);



	});


	$("#appId,#appName").autoComplete({
		minLength: 3,
		cache: false,
		source: function(term, response) {
			invData = {};


			let data = "appid=" + $("#appId").val();
			data += "&appname=" + $("#appName").val();
			$.post("../service/applicationInventory", data).done(function(resp) {
				let invList = [];
				invData = resp;
				for (let i = 0; i < resp.length; i++) {
					invList[i] = resp[i].appid + "<a> </a>" + resp[i].appname + "<a> </a><input type='hidden' remid='" + resp[i].remediationId + "' distro='" + resp[i].distro + "'>";
				}

				response(invList);

			});
		},
		onSelect: function(e, term, item) {
			console.log(term);
			let appid = term.split("<a> </a>")[0];
			let appName = term.split("<a> </a>")[1];
			let others = term.split("<a> </a>")[2];
			let distro = $(others).attr("distro");
			let json = {};
			for (let i = 0; i < invData.length; i++) {
				let d = invData[i];
				if (d.appid == appid) {
					json = d;
					break;
				}
			}
			$("#appId").val(appid);
			$("#appName").val(appName);
			$("#distlist").val(distro);
			$("#assType").val(json.type).trigger("change");
			$("#engName").val(json.engId).trigger("change");
			$("#remName").val(json.remediationId).trigger("change");
			$("[id^=cust]").val("");
			$(json.fields).each(function(a, b) {
				$("#cust" + b.fid).val(b.value);
			});

		}

	});
});

let outputdata = [];
$(function() {
	$("#addAssessor").click(function() {
		//key=$("#assessors").val();
		let value2 = $("#assessors option:selected");
		value2.each(function(option) {
			if (!isInList($(this))) {
				$("#assessorListSelect").append($("<option></option>")
					.attr("value", $(this).val())
					.text($(this).text()));
			}


		});
		let value3 = $("#assessorListSelect option");
		calendar.removeAllEvents();

		outputdata = [];
		let ids = "";
		let users = "";
		value3.each(function(option) {
			ids += $(this).val() + ",";
			users += $(this).text().replace(/[.*]/, "") + ";"

		});
		ids = ids.substring(0, ids.length - 1);
		users = users.substring(0, users.length - 1);

		updateCalendar(users, ids);


	});
	$("#removeAssessor").click(function() {
		let options = $("#assessorListSelect option:selected");
		//let outputdata=[];
		options.each(function(option) {
			$(this).remove();
		});
		let value2 = $("#assessorListSelect option");
		calendar.removeAllEvents();
		value2.each(function(option) {
			updateCalendar($(this).text(), $(this).val())

		});
	});
});
function isInList(option) {
	let val = $(option).val();
	console.log(val);
	if ($("#assessorListSelect option[value='" + val + "']").length > 0)
		return true;
	else return false;


}

function updateCalendar(user, userid) {
	calendar.removeAllEvents();
	//TODO:get assessments first and put on calendar
	let originalEventObject = $(this).data('eventObject');
	let copiedEventObject = $.extend({}, originalEventObject);
	copiedEventObject.allDay = true;
	copiedEventObject.title = $("#appId").val() + " - " + $("#appName").val() + " - " + user;
	copiedEventObject.start = new Date(($("#reservation").val().split("to")[0].trim()));
	copiedEventObject.color = edit_color;
	copiedEventObject.id = "-1";
	let endTmp = $("#reservation").val().split("to")[1];
	let end = new Date(endTmp);
	end = end.setDate(end.getDate() + 1);
	copiedEventObject.end = end;
	calendar.addEvent(copiedEventObject, true);
	$.post('../service/getAssessments', 'id=' + userid).done((adata) => {
		console.log(adata);
		let json = JSON.parse(adata);
		console.log(json);
		let N = json.count;

		for (let i = 0; i < N; i++) {
			let s = json.assessments[i][2];
			let e = json.assessments[i][4];
			let fullname = json.assessments[i][5];
			let t = json.assessments[i][1] + " - " + json.assessments[i][0] + " - " + fullname;
			let aid = json.assessments[i][3].replace('app', '');

			if (s != 'null' && e != 'null') {
				originalEventObject = $(this).data('eventObject');
				copiedEventObject = $.extend({}, originalEventObject);
				copiedEventObject.allDay = true;
				copiedEventObject.title = t;
				copiedEventObject.start = new Date(s);
				copiedEventObject.color = asmt_color;
				copiedEventObject.id = aid;
				let tmpdate = new Date(e);
				tmpdate = tmpdate.setDate(tmpdate.getDate() + 1);
				copiedEventObject.end = tmpdate;
				copiedEventObject.editable = false;
				console.log("6")
				calendar.addEvent(copiedEventObject, true);

			}
		}
		let first = 0;
		N = json.ocount;
		for (let i = 0; i < N; i++) {
			let s = json.ooo[i][2];
			let e = json.ooo[i][3];
			let t = json.ooo[i][0];
			let oid = "ooo" + json.ooo[i][0];
			if (s != 'null' && e != 'null') {
				originalEventObject = $(this).data('eventObject');
				copiedEventObject = $.extend({}, originalEventObject);
				copiedEventObject.allDay = true;
				copiedEventObject.title = t;
				copiedEventObject.start = new Date(s);
				copiedEventObject.color = ooo_color;
				copiedEventObject.id = oid;
				let tmpdate = new Date(e);
				tmpdate = tmpdate.setDate(tmpdate.getDate() + 1);
				copiedEventObject.end = tmpdate;
				copiedEventObject.editable = false;
				calendar.addEvent(copiedEventObject, true);

			}
		}

	});
}

function getAssessors() {
	assessors = {};
	calendar.removeAllEvents();
	//console.log($(this).val());
	let range = $("#reservation").val().trim();
	if (range == "" || range == "to" || range == "-") {
		return;
	}
	let sdate = range.split("to")[0].trim();
	let edate = range.split("to")[1].trim();
	let data = "sdate=" + sdate
	data += "&edate=" + edate
	data += "&selectedTeam=" + $("#teamName").val();
	data += "&action=dateSearch";
	//calendar.getEventById(-1).remove();
	calendar.gotoDate(new Date(sdate));
	$.post("Engagement", data).done(function(resp) {
		$("#assessors").html("");
		for (let N in resp.users) {
			console.log(N);
			let d = resp.users[N];
			let ocText = " <span>[ Not Free ]</span>";
			console.log(d.count);
			if (d.count <= 0)
				ocText = " <span style='color:green'>[ Open ]</span>"

			$("#assessors").append("<option class='asopt' value='" + d.id + "'>" + d.name + " - " + d.team + ocText + "</option>");
			assessors[d.id] = {};
			assessors[d.id]['html'] = "<option class='asopt' value='" + d.id + "'>" + d.name + " - " + d.team + ocText + "</option>";
			assessors[d.id]['name'] = d.name;
		}
		//$("#assessors").select2('val','-1');




	});
}
$(function() {
	$("#random").click(function() {
		let rand = Math.floor(Math.random() * 1000000) + 1000;
		$("#appId").val(rand);
	});

});

function getEditorText(name) {
	return editors[name].getContents();
}