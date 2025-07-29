<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<script src="../dist/js/dashboard.js" ></script>
<link rel="stylesheet" href="../src/dashboard-modern.css">
<style>
.circle {
	border-radius: 50%;
	padding:3px;
}
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper modern-dashboard">
  <!-- Content Header (Page header) -->
  <section class="content-header fade-in">
    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap;">
      <h1>
        <i class="fas fa-tachometer-alt"></i> Dashboard
      </h1>
      <button class="modern-create-btn" onClick="location.href='Engagement?back=assessment'">
        <i class="fas fa-plus"></i> Create Assessment
      </button>
    </div>
  </section>

  <!-- Main content -->
  <section class="content">
  <bs:row>
  <bs:mco colsize="6">
  	<div class="modern-card slide-up">
  		<div class="modern-card-header">
  			<h3><i class="fas fa-list-alt"></i> <a href='AssessmentQueue' style="color: var(--text-primary); text-decoration: none;">Assessment Queue</a></h3>
  		</div>
  		<div class="modern-card-body">
  			<bs:datatable columns="Start,ID,Name,Vulnerabilities" classname="modern-table" id="aqueue">
  			</bs:datatable>
  		</div>
  	</div>
  </bs:mco>
  <bs:mco colsize="6">
  	<div class="modern-card slide-up">
  		<div class="modern-card-header">
  			<h3><i class="fas fa-redo-alt"></i> <a href='Verifications' style="color: var(--text-primary); text-decoration: none;">Retest Queue</a></h3>
  		</div>
  		<div class="modern-card-body">
  			<bs:datatable columns="Start,Name,Vulnerability,Severity" classname="modern-table" id="vqueue">
  			</bs:datatable>
  		</div>
  	</div>
  </bs:mco>
  </bs:row>
  <bs:row>
  <bs:mco colsize="6">
  	<div class="modern-card slide-up modern-notifications">
  		<div class="modern-card-header">
  			<h3><i class="fas fa-bell"></i> Notifications</h3>
  			<button class="modern-clear-btn" id="clearNotifications" style="position: absolute; top: 16px; right: 24px;">
  				<i class="fas fa-trash"></i> Clear All
  			</button>
  		</div>
  		<div class="modern-card-body">
  			<bs:datatable columns="Time,Description,&nbsp;" classname="modern-table" id="notify">
  				<s:iterator value="notifications">
  					<tr class="notification-item">
  						<td class="notification-time"><s:date name="created" format="yyyy-MM-dd hh:mm:ss"/></td>
  						<td class="notification-message">${message}</td>
  						<td><i data-id="${id}" class="fas fa-trash notification-delete delete"></i></td>
  					</tr>
  				</s:iterator>
  			</bs:datatable>
  		</div>
  	</div>
  </bs:mco>
   <bs:mco colsize="6">
  	<div class="modern-card slide-up">
  		<div class="modern-card-header">
  			<h3><i class="fas fa-calendar-week"></i> Your Week</h3>
  		</div>
  		<div class="modern-card-body week-section">
  			<s:if test="current.size == 0">
  				<div style="text-align: center; color: var(--text-muted); padding: 40px 0;">
  					<i class="fas fa-calendar-times" style="font-size: 48px; margin-bottom: 16px; opacity: 0.5;"></i>
  					<p>Nothing scheduled for this week.</p>
  				</div>
  			</s:if>
  			<s:iterator value="current" status="stat">
  				<div class="week-item fade-in">
  					<h4>${name}</h4>
  					<s:if test="assessor.size > 1">
  						<div class="week-collaborators">
  							<i class="fas fa-users"></i> Collaborating with
  							<s:iterator value="assessor" status="stat"><s:if test="#stat.index != 0">, <s:if test="#stat.last">and </s:if></s:if>${fname } ${lname }</s:iterator>
  						</div>
  					</s:if>
  					<div class="week-notes">
  						<textarea class="infos" name="editor${stat.index }" id="editor${stat.index }" disabled="disabled" style="background: transparent; border: none; color: var(--text-secondary); width: 100%; min-height: 80px; resize: none;">
  							<s:property value="accessNotes" escapeHtml="false"/>
  						</textarea>
  					</div>
  				</div>
  			</s:iterator>
  		</div>
  	</div>
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