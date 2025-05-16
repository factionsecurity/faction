require('../scripts/fileupload/css/fileinput.css');
require('./scheduling.css');
require('select2/dist/css/select2.min.css')
require('daterangepicker/daterangepicker.css');
import '../loading/js/jquery-loading';
import Editor from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell'
import '@toast-ui/editor/dist/toastui-editor.css';
import 'tui-color-picker/dist/tui-color-picker.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import * as moment from 'moment';
import 'daterangepicker';
import 'select2';
import 'jquery.gantt';
import { marked } from 'marked';

let assessors = {};
let editors = {
	notes: {}
};
let initialHTML={};
global._token = $("#_token")[0].value;
global.calendar = {};
$(function() {
	if(location.hash == "#tab_3"){
		$('.nav-tabs a[href="#tab_3"]')[0].click()
	}
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
		uploadAsync: true,
		minFileCount: 0,
		maxFileCount: 5,
		allowedFileExtensions: ['msg', 'csv', 'jpg', 'gif', 'png', 'pdf', 'doc', 'xls', 'xlsx', 'docx', 'txt', 'bmp', 'jpeg', 'xml', 'zip', 'rar', 'tar', 'gzip', 'gz', 'xml'],
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
	}).on("filebatchselected", function(event, files) {
    	$("#files").fileinput("upload");
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

	$(".select2").select2();
	if(engName != '') $("#engName").val(engName).trigger("change")
	if(remName != '') $("#remName").val(remName).trigger("change");
	if(campName != '') $("#campName").val(campName).trigger("change");
	if(assType != '') $("#assType").val(assType).trigger("change");
	if(statName != '') $("#statName").val(statName).trigger("change");
	getAssessors();
	if (finalized) {
		readonly_select($(".select2"), true);
	}
	createEditor("notes")
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
	if ($("#distlist").val() != null && $("#distlist").val().trim() != "") {
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
	
	if ($("#assessorListSelect option").length == 0) {
		issues += "<li>Missing an Assessor.</li>";
	}

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

					if (index == size - 1 || size == 0) {

						let data = "appid=" + $("#appId").val();
						data += "&appName=" + encodeURIComponent($("#appName").val());
						data += "&remId=" + $("#remName").val();
						data += "&engId=" + $("#engName").val();
						data += "&type=" + $("#assType").val();
						data += "&statusName=" + $("#statName").val();
						let value3 = $("#assessorListSelect option");

						index = 0;
						value3.each(function(option) {
							data += "&assessorId[" + (index++) + "]=" + $(this).val() + "";
						});
						data += "&notes=" + encodeURIComponent(getEditorText("notes"));
						data += "&distro=" + $("#distlist").val();
						data += "&campId=" + $("#campName").val();
						data += "&sdate=" + $("#reservation").val().split("to")[0].trim();
						let fields = [];
						$('[id^="cust"]').each( (_index,el)=>{
							let id = el.id;
							id = id.replace('cust',"");
							let val = $(el).val();
							
							if(el.type == 'checkbox'){
								val = $(el).is(':checked')
							}
								
							let field = `{"id" : ${id}, "text" : "${val}"}`;
							fields.push(field);
						})
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
						$.post(location.pathname, data).done(function( resp ) {
							if(resp.result == "success"){
								location.reload();
							}else{
								$.alert({
									type: "red",
									title: 'Error!',
									content: resp.message
								});
							}
						
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
function deleteAssessment(el, id) {
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

	let searchTable = $('#searchResults').DataTable({
		"destroy": true,
		"paging": true,
		"pageLength": 10,
		"lengthChange": false,
		"searching": false,
		"ordering": true,
		"info": true,
		"autoWidth": true,
		"order": [[6, "desc"]],
		 columnDefs: [
			{
				target: 11,
				visible: false,
				searchable: false
			},
			{
				target: [3,4,5,9,10],
				orderable: false
			}
        ],
		serverSide: true,
		ajax: {
			"url": "Engagement",
			"type": "POST",
			"data": function ( d ) {
				 return $.extend( {}, d, {
				   "appid": $("#search_appid").val(),
				   "assessorId": $("#search_assessorid").val(),
				   "engId": $("#search_engagementid").val(),
				   "appName": $("#search_appname").val(),
				   "statusName": $("#statusSearch").val(),
				   "action": "search",
				   "max": 10
				 } );
			   }
			 }


	});
	searchTable.on('draw', function(){
		$('#searchResults').off('click', 'tr');
		$('#searchResults').on('click', 'tr', function(event){
			if(this.firstChild.tagName != "TH"){
				const id = searchTable.row(this).data()[11]
				console.log(event.target.outerHTML)
				if(event.target.outerHTML.indexOf('trash') != -1){
					deleteAssessment(this,id);
				}else{
					window.open(`EditAssessment?action=get&aid=${id}`, '_blank');
				}
			}
		});
	});

	$("#searchBtn, #search_appid, #search_appname, #statusSearch").bind("click keypress", function(evt) {
		if (evt.currentTarget.type == "text" && evt.type == "click")
			return;
		if (evt.type == "keypress" && evt.which != 13)
			return;
		searchTable.draw();

	});


	$("#appId,#appName").autoComplete({
		minLength: 3,
		cache: false,
		source: function(term, response) {
			invData = {};


			let data = "appid=" + $("#appId").val();
			data += "&appname=" + encodeURIComponent($("#appName").val());
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
				let el = $("#cust" + b.fid)
				if(el.type == 'checkbox' && b.value == "true"){
					$(el).prop('checked', true);
				}else if(el.type == 'checkbox'){
					$(el).prop('checked', false);
				}else{
					$(el).val(b.value);
				}
				
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
	let start = new Date(($("#reservation").val().split("to")[0].trim()));
	copiedEventObject.start = start;
	copiedEventObject.color = edit_color;
	copiedEventObject.id = "-1";
	let endTmp = $("#reservation").val().split("to")[1];
	let end = new Date(endTmp);
	end = new Date(end.setDate(end.getDate() + 1));
	copiedEventObject.end = end;
	calendar.addEvent(copiedEventObject, true);
	start = new Date(start.setDate(start.getDate() - 30));
	end = new Date(end.setDate(end.getDate() + 30));
	
	$.post('Calendar', `userid=${userid}&team=-1&start=${start.toLocaleDateString("en-US")}&end=${end.toLocaleDateString("en-US")}&action=search`).done(function(json) {
		for (let verification of json.verifications) {
			let s = verification.start;
			let e = verification.end;
			let t = verification.appid + " - " + verification.appname + " - " + verification.vuln;
			let aaid = verification.appid;

			if (s != 'null' && e != 'null') {
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
		for(let assessment of json.assessments){
			let s = assessment.start; 
			let e = assessment.end; 
			let fullname = assessment.username; 
			let t = assessment.appid + " - " + assessment.name + " - " + fullname;
			let aid = assessment.id;
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
				calendar.addEvent(copiedEventObject, true);

			}
			
		}
	});
}

function getAssessors() {
	assessors = {};
	calendar.removeAllEvents();
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
			let d = resp.users[N];
			let ocText = " <span>[ Not Free ]</span>";
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
	return editors[name].getHTML();
}
function createEditor(id){
	initialHTML[id] = entityDecode($(`#${id}`).html());
	$(`#${id}`).html("");
	editors[id]= new Editor({
				el: document.querySelector(`#${id}`),
				previewStyle: 'vertical',
				height: 'auto',
				autofocus: false,
				height: '560px',
				plugins: [colorSyntax, tableMergedCell]
			});
	editors[id].hide();
	editors[id].setHTML(initialHTML[id], false);
	initialHTML[id] = editors[id].getHTML();
	editors[id].show();
	
	/// This is a hack becuase toastui does not have inital undo history set correctly
	/// https://github.com/nhn/tui.editor/issues/3195
	editors[id].on( 'keydown', function(a,e){
		const html = editors[id].getHTML()
		if ((e.ctrlKey || e.metaKey) && e.key == 'z' && html == initialHTML[id]) {
			e.preventDefault();
			throw new Error("Prevent Undo");
		 }
	})
	
}
function entityDecode(encoded){
	let textArea = document.createElement("textarea");
	textArea.innerHTML = encoded;
	return textArea.innerText;
	
}