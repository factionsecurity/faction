<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:hidden value="%{id}" name="appid"></s:hidden>
<s:hidden value="%{assessment.id}" name="asmtid"></s:hidden>
<section class="content-header" style="text-align: center; margin-top:-35px;">
<h1>
 <i class="glyphicon glyphicon-th-list"></i>&nbsp;&nbsp;&nbsp;Assessment
  <b><s:property value="assessment.appId"/> - <s:property value="assessment.name"/></b>
  <small>
  <s:if test="!assessment.Finalized">
  	<button class="btn btn-default" style="z-index:999999" onClick="location.href='EditAssessment?action=get&aid=${assessment.id}&back=assessment'">Edit</button>
  </s:if>
  <s:if test="assessment.InPr"> <span class="text-warning fa fa-eye"></span><b class="text-warning"> (in Peer Review)</b></s:if>
  <s:if test="assessment.prComplete"> <span class="text-success fa fa-eye"></span><b class="text-success"> (Peer Review Completed)</b></s:if>
  <s:if test="notowner"> <span class="text-warning fa fa-warning"></span><b  class="text-warning"> (Manager View)</b></s:if>
  <s:if test="assessment.Finalized"> <span class="text-primary fa fa-check"></span><b  class="text-primary"> Assessment Completed</b></s:if>
  </small>
</section>