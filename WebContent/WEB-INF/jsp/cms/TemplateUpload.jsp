<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<html>
<body>
<bs:row>
<bs:inputgroup name="Name" colsize="6" id="name">${selectedTemplate.name}</bs:inputgroup>
<bs:select name="Team" colsize="6" id="team">
	<s:iterator value="teams">
		<s:if test="selectedTemplate.team.id == id">
			<option selected="selected" value="${id }">${teamName}</option>
		</s:if>
		<s:else>
			<option  value="${id }">${teamName}</option>
		</s:else>
	</s:iterator>
</bs:select>
</bs:row>
<bs:row>
<!--  ${selectedTemplate.variable} -->

<bs:mco colsize="12">
	<s:if test="selectedTemplate.retest">
		<input type="checkbox" id="retest" checked />&nbsp;&nbsp;Retest Template
	</s:if>
	<s:else>
		<input type="checkbox" id="retest"/>&nbsp;&nbsp;Retest Template
	</s:else>

</bs:mco>
</bs:row>
<br>
<bs:row>
<bs:select name="Assessment Type" colsize="12" id="type">
	<s:iterator value="types">
		<s:if test="selectedTemplate.type.id == id">
			<option value="${id }" selected="selected">${type}</option>
		</s:if>
		<s:else>
			<option value="${id }">${type}</option>
			
		</s:else>
	</s:iterator>
</bs:select>
</bs:row>
<bs:row>
<form enctype="multipart/form-data" action="cms" id="imgForm" method="POST" style="display:none">
	<input type="hidden" id="id" value="${id}" name="id"/>
	<input type="hidden" id="action" value="templateSave" name="action"/>
	
		<div class="col-md-12">
                <label class="control-label">Select Docx File</label>
    			<input id="image" type="file" name="file_data"/>
        </div>
	
	
	<bs:mco colsize="12">
		<br>
		<span><i> Currently Uploaded to : <br>${selectedTemplate.filename }</i></span>
	</bs:mco>
	
</form>
</bs:row>
<script src="../plugins/iCheck/icheck.min.js"></script>
<script src="../fileupload/js/fileinput.min.js" type="text/javascript"></script>
<script>
$(function(){
	
	$("#image").fileinput({
		 allowedFileExtensions : ['docx'],
		previewFileExtSettings: { // configure the logic for determining icon file extensions
	        'doc': function(ext) {
	            return ext.match(/(doc|docx)$/i);
	        },
	        'xls': function(ext) {
	            return ext.match(/(xls|xlsx)$/i);
	        },
	        'ppt': function(ext) {
	            return ext.match(/(ppt|pptx)$/i);
	        },
	        'zip': function(ext) {
	            return ext.match(/(zip|rar|tar|gzip|gz)$/i);
	        },
	        'txt': function(ext) {
	            return ext.match(/(txt|csv)$/i);
	        },
	       	'pdf': function(ext) {
            return 	ext.match(/(pdf)$/i);
        }
	    },
	   preferIconicPreview: true, 
		 previewFileIconSettings: {
	        'doc': '<i class="fa fa-file-word-o text-primary"></i>',
	    }
	
	
	  });
});
</script>
</body>
</html>