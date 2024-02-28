<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
<bs:mco colsize="6">
	<bs:row>
		 <bs:select name="Remediation Contact:" colsize="4" id="remUser">
		 	<s:iterator value="remusers">
                      <option value="<s:property value="id"/>"><s:property value="fname"/> <s:property value="lname"/></option>
            </s:iterator>
		 </bs:select>
		 <bs:dt name="Start and End Date:" colsize="4" id="reservation"><s:property value="startStr"/> - <s:property value="endStr"/></bs:dt>
		
		 
	  </bs:row><!--  End of Top Row -->
	  
	  <!--  Add Notes and Actions section -->
	 <bs:row>
	 	<bs:mco colsize="12">
	 		<label>Verification Notes:</label>
            <textarea id="notes" name="notes" rows="10" cols="80"> 
            	   <s:property value="note"/>            
            </textarea>
	 	</bs:mco>
	 </bs:row>
	  <!--  Add Distribution list section -->
	 <bs:row>
	 	<bs:inputgroup colsize="12" name="Distribution List:" id="distlist"> <s:property value="distro"/></bs:inputgroup>
	 </bs:row>
		 <!-- BEGIN of Assessor Selections -->
	 <bs:row>

	 	<bs:select name="Select Assessor:" colsize="12" id="assessors">
			<s:iterator value="assessors">
	            <option value="<s:property value="Id"/>"><s:property value="fname"/> <s:property value="lname"/></option>
	        </s:iterator>
		 </bs:select>

     </bs:row>
     <bs:row>
       <bs:button size="lg" color="primary" colsize="12" text="<i class='glyphicon glyphicon-plus'></i> Save Verification" id="addVerification"></bs:button>
     </bs:row>
</bs:mco> <!--  End of Top col -->
<bs:mco colsize="2">
	<label>Upload Files:</label>
	<input id="files" type="file" multiple name="file_data"/>
</bs:mco>
<bs:mco colsize="4">
	<div class="box box-primary">
        <div class="box-body">
          <!-- THE CALENDAR -->
          <div id="calendar"></div>
        </div><!-- /.box-body -->
     </div><!-- /. box -->
</bs:mco>
</bs:row>