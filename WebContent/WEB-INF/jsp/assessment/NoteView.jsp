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
<style>
#notes {
background-color: white;
}
</style>
<div class="row">

	<!-- Notes Analysis Section -->
	<div class="col-md-2">
		<div class="box box-warning">
			<div class="box-header">
				<h3 class="box-title">
					<i class="glyphicon glyphicon-edit"></i> Notebook <small>Click to open</small>
				</h3>
				<small></small>
				<div class="box-tools pull-right"></div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<div class="form-group">
					<select id="notebook" multiple="false"
						class="form-control templates">
						<s:iterator value="assessment.notebook" status="stat">
							<option value="<s:property value='id'/>" title="<s:property value='createdBy.fname'/> <s:property value='createdBy.lname' />"
								class='globalNote' <s:if test="#stat.first">selected</s:if>>
							<s:property value='name'/>
							</option>
						</s:iterator>
					</select>
				</div>
				<s:if test="!(hideit)">
					<div class="row">
						<div class="col-md-11" style="padding-top: 8px; margin-left:10px">
							<span id="createNote"
								class="vulnControl vulnControl-add createNote" for="notebook"
								title='Create a New Note'> <i class="fa fa-add"></i>
							</span> 
							</span> <span id="deleteNote" class="vulnControl vulnControl-delete deleteNote"
								title='Delete Note' for="notebook"> <i
								class="fa fa-trash"></i>
							</span>
						</div>
					</div>
				</s:if>
			</div>
		</div>
	</div>
	<div class="col-md-10">
		<div class="box box-success">
			<div class="box-header">
				<div class="form-horizontal">
					<div class="form-group">
						<div class="col-sm-4">
							<br>
							<input type="text"  class="form-control" id="noteName"
								value="<s:property value="assessment.notebook[0].name"/>">
						</div>
						<div class="col-sm-4">
							<span id="notes_header" class="edited"></span> 
						</div>
						<div class="col-sm-4">
							<table class="userTable">
							<tr><td><b>Created: </b></td><td id="createdBy">
								<s:property value="assessment.notebook[0].createdBy.fname"/>
								<s:property value="assessment.notebook[0].createdBy.lname"/></td><td id="createdAt">
								<s:date name="assessment.notebook[0].created" format="MM/dd/yyyy hh:mm:ss"/>
								</td></tr>
							<tr><td><b>Updated: </b></td><td id="updatedBy">
								<s:property value="assessment.notebook[0].updatedBy.fname"/>
								<s:property value="assessment.notebook[0].updatedBy.lname"/></td><td id="updatedAt">
								<s:date name="assessment.notebook[0].updated" format="MM/dd/yyyy hh:mm:ss"/>
							</td></tr>
							</table>
						</div>
					</div>
				</div>
			</div>
			<!-- /.box-header -->
			<div class="box-body pad">
				<form>
					<div name="editor3" toolbar="Full" id="notes" readonly="${hideit}">
						<s:property value="assessment.notebook[0].note"/>
					</div>
				</form>
			</div>
		</div>
		<!-- /.box -->
	</div>

</div>