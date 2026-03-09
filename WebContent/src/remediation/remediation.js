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

	
$(function() {
	$(".col-md-1").css("min-width", "100px").css("text-overflow", "ellipsis").css("white-space", "nowrap").css("overflow", "hidden");
	});
	
function updateSelects() {
	let rows = $('#vulntable').DataTable().rows('.selected').data();
	if (rows.length > 0) {
		$("#vuln_note_select").html("");
		$("#vuln_history_select").html("");
		window.location.href="RemediationSchedule?vulnId=" + rows[0][11].vid;
	}
}

let vid = -1;
let aid = -1;
let vName = "";
let appId = "";
$(function() {

	$('#vulntable').dataTable({
		"destroy": true,
		"paging": true,
		"lengthChange": false,
		"searching": false,
		"info": true,
		"autoWidth": true,
		columnDefs: [
			{
				targets: [6],
				render: function(data, type, row) {
					return updateColor(data);
				},
				orderable: true
			},
			{
				targets: [6,7],
				width: "60px",
				orderable: true
				
			},
			{
				targets: [8,9],
				width: "100px",
				orderable: true
				
			},
			{
				targets: [0],
				width: "10px",
				orderable: false
				
			},
			{
				targets: [5],
				orderable: false
			},
			{
				targets: [2,3,4],
				orderable: true
			}
		]


	});

	$('#vulntable').DataTable().on('draw', function() {
		clearLoading(".content");
		$($("#vulntable").DataTable().rows().data()).each(function(a, b) {
			if (searchId != -1 && b[11].vid == searchId) {
				$($("#vulntable").find("tbody").find("tr")[a]).trigger("click");

			}
		});
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

	$("#search, #appid, #appname, #tracking, #vulnName").bind("click keypress", function(evt) {

		if (evt.currentTarget.type == "text" && evt.type == "click")
			return;
		if (evt.type == "keypress" && evt.which != 13)
			return;

		let qs = `OpenVulns?action=get&appId=${$("#appid").val()}&appname=${$("#appname").val()}&tracking=${$("#tracking").val()}&vulnName=${$("#vulnName").val()}&junk%5B0%5D=help`
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
			"pageLength": 10,
			"lengthChange": false,
			"searching": false,
			"info": true,
			"autoWidth": true,
			"order": [[7, "desc"]],
			serverSide: true,
			ajax: {
				"url": qs,
				"type": "POST"
			},
			columnDefs: [
				{
					targets: [6],
					render: function(data, type, row) {
						return updateColor(data);
					},
					orderable: true
					
				},
				{
					targets: [8,9],
					width: "100px",
					orderable: true
					
				},
				{
					targets: [6,7],
					width: "60px",
					orderable: true
					
				},
				{
					targets: [0],
					width: "10px",
					orderable: false
					
				},
				{
					targets: [5],
					orderable: false
				},
				{
					targets: [2,3,4],
					orderable: true
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
					updateCheckbox(this, true);
				} else {
					updateCheckbox(this, false);
				}
			}
		} else {
			$(this).toggleClass('selected');
			if ($(this).hasClass('selected')) {
				updateCheckbox(this, true);
			} else {
				updateCheckbox(this, false);
			}
			updateSelects();
		}
		if ($('#vulntable').DataTable().rows('.selected').data().length == 1 && $("#controls").css("display") == "none") {
			window.location = `RemediationSchedule?vulnId=${vid}`
		}
		return false;
	});
});





