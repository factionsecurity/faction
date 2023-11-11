<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
<bs:mco colsize="6">
<bs:row>
<bs:inputgroup colsize="4" name="App ID: " id="appId"></bs:inputgroup>
<bs:inputgroup colsize="4" name="App Name: " id="appName"></bs:inputgroup>
<bs:select name="Engagement Contact" colsize="4" id="engName">
	<s:iterator value="engagement">
       <option><s:property value="fname"/> <s:property value="lname"/></option>
    </s:iterator>
</bs:select>
<bs:select name="Remediation Contact" colsize="4" id="remName">
	<s:iterator value="remediation">
       <option><s:property value="fname"/> <s:property value="lname"/></option>
    </s:iterator>
</bs:select>
 <s:iterator value="custom">
			 <div class="col-md-4">
			 <div class="form-group">
			     <label><s:property value="key"/>:</label>
			       <input type="text" class="form-control" id="cust<s:property value="id"/>"/>
			   </div><!-- /.form group -->
			 </div>
</s:iterator>

<bs:select name="Select Team:" colsize="4" id="teamName">
 	<s:iterator value="teams">
 		<option><s:property value="name"/></option>
	</s:iterator>
</bs:select>

<bs:select name="Assessment Type:" colsize="4" id="assType">
 	<s:iterator value="assessmentType">
	   <option><s:property value="type"/></option>
	</s:iterator>
</bs:select>
<bs:dt name="Start and End Date:" colsize="4" id="reservation"></bs:dt>

</bs:row>
<bs:row>
<bs:mco colsize="12">
<div class="form-group">
     <label>Select Assessor</label>
     <select multiple class="form-control" id="assessors">
       
     </select>
 </div>
</bs:mco>
</bs:row>
<bs:row>
<bs:button size="md" color="primary" colsize="6" text="<i class='glyphicon glyphicon-plus'></i>Add Assessment to Calendar" id="AddAssessment"></bs:button>
</bs:row>
</bs:mco>
<div class="col-md-6">
	<div class="box box-primary">
        <div class="box-body no-padding">
          <!-- THE CALENDAR -->
          <div id="calendar"></div>
        </div><!-- /.box-body -->
     </div><!-- /. box -->
</div>

</bs:row>

