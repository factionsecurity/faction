<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{ "lineData" :{
	"labels": ["start" <s:property value="dates" escapeHtml="false"/>],
	"datasets": [<s:set var="count" value="0"></s:set><s:iterator value="levels" status="stat">
		<s:if test="risk != null && risk != 'Unassigned' && risk != ''">${count==0?"":","}{
	    "label": "${risk}",
	    "data": [0 ${data.get(riskId)}],
	    "fill": false,
	    "borderColor" : "${colors.get(count)}",
	    "backgroundColor": "${colors.get(count)}",
	    "lineTension": 0.1,
	    "pointBorderWidth": 1,
        "pointHoverRadius": 5,
	    "borderWidth": 3,
	    "pointHitRadius": 10,
	    "pointRadius": 1,
	    "junk" : ${count=count+1}
    	 }</s:if> 
	</s:iterator>	        
    ]
},
"assessments" : 
[	<s:iterator value="asmts" status="astat"><s:if test="#astat.index != 0">,</s:if>
			{"date" : "<s:date name="completed" format="MM/dd/yyyy" />",
			"vulns" : [
			<s:iterator value="vulns" status="stat" >
		           {
		             "value": 1,
		             "color": "red",
		             "highlight": "red",
		             "label": "<s:property value="name"/>"
		           }<s:if test="!#stat.last">,</s:if>
           </s:iterator>
           ]
       }
    </s:iterator>
],
"vulns" : [
	<s:iterator value="asmts" status="astat" var="a">
	<s:if test="#astat.index != 0">,</s:if>{ "start" : "<s:date name="start" format="yyyy-MM-dd"/>", 
		"end" : "<s:date name="completed" format="yyyy-MM-dd" />",
		"appid" : "${a.appId}", 
		"name" : "<s:property value="name"/>",
		"cname" : "<s:property value="campaign.name"/>",
		"users" : "<s:iterator value="assessor" status="ustat"><s:if test="#ustat.index != 0">, </s:if><s:property value="fname"/> <s:property value="lname"/></s:iterator>", 
		"report" : "<s:if test="#a.finalReport != null"><a href='DownloadReport?guid=${a.finalReport.filename}'>Report</a></s:if>",
		"buttons": "<i style='background:#00a65a' class='fa fa-bug circle' onclick='showVulns(this)'></i>&nbsp;<i style='background:#00c0ef' class='glyphicon glyphicon-th-list circle' onclick='showAssessment(this)'></i>",
		"id": "${a.id}",
		"vulns" : [
		<s:iterator value="vulns" var="v" status="vstat">${v.updateRiskLevels()}
		<s:if test="#vstat.index != 0">,</s:if>{
		"name": "<s:property value="name"/>", 
		"severity" : "<s:property value="overallStr"/>",
		"tracking" : "<s:property value="tracking"/>", 
		"start" : "<s:date name="opened" format="yyyy-MM-dd"/>", 
		"end" : "<s:date name="closed" format="yyyy-MM-dd"/>"
		} 
		</s:iterator>
		]}
	</s:iterator>
	]
}