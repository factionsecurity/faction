import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import 'select2';
import 'jquery-confirm';
import {FactionEditor} from '../utils/editor';


let editor = new FactionEditor(-1);
$(function() {
	global._token = $("#_token")[0].value;
	
	editor.createEditor("templateEditor",false,()=>{});
	
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
	/*let editorTimeout={};	
	function queueSave(id, text) {
		$("#edits").html("*");
		clearTimeout(editorTimeout[id]);
		editorTimeout[id] = setTimeout(() => {
			saveIt(id, text);
		}, 2000);
	}
	function saveIt(id, text){
		let data = `tmpId=${id}`
		data += `&summary=${text}`;
		data += "&active=true";
		data += "&_token=" + global._token;
		$.post("globalSave", data).done(function(resp) {
			_token = resp.token;
			if(alertMessage(resp, "Saved!")){
				$("#edits").html("");
			}	
		
		})
		
	}*/

	function ShowSummary(templateId){
		$("#edits").html("");
		$("#editorContainer").removeClass("disabled")
		$.get('tempSearchDetail?tmpId=' + templateId).done(function(data) {
			editor.recreateEditor("templateEditor",data.templates[0].text,false,false,()=>{
				$("#edits").html("*");
			});
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
				"Select a type <select id='templateType' class='form-control select2'><option>summary</option><option>risk</option><option>custom field</option></select>",
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
		let data = `tmpId=${rowData[0]}`
		data += "&summary=" + encodeURIComponent(editor.getEditorText("templateEditor"));
		data += "&active=true";
		data += "&_token=" + global._token;
		$.post("globalSave", data).done(function(resp) {
			_token = resp.token;
			if(alertMessage(resp, "Saved!")){
				$("#edits").html("");
			}	
		
		})
		
	}
	
});