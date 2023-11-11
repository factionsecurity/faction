
require('suneditor/dist/css/suneditor.min.css');
require('../scripts/fileupload/css/fileinput.css');
import suneditor from 'suneditor';
import plugins from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
import {} from 'jquery';
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap'
import 'jquery-ui';
import 'jquery-confirm';
import {} from 'icheck'

const editorOptions = {
	codeMirror: CodeMirror,
	plugins: plugins,
	buttonList: [
		['undo', 'redo', 'font', 'fontSize', 'formatBlock', 'textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks', 'codeView'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	height: 500,
	width: "100%"
};

const editorDisabled = {
	codeMirror: CodeMirror,
	defaultStyle: 'font-family: arial; font-size: 18px',
	buttonList: ['codeView'],
	height: 500,
	width: "100%",
	readOnly: true
};
const editors = {
	notes: suneditor.create("notes", editorDisabled),
	failNotes: suneditor.create("failnotes", editorOptions)
}

function getFiles() {
	$("#files").fileinput('destroy');
	$.get(`RemVulnData?action=getFiles&vulnId=${vulnId}`).done(function(data) {
		$("#files").unbind();
		$("#files").fileinput(data);
		downloadFile(vulnId);
	});

}

$(function() {
	editors['notes'].readOnly(true);
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
					data += `&notes=${encodeURIComponent(editors["failNotes"].getContents())}`;
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

					let data = `&notes=${encodeURIComponent(editors["failNotes"].getContents())}`;
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
					data += `&notes=${editors["failNotes"].getContents()}`;
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
					data += `&notes=${editors["failNotes"].getContents()}`;
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