<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<jsp:include page="../header.jsp" />

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-ok"></i> Verification/Retest Queue <small>Verifications
				Currently Assigned to You</small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<div class="row">
			<div class="col-xs-12">
				<div class="box box-primary">
					<div class="box-body">
						<table id="verificationQueue"
							class="table table-striped table-hover dataTable">
							<thead class="theader">
								<tr>
									<th>AppId</th>
									<th>Name</th>
									<th>Vulnerability</th>
									<th>Assessor</th>
									<th>Start</th>
									<th>End</th>
								</tr>
							</thead>
							<tbody>
								<s:iterator value="verifications">
									<tr id="app<s:property value="id"/>" onClick="goTo(${id})">
										<td><s:property value="assessment.appId" /></td>
										<td><s:property value="assessment.name" /></td>
										<td><s:property
												value="verificationItems[0].vulnerability.name" /></td>
										<td><s:iterator value="assessment.assessor" status="stat">
												<s:if test="#stat.index != 0">,&nbsp;</s:if>
												<s:property value="fname" />&nbsp;<s:property value="lname" />
											</s:iterator></td>
										<td><s:date name="start" format="MM/dd/yyy" /></td>
										<td><s:date name="end" format="MM/dd/yyy" /></td>
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


		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/verification_queue.js"></script>
		<script>
			function goTo(id) {
				document.location = "Verifications?id=" + id;
			}
		</script>

		</body>
		</html>