<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<s:set var="hideit" value="(assessment.InPr || assessment.prComplete || assessment.finalized)" />


<div class="row">
  <div class="col-md-6">
              <!-- Horizontal Form -->
              <div class="box box-info">
                <div class="box-header with-border">
                  <h3 class="box-title"><i class="glyphicon glyphicon-user"></i> Contacts</h3>
                </div><!-- /.box-header -->
                <!-- form start -->
                <form class="form-horizontal">
                  <div class="box-body">
                    <div class="form-group">
                      <label for="assessor" class="col-sm-2 control-label">Assessor(s)</label>
                      <div class="col-sm-10">
                      	<input type="text" disabled="" class="form-control" id="assessor" value="<s:iterator value="assessment.assessor" ><s:property value="fname"/>&nbsp;<s:property value="lname"/>; </s:iterator>">
                        
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="engagement" class="col-sm-2 control-label">Project Manager</label>
                      <div class="col-sm-10">
                        <input type="text" disabled=""  class="form-control" id="engagement" value="<s:property value="assessment.engagement.fname"/>&nbsp;<s:property value="assessment.engagement.lname"/>">
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="remediation" class="col-sm-2 control-label">Remediation</label>
                      <div class="col-sm-10">
                        <input type="text" disabled="" class="form-control" id="remediation" value="<s:property value="assessment.remediation.fname"/>&nbsp;<s:property value="assessment.remediation.lname"/>">
                      </div>
                    </div>
                     <div class="form-group">
                      <label for="Distro" class="col-sm-2 control-label">Distro</label>
                      <div class="col-sm-10">
                        <input type="text" disabled=""  class="form-control" id="Distro" value="<s:property value="assessment.distributionList"/>">
                      </div>
                    </div>
                     <div class="form-group">
                      <label for="Distro" class="col-sm-2 control-label">Status</label>
                      <div class="col-sm-10">
                        <input type="text" disabled=""  class="form-control" id="status" value="<s:property value="assessment.status"/>">
                      </div>
                    </div>

                   
                    </div>
                  </div><!-- /.box-body -->
                </form>
              </div><!-- /.box -->

  <div class="col-md-6">
              <!-- Horizontal Form -->
              <div class="box box-primary">
                <div class="box-header with-border">
                  <h3 class="box-title"><i class="glyphicon  glyphicon-list-alt"></i> Assessment Info</h3>
                </div><!-- /.box-header -->
                <!-- form start -->
                <div class="form-horizontal">
                  <div class="box-body">
                    <div class="form-group">
                      <label for="type" class="col-sm-2 control-label">Type</label>
                      <div class="col-sm-10">
                        <input type="text" disabled="" class="form-control" id="type" value="<s:property value="assessment.type.type"/>">
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="team" class="col-sm-2 control-label">Team</label>
                      <div class="col-sm-10">
                        <input type="text" disabled="" class="form-control" id="team" value="<s:property value="assessment.assessor[0].team.teamName"/>">
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="campaign" disabled=""  class="col-sm-2 control-label">Campaign</label>
                      <div class="col-sm-10">
                        <input type="text" disabled=""  class="form-control" id="campaign" value="<s:property value="assessment.campaign.name"/>">
                      </div>
                    </div>
                    <div class="form-group">
	                    <label class="col-sm-2 control-label">Date range</label>
	                    <div class="col-sm-10">
	                    <div class="input-group ">
	                      <div class="input-group-addon ">
	                        <i class="fa fa-calendar"></i>
	                      </div>
	                      <input disabled=""  class="form-control pull-right" id="reservation" type="text" value='<s:property value="assessment.start"/> - <s:property value="assessment.end"/>'>
	                    </div><!-- /.input group -->
	                    </div>
                  </div>
                  <s:iterator value="assessment.customFields">
                  <div class="form-group">
                      <label for="team" class="col-sm-2 control-label" title="Variable: &#x24;{cf${type.variable}}">${type.key}</label>
                     
                      	<s:if test="!type.readonly" >
                      	 	<div class="col-sm-8">
                        		<input type="text" class="form-control" id="cust${id}" value='${value}' <s:if test="assessment.InPr || assessment.prComplete || assessment.finalized">disabled</s:if>>
                        	 </div>
                        	 <div class="col-sm-2">
                        	  <s:if test="assessment.InPr || assessment.prComplete || assessment.finalized">
		                      <button class="btn btn-default updateCF" for="${id}">Update</button>
		                      </s:if>
		                     </div>
                        </s:if>
                        <s:else>
                        	<div class="col-sm-8">
                        		<input type="text" class="form-control" value='${value}' disabled>
                        	</div>
                        	 	<div class="col-sm-2">
                        	 </div>
                        </s:else>
                   
                      
                    </div>
                  </s:iterator>
                     
                  </div><!-- /.box-body -->
                  
                </div>
              </div><!-- /.box -->
</div>
</div>

 <div class="row">
          <!-- SUMMARY Section -->
            <div class="col-md-12">
              <div class="box box-warning">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-edit"></i> High Level Summary <span id="summary_header" class="edited"></span><small></small></h3>
                  <div class="box-tools pull-right">
                  </div>
                </div><!-- /.box-header -->
                <div class="box-body pad">
                  <form >
                  	<bs:editor name="editor1" toolbar="Full" id="summary" clickToEnable="false" readonly="${hideit}">
                  	<s:property value="assessment.summary"  />
                  	</bs:editor>
                  </form>
                  <s:if test="!(hideit)">
                  <br>
                  <div class="row">
                  	<div class="col-md-3"><a class="btn btn-default saveTemp form-control" for="tempSearch1"><i class="fa fa-save"></i> Save Template</a></div>
                  	<div class="col-md-6"><input id="tempSearch1" class="form-control tempSearch" for="summary" placeholder="Search for Template" /></div>
                  	<div class="col-md-3"><button id="deleteTemp1" class="btn btn-default deleteTemp form-control" for="tempSearch1" disabled><i class="fa fa-trash"></i> Delete Template</button></div>
                  </div>
                  </s:if>
                  
                  
                </div>
              </div><!-- /.box -->
              </div>
  <!-- </div>
   <div class="row">  -->          
              
              <!-- Risk Analysis Section -->
              <div class="col-md-12">
              <div class="box box-danger">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-asterisk"></i> Detailed Summary / Risk Analysis<span id="risk_header" class="edited"></span><small></small></h3>
                  <div class="box-tools pull-right">
                  </div>
                </div><!-- /.box-header -->
                <div class="box-body pad">
                  <form>
                  	<bs:editor name="editor2" toolbar="Full" id="riskAnalysis" clickToEnable="false" readonly="${hideit}">
                  		<s:property value="assessment.riskAnalysis"  />
                  	</bs:editor>
                  </form>
                   <s:if test="!(hideit)">
                  <br>
                   <div class="row">
                  	<div class="col-md-3"><a class="btn btn-default saveTemp form-control" for="tempSearch2"><i class="fa fa-save"></i> Save Template</a></div>
                  	<div class="col-md-6"><input id="tempSearch2" class="form-control tempSearch " for="risk" placeholder="Search for Template" /></div>
                  	<div class="col-md-3"><button id="deleteTemp2" class="btn btn-default deleteTemp form-control" for="tempSearch2" disabled><i class="fa fa-trash"></i> Delete Template</button></div>
                  </div>
                  </s:if>
                </div>
              </div><!-- /.box -->
              </div>
              
 </div>
   <div class="row">            
              
              <!-- Notes Analysis Section -->
              <div class="col-md-12">
              <div class="box box-success">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-pencil"></i> Notes <span id="notes_header" class="edited"></span> <small>Not included in report</small></h3>
                   <div class="box-tools pull-right">
                  </div>
                </div><!-- /.box-header -->
                <div class="box-body pad">
                  <form>
                  	<bs:editor name="editor3" toolbar="Full" id="notes" clickToEnable="false" readonly="${hideit}">
                  		 <s:property value="assessment.Notes"/>
                  	</bs:editor>
                    <!--  <textarea id="notes" name="editor3" rows="10" cols="80" <s:if test="hideit">readonly</s:if>>
                                            <s:property value="assessment.Notes"/>
                    </textarea>-->
                  </form>
                </div>
              </div><!-- /.box -->
              </div>
              
 </div>
    <div class="row">            
              
              <!-- Engagement Notes Section -->
              <div class="col-md-12">
              <div class="box box-primary">
                <div class="box-header">
                  <h3 class="box-title"><i class="glyphicon glyphicon-pencil"></i> Engagement Info  <small>URLs, credentials, and other assessment information. Not included in report</small></h3>
                   <div class="box-tools pull-right">
                  
                  </div>
                </div><!-- /.box-header -->
                 <div class="col-md-6">
                <div class="box-body pad">
                  <form>
                  	<bs:editor name="engagmentnotes" toolbar="None" id="engagmentnotes" readonly="true" clickToEnable="false">
                  		  <s:property value="assessment.accessNotes"/>
                  	</bs:editor>
                    <!--
                    <textarea id="engagmentnotes" name="engagmentnotes" rows="10" cols="80" readonly>
                                            <s:property value="assessment.accessNotes"/>
                    </textarea>-->
                  </form>
                </div>
                </div>
                <div class="col-md-6">
                <div class="form-group">
                <br>
	    			<input id="files" type="file" multiple name="file_data" <s:if test="hideit">disabled</s:if>/>
	     		</div>
                </div>
              </div><!-- /.box -->
              </div>
              
 </div>