<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

	<div class="col-xs-3">
		<div class="box box-danger">
			<div class="box-header">
				<h3 class="box-title">
					<i class="fa fa-bug"></i> Assessment Findings
				</h3>
				<s:if
					test="!(assessment.InPr || assessment.prComplete || assessment.finalized)">
					<span id="deleteMulti" class="fa fa-trash circle pull-right"
						style="background: #192338" title="Delete Multiple Vulns"></span>
				</s:if>
			</div>
			<!-- /.box-header -->
			<div class="box-body">
				<div class="moveDown"></div>
				<table id="vulntable"
					class="table table-striped table-hover dataTable">
					<thead class="theader">
						<tr>
							<th></th>
							<th>Finding</th>
						</tr>
					</thead>
					<tbody>
						<s:iterator value="vulns">
							<tr data-vulnid="${id}" data-opened="<s:date name="opened"  format="MM/dd/yyy"/>" data-severity="${overall}", class="<s:if test="vuln.id == id">selected</s:if>">
								<td class="sev${overallStr}"><input type="checkbox"
									id="ckl<s:property value="id"/>" <s:if test="vuln.id == id">checked</s:if>/></td>
								<s:if test="assessment.type.cvss31 || assessment.type.cvss40">
									<td data-sort="${cvssScore}">
								</s:if>
								<s:else>
									<td data-sort="${overall}">
								</s:else>
								<span class="vulnName"><s:property value="name" /></span>
								<br>
								<span class="category"> <s:property value="category.name" /></span>
								<BR>
								<span class="tracking"> <s:property value="tracking" /></span>
								<BR>
								<span class="severity"><s:property value="overallStr" /></span>
								<BR>
								Opened: <span><s:date name="opened"  format="MM/dd/yyy"/></span>  
								<s:if test="closed">Closed: <span><s:date name="closed" format="MM/dd/yyyy"/></s:if></span>
								</td>
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
