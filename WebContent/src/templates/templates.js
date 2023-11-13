require('suneditor/dist/css/suneditor.min.css');
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import 'select2';
import suneditor from 'suneditor';
import colorPicker from 'suneditor/src/plugins/modules/_colorPicker';
import plugins from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';

var editorOptions = {
	codeMirror: CodeMirror,
	plugins: plugins,
	buttonList: [
		['undo', 'redo','fontSize', 'formatBlock','textStyle'],
		['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
		['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
		['link', 'image', 'fullScreen', 'showBlocks'],

	],
	defaultStyle: 'font-family: arial; font-size: 18px',
	height: 900,
	width: "100%"
};


$(function() {
	global._token = $("#_token")[0].value;
	let editor = suneditor.create("templateEditor", editorOptions);
	let table = $('#templateTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		columnDefs: [
		],

	});
	function ShowSummary(templateId){
		$.get('tempSearchDetail?tmpId=' + templateId).done(function(data) {
			const text = data.templates[0].text;
			editor.setContents(text);
		});
	}
	$("#templateTable").on('click', 'tbody tr', (event) =>{
			const templateId = parseInt(event.currentTarget.id.replace("template", ""));
			ShowSummary(templateId);
	});
});