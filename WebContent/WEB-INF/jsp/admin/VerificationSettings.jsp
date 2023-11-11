<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

			<!-- left column -->
			<div class="col-md-12">
				<!-- general form elements -->
				<div class="box box-primary">
					<div class="box-header with-border">
						<h3 class="box-title">Verification Settings</h3>
					</div>
					<!-- /.box-header -->
					<div class="box-body">
					   <b>These settings control how assessors close verifications.</b>
					   <div>
						  <input type="radio" name="verOption" ${verOption == 0? 'checked':'' } value="0"> When the Assessor closes the verification it is sent back to a a user in the Remediation Role to Manage.<br>
						  <br/>
						  <input type="radio" name="verOption" ${verOption == 1? 'checked':'' } value="1"> When Assessors close the verification it is reported closed in the development environment but the vulnerability is not reported closed.<br>
						  <br/>
						  <input type="radio" name="verOption" ${verOption == 2? 'checked':'' } value="2"> When Assessors close the verification it reports the vulnerability as being closed in the system and will no longer be tracked. <br>
						  <br/>
						  <input type="radio" name="verOption" ${verOption == 3? 'checked':'' } value="3"> When an Assessor closes the Verification the API handles what happens next. 
						</div>
					</div>
				</div>
				<!-- /.box -->
			</div>
			
			
			
