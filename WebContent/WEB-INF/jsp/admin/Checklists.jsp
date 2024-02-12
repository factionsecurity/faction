<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="../header.jsp" />
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>


<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-wrench"></i> CheckLists
      <small></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  <bs:row>
  <bs:mco colsize="12">
  
	  <bs:box type="" title="">
	  <bs:row>
	  <bs:button color="success" size="md" colsize="4" text="<span class='fa fa-plus'></span> Create new checklist" id="add"></bs:button>
	  </bs:row>
	  <br/>
	  <br/>
	  	<bs:row>
	  
		<bs:select name="Select a checklist:" colsize="4" id="list">
			<s:iterator value="lists">
				
				<option value="${id}"><s:property value="name"/></option>
			</s:iterator>
		</bs:select> 

		<div style="padding-top:25px">
		<bs:button color="primary" size="md" colsize="1" text="<span class='fa fa-edit'></span>" id="edit"></bs:button>
		<bs:button color="danger" size="md" colsize="1" text="<span class='fa fa-trash'></span>" id="delete"></bs:button>
		<bs:button color="primary" size="md" colsize="2" text="<span class='fa fa-download'></span> Download Checklist" id="export"></bs:button>
		<i class="fa fa-question-circle text-green" style="font-size:xx-large"></i>
		</div>
		</bs:row>
		<bs:row>
		<bs:mco colsize="12">
		<hr/>
			<bs:datatable columns="id,Question,Edit" classname="" id="questionTable">
				<s:iterator value="lists.get(0).questions">
					<tr><td>${id }</td>
					<td><textarea class="form-control" style="min-width: 100%" id="q${id }">  <s:property value="question"/>   </textarea></td>
					<td><button class="btn btn-primary" onclick="saveQuestion(${id })"><span class="fa fa-save"></span></button>
						<button class="btn btn-danger" onclick="deleteQuestion(this,${lists.get(0).id }, ${id })"><span class="fa fa-trash"></span></button>
					</td>
					</tr>
				</s:iterator>
				<s:if test="lists.get(0).questions">
				<tr>
				<td>Add New</td>
				<td><textarea class='form-control' style='min-width: 100%' id='newQuest'></textarea></td>
				<td>
				<button class='btn btn-success add-question' ><span class='fa fa-plus'></span></button>
				</td>
				</tr>
				</s:if>
			</bs:datatable>
		</bs:mco>
		</bs:row>
		<bs:row>
		<bs:mco colsize="6">
			<h3>Required For: </h3>
			<s:iterator value="types">
				<bs:mco colsize="6">
					<s:if test="id in lists.get(0).types" >
						<input class="types" type="checkbox" id="t${id }" tid="${id }" checked/><s:property value="type"/>
					</s:if>
					<s:else>
						<input class="types" type="checkbox" id="t${id }" tid="${id }"/><s:property value="type"/>
					</s:else>
				</bs:mco>
			</s:iterator>
		</bs:mco>
			<bs:mco colsize="6">
				<bs:row>
				<bs:mco colsize="6">
				<h2>Upload your checklist</h2>
				</bs:mco>
				
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<input id="checklist_file" type="file"  name="file_data"/>
					</bs:mco>
				</bs:row>
			</bs:mco>
			
		</bs:row>
	
	  </bs:box>
  
  
  </bs:mco>
  </bs:row>
  <iframe src="" id="download" style="width:0px;height:0px; border:0px"></iframe>
 
  <jsp:include page="../footer.jsp" />
  <script src="../dist/js/checklist.js"></script>
    </body>
</html>