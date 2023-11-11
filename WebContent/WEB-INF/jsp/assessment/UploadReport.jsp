<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<html>
<body>
<bs:row>
  <bs:mco colsize="12">
  	<bs:box type="success" title="Configured Reports">
  	<bs:row>
		<bs:mco colsize="12">
			<bs:datatable columns="Select, Name,Description" classname="" id="reports">
				<s:iterator value="reports">
					<tr>
					<td><input type="radio" name="select" value="${id}"/></td>
					<td><s:property value="reportName"/></td>
					<td><s:property value="desc"/></td>
					</tr>
				</s:iterator>
			</bs:datatable>
		</bs:mco>
	</bs:row>
	</bs:box>
	</bs:mco>
</bs:row>
<bs:row>
 <bs:mco colsize="12">
  	<bs:box type="success" title="Upload XML Report">
	<bs:mco colsize="12">
	  <div class="form-group">
	  	<label></label>
	 	<input id="vulnReport" type="file"  name="file_data"/>
	  </div>
	  </bs:mco>
	</bs:box>
	</bs:mco> 
</bs:row>
<script src="../plugins/datatables/jquery.dataTables.min.js"></script>
<script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
 <script src="../fileupload/js/fileinput.min.js" type="text/javascript"></script>

<script>

$(function(){
	 $("#vulnReport").fileinput({
		 overwriteInitial: false,
		 uploadUrl: "UploadVulnerabilityReport",
		 uploadAsync: true,
		 minFileCount: 1,
		 maxFileCount: 1,
		 allowedFileExtensions : ['xml'],
		 uploadExtraData: function (){
			 id=$("input[name=select]:checked").val();
			 return {'reportid': id, 'id' : ${id}};
		 }
	  });
	 
	 $('#vulnReport').on('filepreupload', function(event, data, previewId, index, jqXHR) {
			id=$("input[name=select]:checked").val();
			if(typeof id == 'undefined'){
				 $.alert("You Need to Select a Report Templete Above.");
				 return {
			           message: 'You Need to Select a Report Templete Above.'
			       };
		   }
	});
	 
	 $('#vulnReport').on('fileuploaded', function(event, data, previewId, index) {
		 console.log("uploaded");
		 window.location.reload();
	 });
	
});
</script>
