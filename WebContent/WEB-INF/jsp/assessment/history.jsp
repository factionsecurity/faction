<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<bs:row>
	<bs:mco colsize="12">
		<bs:box type="primary" title="Assessment History">
			<bs:row>
				<bs:mco colsize="12">
					<bs:datatable columns="Opened, Vuln, Severity, Assessor, Report" classname="primary" id="history">
						<s:iterator value="history">
							<tr><td>
									<s:date name="opened" format="MM/dd/yyyy"/>
								</td>
								<td>
									<s:property value="vuln"/>
								</td>
								<td>
									<s:property value="severity"/>
								</td>
								<td>
									<s:property value="assessor"/>
								</td>
								<td>
									<a href="../service/Report.pdf?guid=<s:property value="report"/>" target="_blank" >Download Report</a>
								</td>
							</tr>

						</s:iterator>
					</bs:datatable>
				</bs:mco>
			</bs:row>
		</bs:box>
	</bs:mco>
</bs:row>
