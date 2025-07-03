<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<s:set var="hideit"
	value="(assessment.InPr || assessment.prComplete || assessment.finalized)" />

<style>
.templates {
	min-height: 570px;
	max-height: 570px;
}
#summary{
	background-color:white;
}
#risk{
	background-color:white;
}
#notes{
	background-color:white;
}
#engagmentnotes{
	background-color:white;
}
</style>


<div class="row">
	<div class="col-md-6">
		<!-- Horizontal Form -->
		<div class="box box-info">
			<div class="box-header with-border">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-user"></i> Contacts
				</h3>
			</div>
			<!-- /.box-header -->
			<!-- form start -->
			<form class="form-horizontal">
				<div class="box-body">
					<div class="form-group">
						<label for="assessor" class="col-sm-2 control-label">Assessor(s)</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control" id="assessor"
								value="<s:iterator value="assessment.assessor" ><s:property value="fname"/>&nbsp;<s:property value="lname"/>; </s:iterator>">

						</div>
					</div>
					<div class="form-group">
						<label for="engagement" class="col-sm-2 control-label">Project
							Manager</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control"
								id="engagement"
								value="<s:property value="assessment.engagement.fname"/>&nbsp;<s:property value="assessment.engagement.lname"/>">
						</div>
					</div>
					<div class="form-group">
						<label for="remediation" class="col-sm-2 control-label">Remediation</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control"
								id="remediation"
								value="<s:property value="assessment.remediation.fname"/>&nbsp;<s:property value="assessment.remediation.lname"/>">
						</div>
					</div>
					<div class="form-group">
						<label for="Distro" class="col-sm-2 control-label">Distro</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control" id="Distro"
								value="<s:property value="assessment.distributionList"/>">
						</div>
					</div>
					<!--  <div class="form-group">
						<label for="Distro" class="col-sm-2 control-label">Status</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control" id="status"
								value="<s:property value="assessment.status"/>">
						</div>
					</div>-->


				</div>
			<!-- /.box-body -->
			</form>
		</div>
	<!-- /.box -->
	</div>

	<div class="col-md-6">
		<!-- Horizontal Form -->
		<div class="box box-primary">
			<div class="box-header with-border">
				<h3 class="box-title">
					<i class="glyphicon  glyphicon-list-alt"></i> Assessment Info
				</h3>
			</div>
			<!-- /.box-header -->
			<!-- form start -->
			<div class="form-horizontal">
				<div class="box-body">
					<div class="form-group">
						<label for="type" class="col-sm-2 control-label">Type</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control" id="type"
								value="<s:property value="assessment.type.type"/>">
						</div>
					</div>
					<div class="form-group">
						<label for="team" class="col-sm-2 control-label">Team</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control" id="team"
								value="<s:property value="assessment.assessor[0].team.teamName"/>">
						</div>
					</div>
					<div class="form-group">
						<label for="campaign" disabled="" class="col-sm-2 control-label">Campaign</label>
						<div class="col-sm-10">
							<input type="text" disabled="" class="form-control" id="campaign"
								value="<s:property value="assessment.campaign.name"/>">
						</div>
					</div>
					<div class="form-group">
						<label class="col-sm-2 control-label">Date range</label>
						<div class="col-sm-10">
							<div class="input-group ">
								<div class="input-group-addon ">
									<i class="fa fa-calendar"></i>
								</div>
								<input disabled="" class="form-control pull-right"
									id="reservation" type="text"
									value='<s:property value="assessment.start"/> - <s:property value="assessment.end"/>'>
							</div>
							<!-- /.input group -->
						</div>
					</div>
				</div>
				<!-- /.box-body -->

			</div>
		</div>
		<!-- /.box -->
	</div>
</div>

<s:if test="assessment.varsExist">	
					
<div class="row">
	<div class="col-md-12">
		<!-- Horizontal Form -->
		<div class="box box-primary">
			<div class="box-header with-border">
				<h3 class="box-title">
					<i class="glyphicon  glyphicon-list-alt"></i> Assessment Variables
				</h3>
			</div>
			<!-- /.box-header -->
			<!-- form start -->
			<div class="form-horizontal">
				<div class="box-body">
				<div class="row">	
					<s:iterator value="assessment.customFields">
						<s:if test="type.fieldType < 3">
							<div class="form-group col-sm-3">
								<div class="col-sm-12">
								<label class=""
									title='Variable: &#x24;{cf<s:property value="type.variable"/>}'><s:property value="type.key"/>
										<span id="cust${id}_header"></span><br/><small>Variable: &#x24;&#x7B;cf<s:property value="type.variable"/>&#x7D;</small>
										</label>

								<s:if test="!type.readonly">
									<div class="">
										<s:if test="type.fieldType == 0">
											<input type="text" class="form-control" id="cust${id}"
												value='<s:property value="value"/>'
												<s:if test="assessment.InPr || assessment.prComplete || assessment.finalized">disabled</s:if> />
										</s:if>
										<s:if test="type.fieldType == 1">
											<br>
											<input type="checkbox" class="icheckbox_minimal-blue"
												style="width: 20px; height: 20px; margin-top: -30px"
												id="cust<s:property value="id"/>"
												<s:if test="value == 'true'">checked</s:if>
												<s:if test="assessment.InPr || assessment.prComplete || assessment.finalized">disabled</s:if> />
										</s:if>
										<s:if test="type.fieldType == 2">
											<select class='form-control select2 ' style='width: 100%;'
												id="cust<s:property value="id"/>"
												<s:if test="currentAssessment.finalized">readonly</s:if>>
												<s:iterator value="type.defaultValue.split(',')" var="option">
													<s:set var="aOption" value="option" />
													<option value="<s:property value="option"/>"
														<s:if test="value.equals(#aOption)">selected</s:if>><s:property
															value="option" /></option> 
												</s:iterator>
											</select>
										</s:if>
									</div>
								</s:if>
								<s:else>
									<div class="">
										<input type="text" class="form-control" value='${value}'
											disabled>
									</div>
								</s:else>
								</div>
							</div>
						</s:if>
					</s:iterator>
					</div> <!-- end iterator -->
				</div>
			</div>
		</div>
	</div>
</div>
</s:if>


<div class="row">
	<!-- SUMMARY Section -->
	<div class="col-md-10">
		<div class="box box-warning">
			<div class="box-header">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-edit"></i> High Level Summary <span
						id="summary_header" class="edited"></span><small></small>
				</h3>
				<div class="box-tools pull-right"></div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<form>
					<div name="editor1" toolbar="Full" id="summary"
						clickToEnable="false" readonly="${hideit}">
						<s:property value="assessment.summary" />
					</div>
				</form>
				<s:if test="!(hideit)">
					<br>
					<div class="row">
						<div class="col-md-3"></div>
						<div class="col-md-6">
							<input id="tempSearch1" class="form-control tempSearch"
								for="summary" placeholder="Search for Template" />
						</div>
					</div>
				</s:if>


			</div>
		</div>
		<!-- /.box -->
	</div>
	<div class="col-md-2">
		<div class="box box-warning">
			<div class="box-header">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-edit"></i> Templates <small>Double click to append</small>

				</h3>
				<small></small>
				<div class="box-tools pull-right"></div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<div class="form-group">
					<select id="summaryTemplates" multiple="false"
						class="form-control templates">
						<s:iterator value="summaryTemplates">
							<option value="<s:property value='id'/>" title="<s:property value='user.fname'/> <s:property value='user.lname'/>"
								global="<s:property value="global"/>"
								<s:if test="global == true">
								class='globalTemplate'
								</s:if><s:else>
								class='userTemplate'
								</s:else>>
							<s:property value="title"/>
							</option>
						</s:iterator>
					</select>
				</div>
				<s:if test="!(hideit)">
					<div class="row">
						<div class="col-md-1"></div>
						<div class="col-md-11" style="padding-top: 8px">
							<span id="saveTemplateSideBar"
								class="vulnControl vulnControl-add saveTemplate" for="summary"
								title='Save or Create Template'> <i class="fa fa-save"></i>
							</span> <span id="addTemplateSideBar"
								class="vulnControl vulnControl-add addTemplate" for="summary"
								title='Add Templates to Editor'> <i class="fa fa-plus"></i>
							</span> <span id="deleteTemplateSideBar"
								class="vulnControl vulnControl-delete deleteTemplate"
								title='Delete Selected Templates' for="summary"> <i
								class="fa fa-trash"></i>
							</span>
						</div>
					</div>
				</s:if>
			</div>


		</div>
	</div>
	<!-- /.box -->
</div>
<!-- </div>
   <div class="row">  -->

<!-- Risk Analysis Section -->
<div class="row">
	<div class="col-md-10">
		<div class="box box-danger">
			<div class="box-header">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-asterisk"></i> Detailed Summary /
					Risk Analysis / Scope<span id="risk_header" class="edited"></span><small></small>
				</h3>
				<div class="box-tools pull-right"></div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<form>
					<div name="editor2" toolbar="Full" id="risk"
						clickToEnable="false" readonly="${hideit}">
						<s:property value="assessment.riskAnalysis"/>
					</div>
				</form>
				<s:if test="!(hideit)">
					<br>
					<div class="row">
						<div class="col-md-3"></div>
						<div class="col-md-6">
							<input id="tempSearch2" class="form-control tempSearch "
								for="risk" placeholder="Search for Template" />
						</div>
					</div>
				</s:if>
			</div>
		</div>
		<!-- /.box -->
	</div>
	<div class="col-md-2">
		<div class="box box-warning">
			<div class="box-header">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-edit"></i> Templates <small>Double click to append</small>
				</h3>
				<small></small>
				<div class="box-tools pull-right"></div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<div class="form-group">
					<select id="riskTemplates" multiple="false"
						class="form-control templates">
						<s:iterator value="riskTemplates">
							<option value="<s:property value='id'/>" title="<s:property value='user.fname'/> <s:property value='user.lname'/>"
								global="<s:property value='global'/>"
								<s:if test="global == true">
								class='globalTemplate'
							</s:if><s:else>
								class='userTemplate'
							</s:else>>
							<s:property value='title'/>
							</option>
						</s:iterator>
					</select>
				</div>
				<s:if test="!(hideit)">
					<div class="row">
						<!-- <div class="col-md-8">
							<input class="form-control searchTemplate"
								for="risk" placeholder="Search for Template" />
						</div>-->
						<div class="col-md-1"></div>
						<div class="col-md-11" style="padding-top: 8px">
							<span id="saveTemplateSideBar"
								class="vulnControl vulnControl-add saveTemplate" for="risk"
								title='Save or Create Template'> <i class="fa fa-save"></i>
							</span> <span id="addTemplateSideBar"
								class="vulnControl vulnControl-add addTemplate" for="risk"
								title='Add Templates to Editor'> <i class="fa fa-plus"></i>
							</span> <span class="vulnControl vulnControl-delete deleteTemplate"
								title='Delete Templates' for="risk"> <i
								class="fa fa-trash"></i>
							</span>
						</div>
					</div>
				</s:if>
			</div>


		</div>
	</div>
</div>

<div class="row">

	<!-- Engagement Notes Section -->
	<div class="col-md-12">
		<div class="box box-primary">
			<div class="box-header">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-pencil"></i> Engagement Info <small>URLs,
						credentials, and other assessment information. Not included in
						report</small>
				</h3>
				<div class="box-tools pull-right"></div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<div class="col-md-6">
					<form>
						<div name="engagementnotes" toolbar="None"
							id="engagementnotes" readonly="true" clickToEnable="false">
							<s:property value="assessment.accessNotes" />
						</div>
					</form>
				</div>
				<div class="col-md-6">
					<div class="form-group">
						<br> <input id="files" type="file" multiple name="file_data"
							<s:if test="hideit">disabled</s:if> />
					</div>
				</div>
			</div>
			<!-- /.box -->
		</div>

	</div>
</div>
