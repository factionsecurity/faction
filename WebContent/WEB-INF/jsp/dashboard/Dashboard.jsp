<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<script src="../dist/js/dashboard.js" ></script>
<style>
.circle {
	border-radius: 50%;
	padding:3px;
}
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-dashboard"></i> Dashboard
     
    </h1>
  <button class="btn btn-success pull-right dashboardButton" onClick="location.href='Engagement?back=assessment'"><i class="fa fa-plus"></i> Create Assessment</button>
  </section>

  <!-- Main content -->
  <section class="content">
  <bs:row>
  <bs:mco colsize="6">
  	<bs:box type="primary" title="<a href='AssessmentQueue'>Assessment Queue</a>">
  		<bs:datatable columns="Start,ID,Name,Vulnerabilities" classname="" id="aqueue">
  		
  		</bs:datatable>
  	</bs:box>
  </bs:mco>
  <bs:mco colsize="6">
  	<bs:box type="warning" title="<a href='Verifications'>Retest Queue</a>">
  		<bs:datatable columns="Start,Name,Vulnerability,Severity" classname="" id="vqueue">
  		
  		</bs:datatable>
  	</bs:box>
  </bs:mco>
  </bs:row>
  <bs:row>
  <bs:mco colsize="6">
  	<bs:box type="danger" title="Notifications">
  		<bs:row>
  		<bs:mco colsize="12">
  			<button class="btn btn-success pull-right" id="clearNotifications" style="margin-top:-30px;margin-right: 10px"><i class="fa fa-trash"></i> Clear All</button>
  		</bs:mco>
  		</bs:row>
  		<bs:datatable columns="Time,Description,&nbsp;" classname="" id="notify">
  			<s:iterator value="notifications">
  				<tr><td><s:date name="created" format="yyyy-MM-dd hh:mm:ss"/> </td>
  				<td>${message }</td>
  				<td><i data-id="${id}" class="glyphicon glyphicon-trash delete"></i></td></tr>
  			</s:iterator>
  		</bs:datatable>
  	</bs:box>
  </bs:mco>
   <bs:mco colsize="6">
  	<bs:box type="info" title="Your Week">
  		<s:if test="current.size == 0">
  			<center>Nothing to show for now.</center>
  		</s:if>
  		<s:iterator value="current" status="stat">
  			<bs:box type="" title="${name}">
  				<s:if test="assessor.size > 1">
  					You are working with 
  					<s:iterator value="assessor" status="stat"><s:if test="#stat.index != 0">, <s:if test="#stat.last">and </s:if></s:if>${fname } ${lname }</s:iterator>
  					for this assessment.<br><br>
  				</s:if>
  				<textarea class="infos" name="editor${stat.index }" id="editor${stat.index }" disabled="disabled"> 
  					<s:property value="accessNotes" escapeHtml="false"/>
  				</textarea>
  			</bs:box>
  		</s:iterator>
  	</bs:box>
  </bs:mco>
  </bs:row>



              
              


<jsp:include page="../footer.jsp" />
<!--<script src="../dist/js/app.js" ></script>-->
<script>
function updateColors(){
let colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
 <%int count = 9;%>
	<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
		<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
			$("td:contains(${risk})").css("color", colors[<%=count--%>] );
			$("td:contains(${risk})").css("font-weight", "bold");
	</s:if>
</s:iterator>
}
</script>
 
  </body>
</html>