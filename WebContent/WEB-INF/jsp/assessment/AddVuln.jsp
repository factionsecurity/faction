<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<!-- Controls -->
<s:set var="hideit" value="(assessment.InPr || assessment.prComplete || assessment.finalized)" />
<!--  hide it: ${hideit} -->
<s:set var="isFinal" value="(assessment.finalized)" />
<div class="row">
	<div class="col-md-12">
			<bs:row>
			&nbsp;
			</bs:row>
			<bs:row>
			<bs:mco colsize="2">
			<button id="addVuln" class="btn btn-block btn-primary btn-lg" <s:if test="hideit">disabled</s:if>><b><i class="glyphicon glyphicon-plus"></i> Add Vulnerability</b></button>
			</bs:mco>
			</bs:row>
			<bs:row>
			&nbsp;
			</bs:row>
	</div><!-- /.col -->
</div>
<style>

.circle {
	border-radius: 50%;
	width: 25px;
	height: 25px; 
	padding: 7px;
	padding-top:6px;
	font-size: small;
	color:white;
	z-index:100000;
	
}
.moveDown{
z-index:100000;
}
</style>

<!-- Findings  TABLE -->
<div class="row">
            <div class="col-xs-12">
              <div class="box box-danger">
                <div class="box-header">
                  <h3 class="box-title"><i class="fa fa-bug"></i> Vulnerability Findings</h3>&nbsp;&nbsp;<small><a id="removeFilter" style="cursor:pointer">Remove Search Filters</a></small>
                </div><!-- /.box-header -->
                <div class="box-body">
                <div class="moveDown">
                	<s:if test="!(assessment.InPr || assessment.prComplete || assessment.finalized)">
	                <span id="deleteMulti" class="fa fa-trash circle" style="background:#192338" title="Delete Multiple Vulns" ></span>
	                </s:if>
	                 <s:if test="acmanager && !(assessment.InPr || assessment.prComplete || assessment.finalized)">
	                <span id="reasign" class="fa fa-exchange circle" style="background:#192338" title="Reassign To Another Assessment" ></span>
	                <select class="select2 input-sm" id="re_asmtid" style="width: 500px"></select>
	                </s:if>
                </div>  
                  <table id="vulntable" class="table table-striped table-hover dataTable">
                    <thead class="theader">
                      <tr>
                       	<th></th>
                        <th>VulnID</th>
                        <th>Name</th>
                        <th>Category</th>
                        <th>Details?</th>
                        <th>Likelihood</th>
                        <th>Impact</th>
                        <th>Severity</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
					 <s:iterator  value="avulns">
					 	<tr id="showSteps<s:property value="id"/>" >
					 	<td><input type="checkbox" id="ckl<s:property value="id"/>"/></td>
						<td style="width:100px;"><a><s:property value="id"/></a></td>
						<td><s:property value="name" /></td>
						<td><s:property value="defaultVuln.category.name" /></td>
						<td><input type="checkbox" id="detailCbx${id}" onclick="return false;" <s:if test="steps.size() > 0">checked</s:if>></td>
						<td style="width:100px;" class="severity" data-sort="${likelyhood}"><s:property value="likelyhoodStr"/></td>
						<td style="width:100px;" class="severity" data-sort="${impact}"><s:property value="impactStr"/></td>
						<td style="width:100px;" class="severity" data-sort="${overall}"><s:property value="overallStr"/></td>
						<td style="width:100px;">
						<span class="vulnControl " id="vulnID<s:property value="id"/>" <s:if test="hideit && !isFinal">disabled</s:if>><i class="fa fa-edit" title="Edit Vulnerability"></i></span>
						<span class="vulnControl vulnControl-add" id="svulnID<s:property value="id"/>" <s:if test="hideit">disabled</s:if>><i class="fa fa-plus" title="Add Detail"></i></span>
						<span class="vulnControl vulnControl-delete" id="deleteVuln<s:property value="id"/>" <s:if test="hideit">disabled</s:if>><i class="fa fa-trash" title="Delete Vulnerability"></i></span></td>
						</tr>
					</s:iterator>
					</tbody>
                    <tfoot>
                    </tfoot>
                  </table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
       </div>
  </div>
 
 <!-- Technical descriptions  TABLE--> 
<div class="row">
            <div class="col-xs-12">
              <div class="box box-warning">
                <div class="box-header">
                  <h3 class="box-title"><i class="fa fa-bug"></i> Technical Findings</h3> <b><span id="vulnTitle"></span></b> <br><small>You must select a vulnerability above to view it's details.</small>
                </div><!-- /.box-header -->
                <div class="box-body">
                  <table id="stepstable" class="table  table-striped table-hover dataTable">
                    <thead class="theader">
                      <tr>
                        <th>Order</th>
                        <th>Preview</th>
                        <th>VulnID</th>
                        <th>Move</th>
                        <th style="width:100px;">Edit</th>
                        <th style="width:100px;">Delete</th>
                      </tr>
                    </thead>
                    <tbody id="steps">
					
					</tbody>
                    <tfoot>
                    </tfoot>
                  </table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
       </div>
 </div>

 	
 <style>
 .fa-caret-down{
 	position: absolute !important;
  top: 30% !important;
  right: 0;
  padding-right:10px;
 }
 .bcaret{
 	width:150px;
 }
 
 </style>
 
 <!-- VULN ENtrY Modal -->

 <div class="modal" id="vulnModal" >
   <div class="modal-dialog" style="width:75%">
     <div class="modal-content">
       <div class="modal-header bg-red">
         <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
         <h4 class="modal-title"><b><i class="fa fa-bug"></i> Vulnerability Entry Form</b>
         <s:if test="#hideit == false">
         	<button id="clearVuln" type="button" class="btn btn-default pull-right bg-navy-active" style="margin-right:100px; margin-left: 10px">Clear Form</button>
         	<button type="button" class="btn btn-primary pull-right" id="saveVuln2" style="margin-left: 10px"><i class="fa fa-plus"></i> Add Details</button>
         	<button type="button" class="btn btn-primary pull-right" id="saveVuln1"><i class="fa fa-save"></i> Save changes</button>
         </s:if></h4>
       </div>
       <div class="modal-body bg-red">
         <form>
                <div class="box-body">
                  <div class="form-horizontal">
                    <div class="form-group">
                      <label for="title" class="col-sm-2 control-label">Title: *</label>
                      <div class="col-sm-4">
                        <input type="text" class="form-control" id="title" placeholder="Vulnerbility Name">
                      </div>
                      <label for="title" class="col-sm-2 control-label">Overall Severity: *</label>
                      <div class="col-sm-4">
	                      <select class="select2 form-control" id="overall" style="width:100%">
                         <s:iterator value="levels" status="stat">
                      		  <s:if test="risk != null && risk != 'Unassigned' && risk != ''">
                        		  <option value="${stat.index}">${risk}</option>
                        	  </s:if>
                          </s:iterator>
                        </select>
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="dtitle" class="col-sm-2 control-label">Default Title: *</label>
                      <div class="col-sm-4">
                        <input type="text" class="form-control" id="dtitle" placeholder="Type to Search Vulnerbility Name" intVal="-1">
                      </div>

                      <label for="title" class="col-sm-2 control-label">Impact Severtity: *</label>
                      <div class="col-sm-4">
	                      <select class="select2 form-control" id="impact" style="width:100%">
                          <s:iterator value="levels" status="stat">
                      		  <s:if test="risk != null && risk != 'Unassigned' && risk != ''">
                        		  <option value="${stat.index}">${risk}</option>
                        	  </s:if>
                          </s:iterator>
                        </select>
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="category" class="col-sm-2 control-label">Category: *</label>
                      <div class="col-sm-4">
                        <input type="text" class="form-control" id="dcategory" placeholder="Auto Populated Category Name" intVal="-1">
                      </div>
                      <label for="title" class="col-sm-2 control-label">Likelihood Severity: *</label>
                      <div class="col-sm-4">
	                      <select class="select2 form-control" id="likelyhood" style="width:100%">
                         <s:iterator value="levels" status="stat">
                      		<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
                        		<option value="${stat.index}">${risk}</option>
                        	</s:if>
                        </s:iterator>
                      </select>
                      </div>
                    </div>
                  <br>
                   <s:if test="feedEnabled">
                   <div class="form-group">
                       
                      <label for="feedMessage" class="col-sm-2 control-label">Post to Live Feed <input type="checkbox" id="isFeedPost"/></label>
                      <div class="col-sm-10">
                        <input type="text" class="form-control" id=feedMsg placeholder="Look what I Found!">
                      </div>
                    </div>
                     </s:if>
                   </div>
                  
                   
                  
				   <div class="row">
                   <s:iterator value="vulntypes">
                   	<div class="col-md-4">
                    	<div class="form-group">
                   		<label title="Variable: &#x24;{cf${variable}}">${key}</label>
                        <textarea type="text" class="form-control pull-right" rows="3" id="type${id}"></textarea>
                      </div>
                    </div>
                   
                   	<!--<bs:inputgroup name="${key}" colsize="5" id="type${id}"></bs:inputgroup> <div class="col-sm-0.5"></div>-->
                   	
                   </s:iterator>
                   </div>
                   <br>
 					




                  
<div class="row">
                   
          <!-- Vuln Description Section -->
            <div class="col-md-12">
              <div class="box box-default">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-edit"></i> Vulnerability Description: *<small></small></h3>
                </div><!-- /.box-header -->
                <div class="box-body pad">
                  <div>
                  	<bs:editor name="editor4" toolbar="Full" id="description" clickToEnable="false">
                  		
                  	</bs:editor>
                  </div>
                </div>
              </div><!-- /.box -->
              </div>
  </div>
    <div class="row">
          <!-- Vuln Recommendation Section -->
            <div class="col-md-12">
              <div class="box box-info">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-edit"></i> Vulnerability Recommendation: *<small></small></h3>
                </div><!-- /.box-header -->
                <div class="box-body pad">
                  <div>
                  	<bs:editor name="editor5" toolbar="Full" id="recommendation" clickToEnable="false">
                  		
                  	</bs:editor>
                  </div>
                </div>
              </div><!-- /.box -->
              </div>
  </div>
                    
                  </div><!-- /.box-body -->
                </form> <!--  horiz form -->
       </div>
       <div class="modal-footer bg-red">
         <button type="button" class="btn btn-default pull-left" data-dismiss="modal">Close</button>
          <s:if test="#hideit == false">
          <button type="button" class="btn btn-primary" id="saveVuln"><i class="fa fa-save"></i> Save changes</button>
         <button type="button" class="btn btn-primary" id="saveVuln3"><i class="fa fa-plus"></i> Add Details</button>
          </s:if>
       </div>
     </div><!-- /.modal-content -->
   </div><!-- /.modal-dialog -->
 </div><!-- /.modal -->
 
 
 
  <!-- VULN STEP ENTRY Modal -->

 <div class="modal" id="stepModal" >
   <div class="modal-dialog" style="width:75%">
     <div class="modal-content">
     <form enctype="multipart/form-data" action="AddStep" id="stepForm" method="POST">
       <div class="modal-header bg-red">
         <button type="button" class="close step-close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
         <h4 class="modal-title"><b><i class="fa fa-bug"></i> Step Entry Form</b> <span id="stepTitle"></span>
          <s:if test="#hideit == false"><button type="button" id="clearStep" class="btn btn-default pull-right btn-info step-close" style="margin-right:100px">Clear Form</button>
          </s:if>
          </h4>
       </div>
       <div class="modal-body bg-red">
        
          <div class="row">
          <!-- Step Description Section -->
            <div class="col-md-12">
              <div class="box box-default">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-edit"></i> Step Description<small></small></h3>
                </div><!-- /.box-header -->
                <div class="box-body pad">
                  <div>
                  	<bs:editor name="editor6" toolbar="Full" id="step_description" clickToEnable="false">
                  		
                  	</bs:editor>
                    <!-- <textarea id="step_description" name="editor6" rows="5" cols="80">
                                           
                    </textarea>-->
                  </div>
                </div>
                <input type="hidden" name="vulnid" id="stepVulnId"/>
                <input type="hidden" name="action" id="stepAction"/>
                <input type="hidden" name="stepId" id="stepId"/>
              </div><!-- /.box -->
              </div><!-- .col -->
  			</div><!-- .row -->
  			<br>
                  <div class="row">
                  	<!--  <div class="col-md-3"><a class="btn btn-default saveTemp form-control" for="tempSearch3"><i class="fa fa-save"></i> Save Template</a></div>
                  	<div class="col-md-6"><input id="tempSearch3" class="form-control tempSearch" for="step_description" placeholder="Search for Template" /></div>
                  	<div class="col-md-3"><a id="deleteTemp3" class="btn btn-default deleteTemp form-control" for="extempSearch" disabled><i class="fa fa-trash"></i> Delete Template</a></div>
                  </div>-->
       </div>
       <div class="modal-footer bg-red">
         <button type="button" class="btn btn-default pull-left step-close" data-dismiss="modal">Close</button>
          <s:if test="#hideit == false">
          <button type="button" class="btn btn-primary fileinput-upload-button" id="saveStep"><i class="fa fa-save"></i> Save changes</button>
         	<button type="button" class="btn btn-primary pull-right" id="saveStep2"><i class="fa fa-plus"></i> Add Another</button>
          </s:if>
       </div>
       </form>
     </div><!-- /.modal-content -->
   </div><!-- /.modal-dialog -->
 </div><!-- /.modal -->
 
 
<script>
  function getValueFromId(id){
      switch(id){
          <s:iterator value="levels">
          case "${riskId}": return "${risk}";
          </s:iterator>
          default : return "Unassigned";
      }
  }
   function getIdFromValue(value){
      switch(value){
          <s:iterator value="levels">
          case "${risk}": return ${riskId};
          </s:iterator>
          default : return -1;
      }
  }
  </script>