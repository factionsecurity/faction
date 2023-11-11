<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />

<style>
.acceptGreen {
	color: green;
}

.rejectRed {
	color: red;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-eye-open"></i> Peer Review Queue <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="12">
				<bs:datatable columns="AppID,Name,User,Created,Report"
					classname="box-info" id="prQueue">
					<s:iterator value="reviews" status="st">
						<tr id="row<s:property value="assessment.id"/>"
							prid="<s:property value="id"/>">
							<td onClick="trackChanges(${id})"><s:property
									value="assessment.appId" /></td>
							<td onClick="trackChanges(${id})"><s:property
									value="assessment.name" /></td>
							<td onClick="trackChanges(${id})"><s:iterator
									value="assessment.assessor" status="stat">
									<s:if test="stat.index != 0">,&nbsp;</s:if>
									<s:property value="fname" escapeJavaScript="true" />&nbsp;<s:property
										value="lname" escapeJavaScript="true" />
								</s:iterator></td>
							<td onClick="trackChanges(${id})"><s:property
									value="created" /></td>
							<td><bs:mco colsize="6">
									<button class="btn btn-md btn-info" text="Download Report"
										onClick="downPdf(${assessment.id})" id="dl${st.count}">
										<i class='fa fa-download'></i> Download Report
									</button>
								</bs:mco></td>
						</tr>
					</s:iterator>
				</bs:datatable>

			</bs:mco>

		</bs:row>


		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/peerreview_queue.js"></script>

		<script>
			var selectedId = -1;
			
			function downPdf(id){	
				var win = window.open('../service/Report.pdf?id='+id, '_blank');
			}
			function trackChanges(id){			
				document.location="TrackChanges?prqueue=true&prid="+id;
			}
		
		</script>

		</body>
		</html>