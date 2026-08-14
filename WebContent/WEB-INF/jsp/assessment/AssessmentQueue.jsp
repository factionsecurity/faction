<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="fs" uri="/WEB-INF/UtilHandlers.tld" %>
<jsp:include page="../header.jsp" />
    <script src="../dist/js/assessment_queue.js"></script>
<style>
.circle {
	border-radius: 50%;
	padding:3px;
	font-size: small;
}
.circle2 {
	border-radius: 50%;
	width: 20px;
	height: 20px; 
	padding: 5px;
	font-size: x-small;
}
.text-warning{
color:#f39c12;
}
#queueFilters .form-group {
	margin-bottom: 0;
}
#queueFilters .filter-actions {
	margin-top: 25px;
}
#queueFilters .help-block {
	font-size: 11px;
	color: #777;
	margin-bottom: 0;
}
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-th-list"></i> Assessment Queue
       <s:if test="acengagement"><span class="text-warning fa fa-warning"></span><b  class="text-warning"> (Manager View)</b>
      	<small>Viewing all assessments</small>
      	</s:if>
      	<s:else>
      	<small>Assessments Currently Assigned to You</small>
      	</s:else>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">

<div class="row">
	<div class="col-xs-12">
		<div class="box box-primary" id="queueFilters" data-showcompleted="<s:property value="showCompleted"/>">
			<div class="box-header with-border">
				<h3 class="box-title"><i class="fa fa-filter"></i> Filters</h3>
			</div>
			<div class="box-body">
				<div class="row">
					<div class="col-md-3">
						<div class="form-group">
							<label for="statusFilter">Status:</label>
							<select id="statusFilter" class="form-control">
								<option value="">-- All Statuses --</option>
							</select>
						</div>
					</div>
					<div class="col-md-2">
						<div class="form-group">
							<label for="fromDateFilter">From:</label>
							<input type="date" id="fromDateFilter" class="form-control" />
						</div>
					</div>
					<div class="col-md-2">
						<div class="form-group">
							<label for="toDateFilter">To:</label>
							<input type="date" id="toDateFilter" class="form-control" />
						</div>
					</div>
					<div class="col-md-2">
						<div class="form-group">
							<label>Quick Ranges:</label>
							<div class="btn-group" style="width: 100%">
								<button type="button" class="btn btn-default btn-block dropdown-toggle" data-toggle="dropdown">
									<i class="fa fa-clock-o"></i> <span id="rangeLabel">Select Range</span> <span class="caret"></span>
								</button>
								<ul class="dropdown-menu" role="menu" id="rangeDropdown">
									<li><a href="#" data-range="today"><i class="fa fa-calendar"></i> Today</a></li>
									<li><a href="#" data-range="7days"><i class="fa fa-calendar"></i> Next 7 Days</a></li>
									<li><a href="#" data-range="thisweek"><i class="fa fa-calendar"></i> This Week</a></li>
									<li><a href="#" data-range="30days"><i class="fa fa-calendar"></i> Next 30 Days</a></li>
									<li><a href="#" data-range="month"><i class="fa fa-calendar"></i> This Month</a></li>
									<li><a href="#" data-range="lastmonth"><i class="fa fa-calendar"></i> Last Month</a></li>
									<li><a href="#" data-range="year"><i class="fa fa-calendar"></i> This Year</a></li>
									<li><a href="#" data-range="alltime"><i class="fa fa-calendar"></i> All Time</a></li>
								</ul>
							</div>
						</div>
					</div>
					<div class="col-md-3">
						<div class="filter-actions">
							<button type="button" class="btn btn-default btn-block" id="clearFilters">
								<i class="fa fa-times"></i> Clear Filters
							</button>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-md-12">
						<p class="help-block" style="margin-top: 8px">
							Date range matches assessments scheduled at any point within the selected window.
							<s:if test="showCompleted">Completed assessments are included in this view.</s:if>
							<s:else>Selecting the Completed status loads finished assessments, which are otherwise left out of the queue.</s:else>
						</p>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="row">
            <div class="col-xs-12">
              <div class="box box-primary">
                <div class="box-body">
                  <table id="assessment_queue" class="table table-striped table-hover dataTable">
                    <thead class="theader">
                      <tr >
                        <th >AppId</th>
                        <th>Name</th>
                        <th>Assessor</th>
                        <th>Start</th>
                        <th>End</th>
                        <th width="300px">Findings</th>
                        <th width="120px">Status</th>
                        <th width="120px"></th>
                      </tr>
                    </thead>
                    <tbody >
					 <s:iterator  value="assessments" status="stat" var="asmt">
					 	<tr id="app<s:property value="id" />" onClick="goTo(${id })" class="tnoborder">
						<td><s:property value="appId"/></td>
						<td><s:property value="name"/></td>
						<td>
							<s:iterator value="assessor" status="stat">
								<s:if test="#stat.index > 0">,&nbsp;</s:if>
								<s:property value="fname"/>&nbsp;<s:property value="lname"/>
							</s:iterator>
						</td>
						<td><s:date name="start" format="yyyy-MM-dd"/></td>
						<td><s:date name="end" format="yyyy-MM-dd"/></td>
						<td>
							<fs:vulncount asmt="${asmt}" levels="${levels }"></fs:vulncount>
						</td>
						<td>
							<s:property value="status"/>
						</td>
						<td id="status<s:property value="id"/>">
								<span class="circle2 glyphicon glyphicon-book bg-gray circle2" title="Report Generated"></span>
								<span class="circle2 glyphicon glyphicon-comment bg-gray" title="Report Submitted for Peer Review" ></span>
								<span class="circle2 glyphicon glyphicon-ok bg-gray" title="Peer Review Complete"></span>
							</td>
						</tr>
					</s:iterator>
					</tbody>
                    <tfoot>
                    </tfoot>
                  </table>
                </div><!-- /.box-body -->
              </div><!-- /.box -->
       </div>
 </div>
              
              


<jsp:include page="../footer.jsp" />
<!--<script src="../dist/js/app.js" ></script>-->
  </body>
</html>