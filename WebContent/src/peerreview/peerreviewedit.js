require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
import suneditor from 'suneditor';
import { font, fontColor, hiliteColor, fontSize, align, textStyle, image, imageGallery, list, link, formatBlock, table, blockquote } from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
import '../loading/js/jquery-loading';
import 'jquery';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import 'datatables.net-bs';
import { } from 'icheck'
import { marked } from 'marked';

$(function() {
	$("#uploadedFiles").DataTable({
		"paging": false,
		"lengthChange": false,
		"searching": false,
		"ordering": true,
		"info": true,
		"autoWidth": true,
		"order": [[0, "asc"]]
	});


});

$(function() {

	$(".filter").click(function(e) {
		e.preventDefault();
		console.log($(this).attr("href"));
		let href = $(this).attr("href");
		href = href.replace("#", "")
		$(".questions table").DataTable().columns(3).search(href).draw()
	});

	$(".questions table").DataTable({
		"paging": false,
		"info": false,
		"ordering": false,
		"columnDefs": [
			{
				"targets": [0],
				"visible": true,
				"searchable": true

			},
			{
				"targets": [0, 1, 2],
				"visible": true,
				"searchable": false
			},
			{
				"targets": [3],
				"visible": false,
				"searchable": true
			}, {
				"targets": [1, 2],
				"width": "300px"
			}

		]

	});
	//this is a hack.. something is setting witth to 0px
	$(".questions table").css('width', '100%')
});

let editors = {}
//window.editors = editors;
const editorConfig = {
	display: 'block',
	width: '100%',
	codeMirror: CodeMirror,
	plugins: [font, fontColor, hiliteColor, link, fontSize, align, image, imageGallery, list, formatBlock, table, blockquote, textStyle],
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks', 'preview'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	attributesWhitelist: { 'all': "class|data-cid|data-userid|data-username|date-time|title" },
	allowedClassNames: ".*",
	minHeight: 500,
	height: "auto"
};
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
		const md = selected.reduce( (acc,item) => acc + item.innerText +"\n", "") ;
		const html = marked.parse(md);
		const div = document.createElement("div");
		div.innerHTML = html;
		const parent = selected[0].parentNode;
		parent.insertBefore(div, selected[0]);
		for(let i=0; i<selected.length; i++){
			selected[i].remove();
		}
	}
}
const notesConfig = {
	codeMirror: CodeMirror,
	plugins: [font, fontColor, fontSize, image, align, imageGallery, list, formatBlock, table, blockquote, fromMarkdown],
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat', 'fromMarkdown'],
	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	allowedClassNames: ".*",
	attributesWhitelist: { 'all': "class|data-cid|data-userid|data-username|date-time|title" },
	height: 500,
	width: "100%"
};

let iceConfig = {
	handleEvents: true,
	plugins: [
		'IceAddTitlePlugin',
		'IceEmdashPlugin',
		{
			name: 'IceCopyPastePlugin',
			settings: {
				preserve: 'p,a[href],span[id,class]em,strong'
			}
		}
	]
};

function updateVulnEditors() {
	$.each($("[id^=vuln_]"), function(_id, obj) {
		let id = $(obj).attr("id");
		if (id.indexOf("header") != -1) {
			return;
		}
		console.log(id);
		if (id.indexOf("notes") == -1) {
			editorConfig.allowedClassNames = ".*";
			editors[id] = suneditor.create(id, editorConfig)
			setUpTracking(editors[id]);
			editors[id].onKeyDown = function(contents, core) {
				queueSave(id);
			}
		} else {
			editors[id] = suneditor.create(id, notesConfig)
			editors[id].width = "30%";
			editors[id].onKeyDown = function(contents, core) {
				queueSave(id);
			}
		}
	});
}

function setUpTracking(element) {
	try {
		iceConfig.element = element.core.context.element.wysiwygFrame;
		iceConfig.currentUser = { id: userId, name: userName };
		const tracker = new ice.InlineChangeEditor(iceConfig);
		tracker.startTracking();
	} catch (ex) {
		console.log(ex);
	}
}

function completePR() {
	let data = `prid=${prid}`;
	$.post("CompletePR", data).done(function(resp) {
		if (resp.result == "complete") {
			document.location = "PeerReview";
		} else {
			$.alert(resp.message);
		}
	})

}
function saveAllEditors(isComplete = false) {
	let action = "SaveTrackChanges";
	if (isComplete) {
		action = "CompletePR";
	}
	let data = `prid=${prid}`;
	data += "&risk_notes=" + encodeURIComponent(editors.risk_notes.getContents());
	data += "&sum_notes=" + encodeURIComponent(editors.summary_notes.getContents());
	data += "&risk=" + encodeURIComponent(editors.risk.getContents());
	data += "&summary=" + encodeURIComponent(editors.summary.getContents());
	let index = 0;
	$.each($("[id^=vuln_]"), function(_id, obj) {
		let id = $(obj).attr("id");
		data += "&" + id + "=" + encodeURIComponent(editors[id].getContents(false));
	});
	$.each($("[id^=step]"), function(_id, obj) {
		let id = $(obj).attr("id");
		let step = editors[id].getContents();
		//replaces br inside pre that gets removed from ckeditor
		let div = $("<div/>").html(step);
		console.log(div.html());
		div.find("pre").each(function(i, el) {
			let newHTML = $(el).html().replace(/\n/g, "<br/>");
			$(el).html(newHTML);
		});
		data += "&" + id + "=" + encodeURIComponent(div.html());
	});
	$.post(action, data).done(function(resp) {

		if (resp.result == "success") {
			$(".box-title").each((a, b) => {
				const title = b.innerHTML.split("*")[0]
				b.innerHTML = title
			})
		} else if (resp.result == "complete") {
			document.location = "PeerReview";
		}
	})
}

function saveEditor(type) {
	let action = "SaveTrackChanges?action=type";
	let data = `prid=${prid}`;
	if (type == "risk_notes")
		data += "&risk_notes=" + encodeURIComponent(editors.risk_notes.getContents());
	else if (type == "summary_notes")
		data += "&sum_notes=" + encodeURIComponent(editors.summary_notes.getContents());
	else if (type == "risk")
		data += "&risk=" + encodeURIComponent(editors.risk.getContents());
	else if (type == "summary")
		data += "&summary=" + encodeURIComponent(editors.summary.getContents());
	else
		data += "&" + type + "=" + encodeURIComponent(editors[type].getContents());

	$.post(action, data).done(function(resp) {

		if (resp.result == "success") {
			clearLockTimeout[type] = setTimeout(() => {
				$.post(`PRClearLock`, `field=${type}&prid=${prid}`).done();
				document.getElementById(`${type}_header`).innerHTML = ""
				$(".complete").html("<i class='fa fa-check'></i> Complete");
				$(".complete").prop("disabled", false);
			}, 5000);
		} else if (resp.result == "complete") {
			clearTimeout(clearLockTimeout[type]);
			$.post(`PRClearLock`, `field=${type}&prid=${prid}`).done(() => {
				document.location = "PeerReview";
			});
		}
	})

}

function b64DecodeUnicode(str) {
	str = decodeURIComponent(str);
	return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
	}).join(''));
}

let editorTimeout = {};
let clearLockTimeout = {};
function queueSave(type) {
	$.post(`PRSetLock`, `field=${type}&prid=${prid}`).done((resp) => {
		if (resp.result == "success") {
			$(".complete").html("<div class='throbber-loader'>Loading…</div>");
			$(".complete").prop("disabled", true);
			document.getElementById(`${type}_header`).innerHTML = "*"
			clearTimeout(editorTimeout[type]);
			clearTimeout(clearLockTimeout[type]);
			editorTimeout[type] = setTimeout(() => {
				saveEditor(type);
			}, 2000);
		}
	});

}

$(function() {


	editors['summary'] = suneditor.create("appsum", editorConfig);
	setUpTracking(editors['summary']);
	editors.summary.onKeyDown = function(contents, core) {
		queueSave("summary");
	}

	editorConfig.allowedClassNames = ".*";
	editors['risk'] = suneditor.create("risk", editorConfig);
	setUpTracking(editors['risk']);
	editors.risk.onKeyDown = function(contents, core) {
		queueSave("risk");
	}

	editors['risk_notes'] = suneditor.create('risk_notes', notesConfig);
	editors.risk_notes.onKeyDown = function(contents, core) {
		queueSave("risk_notes");
	}
	editors['summary_notes'] = suneditor.create('appsum_notes', notesConfig);
	editors.summary_notes.onKeyDown = function(contents, core) {
		queueSave("summary_notes");
	}

	updateVulnEditors();

	setInterval(() => {
		$.get(`PRCheckLocks?prid=${prid}`).done((resp) => {
			if (resp.message == "Peer Review has been completed.") {
				$.confirm({
					title: "",
					content: resp.message,
					autoClose: "ok|5000",
					buttons: {
						ok: function() {
							location.href = "PeerReview";
						}
					}
				})
			} else {
				Object.keys(editors).forEach(function(type) {
					if (resp[type] && resp[type].isLocked) {
						editors[type].core.context.element.wysiwygFrame.classList.add("disabled");
						document.getElementById(`${type}_header`).innerHTML = `<i class="lockUser">Editing by ${resp[type].lockedBy} ${resp[type].lockedAt}</i>`
						editors[type].disabled();
						editors[type].setContents(b64DecodeUnicode(resp[type].updatedText));
					} else {
						editors[type].enable();
						document.getElementById(`${type}_header`).innerHTML = "";
						editors[type].core.context.element.wysiwygFrame.classList.remove("disabled");
					}
				});
			}
		}
		)

	}, 1000);


	$(".closeit").click(function() {
		$.confirm({
			title: 'Confirm!',
			content: 'Unsaved changes will be lost!',
			buttons: {
				confirm: function() {
					document.location = "PeerReview";
				},
				cancel: function() {

				}
			}
		});

	});

	$(".complete").click(function() {
		if ($(this).hasClass("complete")) {
			$.confirm({
				title: 'Confirm!',
				content: 'Are you sure you want to complete the PR? <br/> This will remove it from the PR queue and send it back to the assessor.',
				buttons: {
					confirm: function() {
						completePR();
					},
					cancel: function() {

					}
				}
			});
		}
	});


});