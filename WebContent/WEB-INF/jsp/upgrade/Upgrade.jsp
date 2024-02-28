<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<jsp:include page="../header.jsp" />
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<style>
</style>
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="fa fa-arrow-up"></i> Upgrade<small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="12">
				<bs:box type="success" title="">
					<div style="padding-left: 30px; padding-right: 30px">
						<bs:row>
							<bs:mco colsize="4"></bs:mco>
							<button class="btn btn-success dashboardButton col-md-4"
								onClick="javascript:document.location='https://portal.factionsecurity.com'">
								<i class="fa fa-arrow-up"></i>&nbsp;&nbsp;&nbsp;Upgrade
								Now&nbsp;&nbsp;&nbsp;<i class="fa fa-arrow-up"></i>
							</button>
						</bs:row>
						<bs:row>
							<bs:mco colsize="12">
								<h1>🚀 Elevate Your Security Assessments with Peer-Reviewed
									Penetration Testing Reports!</h1>
								<h3>Peer-reviewed reports ensure a meticulous examination
									of every facet of your penetration testing methodology. By
									harnessing the collective expertise of your team, you can
									guarantee unparalleled accuracy and precision in identifying
									vulnerabilities and recommending targeted solutions.</h3>

								<h3>Faction enables the following features in Peer Review:</h3>
								<h3>
									<ul>
										<li>Visual Change Tracking And Highlighting</li>
										<li>Version Each Change</li>
										<li>Add Comments To Each Section</li>
										<li>Checks To Ensure All Comments Are Addressed</li>
										<li>Allow Multiple Users To Edit At The Same Time</li>
									</ul>
								</h3>
								<center>
									<video id="peerreview" width="700" controls autoplay loop muted>
										<source src="../dist/upgrade/peer-review.mp4" type="video/mp4">
									</video>
									<script>
										document.getElementById('peerreview')
												.play();
									</script>
								</center>

								<h1>✅ Stay On Top Of Vulnerability Remediation!</h1>
								<h3>Faction is your all-in-one solution for effortless
									Vulnerability Remediation Management. Streamline Vulnerability
									Remediation with custom Service Level Agreements (SLAs),
									ensuring critical weaknesses never go unchecked.</h3>

								<h3>Faction enables the following features in Remediation
									and Vulnerability Management:</h3>
								<h3>
									<ul>
										<li>Custom SLAs For Every Severity</li>
										<li>Custom Warnings Before Issues Reach the SLA Date</li>
										<li>Assign Issues to Assessors for Retest</li>
										<li>Custom Retest Reports</li>
										<li>Vulnerability Annotations and Notes</li>
										<li>Track Issues to Closer in Dev and Prod Environments</li>
									</ul>
								</h3>
								<center>
									<video id="remediation" width="700" controls autoplay loop
										muted>
										<source src="../dist/upgrade/remediation.mp4" type="video/mp4">
									</video>
									<script>
										document.getElementById('remediation')
												.play();
									</script>
								</center>
							</bs:mco>
						</bs:row>
						<bs:row>
							<br />
							<br />
							<br />
							<br />
							<bs:mco colsize="4"></bs:mco>
							<button class="btn btn-success dashboardButton col-md-4"
								onClick="javascript:window.location='https://portal.factionsecurity.com'">
								<i class="fa fa-arrow-up"></i>&nbsp;&nbsp;&nbsp;Upgrade
								Now&nbsp;&nbsp;&nbsp;<i class="fa fa-arrow-up"></i>
							</button>
							<br />
							<br />
						</bs:row>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>

		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/options.js"></script>


		</body>
		</html>