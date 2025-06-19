<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<style>
#notes{
	background-color: white;
}
div[id^='rtCust']{
	background-color: white;
}
</style>

 
<bs:row>
<bs:mco colsize="6">
 <bs:row>
 &nbsp;
 </bs:row>
 <bs:row>
   <bs:button size="lg" color="primary" colsize="2" text="<i class='glyphicon glyphicon-plus'></i> Save" id="AddAssessment"></bs:button>
 </bs:row>
 <bs:row>
 &nbsp;
 </bs:row>
	<bs:row>
		 <!--<bs:inputgroup name="App ID:" id="appId" colsize="3"><s:property value="currentAssessment.appId"/></bs:inputgroup>
		 <bs:button color="primary" size="md" colsize="1" text="<i class='glyphicon glyphicon-equalizer'></i>" id="random"></bs:button>-->
		 <bs:mco colsize="4">
		 <div class="form-group">
		 	<label>App ID: <b>*</b></label>
			 <div class="input-group">
			 	<s:if test="randId">
	  				<input type="text" id="appId" class="form-control" placeholder="Random" aria-describedby="random" value="<s:property value="currentAssessment.appId"/>">
	  			</s:if>
	  			<s:else>
	  				<input type="text" id="appId" class="form-control" placeholder="" aria-describedby="random" value="<s:property value="currentAssessment.appId"/>">
	  			</s:else>
	  			<span class="input-group-addon btn btn-primary" id="random"><i class='glyphicon glyphicon-equalizer'></i></span>
			</div>
		</div>
		</bs:mco>
		
		 <bs:inputgroup name="App Name: <b>*</b>" id="appName" colsize="4" ><s:property value="currentAssessment.name" /></bs:inputgroup>
		 <bs:dt name="Start and End Date: <b>*</b>" colsize="4" id="reservation" readOnly="${currentAssessment.isFinalized() }"><s:property value="startStr"/> to <s:property value="endStr"/></bs:dt>
		 <bs:select name="Engagement Contact: <b></b>" colsize="4" id="engName">
		 	<s:iterator value="engagement">
                      <option value="<s:property value="id"/>"><s:property value="fname"/> <s:property value="lname"/></option>
            </s:iterator>
		 </bs:select>
		 <bs:select name="Remediation Contact: <b></b>" colsize="4" id="remName" readOnly="${currentAssessment.isFinalized() }">
		 	<s:iterator value="remediation">
                      <option value="<s:property value="id"/>"><s:property value="fname"/> <s:property value="lname"/></option>
            </s:iterator>
		 </bs:select>
		
		 <s:iterator value="custom">
			<s:if test="fieldType < 3">
			 <bs:mco colsize="4">
			 <div class="form-group">
			     <label><s:property value="key"/>:</label>
			     	<s:if test="fieldType == 0">
			       		<input type="text" 
			       			class="form-control" 
			       			id="cust<s:property value="id"/>" 
			       			<s:if test="currentAssessment.finalized">readonly</s:if> 
			       			value="<s:property value="defaultValue"/>"/>
			       	</s:if>
			       	<!--  ${fieldType} -->
			     	<s:if test="fieldType == 1">
			       		<br><input type="checkbox" 
			       			class="icheckbox_minimal-blue" style="height:34px"
			       			id="cust<s:property value="id"/>" 
			       			<s:if test="currentAssessment.finalized">readonly</s:if> 
			       			<s:if test="defaultValue == 'true'">checked</s:if>/>
			       	</s:if>
			     	<s:if test="fieldType == 2">
			       		<select
			       			class='form-control select2 ' style='width: 100%;'
			       			id="cust<s:property value="id"/>" 
			       			<s:if test="currentAssessment.finalized">readonly</s:if> >
								<s:iterator value="defaultValue.split(',')" var="option">
									<option value="<s:property value="option"/>"><s:property value="option"/></option>
								</s:iterator>
			       			</select>
			       	</s:if>
			   </div><!-- /.form group -->
			 </bs:mco>
			</s:if>
		 </s:iterator>
		 
		 <bs:select name="Select Team: <b>*</b>" colsize="4" id="teamName" readOnly="${currentAssessment.isFinalized() }">
		 	<s:iterator value="teams">
	            <option value="<s:property value="id"/>"><s:property value="teamName"/></option>
	        </s:iterator>
		 </bs:select>
		 <bs:select name="Assessment Type: <b>*</b>" colsize="4" id="assType"  readOnly="${currentAssessment.isFinalized() }">
			<s:iterator value="assessmentTypes">
	            <option value="<s:property value="id"/>"><s:property value="type"/></option>
	        </s:iterator>
		 </bs:select>
		 <bs:select name="Select Campaign: <b></b>" colsize="4" id="campName"  readOnly="${currentAssessment.isFinalized() }" >
		 	<s:iterator value="campaigns">
		 		<s:if test="selected == true">
	            	<option value="<s:property value="id"/>"><s:property value="name"/></option>
	            </s:if>
	        </s:iterator>
		 	<s:iterator value="campaigns">
		 		<s:if test="selected == null || selected == false">
	            	<option value="<s:property value="id"/>"><s:property value="name"/></option>
	            </s:if>
	        </s:iterator>
		 </bs:select>
		
		 
		 
		 
	  </bs:row><!--  End of Top Row -->
	  <!--  Add Distribution list section -->
	 <bs:row>
	 	<bs:inputgroup colsize="12" name="Distribution List:" id="distlist"  readOnly="${currentAssessment.isFinalized() }"><s:property value="currentAssessment.DistributionList"/> </bs:inputgroup>
	 </bs:row>
	 <br>
		 <!-- BEGIN of Assessor Selections -->
	<bs:row>
	 <bs:mco colsize="11">
	     
	     <div class="form-group">
             <label>Search Assessors:</label>
             <div class="input-group">
             <input type="text" id="searchTerm" class="form-control" placeholder="" aria-describedby="searchList" value="">
			<span class="input-group-addon btn btn-primary" id="searchList"><i class='glyphicon glyphicon-search'></i></span>
        	<span class="input-group-addon btn btn-primary" id="clearList"><i class='fa fa-ban'></i></span>
        	</div>
		</div>
	     </bs:mco>
     </bs:row>
	 <bs:row>
		 <bs:mco colsize="11">
	     
	     <div class="form-group">
	                  <label>Find Assessors:</label>
	                  <select id="assessors" multiple="" class="form-control" <s:if test="currentAssessment.finalized">readonly</s:if>>
	                 
	                  </select>
	              
	     </div>
	     </bs:mco>
	      <div class="col-md-1" style="padding-top: 25px;"><button class="btn btn-md btn-primary" id="addAssessor"><span class="glyphicon glyphicon-plus" ></span></button></div>
     </bs:row>
     
     <bs:row>
     <bs:mco colsize="11">
     <div id="assessorList">
     <div class="form-group">
                  <label>Assigned Assessors: <b>*</b></label>
                  <select id="assessorListSelect" multiple="" class="form-control" <s:if test="currentAssessment.finalized">readonly</s:if>>
                   <s:iterator value="assessors">
	            		<option value="<s:property value="Id"/>"><s:property value="fname"/> <s:property value="lname"/> - <s:property value="team.TeamName"/> [ Assigned ]</option>
	        		  </s:iterator>
                  </select>
                </div>
     </div>
     </bs:mco>
    <div class="col-md-1" style="padding-top: 25px;"><button class="btn btn-md btn-danger" id="removeAssessor"><span class="glyphicon glyphicon-minus" ></span></button></div>
     </bs:row>
	  
	  <!--  Add Notes section -->
	 <bs:row>
	 	<bs:mco colsize="12">
	 		<label>Notes:</label>
            <div id="notes" name="notes" rows="10" cols="80" <s:if test="currentAssessment.finalized">disabled</s:if> > 
            	<s:property value="currentAssessment.AccessNotes"/>                
            </div>
	 	</bs:mco>
	 </bs:row>
	  
     <bs:row>
	     <bs:mco colsize="12">
	     <div class="form-group">
	     	<label>Upload Files</label>
	    	<input id="files" type="file" multiple name="file_data" <s:if test="currentAssessment.finalized">readonly</s:if>/>
	     </div>
	     </bs:mco>
	     
	     <br>
     </bs:row>
     <br>
	 <s:iterator value="custom">
		<s:if test="fieldType == 3">
		 <bs:mco colsize="12">
			 <div class="form-group">
				 <label><s:property value="key"/>:</label>
					<div id="rtCust<s:property value="id"/>" <s:if test="currentAssessment.finalized">disabled</s:if> > 
			       		<s:property value="defaultValue"/>
					</div>
			</div>
		</bs:mco>
		</s:if>
		</s:iterator>
			 
</bs:mco> <!--  End of Top col -->

<bs:mco colsize="6">
	<bs:row>
	<bs:mco colsize="12">
	<div class="box box-primary">
        <div class="box-body">
          <!-- THE CALENDAR -->
          <div id="calendar"></div>
        </div><!-- /.box-body -->
     </div><!-- /. box -->
     </bs:mco>
    
     
     <bs:mco colsize="12">
					<div class="fsgannt" ></div>
	</bs:mco>
	</bs:row>
</bs:mco>

</bs:row>
