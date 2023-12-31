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
				<button id="addVuln" class="btn btn-block btn-primary btn-lg"
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
						<div class="form-group">
							<input type="hidden" id="dvulnerability" /> <label for="title"
								class="col-sm-2 control-label">Title: <span
								id="title_header"></span></label>
							<div class="col-sm-4">
								<input type="text" class="form-control" id="title"
									placeholder="Vulnerbility Name">
							</div>
							<label for="title" class="col-sm-2 control-label">Overall
								Severity: <span id="overall_header"></span>
							</label>
							<div class="col-sm-2">
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
						<div class="form-group">
							<label for="category" class="col-sm-2 control-label">Category:
								<span id="dcategory_header"></span>
							</label>
							<div class="col-sm-4">
								<select class="select2 form-control" id="dcategory"
									style="width: 100%">
									<s:iterator value="categories">
										<option value="${id}">${name}</option>
									</s:iterator>
								</select>
							</div>
							<label for="title" class="col-sm-2 control-label">Impact:
								<span id="impact_header"></span>
							</label>
							<div class="col-sm-2">
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
						<div class="form-group">
							<div class="col-sm-6"></div>
							<label for="title" class="col-sm-2 control-label">Likelihood:
								<span id="likelyhood_header"></span>
							</label>
							<div class="col-sm-2">
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
						<br>
					</div>
					<div class="row">
							<div class="col-sm-12">
							<s:if test="customFields != null && customFields.size() >0"><hr></s:if>
							</div>
						<s:iterator value="customFields">
							<div class="form-group">
								<div class="col-md-6">
									<label class="col-sm-2 control-label"
										title="Variable: &#x24;{cf${variable}}">${key}<span id="type${id}_header"></span></label>
									<div class="col-md-4" style="height:50px">
										<s:if test="fieldType == 0">
											<input type="text" class="form-control pull-right" id="type${id}"
												value='${defaultValue}' data-default='${defaultValue}'
												<s:if test="assessment.InPr || assessment.prComplete || assessment.finalized || readOnly">disabled</s:if> />
										</s:if>
										<s:if test="fieldType == 1">
											<br>
											<input type="checkbox" class="icheckbox_minimal-blue"
												style="width: 20px; height: 20px; position: absolute; margin-top: -13px"
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
										<bs:editor name="description" toolbar="Full" id="description"
											clickToEnable="false">

										</bs:editor>
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
										<bs:editor name="recommendation" toolbar="Full"
											id="recommendation" clickToEnable="false">

										</bs:editor>
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
										<bs:editor name="details" toolbar="Full" id="details"
											clickToEnable="false">

										</bs:editor>
									</div>
								</div>
							</div>
							<!-- /.box -->
						</div>
					</div>

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
					class="table table-striped table-hover dataTable">
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
								<td data-sort="${overall}"><span class="vulnName"><s:property
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