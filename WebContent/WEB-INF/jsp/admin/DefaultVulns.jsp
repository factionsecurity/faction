<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<style>
.jconfirm-content input {
	background: #030D1C;
}

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
</style>
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="fa fa-bug"></i> Default Vulnerabilities and Ratings <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<div class="row">
			<bs:mco colsize="6">
				<bs:row>
					<jsp:include page="AddCat.jsp" />
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="warning"
							title="Vulnerability Tracking Times <span style=font-size:x-small>Number of calendar days each item is due.</span>">

							<s:iterator value="levels">
								<bs:row>
									<s:set var="levelName" value="getLevelString(riskId)" />
									<bs:inputgroup
										name="${levelName}  - Level ${riskId } Due Date:" colsize="6"
										id="due_${riskId}">${daysTillDue }</bs:inputgroup>
									<bs:inputgroup
										name="${levelName }- Level ${riskId } Warning Date:"
										colsize="6" id="warn_${riskId}">${daysTillWarning}</bs:inputgroup>
								</bs:row>
								<bs:row>
								&nbsp;
								</bs:row>
							</s:iterator>

							<br>
							<bs:row>
							</bs:row>
							<bs:row>
								<bs:mco colsize="6"></bs:mco>
								<bs:button color="primary" size="md" colsize="6" text="Update"
									id="saveDates"></bs:button>
							</bs:row>

						</bs:box>
					</bs:mco>
				</bs:row>
				<s:if test="tier != 'consultant'">
					<bs:row>
						<s:include value="VerificationSettings.jsp"></s:include>
					</bs:row>
				</s:if>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:row>
					<jsp:include page="AddVuln.jsp" />
					<!-- End row -->
				</bs:row>
				<bs:row>
					<bs:mco colsize="3">
						<button class="btn btn-danger" id="importDB">Import from
							Faction</button>

					</bs:mco>
					<bs:mco colsize="3">
						<a href="https://github.com/factionsecurity/data/tree/master/db">
							View VulnDB on GitHub</a>
					</bs:mco>
					<bs:mco colsize="3">
						<a class="btn btn-success"
							href="https://docs.factionsecurity.com/Importing%20Your%20Vulnerability%20Templates%20Via%20the%20API/">
							Import Your Own Via API</a>
					</bs:mco>
				</bs:row>
				<bs:row>
				</bs:row>
				<bs:row>
					<br />
				</bs:row>
				<bs:row>
					<bs:mco colsize="12">
						<button class="btn btn-danger" id="downloadVulns">Download
							all Vulnerabilities to CSV</button>

					</bs:mco>
				</bs:row>
				<br />
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="info"
							title="Risk Level Settings <small>Change the name of the risk level</small>">
							<s:iterator value="levels">
								<bs:row>
									<bs:mco colsize="3">
										<label class="pull-right"><s:property
												value="getLevelString(riskId)" /> - Level ${riskId}:</label>
									</bs:mco>
									<bs:mco colsize="6">
										<input id="riskName${riskId }" class="form-control pull-right"
											type="text" placeholder="Risk Name (ex. Critical)"
											value="${risk}"></input>
									</bs:mco>
									<bs:button color="info" size="md" colsize="3" text="Update"
										id="updateRisk${riskId}">
									</bs:button>
								</bs:row>
								<br>
							</s:iterator>

						</bs:box>
					</bs:mco>
				</bs:row>
			</bs:mco>
		</div>



		<jsp:include page="../msgModal.jsp" />

		<jsp:include page="../footer.jsp" />
		<script>
			let vulnTypes = []
<s:iterator value = "vulntypes" status="stat">
			vulnTypes.push(${ id });
</s:iterator >

			function getValueFromId(id) {
				switch (id) {
		  		<s:iterator value="levels">
					case "${riskId}": return "${risk}";
				</s:iterator>
		  			default : return "Unassigned";
				}
				return -1;
	  		}
			function getIdFromValue(value) {
				switch (value) {
		  		<s:iterator value="levels">
					case "${risk}": return ${riskId};
				</s:iterator>
		  			default : return "-1";
				}
				return -1;
	  		}
		</script>
		<script src="../dist/js/default_vulns.js"></script>

		</body>

		</html>