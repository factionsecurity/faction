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
    color: #333;
    font-weight: 600;
    margin-bottom: 25px;
    display: flex;
    align-items: center;
}
.stats-section-header i {
    margin-right: 10px;
    color: #666;
}

/* Severity Distribution Cards */
.severity-distribution-card {
    background: #192338;
    border-radius: 20px;
    padding: 20px;
    height: 100%;
    position: relative;
    overflow: hidden;
}
.severity-distribution-card::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 70%);
    pointer-events: none;
}
.severity-period-title {
    color: rgba(255, 255, 255, 0.9);
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 15px;
    text-align: center;
}
.severity-item {
    margin: 10px 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 10px;
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.05);
    position: relative;
    overflow: hidden;
}
.severity-item::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: currentColor;
    opacity: 0.6;
}
.severity-item:hover {
    transform: translateX(5px);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.severity-label {
    font-size: 14px;
    font-weight: 600;
    text-transform: capitalize;
    color: rgba(255, 255, 255, 0.9);
}
.severity-count {
    font-size: 18px;
    font-weight: 700;
    color: rgba(255, 255, 255, 0.9);
}
/* Chart canvas container */
#weeklyChart canvas,
#monthlyChart canvas,
#yearlyChart canvas,
#totalChart canvas {
    max-height: 200px;
}

/* Other Styles */
.severity-badge {
    border-radius: 50%;
    padding: 3px 8px;
    font-size: small;
    font-weight: bold;
    margin: 2px;
}
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
          <i class="glyphicon glyphicon-th-list"></i> Assessment Statistics
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
    
    <!-- Vulnerability Distribution by Severity -->
    <bs:row>
      <bs:mco colsize="12">
        <bs:box type="info" title="Vulnerability Distribution by Severity">
          <bs:row>
            <bs:mco colsize="3">
              <div class="severity-distribution-card">
                <h5 class="severity-period-title">This Week</h5>
                <div id="weeklyChart">
                  <s:iterator value="vulnerabilityStats['weekly']">
                    <div class="severity-item">
                      <span class="severity-label">${key}</span>
                      <span class="severity-count">${value}</span>
                    </div>
                  </s:iterator>
                </div>
              </div>
            </bs:mco>
            <bs:mco colsize="3">
              <div class="severity-distribution-card">
                <h5 class="severity-period-title">This Month</h5>
                <div id="monthlyChart">
                  <s:iterator value="vulnerabilityStats['monthly']">
                    <div class="severity-item">
                      <span class="severity-label">${key}</span>
                      <span class="severity-count">${value}</span>
                    </div>
                  </s:iterator>
                </div>
              </div>
            </bs:mco>
            <bs:mco colsize="3">
              <div class="severity-distribution-card">
                <h5 class="severity-period-title">This Year</h5>
                <div id="yearlyChart">
                  <s:iterator value="vulnerabilityStats['yearly']">
                    <div class="severity-item">
                      <span class="severity-label">${key}</span>
                      <span class="severity-count">${value}</span>
                    </div>
                  </s:iterator>
                </div>
              </div>
            </bs:mco>
            <bs:mco colsize="3">
              <div class="severity-distribution-card">
                <h5 class="severity-period-title">All Time</h5>
                <div id="totalChart">
                  <s:iterator value="vulnerabilityStats['total']">
                    <div class="severity-item">
                      <span class="severity-label">${key}</span>
                      <span class="severity-count">${value}</span>
                    </div>
                  </s:iterator>
                </div>
              </div>
            </bs:mco>
          </bs:row>
        </bs:box>
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
                    <button type="button" class="btn btn-default pull-right" id="daterange-btn">
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
                    listKey="name"
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
                  <button type="submit" class="btn btn-primary form-control">
                    <i class="glyphicon glyphicon-search"></i> Search
                  </button>
                </div>
              </bs:mco>
            </bs:row>
          </form>
        </bs:box>
      </bs:mco>
    </bs:row>
    
    <!-- Search Results -->
    <s:if test="searchAction == 'search'">
      <bs:row>
        <bs:mco colsize="12">
          <bs:box type="success" title="Search Results">
            <bs:datatable columns="AppId,Name,Type,Team,Assessor,Start,End,Status,Findings" classname="table-striped" id="searchResults">
              <s:iterator value="searchResults" status="stat" var="asmt">
                <tr>
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
    </s:if>
    
    <!-- Recent Vulnerabilities -->
    <bs:row>
      <bs:mco colsize="12">
        <bs:box type="danger" title="Recently Added Vulnerabilities">
          <bs:datatable columns="Created,Assessment,Vulnerability Name,Severity,Category,Tracking" classname="table-striped" id="recentVulns">
            <s:iterator value="recentVulnerabilities">
              <tr>
                <td><s:date name="created" format="yyyy-MM-dd HH:mm"/></td>
                <td>Assessment #<s:property value="assessmentId"/></td>
                <td><s:property value="name"/></td>
                <td class="severity-cell" data-severity="<s:property value='overall'/>">
                  <s:property value="overallStr"/>
                </td>
                <td><s:property value="category.name"/></td>
                <td><s:property value="tracking"/></td>
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

<script>
// Apply color coding to severity levels
function updateColors() {
    // Get colors array from server (supports up to 10 custom severity levels)
    let colors = [<s:iterator value="colors" status="stat" var="c"><s:if test="!#stat.first">,</s:if>"<s:property value="c" escapeJavaScript="true"/>"</s:iterator>];
    
    console.log('Colors from server:', colors);
    
    // Build severity-to-color mapping based on risk level order
    // Colors are ordered from highest severity (first color) to lowest
    var severityColors = {};
    var severityOrder = [];
    <s:iterator value="riskLevels" status="stat">
        <s:if test="risk != null && risk.trim() != ''">
            severityOrder.push({
                name: "<s:property value='risk' escapeJavaScript='true'/>",
                id: <s:property value="riskId"/>
            });
        </s:if>
    </s:iterator>
    
    // Sort by riskId to ensure proper ordering (lower ID = higher severity)
    severityOrder.sort(function(a, b) {
        return b.id - a.id;
    });
    
    // Assign colors based on sorted order
   
    severityOrder.forEach(function(severity, index) {
        if (index < colors.length) {
            severityColors[severity.name] = colors[index];
        }
    });
    
    console.log('Severity color mapping:', severityColors);
    
    // Apply colors to severity items in distribution cards
    $('.severity-item').each(function() {
        var severityLabel = $(this).find('.severity-label').text();
        if (severityColors[severityLabel]) {
            $(this).find('.severity-label').css({
                'color': severityColors[severityLabel],
                'font-weight': 'bold'
            });
            $(this).find('.severity-count').css({
                'color': severityColors[severityLabel],
                'font-weight': 'bold'
            });
            // Update the left border color indicator
            $(this).css('border-left-color', severityColors[severityLabel]);
        }
    });
    
    // Apply colors to table cells containing severity values
    $('td.severity-cell').each(function() {
        var text = $(this).text().trim();
        if (severityColors[text]) {
            $(this).css({
                'color': severityColors[text],
                'font-weight': 'bold'
            });
        }
    });
    
    // Apply background colors to severity items on hover
    $('.severity-item').hover(
        function() {
            var severityLabel = $(this).find('.severity-label').text();
            if (severityColors[severityLabel]) {
                // Convert hex to rgba for transparency
                var color = severityColors[severityLabel];
                var r = parseInt(color.slice(1, 3), 16);
                var g = parseInt(color.slice(3, 5), 16);
                var b = parseInt(color.slice(5, 7), 16);
                $(this).css('background-color', 'rgba(' + r + ',' + g + ',' + b + ', 0.15)');
            }
        },
        function() {
            $(this).css('background-color', 'rgba(255, 255, 255, 0.05)');
        }
    );
    
}

$(document).ready(function() {
    updateColors();
    
    // Reapply colors after a short delay to ensure DOM is fully rendered
    setTimeout(function() {
        updateColors();
    }, 500);
});
</script>

</body>
</html>