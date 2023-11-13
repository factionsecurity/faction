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
import 'jquery-confirm';



$(function() {
	let editorOptions = {
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
		width: "100%",
		callBackSave: function(){
			saveIt()
		}
	};
	global._token = $("#_token")[0].value;
	let editor = suneditor.create("templateEditor", editorOptions);
	editor.onInput = function(contents, core){
		$("#edits").html("*");
	}
	editor.onSave = function(){
		console.log("save")
	}
	global.table = $('#templateTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true
	});
	function ShowSummary(templateId){
		$.get('tempSearchDetail?tmpId=' + templateId).done(function(data) {
			const text = data.templates[0].text;
			editor.setContents(text);
		});
	}
	$("#templateTable").on('click', 'tbody tr',function(event){
			const templateId = parseInt(event.currentTarget.id.replace("template", ""));
			$(".selected").each( (_index, tr) => $(tr).removeClass("selected"))
			$(event.currentTarget).addClass("selected");
			let data = table.row(this).data()
			$("#templateName").html(data[1])	
			ShowSummary(data[0]);
	});
	$("#createTemplate").on('click', async (event) =>{
		$.confirm({
			title: "New Template",
			content: "Enter a Template name: <input id='tempName' class='form-control'></input><br>" +
				"Select a type <select id='templateType' class='form-control select2'><option>summary</option><option>risk</option></select>",
			buttons: {
				create: function(){
					const templateName = $("#tempName").val();
					const type = $("#templateType").val()
					let data = `term=${templateName}`
					data += "&summary=";
					data += `&type=${type}`;
					data += "&global=true";
					data += "&active=true";
					data += "&_token=" + global._token;
					$.post("tempSave", data).done(function(resp) {
						_token = resp.token;
						location.reload()
						const template = resp.templates[0]
						table.row.add([template.tmpId, template.title,template.type, template.created, template.user, 
						"<input type=checkbox class='activeCheckBox' checked/>",
						'<span class="vulnControl vulnControl-delete"><i class="fa fa-trash" title="Delete Template"></i></span></td>'
						]).draw(false)
					});
					
				},
				cancel: function(){}
			}
			
		})
		
	});
	$("#saveTemplate").on('click', async (event) =>{
		saveIt()
	});
	function saveIt(){
		let tr=$(".selected")[0]
		let rowData = table.row(tr).data()
		const templateName = rowData[1]
		const type = rowData[2]
		let data = `term=${templateName}`
		data += "&summary=" + encodeURIComponent(editor.getContents());
		data += `&type=${type}`;
		data += "&global=true";
		data += "&active=true";
		data += "&_token=" + global._token;
		$.post("tempSave", data).done(function(resp) {
			_token = resp.token;
			$("#edits").html("");
		});
		
	}
	
	$(".vulnControl-delete").on('click', async (event) =>{
		const row = event.currentTarget.parentElement.parentElement
		const rowData = table.row(row).data()
		
		$.confirm({
			title: "Confirm?",
			content: "Are you sure you want to delete these templates?<br><b>"+rowData[1]+"</b>",
			buttons: {
				confirm: async function() {
					let messages=[]
					$.post(`tempDelete`, `tmpId=${rowData[0]}`).done(function(resp) {
						_token=resp.token;
						table.row(row).remove().draw()		
						alertMessage(messages[0], "Templates Deleted.");
					});
				},
				cancel: function() { }
			}
		});
	});
	$(".activeCheckBox").on('change', function(event){
		const row = event.currentTarget.parentElement.parentElement
		const rowData = table.row(row).data()
		const active = this.checked
		
			$.post(`tempActive`, `tmpId=${rowData[0]}&active=${active}`).done(function(resp) {
				_token=resp.token;
			});
		
	})
});