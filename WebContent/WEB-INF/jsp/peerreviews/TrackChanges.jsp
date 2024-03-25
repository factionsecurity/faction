<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<jsp:include page="../header.jsp" />
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link rel="stylesheet" href="../plugins/ice.css">
<link href="../dist/css/throbber.css" media="all" rel="stylesheet" type="text/css" />
<script src="../plugins/ice_patched.js"></script>
<style>
.rating{
padding-left:0px;
}
.text-header{
	background: lightgray;
    color: black;
    padding-left: 10px;
    height: 20px;
}
.questions table {
	width: "100%";
}
.acceptGreen{
	color:green !important;
}
.rejectRed{
	color:red !important;
}
.lockUser{
	color:black
}
.disabled{
	background: lightgray;
}
.box-title u{
font-size: xx-large;
}
.meta {
font-weight: bold;
}
.catName {
color:white;
}


</style>
<style>

    input[type='radio'] { transform: scale(2); margin-left: 20px;padding: 40px;}
    </style>
<link href="../plugins/jquery-confirm/css/jquery-confirm.css" media="all" rel="stylesheet" type="text/css" />

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-eye-open"></i> Peer Review for <b>[${asmt.appId}] -  ${asmt.name }</b> - <i>${asmt.assessor[0].fname} ${asmt.assessor[0].lname}</i>
      <small></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  
  
<bs:row>
	<bs:mco colsize="12">
 <div class="nav-tabs-custom">
   <ul class="nav nav-tabs">
     <li class="active"><a href="#tab_1" data-toggle="tab">Overview</a></li>
     <li><a href="#tab_2" data-toggle="tab">Vulnerabilities</a></li>
     <li><a href="#tabfiles" data-toggle="tab">Files</a></li>
     <s:iterator value="checklists" var="cl">
     	<li><a href="#tabcl_${key}" data-toggle="tab"><s:property value="value[0].checklist"/> Checklist</a></li>
     </s:iterator>
   </ul>
   <div class="tab-content">
     <div class="tab-pane active" id="tab_1">
			  <bs:row>
				<bs:mco colsize="12">
			    <bs:box title="Application Summary" type="primary">
					<bs:row>
					<bs:mco colsize="7">
					<div class="text-header" id="summary_header"></div>
			  		<textarea id="appsum" style="width: 100%" ><s:property value="asmt.summary"/></textarea>
			  		</bs:mco>
			  		<bs:mco colsize="4">
						<div class="text-header">Notes <span id="summary_notes_header"></span></div>
			  			<textarea id="appsum_notes" style="width: 100%"><s:property value="asmt.pr_sum_notes"/></textarea>
			  		</bs:mco>
			  		<bs:mco colsize="1" style="padding-right:30px">
			  					<bs:row>
				  					<button class="btn btn-danger complete col-md-12" <s:if test="!showComplete">disabled</s:if>><i class="fa fa-check"></i> Complete</button><br><br>
				  				</bs:row>
			  		</bs:mco>
			  		</bs:row>
			  	</bs:box>
			  	</bs:mco>
			  </bs:row>
			   <bs:row>
				<bs:mco colsize="12">
			    <bs:box title="Risk Analysis" type="primary">
					<bs:row>
					<bs:mco colsize="7">
					<div class="text-header" id="risk_header"></div>
			  		<textarea id="risk" style="width: 100%" <s:if test="!showComplete">readOnly</s:if>><s:property value="asmt.riskAnalysis"/></textarea>
			  		</bs:mco>
			  		<bs:mco colsize="4">
						<div class="text-header">Notes <span id="risk_notes_header"></span></div>
			  			<textarea id="risk_notes" style="width: 100%"><s:property value="asmt.pr_risk_notes"/></textarea>
			  		</bs:mco>
			  		<bs:mco colsize="1" style="padding-right: 30px">
			  					<bs:row>
				  					<button class="btn btn-danger complete col-md-12" <s:if test="!showComplete">disabled</s:if>><i class="fa fa-check"></i> Complete</button><br><br>
				  				</bs:row>
			  		</bs:mco>
			  		</bs:row>
			  	</bs:box>
			  	</bs:mco>
			  </bs:row>
		</div>
		<div class="tab-pane" id="tab_2">
			  <s:iterator value="asmt.vulns" status="stat" var="v">
			  	${v.updateRiskLevels()}
			   <bs:row>
				<bs:mco colsize="12">
			    <bs:box title="<b style='color:lightgray'><u>${name }</u></b>" type="warning">
			    	<bs:row>
			    		<s:if test="asmt.type.cvss31 || asmt.type.cvss40">
						<bs:mco colsize="1">
							<div class="scoreBody" style="margin-top:5px">
								<h3 class="scoreBox <s:property value="overallStr"/>" id="score"> <s:property value="cvssScore"/></h3>
								<span class="severityBox <s:property value="overallStr"/>" id="severity"><s:property value="overallStr"/></span>
							</div>
						</bs:mco>
			    		<bs:mco colsize="2">
							<span class='meta'><u>Category:</u></span><br/>
							<span class='meta'><s:property value="category.name"/></span><br/>
							<br/>
							<span class='meta'><u>CVSS Vector:</u></span><br/>
							<span class='meta'><s:property value="cvssString"/></span><br/>
						</bs:mco>
			    		</s:if>
			    		<s:else>
			    		<bs:mco colsize="12">
							<div class='rating'>
								<span class='meta'>Category:</span><br/>
								<span class='catName'><s:property value="category.name"/></span><br/>
								<span class='meta'>Severity:</span>
								<br>
								<span class='label ' title='Severity'><s:property value="overallStr"/> Severity</span>
								<span class='label ' title='Impact'><s:property value="impactStr"/> Impact</span>
								<span class='label ' title='Likelihood'><s:property value="likelyhoodStr"/> Likelihood</span>
							</div>
							</bs:mco>
			    		</s:else>
			    	</bs:row>
			    	<bs:row>
			    		&nbsp;
			    	</bs:row>
			    	<bs:row>
					   
						<bs:mco colsize="7">
							<div class="text-header" >Description <span id="vuln_desc['${id}']_header"></span></div>
				  			<textarea id="vuln_desc['${id}']" style="width: 100%" <s:if test="!showComplete">readOnly</s:if>><s:property value="description"/></textarea>
				  		</bs:mco>
				  		<bs:mco colsize="4">
							<div class="text-header" >Description Notes <span id="vuln_desc_notes['${id}']_header"></span></div>
				  			<textarea id="vuln_desc_notes['${id}']" style="width: 100%"><s:property value="desc_notes"/></textarea>
				  		</bs:mco>
				  		
			  			<bs:mco colsize="1" style="padding-right:30px">
			  					<bs:row>
				  					<button class="btn btn-danger complete col-md-12" <s:if test="!showComplete">disabled</s:if>><i class="fa fa-check"></i> Complete</button><br><br>
				  				</bs:row>
			  			</bs:mco>
				  		</bs:row>
						<bs:row>
						<bs:mco colsize="7">
							<div class="text-header" >Recommendation <span id="vuln_rec['${id}']_header"></span></div>
				  			<textarea id="vuln_rec['${id}']" style="width: 100%" <s:if test="!showComplete">readOnly</s:if>><s:property value="recommendation"/></textarea>
				  		</bs:mco>
				  		<bs:mco colsize="4">
							<div class="text-header">Recommendation Notes <span id="vuln_rec_notes['${id}']_header"></span></div>
				  			<textarea id="vuln_rec_notes['${id}']" style="width: 100%"><s:property value="rec_notes"/></textarea>
				  		</bs:mco>
			  			<bs:mco colsize="1" style="padding-right:30px">
			  			
				  			<bs:row>
				  					<button class="btn btn-danger complete col-md-12" <s:if test="!showComplete">disabled</s:if>><i class="fa fa-check"></i> Complete</button><br><br>
				  				</bs:row>
			  		</bs:mco>
			  		</bs:row>
						<bs:row>
						<bs:mco colsize="7">
							<div class="text-header" >Details <span id="vuln_details['${id}']_header"></span></div>
				  			<textarea id="vuln_details['${id}']" style="width: 100%" <s:if test="!showComplete">readOnly</s:if>><s:property value="details"/></textarea>
				  		</bs:mco>
				  		<bs:mco colsize="4">
							<div class="text-header">Detail Notes <span id="vuln_detail_notes['${id}']_header"></span></div>
				  			<textarea id="vuln_detail_notes['${id}']" style="width: 100%"><s:property value="detail_notes"/></textarea>
				  		</bs:mco>
			  			<bs:mco colsize="1" style="padding-right:30px">
			  			
				  			<bs:row>
				  					<button class="btn btn-danger complete col-md-12" <s:if test="!showComplete">disabled</s:if>><i class="fa fa-check"></i> Complete</button><br><br>
				  				</bs:row>
			  		</bs:mco>
			  		</bs:row>
			  	</bs:box>
			  	</bs:mco>
			  </bs:row>
			  </s:iterator>
		</div>
		<div class="tab-pane" id="tabfiles">
			<bs:row>
					<bs:mco colsize="6">
				    <bs:box title="Uploaded Files" type="info">
				    <bs:datatable columns="name" classname="" id="uploadedFiles">
				    <s:iterator value="files">
				    <tr><td><a href="GetEngFile?name=<s:property value="uuid"/>"><s:property value="name"/></a></td></tr>
				    </s:iterator>
				    </bs:datatable>
				 	</bs:box>
				 	</bs:mco>
				 	</bs:row>
		</div>
		<s:iterator value="checklists">
		<div class="tab-pane" id="tabcl_${key}">
		
		  		<bs:datatable columns="Question,Filter: <a class='filter' href=#pass>Pass</a> - <a class='filter' href=#fail>Fail</a> - <a class='filter' href=#incom>Incomplete</a> - <a class='filter' href=#na>NA</a> - <a class='filter' href=#>All</a>,Assessor Notes,Status" classname="questions" id="checklist">
				
				<s:iterator value="value">
					<tr>
					<td><s:property value="question"/></td>
					<td style="width:30%; font-size:larger">
					
						<s:if test="answer.value == 0">
							<input type=radio name="rd${id }" value="0" checked >&nbsp;Incomplete&nbsp;&nbsp;</input> 
							<input type=radio name="rd${id }" value="1" disabled>&nbsp;NA&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="3" disabled>&nbsp;Pass</input>  
							<input type=radio name="rd${id }" value="2" disabled>&nbsp;&nbsp;&nbsp;Fail</input>
							<div style="display:none">xxxIncomxxx</div>
						</s:if>
						<s:elseif test="answer.value == 1">
							<input type=radio name="rd${id }" value="0" disabled>&nbsp;Incomplete</input> 
							<input type=radio name="rd${id }" value="1" checked>&nbsp;NA&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="3" disabled>&nbsp;Pass&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="2" disabled>&nbsp;Fail&nbsp;&nbsp;</input>
							<div style="display:none">xxxNAxxx</div>
						</s:elseif>
						<s:elseif test="answer.value == 2">
							<input type=radio name="rd${id }" value="0" disabled>&nbsp;Incomplete&nbsp;&nbsp;</input> 
							<input type=radio name="rd${id }" value="1" disabled>&nbsp;NA&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="3" disabled>&nbsp;Pass&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="2" checked>&nbsp;Fail&nbsp;&nbsp;</input>
							<div style="display:none">xxxFailedxxx</div>
						</s:elseif>
						<s:elseif test="answer.value == 3">
							<input type=radio name="rd${id }" value="0" disabled>&nbsp;Incomplete&nbsp;&nbsp;</input> 
							<input type=radio name="rd${id }" value="1" disabled>&nbsp;NA&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="3" checked>&nbsp;Pass&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="2" disabled>&nbsp;Fail&nbsp;&nbsp;</input>
							<div style="display:none">xxxPassedxxx</div>
						</s:elseif>
						<s:else>
							<input type=radio name="rd${id }" value="0" checked>&nbsp;Incomplete&nbsp;&nbsp;</input> 
							<input type=radio name="rd${id }" value="1" disabled>&nbsp;NA&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="3" disabled>&nbsp;Pass&nbsp;&nbsp;</input>  
							<input type=radio name="rd${id }" value="2" disabled>&nbsp;Fail&nbsp;&nbsp;</input>
							<div style="display:none">xxxIncomxxx</div>
						</s:else>
						
						</td>
					<td><textarea class="form-control" style="width:100%" id="tx${id }" readonly>${notes}</textarea></td>
					<td>
						<s:if test="answer.value == 0">
							Incomplete 	
						</s:if>
						<s:elseif test="answer.value == 1">
							NA
						</s:elseif>
						<s:elseif test="answer.value == 2">
							Fail
						</s:elseif>
						<s:elseif test="answer.value == 3">
							Pass
						</s:elseif>
						<s:else>
							Incomplete
						</s:else>
					</td>
					</tr>
				</s:iterator>
			</bs:datatable>
			</div>
	</s:iterator>
		
	</div>
	</div>
	</bs:mco>
	</bs:row>
  
  
  
 

  
  <jsp:include page="../footer.jsp" />
	
   <script>
   	const userId = "${sessionUser.id}";
   	const userName = "${sessionUser.fname} ${sessionUser.lname}";
	let prid="${prid}";
	let asmtId="${asmt.id}";
colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
	<s:if test="!(asmt.type.cvss31 || asmt.type.cvss40)">
	 <% int count=9; %>
	 <s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
	 	<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
			$("span:contains('${risk}')").css("background", colors[<%=count %>]);
			<% count = count-1; %>
		</s:if>
	</s:iterator>
	</s:if>
   
   </script>
	
   <s:if test="prqueue">
		<script src="../dist/js/peerreviewedit.js"></script>
   </s:if>
   <s:else>
		<script src="../dist/js/assessorreviewedit.js"></script>
   </s:else>
   <script>
	$("input[type=radio]").iCheck({
			radioClass: 'iradio_square-blue'
		   
		  });
   </script>


  </body>
</html>
