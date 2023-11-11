<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="../../header.jsp" />
<style>

.page {
cursor: pointer;
}
.page:hover{
	 font-weight: bold;
}
.css{
width:100%;
height: 700px;
}
</style>
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="../codemirror/lib/codemirror.css">
<link rel="stylesheet" href="../codemirror/addon/hint/show-hint.css">
<link rel="stylesheet" href="../codemirror/theme/rubyblue.css">
<script src="../codemirror/lib/codemirror.js"></script>
<script src="../codemirror/addon/edit/matchbrackets.js"></script>
<script src="../codemirror/mode/python/python.js"></script>
<style>
.CodeMirror {


 /* Set height, width, borders, and global font properties here */
    font-family: monospace;
    height: 150px;
}
.highlight{
	
	background-color: #3c8dbc;
	border-radius: 5px;
	color:white;
	
}

.modules{
	padding:10px 5px 10px 5px;
	
	font-size: Large;
	border-bottom: 1px solid;
	border-color: #E5E4E2;

}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="fa fa-code"></i> Integration API
      <small>Integrate with other products via python</small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  <jsp:include page="IntegrationUI.jsp"></jsp:include>
  
  
  
  <jsp:include page="../../footer.jsp" />
 
    <!-- DataTables -->
    <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="../plugins/fastclick/fastclick.min.js"></script>
    <script src="//cdn.ckeditor.com/4.15.1/standard/ckeditor.js"></script>
    <script src="../plugins/iCheck/icheck.min.js"></script>
    <script src="../fileupload/js/fileinput.min.js" type="text/javascript"></script>
    <script>
    var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
        mode: {name: "python",
               version: 3,
               singleLineStringErrors: false},
        lineNumbers: true,
        indentUnit: 4,
        matchBrackets: true
    });
    editor.setOption("theme", "rubyblue");
    var args = CodeMirror.fromTextArea(document.getElementById("args"), {
        mode: {name: "python",
               version: 3,
               singleLineStringErrors: false},
        lineNumbers: true,
        indentUnit: 4,
        matchBrackets: true,
        readOnly: true
    });
    args.setOption("theme", "rubyblue");
    var retArgs = CodeMirror.fromTextArea(document.getElementById("retArgs"), {
        mode: {name: "python",
               version: 3,
               singleLineStringErrors: false},
        lineNumbers: true,
        indentUnit: 4,
        matchBrackets: true,
        readOnly: true
    });
    retArgs.setOption("theme", "rubyblue");
    var Console = CodeMirror.fromTextArea(document.getElementById("console"), {
        mode: {name: "python",
               version: 3,
               singleLineStringErrors: false},
        lineNumbers: true,
        indentUnit: 4,
        matchBrackets: true,
        readOnly: true
    });
    Console.setOption("theme", "rubyblue");
    var selectedModule="";
    $(function(){
    	
	    $($("#code").next()).css("height", "600px");
	    $($("#console").next()).css("height", "600px");
	    $('input[type="checkbox"]').iCheck({
	        checkboxClass: 'icheckbox_minimal-red',
	        radioClass: 'iradio_minimal-red'
	      });
	    <s:iterator value="integrations" status="stat" >
	    	
	    	<s:if test="enabled==true">
	    		$("#check_<s:property value="name"/>").iCheck('check');
	    	</s:if>
	    	
	    </s:iterator>
	   
	    
	    $('.icheckbox_minimal-red').css("float","right");
	    
	    $('input[type="checkbox"]').on('ifChanged', function (event) {
	        var checked = $(this).is(":checked");
	        var module = $(this).attr("id").replace("check_","");
	        var data = "action=checked&module=" + module + "&enabled=" + checked;
	        $.post("Integration", data).done(function(){});
	    });
	    
	   $(".savebtn").click(function(){
		   var enabled = "false";
		   if($("#check_"+selectedModule).is(":checked"))
			   enabled="true";
		   
		   var data = "action=save";
		   data += "&code=" + encodeURIComponent(editor.getValue());
		   data += "&module=" + selectedModule;
		   data += "&enabled=" +enabled;
		   $.post("Integration", data).done(function(resp){
			   
		   });
		   
	   });
	   $(".testbtn").click(function(){
		   $("#testModal").modal("show");
		   Console.setValue("Running..."); 
		   var enabled="false";
		   if($("#check"+selectedModule.replace("mod","")).is(":checked"))
			   enabled="true";
		   var data = "action=save";
		   data += "&code=" + encodeURIComponent(editor.getValue());
		   data += "&module=" + selectedModule;
		   data += "&enabled=" +enabled;
		   $.post("Integration", data).done(function(resp){
			   var data = "action=run";
			   data += "&module=" + selectedModule;
			   $.post("Integration", data).done(function(resp){
				   Console.setValue(decodeURIComponent(resp.output));  
			   }).error(function(){
				   Console.setValue("Unknown Error!"); 
			   });
			   
		   });
		   
	   });
	   
	   
	   $("div[id^='mod']").click(function(){
		   selectedModule=$(this).attr("id");
		   $("div[id^='mod']").removeClass('highlight');
		   $(this).addClass('highlight');
		   var data = "action=get";
		   data += "&module=" + selectedModule;
		   $.post("Integration", data).done(function(resp){
			   var code = resp.code;
			   editor.setValue(decodeURIComponent(code));
			   var inputs="";
			   var outputs="";
			   resp.inputs.forEach(function(value){
				   inputs += value+"\n"
			   });
			   args.setValue(inputs);
			   resp.outputs.forEach(function(value){
				   outputs += value+"\n";
			   });
			   retArgs.setValue(outputs);
			   
		   });
	   });
    });

    
    </script>
    


  </body>
</html>