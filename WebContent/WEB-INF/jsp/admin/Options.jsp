<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<jsp:include page="../header.jsp" />
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<style>
.select2-dropdown {
	z-index: 99999999;
	box-shadow: rgba(0, 0, 0, 0.15) 1.95px 1.95px 2.6px;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-wrench"></i> Settings and Options <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="success" title="Assessment Type">
					<bs:row>
						<bs:button color="success" size="md" colsize="3"
							text="<i class='fa fa-plus'></i> Add" id="addType"></bs:button>
					</bs:row>
					<br>
					<bs:row>
						<bs:mco colsize="12">
							<bs:datatable columns="Name,Risk Rating System,&nbsp;" classname="" id="type">
								<s:iterator value="types">
									<tr>
										<td><s:property value="type" /></td>
										<td><s:property value="ratingSystemName" /></td>
										<td width="100px"><span onclick="editType(this,${id })"
											class="vulnControl"> <i class="fa fa-edit"></i>
										</span> <span onclick="delType(this,${id })"
											class="vulnControl vulnControl-delete"> <i
												class="fa fa-trash"></i>
										</span></td>
									</tr>
								</s:iterator>
							</bs:datatable>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
			<bs:mco colsize="6">
				<bs:box type="warning" title="Campaign">
					<bs:row>
						<bs:button color="success" size="md" colsize="3"
							text="<i class='fa fa-plus'></i> Add" id="addCampaign">
						</bs:button>
					</bs:row>
					<br>
					<bs:row>
						<bs:mco colsize="12">
							<bs:datatable columns="Name,Options" classname="" id="campaign">
								<s:iterator value="campaigns">
									<tr>
										<td><s:property value="name" /></td>
										<td width="100px"><span
											onclick="editCampaign(this,${id })" class="vulnControl">
												<i class="fa fa-edit"></i>
										</span> <span onclick="delCampaign(this,${id })"
											class="vulnControl vulnControl-delete"> <i
												class="fa fa-trash"></i>
										</span></td>
									</tr>
								</s:iterator>

							</bs:datatable>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>

			<bs:mco colsize="6">
				<s:if test="acadmin == true && tier != 'consultant'">
					<bs:box type="primary" title="Custom Fields">
						<bs:row>
							<bs:button color="success" size="md" colsize="3"
								text="<i class='fa fa-plus'></i> Add" id="addCF">
							</bs:button>
						</bs:row>
						<br>
						<bs:row>
							<bs:mco colsize="12">
								<bs:datatable
									columns="Name,Variable,Default,Field Type,Applied To,Read Only,Edit"
									classname="" id="campaign">
									<s:iterator value="custom">
										<tr>
											<td><input value="<s:property value="key"/>" id="key${id}"
												class="form-control pull-right" /></td>
											<td><input id="var${id}" value="<s:property value="variable"/>"
												class="form-control pull-right" /></td>
											<td><input id="default${id}" value="<s:property value="defaultValue"/>"
												class="form-control pull-right" /></td>
											<td><s:property value="fieldTypeStr"/></td>
											<td><s:property value="typeStr"/></td>
											<s:if test="readonly">
												<td><input type=checkbox id="ro${id}" checked /></td>
											</s:if>
											<s:else>
												<td><input type=checkbox id="ro${id}" /></td>
											</s:else>
											<td style="width: 70px"><span for="${id}" class="vulnControl updCF"><i
													class="fa fa-save"></i></span><span for="${id}"
												class="vulnControl vulnControl-delete delCF"><i
													class="fa fa-trash"></i></span></td>
										</tr>
									</s:iterator>

								</bs:datatable>
							</bs:mco>
						</bs:row>
					</bs:box>
				</s:if>
				<s:if test="acadmin == true">
					<bs:box type="primary" title="Configuration Settings">
						<bs:row>
							<bs:mco colsize="6">
								<div class="checkbox">
									<label> <input type="checkbox" id="prEnabled"
										${prChecked }> Enable Peer Review Queue
									</label>
								</div>
							</bs:mco>
							<bs:mco colsize="6">
							<s:if test="prChecked == 'checked'">
								<div class="checkbox">
									<label> <input type="checkbox" id="prSelfReview"
										${ selfPeerReview }> Allow Self Peer Review
									</label>
								</div>
							</s:if>
							</bs:mco>
							<bs:mco colsize="6">
								<div class="checkbox">
									<label> <input type="checkbox" id="randEnabled"
										${randChecked }> Allow Random AppId
									</label>
								</div>
							</bs:mco>
							<bs:inputgroup name="Bold Title:" colsize="12" id="title1"
								placeholder="Fuse"><s:property value="title[0]"/></bs:inputgroup>
							<bs:inputgroup name="Secondary Title:" colsize="12" id="title2"
								placeholder="FACTION"><s:property value="title[1]"/></bs:inputgroup>
							<br>
							<bs:button color="info" size="md" colsize="3"
								text="<i class='fa fa-save'></i> Save Titles" id="updateTitles">
							</bs:button>
						</bs:row>
					</bs:box>
				</s:if>

				<s:if test="acadmin == true">

					<bs:box type="primary" title="Assessment Status">
						<bs:inputgroup name="Status Name:" colsize="6" id="asmtStatus"></bs:inputgroup>
						<bs:button color="primary" size="md" colsize="3"
							text="<i class='fa fa-plus'></i> Add" id="addstatus"
							addlabel="true"></bs:button>
						<br>
						<br>
						<br>
						<br>
						<bs:mco colsize="12">
							<bs:datatable columns="Status,Default,Delete" classname="" id="">
								<s:iterator value="EMS.status" var="stat">
									<tr>
										<td><s:property value="stat" /></td>
										<td width="50px" status="<s:property value="stat"/>"><s:if
												test="#stat == EMS.defaultStatus">
												<input type="checkbox" class="statuscheck" checked>
											</s:if> <s:else>
												<input type="checkbox" class="statuscheck">
											</s:else></td>
										<td width="50px"><span
											class="vulnControl vulnControl-delete"
											onClick="deleteStatus('<s:property value="stat"/>')"><i
												class="fa fa-trash"></i></span></td>
									</tr>
								</s:iterator>
							</bs:datatable>
						</bs:mco>
					</bs:box>


				</s:if>

			</bs:mco>
			<s:if test="acadmin == true">
				<bs:mco colsize="6">
					<bs:box type="info" title="Email Settings">
						<bs:row>
							<bs:inputgroup name="Email Server:" colsize="12" id="emailServer"
								placeholder="Email Server Address"><s:property value="EMS.server" /></bs:inputgroup>
							<bs:inputgroup name="Email Port:" colsize="12" id="emailPort"
								placeholder="Email Server Port"><s:property value="EMS.port" /></bs:inputgroup>
							<bs:inputgroup name="Email Protocol:" colsize="12"
								id="emailProto" placeholder="smtp,pop,imap"><s:property value="EMS.type" /></bs:inputgroup>
							<div class="col-md-12">
								<div class="checkbox">
									<label> <input type="checkbox" id="isAuth"
										${authChecked}> Use Authentication
									</label> <label> <input type="checkbox" id="isTLS"
										${tlsChecked}> Use TLS
									</label> <label> <input type="checkbox" id="isSSL"
										${sslChecked}> Use SSL
									</label>
								</div>
							</div>
							<bs:inputgroup
								name="Account Username (Sender Account if UnAuthenticated):"
								colsize="12" id="emailName" placeholder="Username"><s:property value="EMS.uname" /></bs:inputgroup>
							<bs:inputgroup name="Email from Address:" colsize="12"
								id="fromAddress" placeholder="From Address"><s:property value="EMS.fromaddress" /></bs:inputgroup>
							<bs:inputgroup name="Account Password:" colsize="12"
								id="emailPass" placeholder="Password" password="true">*****</bs:inputgroup>
							<bs:inputgroup name="Email Subject Prefix: " colsize="12"
								id="emailPrefix" placeholder="Faction : "><s:property value="EMS.prefix" /></bs:inputgroup>
							<bs:mco colsize="12">
								<div class="form-group">
									<label>Email Signature:</label>
									<textarea rows="12" cols="100" class="form-control pull-right"
										name="emailSignature" id="emailSignature"><s:property
											value="EMS.signature" /></textarea>
								</div>
							</bs:mco>
							<br>
							<br>

						</bs:row>
						<br>
						<bs:row>
							<bs:button color="info" size="md" colsize="4"
								text="<i class='fa fa-save'></i> Save" id="saveEmail">
							</bs:button>
							<bs:button color="warning" size="md" colsize="4"
								text="<i class='fa fa-envelope'></i> Save and Test Email"
								id="testEmail">
							</bs:button>
						</bs:row>

					</bs:box>
				</bs:mco>

			</s:if>

		</bs:row>




		<jsp:include page="../footer.jsp" />
		<script src="../dist/js/options.js"></script>


		</body>
		</html>