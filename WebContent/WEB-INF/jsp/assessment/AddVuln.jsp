<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<!-- Controls -->
<s:set var="hideit"
	value="(assessment.InPr || assessment.prComplete || assessment.finalized)" />
<!--  hide it: ${hideit} -->
<s:set var="isFinal" value="(assessment.finalized)" />
<div class="row">
	<div class="col-md-12">
		<bs:row>
			&nbsp;
			</bs:row>
		<bs:row>
			<bs:mco colsize="2">
				<button id="addVuln" class="btn btn-primary btn-lg"
					<s:if test="hideit">disabled</s:if>>
					<b><i class="glyphicon glyphicon-plus"></i> New Vulnerability</b>
				</button>
			</bs:mco>
		</bs:row>
		<bs:row>
			&nbsp;
			</bs:row>
	</div>
	<!-- /.col -->
</div>
<style>
.btn:active.focus, .btn:active:focus, .btn:focus {
	outline: none !important;
}

.focus {
	outline: none !important;
}

.active {
	outline: none !important;
}

.activeVector {
	background-color: purple !important;
	color: white !important;
	font-weight: bold;
}

label.btn {
	background-color: lightgray;
	color: #030D1C;
}

label.btn:hover {
	font-weight: bold;
}

.scoreBody {
	background-color: lightGray;
	border-radius: 9px;
	text-align: center;
	padding-bottom: 5px;
	margin-bottom: 40px;
	width: 150px;
}

.scoreBody h3 {
	font-size: xxx-large;
	color: lightgray;
	border-top-right-radius: 9px;
	border-top-left-radius: 9px;
	margin-top: 0px;
}

.scoreBody span {
	font-size: large;
	font-weight: bold;
}

h3.None {
	background-color: #00a65a;
}

span.None {
	color: #00a65a;
}

h3.Low {
	background-color: #39cccc;
}

span.Low {
	color: #39cccc;
}

h3.Medium {
	background-color: #00c0ef;
}

span.Medium {
	color: #00c0ef;
}

h3.High {
	background-color: #f39c12;
}

span.High {
	color: #f39c12;
}

h3.Critical {
	background-color: #dd4b39;
}

span.Critical {
	color: #dd4b39;
}

.circle {
	border-radius: 50%;
	width: 25px;
	height: 25px;
	padding: 7px;
	padding-top: 6px;
	font-size: small;
	color: white;
	z-index: 100000;
}

.moveDown {
	z-index: 100000;
}

tr  td {
	border-top: 0px !important;
}

.disabled {
	opacity: 0.2;
	pointer-events: none;
}

td:first-child {
	border-left-width: 5px;
	border-left-style: solid;
}

.selected td:first-child {
	border-left-style: dotted;
}

.userEdit {
	background: #f39c12;
	color: white;
	font-weight: bold;
	border-radius: 5px;
	padding-left: 4px;
	padding-right: 7px;
	padding-top: 2px;
	padding-bottom: 4px;
	margin-left: 30px;
}

.cvsstrue {
	display: block;
}
.cvssfalse {
	display: none;
}

#description {
	background-color: white
}
#recommendation {
	background-color: white
}
#details {
	background-color: white
}
</style>

<div class="row">
	<div class="col-xs-8">
		<div id="vulnForm" class="box box-danger disabled">
			<div class="box-header">
				<h3 class="box-title">
					<i class="fa fa-bug"></i> Vulnerability Details
				</h3>
			</div>
			<form>
				<div class="box-body">
					<div class="form-horizontal">
						<bs:mco colsize="6">
							<bs:row>
								<div class="form-group">
									<input type="hidden" id="dvulnerability" /> <label for="title"
										class="col-sm-2 control-label">Title: <span
										id="title_header"></span></label>
									<div class="col-sm-10">
										<input type="text" class="form-control" id="title"
											placeholder="Vulnerbility Name">
									</div>
								</div>
							</bs:row>
							<bs:row>
								<div class="form-group">
									<label for="category" class="col-sm-2 control-label">Category:
										<span id="dcategory_header"></span>
									</label>
									<div class="col-sm-10">
										<select class="select2 form-control" id="dcategory"
											style="width: 100%">
											<s:iterator value="categories">
												<option value="${id}">${name}</option>
											</s:iterator>
										</select>
									</div>
								</div>

							</bs:row>
							<s:if test="sectionsEnabled">
								<bs:row>
									<div class="form-group">
										<label for="section" class="col-sm-2 control-label">Section:
											<span id="reportSection_header"></span>
										</label>
										<div class="col-sm-10">
											<select class="select2 form-control field-error" id="reportSection"
												style="width: 100%">
												<s:iterator value="sections" var="section">
													<option value="<s:property value="#section[0]"/>"><s:property value="#section[1]"/></option>
												</s:iterator>
											</select>
										</div>
									</div>
								</bs:row>
							</s:if>
							<s:if test="assessment.type.cvss31 || assessment.type.cvss40" >
							<div class="cvss<s:property value="assessment.type.cvss31 || assessment.type.cvss40"/>">
								<bs:row>
										<div class="form-group">
											<label for="title" class="col-sm-2 control-label">CVSS: <span id="cvssString_header"></span>
											</label>
											<div class="col-sm-9">
												<input type="text" class="form-control" id="cvssString"
													<s:if test="assessment.type.cvss31">placeholder="CVSS:3.1/..."</s:if><s:else>placeholder="CVSS:4.0/..."</s:else> />
												<input type="hidden" id="cvssScore"/>
												<input type="hidden" id="overall"/>
											</div>
											<span id="cvssModal" class="btn btn-primary col-sm-1"><i class="fa-solid fa-calculator"></i></span>
										</div>
								</bs:row>
							</div>
							</s:if>
						</bs:mco>
						<bs:mco colsize="6">
							<div class="cvss<s:property value="!(assessment.type.cvss31||assessment.type.cvss40)"/>">
								<bs:row>
									<div class="form-group">
										<label for="title" class="col-sm-4 control-label">Overall
											Severity: <span id="overall_header"></span>
										</label>
										<div class="col-sm-4">
											<select class="select2 form-control" id="overall"
												style="width: 100%">
												<s:iterator value="levels" status="stat">
													<s:if
														test="risk != null && risk != 'Unassigned' && risk != ''">
														<option value="${stat.index}">${risk}</option>
													</s:if>
												</s:iterator>
											</select>
										</div>
									</div>
								</bs:row>
								<bs:row>
									<div class="form-group">
										<label for="title" class="col-sm-4 control-label">Impact:
											<span id="impact_header"></span>
										</label>
										<div class="col-sm-4">
											<select class="select2 form-control" id="impact"
												style="width: 100%">
												<s:iterator value="levels" status="stat">
													<s:if
														test="risk != null && risk != 'Unassigned' && risk != ''">
														<option value="${stat.index}">${risk}</option>
													</s:if>
												</s:iterator>
											</select>
										</div>
									</div>
								</bs:row>
								<bs:row>
									<div class="form-group">
										<label for="title" class="col-sm-4 control-label">Likelihood:
											<span id="likelyhood_header"></span>
										</label>
										<div class="col-sm-4">
											<select class="select2 form-control" id="likelyhood"
												style="width: 100%">
												<s:iterator value="levels" status="stat">
													<s:if
														test="risk != null && risk != 'Unassigned' && risk != ''">
														<option value="${stat.index}">${risk}</option>
													</s:if>
												</s:iterator>
											</select>
										</div>
									</div>
								</bs:row>
							</div>
							<div class="cvss<s:property value="(assessment.type.cvss31 || assessment.type.cvss40)"/>">
								<bs:row>
									<bs:mco colsize="12">
										<div class="scoreBody pull-right">
											<h3 class="scoreBox None" id="score">0.0</h3>
											<span class="severityBox None" id="severity">None</span>
										</div>
									</bs:mco>
								</bs:row>
							</div>
						</bs:mco>
						<br>
					</div>
					<div class="row">
						<div class="col-sm-12">
							<s:if test="customFields != null && customFields.size() >0">
								<hr>
							</s:if>
						</div>
						<s:iterator value="customFields">
						<s:if test="fieldType != 3">
							<div class="form-group">
								<div class="col-md-6">
									<label class="col-sm-2 control-label"
										title="Variable: &#x24;{cf${variable}}">${key}<span
										id="type${id}_header"></span></label>
									<div class="col-md-4" style="height: 50px">
										<s:if test="fieldType == 0">
											<input type="text" class="form-control pull-right"
												id="type${id}" value='${defaultValue}'
												data-default='${defaultValue}'
												<s:if test="assessment.InPr || assessment.prComplete || assessment.finalized || readOnly">disabled</s:if> />
										</s:if>
										<s:if test="fieldType == 1">
											<input type="checkbox" class="icheckbox_minimal-blue"
												style="width: 20px; height: 20px; margin-top: -13px"
												data-default='${defaultValue}'
												id="type<s:property value="id"/>"
												<s:if test="defaultValue == 'true'">checked</s:if>
												<s:if test="assessment.InPr || assessment.prComplete || assessment.finalized || readonly">disabled</s:if> />
										</s:if>
										<s:if test="fieldType == 2">
											<select class='form-control select2 ' style='width: 100%;'
												id="type<s:property value="id"/>"
												data-default='${defaultValue.split(",")[0]}'
												<s:if test="assessment.InPr || assessment.prComplete || assessment.finalized || readonly">disabled</s:if>>
												<s:if test="currentAssessment.finalized">readonly</s:if>>
												<s:iterator value="defaultValue.split(',')" var="option">
													<option value="<s:property value="option"/>"
														<s:if test="option == defaultValue">selected</s:if>><s:property
															value="option" /></option>
												</s:iterator>
											</select>
										</s:if>
									</div>
								</div>
							</div>
							</s:if>

						</s:iterator>
					</div>
					<div class="row">

						<!-- Vuln Description Section -->
						<div class="col-md-12">
							<div class="box box-default">
								<div class="box-header">
									<h3 class="box-title">
										<i class="glyphicon glyphicon-edit"></i> Vulnerability
										Description: <span id="description_header"></span>
									</h3>
								</div>
								<!-- /.box-header -->
								<div class="box-body pad">
									<div>
										<div name="description" toolbar="Full" id="description"
											clickToEnable="false">

										</div>
									</div>
								</div>
							</div>
							<!-- /.box -->
						</div>
					</div>
					<div class="row">
						<!-- Vuln Recommendation Section -->
						<div class="col-md-12">
							<div class="box box-info">
								<div class="box-header">
									<h3 class="box-title">
										<i class="glyphicon glyphicon-edit"></i> Vulnerability
										Recommendation: <span id="recommendation_header"></span>
									</h3>
								</div>
								<!-- /.box-header -->
								<div class="box-body pad">
									<div>
										<div name="recommendation" toolbar="Full"
											id="recommendation" clickToEnable="false">

										</div>
									</div>
								</div>
							</div>
							<!-- /.box -->
						</div>
					</div>
					<div class="row">
						<!-- Vuln Recommendation Section -->
						<div class="col-md-12">
							<div class="box box-info">
								<div class="box-header">
									<h3 class="box-title">
										<i class="glyphicon glyphicon-edit"></i> Details: <span
											id="details_header"></span>
									</h3>
								</div>
								<!-- /.box-header -->
								<div class="box-body pad">
									<div>
										<div name="details" toolbar="Full" id="details"
											clickToEnable="false">

										</div>
									</div>
								</div>
							</div>
							<!-- /.box -->
						</div>
					</div>
					
					<!-- Custom Forms -->
						<jsp:include page="VulnRichTextForms.jsp"/>
					<!--  Custom Forms -->

				</div>
				<!-- /.box-body -->
			</form>
		</div>
	</div>
	<div class="col-xs-4">
		<div class="box box-danger">
			<div class="box-header">
				<h3 class="box-title">
					<i class="fa fa-bug"></i> Vulnerability Findings
				</h3>
				<s:if
					test="!(assessment.InPr || assessment.prComplete || assessment.finalized)">
					<span id="deleteMulti" class="fa fa-trash circle pull-right"
						style="background: #192338" title="Delete Multiple Vulns"></span>
				</s:if>
			</div>
			<!-- /.box-header -->
			<div class="box-body">
				<div class="moveDown"></div>
				<table id="vulntable"
					class="table table-striped table-hover dataTable" style="width:100%">
					<thead class="theader">
						<tr>
							<th></th>
							<th>Finding</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<s:iterator value="avulns">
							<tr data-vulnid="${id}">
								<td class="sev${overallStr}"><input type="checkbox"
									id="ckl<s:property value="id"/>" /></td>
								<s:if test="assessment.type.cvss31 || assessment.type.cvss40">
								<td data-sort="${cvssScore}">
								</s:if>
								<s:else>
								<td data-sort="${overall}">
								</s:else>
								<span class="vulnName"><s:property
											value="name" /></span><br> <span class="category"> <s:property
											value="category.name" /></span><BR> <span class="severity"><s:property
											value="overallStr" /></span></td>
								<td><span class="vulnControl vulnControl-delete"
									id="deleteVuln<s:property value="id"/>"
									<s:if test="hideit">disabled</s:if>><i
										class="fa fa-trash" title="Delete Vulnerability"></i></span></td>
							</tr>
						</s:iterator>
					</tbody>
					<tfoot>
					</tfoot>
				</table>
			</div>
			<!-- /.box-body -->
		</div>
		<!-- /.box -->
	</div>
</div>

<input type="hidden" id="isCVSS40" value="<s:property value="assessment.type.cvss40"/>"></input>
<input type="hidden" id="isCVSS31" value="<s:property value="assessment.type.cvss31"/>"></input>

<script>
  function getValueFromId(id){
      switch(id){
          <s:iterator value="levels">
          case "${riskId}": return "${risk}";
          </s:iterator>
          default : return "Unassigned";
      }
  }
   function getIdFromValue(value){
      switch(value){
          <s:iterator value="levels">
          case "${risk}": return ${riskId};
          </s:iterator>
          default : return -1;
      }
  }
  </script>