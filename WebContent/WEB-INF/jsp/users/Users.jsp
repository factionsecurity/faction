<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link href="../dist/css/jquery.autocomplete.css" media="all"
	rel="stylesheet" type="text/css" />
<style>
.disabled {
	opacity: 0.3;
}
</style>
<link rel="stylesheet" href="../plugins/iCheck/all.css">

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			<i class="glyphicon glyphicon-user"></i> User Admin <small>
				User Limit: ${users.size()} of ${userLimit}</small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">


		<div class="row">
			<div class="col-xs-12">

				<div class="box box-primary">
					<div class="box-body">

						<div class="row">
							<div class="col-xs-6">
							<div class="col-sm-3" style="margin-bottom: -30px; z-index: 1">
								<button class="btn btn-block btn-primary btn-sm" id="addUser">Add
									User</button>
							</div>
							</div>
						</div>
						<table id="userTable"
							class="table table-striped table-hover dataTable">
							<thead class="theader">
								<tr>
									<th>UserName</th>
									<th>Name</th>
									<th>email</th>
									<th>Team</th>
									<th>Authentication</th>
									<th>Last Login</th>
									<th>Locked?</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
								<s:iterator value="users">
									<tr>
										<td><s:property value="username" /></td>
										<td><s:property value="fname" />&nbsp;<s:property
												value="lname" />&nbsp;</td>
										<td><s:property value="email" /></td>
										<td><s:property value="team.teamName" /></td>
										<td><s:property value="authMethod" /></td>
										<td><s:date name="lastLogin" format="yyyy-MM-dd hh:mm:ss" /></td>
										<td><s:if test="failedAuth > 5">
												<span class="fa fa-lock text-danger"
													style="font-size: x-large;" onClick="unlock(${id})"></span>
											</s:if> <s:else>
												<span class="fa fa-unlock text-success"
													style="font-size: x-large"></span>
											</s:else></td>
										<td>
											<span class="vulnControl editUser"
												onClick="edit(${id})">
												<i class="fa fa-edit"></i>
											</span> <s:if test="inActive == false">
												<span class="vulnControl vulnControl-delete deleteUser"
													onClick="del(${id}, '<s:property value="fname"/> <s:property value="lname"/>')">
													<i class="fa fa-trash"></i>
												</span>
											</s:if> <s:else>
												<span class="vulnControl vulnControl-delete"
													onClick="edit(${id})">
													<i class="fa fa-ban"></i>
												</span>
											</s:else></td>
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
		<div class="row">
			<div class="col-sm-6">
				<div class="box box-primary">
					<div class="box-body">
						<div class="row">
							<div class="col-sm-3" style="margin-bottom: -30px; z-index: 1">
								<button id="createTeam" class="btn btn-block btn-primary btn-sm">Add
									Team</button>
							</div>
						</div>
						<table id="teamTable"
							class="table table-striped table-hover dataTable">
							<thead class="theader">
								<tr>
									<th>Team Name</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
								<s:iterator value="teams">
									<tr>
										<td><s:property value="teamName" /></td>
										<td>
											<span class="vulnControl"
												onClick="editTeam(this,${id})">
												<i class="fa fa-edit"></i>
											</span>
											<span class="vulnControl vulnControl-delete deleteTeam"
												onClick="delTeam(${id})">
												<i class="fa fa-trash"></i>
											</span>
										</td>
									</tr>
								</s:iterator>
							</tbody>
							<tfoot>
							</tfoot>

						</table>
					</div>
				</div>
			</div>
			<div class="col-sm-6">
				<div class="row">
					<div class="box box-primary">
						<div class="box-body">
							<div class="row">
								<bs:inputgroup name="Use It Or Loose It Days" colsize="4"
									id="uioli"><s:property value="uioli" /></bs:inputgroup>
								<bs:button color="primary" id="uioli-btn" text="Update"
									size="md" colsize="2" addlabel="true"></bs:button>
							</div>

						</div>
					</div>
				</div>
				<div class="row">
					<div class="box box-primary">
						<div class="box-header with-border">
							<h3 class="box-title">LDAP Configuration</h3>
						</div>
						<div class="box-body">
							<div class="row">
								<bs:inputgroup name="LDAP URL" colsize="4" id="ldapURL"
									placeholder="ldaps://corp.factionsecurity.com"><s:property value="ldapURL" /></bs:inputgroup>
								<bs:inputgroup name="LDAP Base DN" colsize="4" id="ldapBaseDn"
									placeholder="dc=factionsecurity,dc=com"><s:property value="ldapBaseDn" /></bs:inputgroup>
								<bs:inputgroup name="LDAP Bind DN" colsize="4" id="ldapUserName"
									placeholder="cn=corp.fusesoftsecurity.com,dc=factionsecurity,dc=com"><s:property value="ldapUserName" /></bs:inputgroup>
								<bs:inputgroup name="LDAP Password" colsize="4"
									id="ldapPassword" password="true"
									placeholder="LDAP Bind Password"></bs:inputgroup>
								<div class="col-sm-4">
									<div class="form-group">
										<label>Security</label> <select class="form-control select2"
											style="width: 100%;" id="ldapSecurity">
											<option value="None"
												<s:if test="ldapSecurity == \"None\"">selected="selected"</s:if>>None</option>
											<option value="SSL"
												<s:if test="ldapSecurity == \"SSL\"">selected="selected"</s:if>>SSL</option>
											<option value="TLS"
												<s:if test="ldapSecurity == \"TLS\"">selected="selected"</s:if>>TLS</option>
										</select>
									</div>
								</div>
								<div class="col-sm-4">
									<div class="form-group">
										<label>Disable Cert Verification</label>
										<div class="form-control" style="background: #0000">
											<input class="icheckbox_minimal-blue" type="checkbox"
												id="isInsecure" <s:if test="isInsecure">checked</s:if> />
										</div>
									</div>
								</div>
								<bs:inputgroup name="LDAP Object Class" colsize="4"
									id="ldapObjectClass" placeholder="e.g. user, person"><s:property value="ldapObjectClass" /></bs:inputgroup>

								<bs:button color="primary" id="ldapSave" text="Save" size="md"
									colsize="4" addlabel="true"></bs:button>
								<bs:button color="success" id="ldapTest" text="Test Connection"
									size="md" colsize="4" addlabel="true"></bs:button>
							</div>

						</div>
					</div>
				</div>
				<div class="row">
					<div class="box box-primary">
						<div class="box-header with-border">
							<h3 class="box-title">OAUTH2.0 Configuration</h3>
						</div>
						<div class="box-body">
							<div class="row">
								<bs:inputgroup name="Client Id" colsize="4" id="oauthClientId"
									placeholder=""><s:property value="oauthClientId" /></bs:inputgroup>
								<bs:inputgroup name="OAuth Discovery URI" colsize="4" id="oauthDiscoveryURI"
									placeholder="https://domain/.well-known/openid-configuration"><s:property value="oauthDiscoveryURI" /></bs:inputgroup>
								<bs:inputgroup name="Client Secret" colsize="4"
									id="oauthClientSecret" password="true"
									placeholder=""></bs:inputgroup>
								<bs:button color="primary" id="oauthSave" text="Save" size="md"
									colsize="4" addlabel="true"></bs:button>
							</div>

						</div>
					</div>
				</div>
				<div class="row">
					<div class="box box-primary">
						<div class="box-header with-border">
							<h3 class="box-title">SAML2 Configuration</h3>
						</div>
						<div class="box-body">
							<div class="row">
								<bs:inputgroup name="App Federation Metadata Url" colsize="12" id="saml2MetaUrl"
									placeholder=""><s:property value="saml2MetaUrl" /></bs:inputgroup>
								<bs:button color="primary" id="saml2Save" text="Save" size="md"
									colsize="4" addlabel="true"></bs:button>
							</div>

						</div>
					</div>
				</div>
			</div>



			<!-- USER Modal -->

			<div class="modal" id="userModal">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header bg-red">
							<button type="button" class="close" data-dismiss="modal"
								aria-label="Close">
								<span aria-hidden="true">&times;</span>
							</button>
							<h4 class="modal-title">
								<b><i class="glyphicon glyphicon-user"></i> User Edit Form</b>
							</h4>
						</div>
						<div class="modal-body bg-red">
							<form class="form-horizontal" autocomplete="off">
								<div class="box-body">
									<div class="form-group">
										<label>User: *</label> <input class="form-control" id="uname"
											placeholder="User" autocomplete="new-password">
									</div>
									<div class="form-group">
										<label>Password:</label> <input class="form-control" id="credential"
											type="password" placeholder="Sends Registration Email if Blank" autocomplete="new-password">
									</div>
									<div class="form-group">
										<label>First Name: *</label> <input type="search"
											class="form-control" id="fname" placeholder="First">
									</div>
									<div class="form-group">
										<label for="lname">Last: *</label> <input type="input"
											class="form-control" id="lname" placeholder="Last">
									</div>
									<div class="form-group">
										<label>Email: *</label> <input type="input"
											class="form-control" id="email" placeholder="test@test.com">
									</div>
									<div class="form-group">
										<label>Team Name: *</label> <select
											class="form-control select2" style="width: 100%;"
											id="teamName">
											<s:iterator value="teams">
												<option value="<s:property value="id"/>"><s:property
														value="teamName" /></option>
											</s:iterator>
											<option value="-1" selected>None</option>
										</select>
									</div>
									<div class="form-group">
										<label>Authentication Method: *</label> <select
											class="form-control select2" style="width: 100%;"
											id="authMethod">
											<option value="Native">Native</option>
											<option value="LDAP">LDAP</option>
											<option value="OAUTH2.0">OAUTH 2.0</option>
											<option value="SAML2">SAML2</option>
										</select>
									</div>
									<div class="form-group">
										<label for="api">API Key:</label> <input type="input"
											class="form-control" id="api" placeholder="API Key GUID"
											readonly>
									</div>
									<!-- Permissions -->
									<div class="form-group">
										<div class="row">
											<label class="col-sm-3 ${tier=='consultant'? "disabled" : ""}">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="true">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="adminck"
														${tier=='consultant'? "disabled" : ""}>
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> Admin
											</label> <label class="col-sm-3  ${tier=='consultant'? "disabled" : ""}">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="true">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="mgrck"
														${tier=='consultant'? "disabled" : ""}>
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> Manager
											</label> <label class="col-sm-3 ${tier=='consultant'? "disabled" : ""}">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="false">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="assck"
														${tier=='consultant'? "disabled" : ""}>
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> Assessor
											</label> <label class="col-sm-3 ${tier=='consultant'? "disabled" : ""}">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="false">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="engck"
														${tier=='consultant'? "disabled" : ""}>
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> Scheduling
											</label> <label class="col-sm-3 ${tier=='consultant'? "disabled" : ""}">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="false">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="remck"
														${tier=='consultant'? "disabled" : ""}>
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> Remediation
											</label> <label class="col-sm-3">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="false">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="apick">
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> API Key
											</label> <label class="col-sm-3 ${tier=='consultant'? "disabled" : ""}">
												<div class="icheckbox_minimal-blue"
													style="position: relative;" aria-checked="false"
													aria-disabled="false">
													<input type="checkbox" class="minimal"
														style="position: absolute; opacity: 0;" id="activeck"
														${tier=='consultant'? "disabled" : ""}>
													<ins class="iCheck-helper"
														style="position: absolute; top: 0%; left: 0%; display: block; width: 100%; height: 100%; margin: 0px; padding: 0px; background: rgb(255, 255, 255) none repeat scroll 0% 0%; border: 0px none; opacity: 0;"></ins>
												</div> inActive
											</label> <br> <label class="col-sm-12">Access Level * </label>
											<div class="col-sm-12 ${tier=='consultant'? "disabled" : ""}">
												<input type=radio name="accesslevel" value="2"
													${tier=='consultant'? "disabled" : ""}>&nbsp;Only
												Assessments Owned by User&nbsp;&nbsp;</input> <input type=radio
													name="accesslevel" value="1"
													${tier=='consultant'? "disabled" : ""}>&nbsp;Team
												Assessments&nbsp;&nbsp;</input> <input type=radio name="accesslevel"
													value="0" ${tier=='consultant'? "disabled" : ""}>&nbsp;All
												Assessments&nbsp;&nbsp;</input>
											</div>
										</div>
									</div>
								</div>

							</form>
						</div>
						<div class="modal-footer bg-red">
							<button type="button" class="btn btn-default pull-left"
								data-dismiss="modal">Close</button>
							<button type="button" class="btn btn-primary" id="saveUser">
								<i class="fa fa-save"></i> Save changes
							</button>
						</div>
					</div>
					<!-- /.modal-content -->
				</div>
				<!-- /.modal-dialog -->
			</div>
			<!-- /.modal -->


			<div class="modal" id="teamModal">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header bg-red">
							<button type="button" class="close" data-dismiss="modal"
								aria-label="Close">
								<span aria-hidden="true">&times;</span>
							</button>
							<h4 class="modal-title">
								<b><i class="glyphicon glyphicon-user"></i> Team Edit Form</b>
							</h4>
						</div>
						<div class="modal-body bg-red">
							<form class="form-horizontal">
								<div class="box-body">
									<div class="form-group">
										<label>Team Name:</label> <input type="input"
											class="form-control" id="team_name" placeholder="Team name">
									</div>
							</form>
						</div>
						<div class="modal-footer bg-red">
							<button type="button" class="btn btn-default pull-left"
								data-dismiss="modal">Close</button>
							<button type="button" class="btn btn-primary" id="addTeam">
								<i class="fa fa-save"></i> Save changes
							</button>
						</div>
					</div>
					<!-- /.modal-content -->
				</div>
				<!-- /.modal-dialog -->
			</div>
			<!-- /.modal -->


			<jsp:include page="../footer.jsp" />
			<script src="../dist/js/users.js" charset="utf-8"></script>


			</body>
			</html>