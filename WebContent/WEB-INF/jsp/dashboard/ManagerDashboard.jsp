<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<%@taglib prefix="fs" uri="/WEB-INF/UtilHandlers.tld" %>
<jsp:include page="../header.jsp" />
<script src="../dist/js/manager_dashboard.js"></script>
<style>
.circle {
	border-radius: 50%;
	padding:3px;
}
/* Modern Stats Card Design */
.stats-card {
    background: #192338;
    border-radius: 20px;
    padding: 0;
    margin-bottom: 25px;
    position: relative;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}
.stats-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}
.stats-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, var(--accent-color) 0%, var(--accent-light) 100%);
}
.stats-card.aqua { --accent-color: #00c0ef; --accent-light: #3dd5f3; }
.stats-card.green { --accent-color: #00a65a; --accent-light: #00d068; }
.stats-card.yellow { --accent-color: #f39c12; --accent-light: #ffb347; }
.stats-card.red { --accent-color: #dd4b39; --accent-light: #ff6b5a; }

.stats-card-body {
    padding: 25px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.stats-icon-wrapper {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-light) 100%);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
.stats-icon-wrapper::after {
    content: '';
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 18px;
    background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 100%);
}
.stats-icon {
    font-size: 26px;
    color: #fff;
    z-index: 1;
}
.stats-content {
    flex: 1;
    padding-left: 20px;
    text-align: right;
}
.stats-label {
    color: rgba(255, 255, 255, 0.6);
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 5px;
    font-weight: 500;
}
.stats-value {
    color: #fff;
    font-size: 36px;
    font-weight: 700;
    line-height: 1;
    margin: 0;
    background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.8) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}
.stats-section-header {
    font-weight: 600;
    margin-bottom: 25px;
    display: flex;
    align-items: center;
}
.stats-section-header i {
    margin-right: 10px;
}

/* Other Styles */
.search-section {
    margin-bottom: 20px;
    padding: 20px;
    background: #f4f4f4;
    border-radius: 5px;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-stats"></i> Manager Dashboard
      <small>Assessment and Vulnerability Overview</small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
    
    <!-- Statistics Row -->
    <bs:row>
      <!-- Assessment Statistics -->
      <bs:mco colsize="6">
        <h4 class="stats-section-header">
          <i class="glyphicon glyphicon-th-list"></i> Completed Assessment Statistics
        </h4>
        <bs:row>
          <bs:mco colsize="6">
            <div class="stats-card aqua">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-calendar stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">This Week</div>
                  <h2 class="stats-value">${weeklyAssessments}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
          <bs:mco colsize="6">
            <div class="stats-card green">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-calendar stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">This Month</div>
                  <h2 class="stats-value">${monthlyAssessments}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
          <bs:mco colsize="6">
            <div class="stats-card yellow">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-time stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">This Year</div>
                  <h2 class="stats-value">${yearlyAssessments}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
          <bs:mco colsize="6">
            <div class="stats-card red">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-list stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">All Time</div>
                  <h2 class="stats-value">${totalAssessments}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
        </bs:row>
      </bs:mco>
      
      <!-- Vulnerability Statistics -->
      <bs:mco colsize="6">
        <h4 class="stats-section-header">
          <i class="glyphicon glyphicon-warning-sign"></i> Vulnerability Statistics
        </h4>
        <bs:row>
          <bs:mco colsize="6">
            <div class="stats-card aqua">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-exclamation-sign stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">This Week</div>
                  <h2 class="stats-value">${weeklyVulns}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
          <bs:mco colsize="6">
            <div class="stats-card green">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-exclamation-sign stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">This Month</div>
                  <h2 class="stats-value">${monthlyVulns}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
          <bs:mco colsize="6">
            <div class="stats-card yellow">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-warning-sign stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">This Year</div>
                  <h2 class="stats-value">${yearlyVulns}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
          <bs:mco colsize="6">
            <div class="stats-card red">
              <div class="stats-card-body">
                <div class="stats-icon-wrapper">
                  <i class="glyphicon glyphicon-list stats-icon"></i>
                </div>
                <div class="stats-content">
                  <div class="stats-label">All Time</div>
                  <h2 class="stats-value">${totalVulns}</h2>
                </div>
              </div>
            </div>
          </bs:mco>
        </bs:row>
      </bs:mco>
    </bs:row>
    
    <!-- Search Section -->
    <bs:row>
      <bs:mco colsize="12">
        <bs:box type="primary" title="Search Assessments">
          <form action="ManagerDashboard" method="post">
            <s:hidden name="searchAction" value="search"/>
            <s:hidden name="_token" value="%{_token}"/>
            <bs:row>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>Date Range:</label>
                  <div class="input-group">
                    <button type="button" class="btn btn-primary pull-right" id="daterange-btn">
                      <i class="fa fa-calendar"></i> <span id="daterange-text">Select Date Range</span>
                      <i class="fa fa-caret-down"></i>
                    </button>
                  </div>
                  <s:hidden name="startDate" id="startDate"/>
                  <s:hidden name="endDate" id="endDate"/>
                </div>
              </bs:mco>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>Assessment Type:</label>
                  <s:select name="typeId" 
                    list="assessmentTypes" 
                    listKey="id" 
                    listValue="type" 
                    headerKey="0" 
                    headerValue="-- All Types --"
                    cssClass="form-control"/>
                </div>
              </bs:mco>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>Team:</label>
                  <s:select name="teamId"
                    list="teams"
                    listKey="id"
                    listValue="teamName"
                    headerKey="0"
                    headerValue="-- All Teams --"
                    cssClass="form-control"/>
                </div>
              </bs:mco>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>Status:</label>
                  <s:select name="status"
                    list="statuses"
                    listKey="id"
                    listValue="name"
                    headerKey="0"
                    headerValue="-- All Statuses --"
                    cssClass="form-control"/>
                </div>
              </bs:mco>
            </bs:row>
            <bs:row>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>Assessor:</label>
                  <s:select name="assessorId"
                    list="assessors"
                    listKey="id"
                    listValue="fname + ' ' + lname"
                    headerKey="0"
                    headerValue="-- All Assessors --"
                    cssClass="form-control"/>
                </div>
              </bs:mco>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>Campaign:</label>
                  <s:select name="campaignId"
                    list="campaigns"
                    listKey="id"
                    listValue="name"
                    headerKey="0"
                    headerValue="-- All Campaigns --"
                    cssClass="form-control"/>
                </div>
              </bs:mco>
              <bs:mco colsize="3">
                <div class="form-group">
                  <label>&nbsp;</label>
                  <button type="submit" class="btn btn-block btn-primary btn-md">
                    <i class="glyphicon glyphicon-search"></i> Search
                  </button>
                </div>
              </bs:mco>
            </bs:row>
          </form>
        </bs:box>
      </bs:mco>
    </bs:row>
    
    <!-- Filtered Statistics -->
    <bs:row>
      <!-- Vulnerability Severity Breakdown -->
      <bs:mco colsize="4">
        <bs:box type="danger" title="Vulnerability Severity Breakdown">
          <div style="max-height: 300px; overflow-y: auto;">
            <table class="table table-condensed">
              <thead>
                <tr>
                  <th>Severity</th>
                  <th class="text-right">Count</th>
                </tr>
              </thead>
              <tbody>
                <s:iterator value="filteredSeverityStats">
                  <s:if test="value > 0">
                    <tr>
                      <td><strong><s:property value="key"/></strong></td>
                      <td class="text-right">
                        <span class="badge" style="background-color: <s:property value='severityColorMap[key]'/>; color: white; padding: 3px 8px; font-weight: bold; font-size: 12px;">
                          <s:property value="value"/>
                        </span>
                      </td>
                    </tr>
                  </s:if>
                </s:iterator>
                <s:if test="filteredSeverityStats.isEmpty() || filteredSeverityStats.values().stream().allMatch(v -> v == 0)">
                  <tr>
                    <td colspan="2" class="text-center text-muted">No vulnerabilities found</td>
                  </tr>
                </s:if>
              </tbody>
              <tfoot>
                <tr style="background-color: #030d1c7a; font-weight: bold;">
                  <td>Total</td>
                  <td class="text-right">
                    <span class="badge bg-gray">${totalFilteredVulns}</span>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </bs:box>
      </bs:mco>
      
      <!-- Assessment Status Breakdown -->
      <bs:mco colsize="4">
        <bs:box type="warning" title="Assessment Status Breakdown">
          <div style="max-height: 300px; overflow-y: auto;">
            <table class="table table-condensed">
              <thead>
                <tr>
                  <th>Status</th>
                  <th class="text-right">Count</th>
                </tr>
              </thead>
              <tbody>
                <s:iterator value="filteredStatusStats">
                  <s:if test="value > 0">
                    <tr>
                      <td><strong><s:property value="key"/></strong></td>
                      <td class="text-right"><span class="badge bg-yellow"><s:property value="value"/></span></td>
                    </tr>
                  </s:if>
                </s:iterator>
                <s:if test="filteredStatusStats.isEmpty() || filteredStatusStats.values().stream().allMatch(v -> v == 0)">
                  <tr>
                    <td colspan="2" class="text-center text-muted">No assessments found</td>
                  </tr>
                </s:if>
              </tbody>
              <tfoot>
                <tr style="background-color: #030d1c7a; font-weight: bold;">
                  <td>Total</td>
                  <td class="text-right">
                    <span class="badge bg-yellow">${totalFilteredAssessments}</span>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </bs:box>
      </bs:mco>
      
      <!-- Completed Assessments by Assessor -->
      <bs:mco colsize="4">
        <bs:box type="success" title="Completed Assessments by Assessor">
          <div style="max-height: 400px; overflow-y: auto;">
            <table class="table table-condensed">
              <thead>
                <tr>
                  <th>Assessor</th>
                  <th class="text-right">Completed</th>
                </tr>
              </thead>
              <tbody>
                <s:iterator value="filteredAssessorStats">
                  <tr>
                    <td><strong><s:property value="key"/></strong></td>
                    <td class="text-right"><span class="badge bg-green"><s:property value="value"/></span></td>
                  </tr>
                </s:iterator>
                <s:if test="filteredAssessorStats.isEmpty()">
                  <tr>
                    <td colspan="2" class="text-center text-muted">No completed assessments found</td>
                  </tr>
                </s:if>
              </tbody>
              <tfoot>
                <tr style="background-color: #030d1c7a; font-weight: bold;">
                  <td>Total</td>
                  <td class="text-right">
                    <span class="badge bg-green">${totalCompletedAssessments}</span>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </bs:box>
      </bs:mco>
    </bs:row>
    
    <!-- Assessments Table (Always shown) -->
    <bs:row>
      <bs:mco colsize="12">
        <bs:box type="primary" title="Assessments">
          <bs:datatable columns="Action,AppId,Name,Type,Team,Assessor,Start,End,Completed,Status,Findings" classname="table-striped" id="searchResults">
            <s:iterator value="searchResults" status="stat" var="asmt">
              <tr>
                <td>
                  <a target="_blank" href="EditAssessment?action=get&aid=<s:property value='id'/>" class="" title="Edit Assessment">
                    <i class="fa fa-pencil"></i>
                  </a>
                </td>
                <td><s:property value="appId"/></td>
                <td><s:property value="name"/></td>
                <td><s:property value="type.type"/></td>
                <td>
                  <s:iterator value="assessor" status="assessorStat">
                    <s:if test="#assessorStat.index == 0 && team != null">
                      <s:property value="team.teamName"/>
                    </s:if>
                  </s:iterator>
                </td>
                <td>
                  <s:iterator value="assessor" status="assessorStat">
                    <s:if test="#assessorStat.index > 0">, </s:if>
                    <s:property value="fname"/> <s:property value="lname"/>
                  </s:iterator>
                </td>
                <td><s:date name="start" format="yyyy-MM-dd"/></td>
                <td><s:date name="end" format="yyyy-MM-dd"/></td>
                <td><s:date name="completed" format="yyyy-MM-dd"/></td>
                <td><s:property value="status"/></td>
                <td>
                  <fs:vulncount asmt="${asmt}" levels="${riskLevels}"></fs:vulncount>
                </td>
              </tr>
            </s:iterator>
          </bs:datatable>
        </bs:box>
      </bs:mco>
    </bs:row>

  </section>
  <!-- /.content -->
</div>
<!-- /.content-wrapper -->

<jsp:include page="../footer.jsp" />


</body>
</html>