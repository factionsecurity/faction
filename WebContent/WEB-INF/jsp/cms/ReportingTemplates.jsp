<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<style>
.page {
	cursor: pointer;
}

.page:hover {
	font-weight: bold;
}

.css {
	width: 100%;
	height: 700px;
}
</style>
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-book"></i> Report Templates <small><a
				href="https://www.fusesoftsecurity.com/manual/report-designer-tags/"
				target="_blank">Listing of Report Template Tags</a></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:box type="success" title="">
			<bs:row>
				<bs:mco colsize="3">
					<bs:row>
						<bs:mco colsize="12">
							<bs:box type="primary" title="CSS">
								<bs:row>
									<bs:inputgroup name="Default Font Family" colsize="12"
										id="fontname"><s:property value="fontname" /></bs:inputgroup>
								</bs:row>
								<br>
								<bs:row>
									<bs:button color="primary" size="md" colsize="6" text="Update"
										id="cssUpdate"></bs:button>
									<bs:button color="warning" size="md" colsize="6"
										text="Edit CSS" id="editCSS"></bs:button>
								</bs:row>
								<br>
								<hr>
								<s:if test="sectionsEnabled">
									<bs:row>
										<div class="box-header with-border">
											<h3 class="box-title">Report Sections</h3>
										</div>
									</bs:row>
									<bs:row>
										<input type="text" id="sectionName" class="form-control" placeholder="Report Section Name"/>
										<br>
										<button class="btn btn-block btn-info btn-lg" id="addSection">Add
											Section</button>
											<br>
									</bs:row>
									<bs:row>
										<bs:datatable columns="Section Name,Variable,Edit"
											classname="primary" id="reportSectionsTable">
											<s:iterator value="reportSections" var="sectionName">
												<tr>
													<td><s:property value="#sectionName[1]" /></td>
													<td><s:property value="#sectionName[0]" /></td>
													<td> <span class="vulnControl vulnControl-delete deleteSection"
														id="deleteSection_<s:property value="#sectionName[0]"/>"> <i class="fa fa-trash"></i>
													</span></td>
												</tr>
											</s:iterator>
										</bs:datatable>
									</bs:row>
								</s:if>
								<br>
								<hr>
								<bs:row>
									<div class="box-header with-border">
										<h3 class="box-title">Generate Sample Reports</h3>
									</div>
								</bs:row>
								<bs:row>
									<bs:select name="Assessment Type" colsize="6" id="asmtType">
										<s:iterator value="types">
											<option value="${id }">${type }</option>
										</s:iterator>
									</bs:select>
									<bs:select name="Assessment Team" colsize="6" id="asmtTeam">
										<s:iterator value="teams">
											<option value="${id }">${teamName }</option>
										</s:iterator>
									</bs:select>
								</bs:row>
								<bs:row>
									<bs:mco colsize="12">
										<input id="doRetest" type="checkbox" />&nbsp;&nbsp;Gen Retest Report</bs:mco>
								</bs:row>
								<bs:row>
									<br>
									<bs:button color="success" size="md" colsize="12"
										text="Show Sample Report" id="sample"></bs:button>
								</bs:row>

							</bs:box>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="12">
							<hr>
							<bs:box type="primary" title="Documentation">
								<ul>
									<li><a
										href="https://github.com/factionsecurity/report_templates/raw/main/default-report-template.docx">Example
											Template docx</a></li>
									<li><a
										href="https://docs.factionsecurity.com/Custom%20Security%20Report%20Templates/">Report
											Variables and Scripting</a></li>
								</ul>
							</bs:box>
						</bs:mco>
					</bs:row>
				</bs:mco>
				<bs:mco colsize="9">
					<button class="btn btn-success" style="margin-bottom: 5px;"
						id="addTemplate">Add Template</button>
					<bs:datatable columns="Name,Team,Type,Retest?,Edit"
						classname="primary" id="templates">
						<s:iterator value="templates">
							<tr>
								<td><s:property value="name" /></td>
								<td><s:property value="team.teamName" /></td>
								<td><s:property value="type.type" /></td>
								<td>${retest}</td>
								<td><span class="vulnControl editUser" id="tmpEdit${id}">
										<i class="fa fa-edit"></i>
								</span> <span class="vulnControl vulnControl-delete deleteUser"
									id="tmpDel${id}"> <i class="fa fa-trash"></i>
								</span></td>
							</tr>
						</s:iterator>
					</bs:datatable>
				</bs:mco>
			</bs:row>
		</bs:box>



		<bs:modal modalId="cssModal" saveId="SaveCSS" title="Edit CSS">
			<textarea id="css" style="height: auto">${css}</textarea>
		</bs:modal>

		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/cms.js" charset="utf-8"></script>
		</body>
		</html>