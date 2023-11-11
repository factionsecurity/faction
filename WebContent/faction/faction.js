//import SUNEDITOR from 'suneditor'
//import CKEDITOR from 'ckeditor'
//import * as $ from 'jquery'
//import './dashboard'
let ooo_color = "#27ae60";
let edit_color = "#95a5a6";
let asmt_color = "#c0392b";
let ver_color = "#e67e22";

let editors = {}
function showTextEditor(id, name, toolbar){
	div = $('#'+id);
	html = $('#'+id).html();
	$(div).replaceWith("<textarea id=\"" + id + "\" name=\""+ name +"\" rows=\"10\" cols=\"80\" />");
	//CKEDITOR.replace(name, {customConfig : 'ckeditor_config.js', toolbar: toolbar});
	//CKEDITOR.instances[id].setData(html);
	editors[name] = SUNEDITOR.create(document.getElementById(id))
	editors[name].setContent(html)
	
}
/*function showTextEditor(name, toolbar){
	CKEDITOR.replace(name, {customConfig : 'ckeditor_config.js', toolbar: toolbar});
	editors["name"] = SUNEDITOR.create(document.getElementsByName(name)[0])
}*/
function getEditorText(id){
	if( typeof CKEDITOR.instances[id]  == 'undefined'){
		return $('#'+id).html();
	}else{
		return CKEDITOR.instances[id].getData();
	}
}
function setEditorText(id, data){
	if( typeof CKEDITOR.instances[id]  == 'undefined'){
		$('#'+id).html(data);
	}else{
		CKEDITOR.instances[id].setData(data);
	}
}

