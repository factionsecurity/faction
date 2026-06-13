<%@page import="org.apache.struts2.components.Include" %>
    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
        <%@ taglib prefix="s" uri="/struts-tags" %>
            <%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld" %>
                <%@taglib prefix="fs" uri="/WEB-INF/UtilHandlers.tld" %>
                    <jsp:include page="../header.jsp" />
                    <script src="../dist/js/manager_dashboard.js"></script>
                    <style>
                        .circle {
                            border-radius: 50%;
                            padding: 3px;
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

                        .stats-card.aqua {
                            --accent-color: #00c0ef;
                            --accent-light: #3dd5f3;
                        }

                        .stats-card.green {
                            --accent-color: #00a65a;
                            --accent-light: #00d068;
                        }

                        .stats-card.yellow {
                            --accent-color: #f39c12;
                            --accent-light: #ffb347;
                        }

                        .stats-card.red {
                            --accent-color: #dd4b39;
                            --accent-light: #ff6b5a;
                        }

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
                            background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
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
                            background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
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

                        /* Severity checkbox list */
                        .severity-checkbox-list {
                            border-radius: 4px;
                            padding: 8px 10px;
                            background: #030d1c !important;
                            max-height: 110px;
                            overflow-y: auto;
                        }

                        .severity-checkbox-list label {
                            display: inline;
                            font-weight: normal;
                            margin: 0;
                            padding: 2px 0;
                            cursor: pointer;
                        }

                        .severity-checkbox-list input[type="checkbox"] {
                            margin-right: 6px;
                            vertical-align: middle;
                        }

                        /* Other Styles */
                        .search-section {
                            margin-bottom: 20px;
                            padding: 20px;
                            background: #f4f4f4;
                            border-radius: 5px;
                        }

                        /* Date picker styling */
                        .manager-dashboard-datepicker.ui-datepicker {
                            z-index: 10000 !important;
                            background: #ffffff;
                            border: none;
                            border-radius: 12px;
                            box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 2px 6px rgba(15, 23, 42, 0.08);
                            padding: 14px;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                            font-size: 13px;
                            color: #1f2937;
                            width: 280px;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-header {
                            background: linear-gradient(135deg, #192338 0%, #2c3a5e 100%);
                            border: none;
                            border-radius: 8px;
                            color: #ffffff;
                            padding: 8px 6px;
                            margin-bottom: 10px;
                            position: relative;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-title {
                            font-weight: 600;
                            font-size: 14px;
                            letter-spacing: 0.3px;
                            line-height: 28px;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev,
                        .manager-dashboard-datepicker .ui-datepicker-next {
                            top: 50%;
                            transform: translateY(-50%);
                            width: 28px;
                            height: 28px;
                            border-radius: 50%;
                            background: rgba(255, 255, 255, 0.18);
                            border: none;
                            cursor: pointer;
                            transition: background 0.15s ease;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev {
                            left: 6px;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-next {
                            right: 6px;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev:hover,
                        .manager-dashboard-datepicker .ui-datepicker-next:hover,
                        .manager-dashboard-datepicker .ui-datepicker-prev-hover,
                        .manager-dashboard-datepicker .ui-datepicker-next-hover {
                            background: rgba(255, 255, 255, 0.32);
                            border: none;
                            top: 50%;
                            transform: translateY(-50%);
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev-hover {
                            left: 6px;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-next-hover {
                            right: 6px;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev .ui-icon,
                        .manager-dashboard-datepicker .ui-datepicker-next .ui-icon {
                            background-image: none;
                            text-indent: -99999px;
                            overflow: hidden;
                            display: block;
                            width: 100%;
                            height: 100%;
                            margin: 0;
                            position: relative;
                            top: 0;
                            left: 0;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev .ui-icon::before,
                        .manager-dashboard-datepicker .ui-datepicker-next .ui-icon::before {
                            content: '';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            width: 8px;
                            height: 8px;
                            border-top: 2px solid #ffffff;
                            border-right: 2px solid #ffffff;
                            text-indent: 0;
                            pointer-events: none;
                        }

                        .manager-dashboard-datepicker .ui-datepicker-prev .ui-icon::before {
                            transform: translate(-30%, -50%) rotate(-135deg);
                        }

                        .manager-dashboard-datepicker .ui-datepicker-next .ui-icon::before {
                            transform: translate(-70%, -50%) rotate(45deg);
                        }

                        .manager-dashboard-datepicker select.ui-datepicker-month,
                        .manager-dashboard-datepicker select.ui-datepicker-year {
                            background: rgba(255, 255, 255, 0.95);
                            color: #1f2937;
                            border: none;
                            border-radius: 6px;
                            padding: 3px 6px;
                            font-size: 12px;
                            font-weight: 500;
                            margin: 0 2px;
                        }

                        .manager-dashboard-datepicker table {
                            margin: 0;
                            font-size: 13px;
                            border-collapse: separate;
                            border-spacing: 2px;
                        }

                        .manager-dashboard-datepicker th {
                            color: #94a3b8;
                            font-weight: 600;
                            font-size: 11px;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                            padding: 6px 0;
                            border: none;
                        }

                        .manager-dashboard-datepicker td {
                            padding: 1px;
                            border: none;
                        }

                        .manager-dashboard-datepicker td span,
                        .manager-dashboard-datepicker td a {
                            display: block;
                            text-align: center;
                            padding: 7px 0;
                            border: none;
                            background: transparent;
                            color: #1f2937;
                            border-radius: 8px;
                            font-weight: 500;
                            transition: background 0.15s ease, color 0.15s ease, transform 0.1s ease;
                        }

                        .manager-dashboard-datepicker td a:hover {
                            background: #eef2f7;
                            color: #1f2937;
                            transform: scale(1.05);
                        }

                        .manager-dashboard-datepicker td .ui-state-highlight {
                            background: #eef0f5;
                            color: #192338;
                            font-weight: 700;
                        }

                        .manager-dashboard-datepicker td .ui-state-active,
                        .manager-dashboard-datepicker td a.ui-state-active {
                            background: linear-gradient(135deg, #192338 0%, #2c3a5e 100%);
                            color: #ffffff;
                            font-weight: 600;
                            box-shadow: 0 4px 10px rgba(25, 35, 56, 0.4);
                        }

                        .manager-dashboard-datepicker td.ui-datepicker-other-month span,
                        .manager-dashboard-datepicker td.ui-datepicker-other-month a {
                            color: #cbd5e1;
                        }

                        .manager-dashboard-datepicker td.ui-datepicker-unselectable span {
                            color: #cbd5e1;
                            opacity: 0.6;
                        }

                        .input-group.date {
                            position: relative;
                        }

                        .input-group.date .input-group-addon {
                            cursor: pointer;
                            background-color: #3c8dbc;
                            border-color: #3c8dbc;
                            color: white;
                            cursor: pointer;
                        }
                        .input-group-addon {
                            cursor: pointer;
                            background-color: #030d1c !important;
                            border-color: #030d1c !important;
                            color: white;
                            cursor: pointer;
                        }

                        .input-group.date .input-group-addon:hover {
                            background-color: #367fa9;
                            border-color: #367fa9;
                        }

                        /* Quick range dropdown styling */
                        #rangeDropdown li a {
                            padding: 8px 15px;
                        }

                        #rangeDropdown li a:hover {
                            background-color: #f5f5f5;
                        }

                        #rangeDropdown li a i {
                            margin-right: 8px;
                            width: 16px;
                            text-align: center;
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
                                            <s:hidden name="searchAction" value="search" />
                                            <s:hidden name="_token" value="%{_token}" />
                                            <bs:row>
                                                <bs:mco colsize="4">
													<bs:row>
														<bs:mco colsize="4">
															<div class="form-group">
																<label>Start Date:</label>
																<div class="input-group">
																	<input type="text" class="form-control" id="startDateDisplay" value="<s:date name="startDate" format="MM/dd/yyyy" />" placeholder="mm/dd/yyyy" />
																	<span class="input-group-addon" id="startDateBtn"><i class="fa fa-calendar"></i></span>
																	<s:hidden name="startDate" id="startDate" />
																</div>
																<span class="help-block date-error" id="startDateError" style="color: #dd4b39; display: none; margin-top: 4px;">Please enter a valid date</span>
															</div>
														</bs:mco>
														<bs:mco colsize="4">
															<div class="form-group">
																<label>End Date:</label>
																<div class="input-group">
																	<input type="text" class="form-control" id="endDateDisplay" value="<s:date name="endDate" format="MM/dd/yyyy" />" placeholder="mm/dd/yyyy" />
																	<span class="input-group-addon" id="endDateBtn"><i class="fa fa-calendar"></i></span>
																	<s:hidden name="endDate" id="endDate" />
																</div>
																<span class="help-block date-error" id="endDateError" style="color: #dd4b39; display: none; margin-top: 4px;">Please enter a valid date</span>
															</div>
														</bs:mco>
														<bs:mco colsize="4">
															<div class="form-group">
																<label>Quick Ranges:</label>
																<div class="btn-group" style="width: 100%">
																	<button type="button" class="btn btn-default btn-sm dropdown-toggle" data-toggle="dropdown">
																		<i class="fa fa-clock-o"></i> <span id="rangeLabel">Select Range</span> <span class="caret"></span>
																	</button>
																	<ul class="dropdown-menu" role="menu" id="rangeDropdown">
																		<li><a href="#" data-range="today"><i class="fa fa-calendar"></i> Today</a></li>
																		<li><a href="#" data-range="yesterday"><i class="fa fa-calendar"></i> Yesterday</a></li>
																		<li><a href="#" data-range="7days"><i class="fa fa-calendar"></i> Last 7 Days</a></li>
																		<li><a href="#" data-range="30days"><i class="fa fa-calendar"></i> Last 30 Days</a></li>
																		<li><a href="#" data-range="month"><i class="fa fa-calendar"></i> This Month</a></li>
																		<li><a href="#" data-range="lastmonth"><i class="fa fa-calendar"></i> Last Month</a></li>
																		<li><a href="#" data-range="year"><i class="fa fa-calendar"></i> This Year</a></li>
																		<li><a href="#" data-range="alltime"><i class="fa fa-calendar"></i> All Time</a></li>
																	</ul>
																</div>
															</div>
														</bs:mco>
													</bs:row>
												</bs:mco>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>Assessment Type:</label>
                                                        <s:select name="typeId" list="assessmentTypes" listKey="id"
                                                            listValue="type" headerKey="0" headerValue="-- All Types --"
                                                            cssClass="form-control" />
                                                    </div>
                                                </bs:mco>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>Team:</label>
                                                        <s:select name="teamId" list="teams" listKey="id"
                                                            listValue="teamName" headerKey="0"
                                                            headerValue="-- All Teams --" cssClass="form-control" />
                                                    </div>
                                                </bs:mco>
                                            </bs:row>
                                            <bs:row>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>Status:</label>
                                                        <s:select name="status" list="statuses" listKey="id"
                                                            listValue="name" headerKey="0"
                                                            headerValue="-- All Statuses --" cssClass="form-control" />
                                                    </div>
                                                </bs:mco>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>Assessor:</label>
                                                        <s:select name="assessorId" list="assessors" listKey="id"
                                                            listValue="fname + ' ' + lname" headerKey="0"
                                                            headerValue="-- All Assessors --" cssClass="form-control" />
                                                    </div>
                                                </bs:mco>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>Campaign:</label>
                                                        <s:select name="campaignId" list="campaigns" listKey="id"
                                                            listValue="name" headerKey="0"
                                                            headerValue="-- All Campaigns --" cssClass="form-control" />
                                                    </div>
                                                </bs:mco>
                                            </bs:row>
                                            <bs:row>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>Severity:</label>
                                                        <div class="severity-checkbox-list">
                                                            <s:checkboxlist name="severityIds" list="activeRiskLevels"
                                                                listKey="riskId" listValue="risk" />
                                                        </div>
                                                        <span class="help-block" style="font-size: 11px; color: #777; margin-top: 4px;">Matches assessments containing at least one vulnerability of the selected severities.</span>
                                                    </div>
                                                </bs:mco>
                                                <bs:mco colsize="4">
                                                    <div class="form-group">
                                                        <label>&nbsp;</label>
                                                        <button type="submit" class="btn btn-block btn-primary btn-md">
                                                            <i class="glyphicon glyphicon-search"></i> Search
                                                        </button>
                                                    </div>
                                                </bs:mco>
                                                <bs:mco colsize="2">
                                                    <div class="form-group">
                                                        <label>&nbsp;</label>
														<div class="btn btn-block btn-success btn-md" id="exportAssessmentsCsvBtn"
															onclick="exportAssessmentsToCSV()">
															<i class="fa fa-download"></i> Export Assessments to CSV
														</div>
													</div>
												 </bs:mco>
                                                <bs:mco colsize="2">
                                                    <div class="form-group">
                                                        <label>&nbsp;</label>
														<div class="btn btn-block btn-info btn-md" id="exportVulnsCsvBtn"
															onclick="exportVulnerabilitiesToCSV()">
															<i class="fa fa-download"></i> Export Vulnerabilities to CSV
														</div>
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
                                                                <td><strong>
                                                                        <s:property value="key" />
                                                                    </strong></td>
                                                                <td class="text-right">
                                                                    <span class="badge"
                                                                        style="background-color: <s:property value='severityColorMap[key]'/>; color: white; padding: 3px 8px; font-weight: bold; font-size: 12px;">
                                                                        <s:property value="value" />
                                                                    </span>
                                                                </td>
                                                            </tr>
                                                        </s:if>
                                                    </s:iterator>
                                                    <s:if
                                                        test="filteredSeverityStats.isEmpty() || filteredSeverityStats.values().stream().allMatch(v -> v == 0)">
                                                        <tr>
                                                            <td colspan="2" class="text-center text-muted">No
                                                                vulnerabilities found</td>
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
                                                                <td><strong>
                                                                        <s:property value="key" />
                                                                    </strong></td>
                                                                <td class="text-right"><span class="badge bg-yellow">
                                                                        <s:property value="value" />
                                                                    </span></td>
                                                            </tr>
                                                        </s:if>
                                                    </s:iterator>
                                                    <s:if
                                                        test="filteredStatusStats.isEmpty() || filteredStatusStats.values().stream().allMatch(v -> v == 0)">
                                                        <tr>
                                                            <td colspan="2" class="text-center text-muted">No
                                                                assessments found</td>
                                                        </tr>
                                                    </s:if>
                                                </tbody>
                                                <tfoot>
                                                    <tr style="background-color: #030d1c7a; font-weight: bold;">
                                                        <td>Total</td>
                                                        <td class="text-right">
                                                            <span
                                                                class="badge bg-yellow">${totalFilteredAssessments}</span>
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
                                                            <td><strong>
                                                                    <s:property value="key" />
                                                                </strong></td>
                                                            <td class="text-right"><span class="badge bg-green">
                                                                    <s:property value="value" />
                                                                </span></td>
                                                        </tr>
                                                    </s:iterator>
                                                    <s:if test="filteredAssessorStats.isEmpty()">
                                                        <tr>
                                                            <td colspan="2" class="text-center text-muted">No completed
                                                                assessments found</td>
                                                        </tr>
                                                    </s:if>
                                                </tbody>
                                                <tfoot>
                                                    <tr style="background-color: #030d1c7a; font-weight: bold;">
                                                        <td>Total</td>
                                                        <td class="text-right">
                                                            <span
                                                                class="badge bg-green">${totalCompletedAssessments}</span>
                                                        </td>
                                                    </tr>
                                                </tfoot>
                                            </table>
                                        </div>
                                    </bs:box>
                                </bs:mco>
                            </bs:row>

                            <!-- Results Tables (tabbed) -->
                            <bs:row>
                                <bs:mco colsize="12">
                                    <div class="nav-tabs-custom">
                                        <ul class="nav nav-tabs">
                                            <li class="active"><a href="#tab_assessments" data-toggle="tab"><i class="fa fa-tasks"></i> Assessments</a></li>
                                            <li><a href="#tab_vulnerabilities" data-toggle="tab"><i class="fa fa-bug"></i> Vulnerabilities</a></li>
                                        </ul>
                                        <div class="tab-content">
                                            <div class="tab-pane active" id="tab_assessments">
                                                <bs:datatable
                                                    columns="Action,AppId,Name,Type,Team,Assessor,Start,End,Completed,Status,Findings"
                                                    classname="table-striped" id="searchResults">
                                            <s:iterator value="searchResults" status="stat" var="asmt">
                                                <tr>
                                                    <td>
                                                        <a target="_blank"
                                                            href="EditAssessment?action=get&aid=<s:property value='id'/>"
                                                            class="" title="Edit Assessment">
                                                            <i class="fa fa-pencil"></i>
                                                        </a>
                                                        &nbsp;
                                                        <a target="_blank"
                                                            href="../service/Report.pdf?id=<s:property value='id'/>"
                                                            title="Download Report">
                                                            <i class="fa fa-download"></i>
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <s:property value="appId" />
                                                    </td>
                                                    <td>
                                                        <s:property value="name" />
                                                    </td>
                                                    <td>
                                                        <s:property value="type.type" />
                                                    </td>
                                                    <td>
                                                        <s:iterator value="assessor" status="assessorStat">
                                                            <s:if test="#assessorStat.index == 0 && team != null">
                                                                <s:property value="team.teamName" />
                                                            </s:if>
                                                        </s:iterator>
                                                    </td>
                                                    <td>
                                                        <s:iterator value="assessor" status="assessorStat">
                                                            <s:if test="#assessorStat.index > 0">, </s:if>
                                                            <s:property value="fname" />
                                                            <s:property value="lname" />
                                                        </s:iterator>
                                                    </td>
                                                    <td>
                                                        <s:date name="start" format="yyyy-MM-dd" />
                                                    </td>
                                                    <td>
                                                        <s:date name="end" format="yyyy-MM-dd" />
                                                    </td>
                                                    <td>
                                                        <s:date name="completed" format="yyyy-MM-dd" />
                                                    </td>
                                                    <td>
                                                        <s:property value="status" />
                                                    </td>
                                                    <td>
                                                        <fs:vulncount asmt="${asmt}" levels="${riskLevels}">
                                                        </fs:vulncount>
                                                    </td>
                                                </tr>
                                            </s:iterator>
                                                </bs:datatable>
                                            </div><!-- /#tab_assessments -->
                                            <div class="tab-pane" id="tab_vulnerabilities">
                                                <bs:datatable
                                                    columns="Action,Vulnerability,Assessment,AppId,Severity,CVSS,Category,Opened,Closed,Status,Tracking"
                                                    classname="table-striped" id="vulnResults">
                                                    <s:iterator value="vulnerabilityResults" var="vrow">
                                                        <tr class="vuln-detail-row" data-vulnid="<s:property value='vulnId'/>" style="cursor: pointer;">
                                                            <td>
                                                                <a target="_blank"
                                                                    href="EditAssessment?action=get&aid=<s:property value='assessmentId'/>"
                                                                    title="Open Assessment">
                                                                    <i class="fa fa-pencil"></i>
                                                                </a>
                                                            </td>
                                                            <td><s:property value="name" /></td>
                                                            <td><s:property value="assessmentName" /></td>
                                                            <td><s:property value="appId" /></td>
                                                            <td>
                                                                <span class="badge"
                                                                    style="background-color: <s:property value='severityColorMap[#vrow.severity]'/>; color: white; padding: 3px 8px; font-weight: bold; font-size: 12px;">
                                                                    <s:property value="severity" />
                                                                </span>
                                                            </td>
                                                            <td><s:property value="cvssScore" /></td>
                                                            <td><s:property value="category" /></td>
                                                            <td><s:date name="opened" format="yyyy-MM-dd" /></td>
                                                            <td><s:date name="closed" format="yyyy-MM-dd" /></td>
                                                            <td><s:property value="status" /></td>
                                                            <td><s:property value="tracking" /></td>
                                                        </tr>
                                                    </s:iterator>
                                                </bs:datatable>
                                            </div><!-- /#tab_vulnerabilities -->
                                        </div><!-- /.tab-content -->
                                    </div><!-- /.nav-tabs-custom -->
                                </bs:mco>
                            </bs:row>

                        </section>
                        <!-- /.content -->
                    </div>
                    <!-- /.content-wrapper -->

                    <!-- Slide-out panel showing full vulnerability details for the clicked row -->
                    <style>
                        .vuln-detail-overlay {
                            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                            background: rgba(0,0,0,0.4); z-index: 1040; display: none;
                        }
                        .vuln-detail-panel {
                            position: fixed; top: 0; right: 0; height: 100%;
                            width: 640px; max-width: 95%;
                            background: #fff; z-index: 1050;
                            box-shadow: -2px 0 12px rgba(0,0,0,0.3);
                            transform: translateX(100%); transition: transform .25s ease;
                            display: flex; flex-direction: column;
                        }
                        .vuln-detail-panel.open { transform: translateX(0); }
                        .vuln-detail-panel .vuln-detail-topbar {
                            padding: 12px 18px; border-bottom: 1px solid rgba(255,255,255,0.15);
                            display: flex; justify-content: space-between; align-items: center;
                            background: #225080;
                        }
                        .vuln-detail-panel .vuln-detail-topbar h4 { margin: 0; font-size: 18px; color: #fff; }
                        .vuln-detail-panel .vuln-detail-topbar .btn-box-tool { color: #fff; }
                        .vuln-detail-panel .vuln-detail-body {
                            padding: 18px; overflow-y: auto; flex: 1;
                            background: #0f1522; color: #c9d3e0;
                        }
                        .vuln-detail-panel .vuln-detail-body a { color: #6fb0ff; }
                        .vuln-detail-panel .vuln-detail-section-title {
                            margin-top: 20px; padding-bottom: 5px; border-bottom: 1px solid rgba(255,255,255,0.15);
                            font-weight: bold; color: #fff;
                        }
                        .vuln-detail-panel .vuln-detail-content { word-wrap: break-word; }
                        .vuln-detail-panel .vuln-detail-content img { max-width: 100%; height: auto; }
                        .vuln-detail-panel .vuln-detail-body table { color: #c9d3e0; }
                        .vuln-detail-panel .vuln-detail-body table td,
                        .vuln-detail-panel .vuln-detail-body table th { border-color: rgba(255,255,255,0.12); }
                        .vuln-detail-panel .vuln-detail-meta th { color: #8a97a8; }
                    </style>
                    <div id="vulnDetailOverlay" class="vuln-detail-overlay"></div>
                    <div id="vulnDetailPanel" class="vuln-detail-panel">
                        <div class="vuln-detail-topbar">
                            <h4>Vulnerability Details</h4>
                            <button type="button" id="vulnDetailClose" class="btn btn-box-tool" aria-label="Close">
                                <i class="fa fa-times fa-lg"></i>
                            </button>
                        </div>
                        <div id="vulnDetailBody" class="vuln-detail-body"></div>
                    </div>

                    <jsp:include page="../footer.jsp" />

                    <script type="text/javascript">
                        function exportAssessmentsToCSV() {
                            // Get the current search parameters from the form
                            var form = document.querySelector('form[action="ManagerDashboard"]');
                            var formData = new FormData(form);

                            // Create a new form for CSV export
                            var exportForm = document.createElement('form');
                            exportForm.method = 'POST';
                            exportForm.action = 'ManagerDashboardExportCSV';
                            exportForm.style.display = 'none';

                            // Copy all search parameters to the export form
                            for (var pair of formData.entries()) {
                                var input = document.createElement('input');
                                input.type = 'hidden';
                                input.name = pair[0];
                                input.value = pair[1];
                                exportForm.appendChild(input);
                            }

                            // Add search action to ensure export uses search logic
                            var searchActionInput = document.createElement('input');
                            searchActionInput.type = 'hidden';
                            searchActionInput.name = 'searchAction';
                            searchActionInput.value = 'search';
                            exportForm.appendChild(searchActionInput);

                            // Submit the form
                            document.body.appendChild(exportForm);
                            exportForm.submit();
                            document.body.removeChild(exportForm);
                        }

                        function exportVulnerabilitiesToCSV() {
                            // Get the current search parameters from the form
                            var form = document.querySelector('form[action="ManagerDashboard"]');
                            var formData = new FormData(form);

                            // Create a new form for CSV export
                            var exportForm = document.createElement('form');
                            exportForm.method = 'POST';
                            exportForm.action = 'ManagerDashboardExportVulnerabilitiesCSV';
                            exportForm.style.display = 'none';

                            // Copy all search parameters to the export form
                            for (var pair of formData.entries()) {
                                var input = document.createElement('input');
                                input.type = 'hidden';
                                input.name = pair[0];
                                input.value = pair[1];
                                exportForm.appendChild(input);
                            }

                            // Add search action to ensure export uses search logic
                            var searchActionInput = document.createElement('input');
                            searchActionInput.type = 'hidden';
                            searchActionInput.name = 'searchAction';
                            searchActionInput.value = 'search';
                            exportForm.appendChild(searchActionInput);

                            // Submit the form
                            document.body.appendChild(exportForm);
                            exportForm.submit();
                            document.body.removeChild(exportForm);
                        }
                    </script>

                    </body>

                    </html>
