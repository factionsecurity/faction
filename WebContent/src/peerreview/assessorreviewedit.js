
require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
//require('bootstrap/dist/css/bootstrap.css');
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
require("../../plugins/iCheck/icheck.min.js")

let editors = {};
let acceptAllChanges = {
	name: 'acceptAllChanges',
	display: 'command',
	title: 'Accept All Changes',
	buttonClass: '',
	innerHTML: '<i class="fa fa-check-circle" style="color:green"></i>',
	add: function(core, targetElement) {
		const context = core.context;
		core.tracking = {};
		core.tracking.startTracking = function(element) {
			const iceConfig = {
				element: element,
				currentUser: { id: userId, name: userName },
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
			core.tracker = new ice.InlineChangeEditor(iceConfig);
			core.tracker.startTracking();
		}
		const element = context.element.wysiwygFrame;
		core.tracking.startTracking(element)
		core.tracking.lastchange = context.element.wysiwygFrame.innerHTML;

	},

	action: function() {

		this.tracking.lastchange = this.context.element.wysiwygFrame.innerHTML;
		this.tracker.acceptAll();
		this.history.push(true)
		queueSave();
	}
}
let rejectAllChanges = {
	name: 'rejectAllChanges',
	display: 'command',
	title: 'Reject All Changes',
	buttonClass: '',
	innerHTML: '<i class="fa fa-times-circle" style="color:red"></i>',
	add: function(core, targetElement) {
	},
	action: function() {
		this.tracking.lastchange = this.context.element.wysiwygFrame.innerHTML;
		this.tracker.rejectAll();
		this.history.push(true);
		queueSave();
	}
}

let undoLastChange = {
	name: 'undoLastChange',
	display: 'command',
	title: 'Undo Last Change',
	buttonClass: '',
	innerHTML: '<i class="fa fa-rotate-left"></i>',
	add: function(core, targetElement) {
	},
	action: function() {
		this.tracker.stopTracking();
		this.context.element.wysiwygFrame.innerHTML = this.tracking.lastchange;
		const element = this.context.element.wysiwygFrame;
		this.tracking.startTracking(element)
		this.tracking.lastchange = this.context.element.wysiwygFrame.innerHTML;
		this.history.push(true)
		queueSave();
	}
}

let acceptSingleChange = {
	name: 'acceptSingleChange',
	display: 'command',
	title: 'Accept Change',
	buttonClass: '',
	innerHTML: '<i class="fa fa-check" style="color:lightgray"></i>',
	add: function(core, targetElement) {
		core.context.acceptSingleChange = {
			targetButton: targetElement
		}
	},
	active: function(element) {
		if (element && (this.util.hasClass(element, 'ins') || this.util.hasClass(element, 'del'))) {
			this.util.addClass(this.context.acceptSingleChange.targetButton.firstChild, 'acceptGreen');
			this.tracking.element = element;
			return true;
		} else {
			this.util.removeClass(this.context.acceptSingleChange.targetButton.firstChild, 'acceptGreen');
			this.tracking.element = null;

		}
		return false;
	},
	action: function() {
		const element = this.tracking.element;
		if (element) {
			this.tracking.lastchange = this.context.element.wysiwygFrame.innerHTML;
			this.tracker.acceptChange(element);
			this.history.push(true)
			queueSave();
		}
	}
}
let rejectSingleChange = {
	name: 'rejectSingleChange',
	display: 'command',
	title: 'Reject Change',
	buttonClass: '',
	innerHTML: '<i class="fa fa-close" style="color:lightgray"></i>',
	add: function(core, targetElement) {
		core.context.rejectSingleChange = {
			targetButton: targetElement
		}
	},
	active: function(element) {
		if (element && (this.util.hasClass(element, 'ins') || this.util.hasClass(element, 'del'))) {
			this.util.addClass(this.context.rejectSingleChange.targetButton.firstChild, 'rejectRed');
			this.tracking.element = element;
			return true;
		} else {
			this.util.removeClass(this.context.rejectSingleChange.targetButton.firstChild, 'rejectRed');
			this.tracking.element = null;

		}
		return false;
	},
	action: function() {
		const element = this.tracking.element;
		if (element) {
			this.tracking.lastchange = this.context.element.wysiwygFrame.innerHTML;
			this.tracker.rejectChange(element);
			this.history.push(true)
			queueSave();
		}
	}
}



const editorConfig = {
	display: 'block',
	width: '100%',
	codeMirror: CodeMirror,
	plugins: [acceptAllChanges, rejectAllChanges, undoLastChange, acceptSingleChange, rejectSingleChange, textStyle, font, fontColor, hiliteColor, link, fontSize, align, image, imageGallery, list, formatBlock, table, blockquote, textStyle],
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock', 'textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks'],
		['acceptAllChanges', 'rejectAllChanges'], ['acceptSingleChange', 'rejectSingleChange']

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	attributesWhitelist: { 'all': "class|data-cid|data-userid|data-username|date-time|title" },
	allowedClassNames: ".*",
	height: 500
};
const noteConfig = {
	codeMirror: CodeMirror,
	plugins: [font, fontColor, fontSize, image, align, imageGallery, list, formatBlock, table, blockquote],
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	allowedClassNames: ".*",
	attributesWhitelist: { 'all': "class|data-cid|data-userid|data-username|date-time|title" },
	height: 500,
	width: "100%"
};
function updateVulnEditors() {
	$.each($("[id^=vuln_]"), function(_id, obj) {
		let id = $(obj).attr("id");
		if(id.indexOf("header") != -1) {
			return;
		}
		if (id.indexOf("notes") == -1) {
			editorConfig.allowedClassNames=".*";
			editors[id] = suneditor.create(id, editorConfig)
			editors[id].onKeyDown = function(contents, core) {
				queueSave();
			}
		} else {
			editors[id] = suneditor.create(id, noteConfig)
			editors[id].disabled()
		}
	});
}
function updateStepEditors() {
	$.each($("[id^=step]"), function(_id, obj) {
		let id = $(obj).attr("id");
		if(id.indexOf("header") != -1) {
			return;
		}
		if (id.indexOf("notes") == -1) {
			editorConfig.allowedClassNames=".*";
			editors[id] = suneditor.create(id, editorConfig)
			editors[id].onKeyDown = function(contents, core) {
				queueSave();
			}
		} else {
			editors[id] = suneditor.create(id, noteConfig)
			editors[id].disabled()
		}

	});
}
function saveAllEditors(isComplete=false){
	
		let data = "action=save";
		let action="SaveChanges"
		
		if(isComplete){
			data = "action=complete";
			action="CompleteChanges"
		}
		data += `&prid=${prid}`;
		data += "&risk=" + encodeURIComponent(editors.risk.getContents());
		data += "&summary=" + encodeURIComponent(editors.appsum.getContents());
		data += "&risk_notes=" + encodeURIComponent(editors.risk_notes.getContents());
		data += "&sum_notes=" + encodeURIComponent(editors.appsum_notes.getContents());
		let index = 0;
		$.each($("[id^=vuln_]"), function(_id, obj) {
			let id = $(obj).attr("id");
			if(id.indexOf("header") != -1) {
				return;
			}
			data += "&" + id + "=" + encodeURIComponent(editors[id].getContents());
		});
		$.post(action, data).done(function(resp) {
			if (resp.result == "success") {
				$(".box-title").each((a, b) => {
					const title = b.innerHTML.split("*")[0]
					b.innerHTML = title
				})
				/*$.alert({
					title: "Saved",
					content: "Peer Review Has been Saved."
				});*/
			} else if (resp.errors) {
				let errors = resp.errors;
				let bullets = "<ul>";
				$.each(errors, function(_id, error) {
					bullets += "<li>" + error + "</li>";
				});
				bullets += "</ul>";
				$.alert({
					title: 'Alert!',
					content: bullets,
				});
			}else if(resp.message){
				$.alert({
					title: 'Error!',
					content: resp.message,
				});
			}else if(resp.result == "complete"){
				document.location = `Assessment?id=app${asmtId}#tab_3`;
			}
		})
}

let editorTimeout = {};
function queueSave() {
	$(".box-title").each((a, b) => {
		const title = b.innerHTML.split("*")[0]
		b.innerHTML = title + " *"
	})
	clearTimeout(editorTimeout);
	editorTimeout = setTimeout(() => {
		saveAllEditors();
	}, 2000)

}
$(function() {


	editors['appsum'] = suneditor.create('appsum', editorConfig)
	editorConfig.allowedClassNames=".*";
	editors.appsum.onKeyDown = function(contents, core) {
		queueSave();
	}
	editors['risk'] = suneditor.create('risk', editorConfig)
	editorConfig.allowedClassNames=".*";
	editors.risk.onKeyDown = function(contents, core) {
		queueSave();
	}
	editors['risk_notes'] = suneditor.create('risk_notes', noteConfig)
	editors['risk_notes'].disabled()
	editors['appsum_notes'] = suneditor.create('appsum_notes', noteConfig)
	editors['appsum_notes'].disabled()

	updateVulnEditors()
	updateStepEditors()


	$(".save, .complete").click(function() {
		if ($(this).hasClass("complete")){
			saveAllEditors(true);
		}else{
			saveAllEditors(false);
		}

	});
	$(".closeit").click(function() {
		$.confirm({
			title: 'Confirm!',
			content: 'Unsaved changes will be lost!',
			buttons: {
				confirm: function() {
					window.history.back();
				},
				cancel: function() {
				}
			}

		});

	});
	$(".complete2").click(function() {
		let data = "action=complete";
		data += `&prid=${prid}`;
		data += "&risk=" + encodeURIComponent(editors.risk.getContents());
		data += "&summary=" + encodeURIComponent(editors.appsum.getContents());
		data += "&risk_notes=" + encodeURIComponent(editors.risk_notes.getContents());
		data += "&sum_notes=" + encodeURIComponent(editors.appsum_notes.getContents());
		let index = 0;
		$.each($("[id^=vuln_]"), function(_id, obj) {
			let id = $(obj).attr("id");
			data += "&" + id + "=" + encodeURIComponent(editors[id].getContents());
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
			data += "&" + id + "=" + encodeURIComponent(step);
		});

		$.post("CompleteChanges", data).done(function(resp) {
			if (resp.result == "success") {
				document.location = `Assessment?id=app${asmtId}#tab_3`;
			} else {
				let errors = resp.errors;
				let bullets = "<ul>";
				$.each(errors, function(_id, error) {
					bullets += "<li>" + error + "</li>";
				});
				bullets += "</ul>";
				$.alert({
					title: 'Alert!',
					content: bullets,
				});
			}
		})
	});

	setup(2000);
	$(".nav-tabs").click(function() {
		console.log("resize");
		setup(500);

	});



});
function setup(time) {
	setTimeout(() => {

	}, time)
}