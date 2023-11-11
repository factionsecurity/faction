import 'jquery'
import 'suneditor'
var editors = {};
/*
/*function showTextEditor(id, name, toolbar){
	div = $('#'+id);
	html = $('#'+id).html();
	console.log(html);
	$(div).replaceWith("<textarea id=\"" + id + "\" name=\""+ name +"\" rows=\"10\" cols=\"80\" />");
	CKEDITOR.replace(name, {customConfig : 'ckeditor_config.js', toolbar: toolbar});
	CKEDITOR.instances[id].setData(html);
}
function showTextEditor(name, toolbar){
	//CKEDITOR.replace(name, {customConfig : 'ckeditor_config.js', toolbar: toolbar});
	editors["name"] = SUNEDITOR.create(document.getElementsByName(name)[0])
}
/*function getEditorText(id){
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
showTextEditor("editor1", "")
*/
