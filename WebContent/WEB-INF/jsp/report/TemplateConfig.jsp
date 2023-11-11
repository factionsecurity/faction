<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<html>
<head>

</head>
<body>
<bs:row>
	<bs:inputgroup name="Report Name: *" colsize="12" id="reportName">${currentMap.reportName}</bs:inputgroup>
	<s:if test="id != -1">
	<div class="col-md-12">
		<div class="form-group">
		<label>Default Vulnerability: </label>
		<input type="text" class="form-control pull-right" id="dtitle" value="${currentMap.defaultVuln.name}" intVal="${currentMap.defaultVuln.id}" style="z-index:99999999">
		</div>
	</div>
	<bs:inputgroup name="Starting XML Parameter: *" colsize="12" id="listname">${currentMap.listname}</bs:inputgroup>
	</s:if>
</bs:row>

<br/>
<s:if test="id != -1">
<bs:row>

<bs:mco colsize="12">
	<bs:datatable columns="XML Param, Faction Param, Has Elements, Base64 Encoded, Delete" classname="" id="config">
	<s:iterator value="currentMap.mapping" var="map">
		<tr>
			<td><input value='${map.param }' id="param${map.id }"/></td><td>
			<select id="select${map.id }">
			<s:iterator value="props" var="mmm">
				<s:if test="#map.prop.value == #mmm.value">
					<option value="${mmm.value }" selected>
				</s:if>
				<s:else>
					<option value="${mmm.value }" >
				</s:else>
					${mmm }
				</option>
			</s:iterator>
			</select>
			</td>
			<td>
				<s:if test="#map.recursive==true">
				<input id="recurse${map.id }" type="checkbox" id="" checked/>
				</s:if>
				<s:else>
					<input  id="recurse${map.id }"type="checkbox" id="" />
				</s:else>
			</td>
			<td>
				<s:if test="#map.base64==true">
					<input  id="b64${map.id }" type="checkbox" id=""checked/>
				</s:if>
				<s:else>
					<input id="b64${map.id }" type="checkbox" id="" />
				</s:else>
			</td>
			<td><span class="fa fa-trash fa-danger" onClick="deleteMap(this, ${map.id})"></span>		
		</tr>
	</s:iterator>
	
	</bs:datatable>
</bs:mco>
</bs:row>
<bs:row>
<bs:button color="primary" size="md" colsize="6" text="<span class='fa fa-plus'></span> Add Row" id="addRow"></bs:button>

</bs:row>
</s:if>

 <script src="../dist/js/jquery.autocomplete.min.js"></script>
 
<script>

function getOptions(id){
var options="<select id='select" + id + "'>"
<s:iterator value="props" var="prop">
	options+="<option  value='${prop.value }' >${prop }</option>";
</s:iterator>
options+="</select>";
return options;
}
var table;
$(function(){
	 table = $("#config").dataTable({
		 paging: false,
		 searching:false
	 });

	$("#addRow").click(function(){
		 rand = Math.floor(Math.random() * 1000) + 1;
		 row = ["<input type=text class='newrow" + rand + "'/>", 
			 getOptions(rand), 
			 "<input type='checkbox' id='hasmore" + rand +"' />", 
			 "<input type='checkbox' id='base64" + rand +"'/>",
			 "<span class='fa fa-trash fa-danger' onClick='deleteRow(this)'/></span>"]
		 $("#config").DataTable().row.add(row).draw();
	});
	
	$("#dtitle").autoComplete({
		  minChars: 3,
		  source: function(term, response){
		        $.getJSON('DefaultVulns?action=json&terms=' + term, 
	    		        function(data){ 
  		        		vulns = data.vulns; 
  		        		list = [];
  		        		for(i=0; i < vulns.length; i++){
								list[i]=vulns[i].vulnId +" :: " +vulns[i].name + " :: " + vulns[i].category;
	    		        		}
  		        		console.log(list)
  		        		response(list);
  		        		}
		        );
		    },
		    onSelect: function(e, term, item){
		    	splits = term.split(" :: ");
	  		    $("#dtitle").val(splits[1]);
	  		    vulnid=splits[0];
	  		    $("#dtitle").attr("intVal", vulnid);

  		    }
  	  });
})

function deleteMap(el, mapid){
	data="id="+${id}
	data+="&mapid="+mapid;
	$.post("DeleteReportMapItem",data).done(function(){
		 $("#config").DataTable().row( $(el).parents('tr') )
			     .remove()
			     .draw();
	}
	);
	
}
function deleteRow(el){
	 $("#config").DataTable().row( $(el).parents('tr') )
		     .remove()
		     .draw();
}
</script>
</body>
</html>