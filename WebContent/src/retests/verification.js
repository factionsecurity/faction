require('../scripts/fileupload/css/fileinput.css');
require('../loading/css/jquery-loading.css');
import Editor from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell'
import '@toast-ui/editor/dist/toastui-editor.css';
import 'tui-color-picker/dist/tui-color-picker.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
import {} from 'jquery';
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap'
import 'jquery-ui';
import 'jquery-confirm';
import {} from 'icheck'
import { marked } from 'marked';
let initialHTML={}
function createEditor(id, disabled){
	initialHTML[id] = entityDecode($(`#${id}`).html());
	$(`#${id}`).html("");
	let editor = new Editor({
			el: document.querySelector(`#${id}`),
			previewStyle: 'vertical',
			height: 'auto',
			autofocus: false,
			height: '560px',
			viewer: disabled,
			plugins: [colorSyntax, tableMergedCell]
		});
	editor.hide();
	editor.setHTML(initialHTML[id], false);
	initialHTML[id] = editor.getHTML();
	editor.show();
	if(disabled){
		editor.on('keydown', function(t,e) {
			if ( !((e.ctrlKey || e.metaKey) && e.key == 'c')) {
				e.preventDefault();
				throw new Error("Prevent Edit");
			 }
			
		});
	}
	return editor;
}


const editors = {
	notes: createEditor("notes", true), 
	failNotes: createEditor("failnotes", false),
	description: createEditor("description", true),
	recommendation: createEditor("recommendation", true),
	details: createEditor("details", true)
}

function getFiles() {
	$("#files").fileinput('destroy');
	$.get(`RemVulnData?action=getFiles&vulnId=${vulnId}`).done(function(data) {
		$("#files").unbind();
		$("#files").fileinput(data);
		downloadFile(vulnId);
	});

}
function downloadFile(id) {
	$("#dl-" + id).click(function() {
		console.log("clicked");
		let id = $(this).attr("id").replace("dl-", "");
		document.getElementById('dlFrame').src = "../service/fileUpload?id=" + id;

	});
}

$(function() {
	getFiles()

	$("#open").click(function() {
		const url = `../service/Report.pdf?guid=${reportName}`;
		window.open(url, '_blank');
	});

	$("#save").click(function() {
		let pass = $('input:radio[name=r3]:checked').val();
		let msgText = pass == '1'? "<span style='color:green'>PASS</span>": "<span style='color:red' >FAIL</span>";
		let type =  pass == '1'? 'green': 'red';
		$.confirm({
			title: "Are you sure?",
			content: `This will ${msgText} the vulnerability re-test.`,
			type: type,
			buttons: {
				confirm: function() {
					let pass = $('input:radio[name=r3]:checked').val();

					let data = "pass=" + pass;
					data += `&notes=${encodeURIComponent(editors["failNotes"].getHTML())}`;
					data += `&vid=${vulnId}`;
					data += `&ver=${verificationId}`;
					data += "&action=submit";
					$.post("Verifications", data).done( () => {

						document.location = "Verifications";

					});
				},
				cancel: function() { }
			}
		});
	});


	$("#cancel").click(function() {
		$.confirm({
			title: "Are you sure?",
			content: "This will remove the vulnerability from your Queue. It will need to be reassigned to close. <br> <br><b>Are you sure?</b>",
			type: 'red',
			buttons: {
				confirm: function() {

					let data = `&notes=${encodeURIComponent(editors["failNotes"].getHTML())}`;
					data += `&vid=${vulnId}`;
					data += `&ver=${verificationId}`;
					$.post("CancelVerification", data).done( () => {
						//document.location = "Verifications";
						$.alert({
							title: "Success",
							content: "The verification has been cancelled .",
							close: function() {
								document.location = "Verifications";
							}
						});

					});

				},
				cancel: function() { }
			}
		});
	});

	$("#saved").click(function() {
		$.confirm({
			title: "Are you sure?",
			content: "This will set the vulnerability state to <b>fixed</b> in the development environment. <br> <br><b>Are you sure?</b>",
			buttons: {
				confirm: function() {
					let pass = $('input:radio[name=r3]:checked').val();

					let data = "pass=" + pass;
					data += `&notes=${editors["failNotes"].getHTML()}`;
					data += `&vid=${vulnId}`;
					data += `&ver=${verificationId}`;
					data += "&action=submitDev"
					$.post("Verifications", data).done( () => {
						//document.location = "Verifications";
						$.alert({
							title: "Success",
							content: "Issue is marked closed in the development environment.",
							close: function() {
								document.location = "Verifications";
							}
						});

					});
				},
				cancel: function() { }
			}
		});
	});
	$("#savep").click(function() {
		$.confirm({
			title: "Are you sure?",
			content: "This will set the vulnerability state to  <b>fixed</b> in the production environment and it will no longer be tracked. <br> <br><b>Are you sure?</b>",
			buttons: {
				confirm: function() {
					let pass = $('input:radio[name=r3]:checked').val();

					let data = "pass=" + pass;
					data += `&notes=${editors["failNotes"].getHTML()}`;
					data += `&vid=${vulnId}`;
					data += `&ver=${verificationId}`;
					data += "&action=submitProd"
					$.post("Verifications", data).done( () => {
						//document.location = "Verifications";
						$.alert({
							title: "Success",
							content: "Issue is marked closed in the production environment.",
							close: function() {
								document.location = "Verifications";
							}
						});

					});
				},
				cancel: function() { }
			}


		});

	});


});
function entityDecode(encoded){
	let textArea = document.createElement("textarea");
	textArea.innerHTML = encoded;
	return textArea.innerText;
	
}