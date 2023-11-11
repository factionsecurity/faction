<%@page import="org.apache.struts2.components.Include"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
{ "lineData" :{
	"labels": ["start", <s:property value="dates" escapeHtml="false"/>],
	"datasets": [
		{
	    "label": "Critical",
	    "data": [0,<s:property value="crits"/>],
	    "fill": false,
	    "borderColor" : "#d9534f",
	    "backgroundColor": "#d9534f",
	    "lineTension": 0.1,
	    "pointBorderWidth": 1,
        "pointHoverRadius": 5,
	    "borderWidth": 3,
	    "pointHitRadius": 10,
	    "pointRadius": 1
    	 }
    	 ,
    	 {
	    "label": "High",
	    "data": [0,<s:property value="highs"/>],
	    "fill": false,
	    "borderColor" : "#f0ad4e",
	    "backgroundColor": "#f0ad4e",
	    "lineTension": 0.1,
	    "pointBorderWidth": 1,
        "pointHoverRadius": 5,
	    "borderWidth": 3,
	    "pointHitRadius": 10,
	    "pointRadius": 1
    	 } 
    	 ,
    	 {
	    "label": "Medium",
	    "data": [0,<s:property value="meds"/>],
	    "fill": false,
	    "borderColor" : "#337ab7",
	    "backgroundColor": "#337ab7",
	    "lineTension": 0.1,
	    "pointBorderWidth": 1,
        "pointHoverRadius": 5,
	    "borderWidth": 3,
	    "pointHitRadius": 10,
	    "pointRadius": 1
    	 } 
    	 ,
    	 {
	    "label": "Low",
	    "data": [0,<s:property value="lows"/>],
	    "fill": false,
	    "borderColor" : "#5cb85c",
	    "backgroundColor": "#5cb85c",
	    "pointBorderWidth": 1,
        "pointHoverRadius": 5,
	    "lineTension": 0.1,
	    "borderWidth": 3,
	    "pointHitRadius": 10,
	    "pointRadius": 1
    	 } 
    	        
    ]
},
"assessments" : 
[	<s:iterator value="asmts" status="astat"><s:if test="#astat.index != 0">,</s:if>
			{"date" : "<s:date name="completed" format="MM/dd/yyyy" />",
			"vulns" : [
			<s:iterator value="vulns" status="stat" ><s:if test="#stat.index != 0">,</s:if>
		           {
		             "value": 1,
		             "color": "red",
		             "highlight": "red",
		             "label": "<s:property value="name"/>"
		           }
           </s:iterator>
           ]
       }
    </s:iterator>
]
}