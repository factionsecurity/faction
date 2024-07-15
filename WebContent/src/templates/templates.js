import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import 'select2';
import 'jquery-confirm';
import Editor from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell'
import '@toast-ui/editor/dist/toastui-editor.css';
import 'tui-color-picker/dist/tui-color-picker.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';



$(function() {
	global._token = $("#_token")[0].value;
	let editor = new Editor({
				el: document.querySelector('#templateEditor'),
				previewStyle: 'vertical',
				height: '600px',
				autofocus: false
			});
	
	global.table = $('#templateTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true
	});
	function setUpEvents(){
		$(".vulnControl-delete").off('click');
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
		$(".activeCheckBox").off('change');
		$(".activeCheckBox").on('change', function(event){
			const row = event.currentTarget.parentElement.parentElement
			const rowData = table.row(row).data()
			const active = this.checked
			
				$.post(`tempActive`, `tmpId=${rowData[0]}&active=${active}`).done(function(resp) {
					_token=resp.token;
				});
			
		})
		
	}
	setUpEvents();
	global.table.on('draw', function(){
		setUpEvents();
	});
	function ShowSummary(templateId){
		editor.off( 'change');
		$("#editorContainer").removeClass("disabled")
		$.get('tempSearchDetail?tmpId=' + templateId).done(function(data) {
			const text = data.templates[0].text;
			editor.setHTML(text);
			editor.on( 'change', function(t, e){
				$("#edits").html("*");
			})
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
		data += "&summary=" + encodeURIComponent(editor.getHTML());
		data += `&type=${type}`;
		data += "&global=true";
		data += "&active=true";
		data += "&_token=" + global._token;
		$.post("tempSave", data).done(function(resp) {
			_token = resp.token;
			$("#edits").html("");
		});
		
	}
	
});