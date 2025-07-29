<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:hidden value="%{id}" name="appid"></s:hidden>
<s:hidden value="%{assessment.id}" name="asmtid"></s:hidden>
<section class="content-header fade-in">
<h1>
 <i class="fas fa-clipboard-list"></i>Assessment
  <strong><s:property value="assessment.appId"/> - <s:property value="assessment.name"/></strong>
  <small>
  <s:if test="!assessment.Finalized">
  	<button class="btn btn-primary" onClick="location.href='EditAssessment?action=get&aid=${assessment.id}&back=assessment'">
  		<i class="fas fa-edit"></i> Edit Assessment
  	</button>
  </s:if>
  <s:if test="assessment.InPr">
  	<span class="text-warning"><i class="fas fa-eye"></i> <strong>In Peer Review</strong></span>
  </s:if>
  <s:if test="assessment.prComplete">
  	<span class="text-success"><i class="fas fa-check-circle"></i> <strong>Peer Review Completed</strong></span>
  </s:if>
  <s:if test="notowner">
  	<span class="text-warning"><i class="fas fa-user-shield"></i> <strong>Manager View</strong></span>
  </s:if>
  <s:if test="assessment.Finalized">
  	<span class="text-primary"><i class="fas fa-flag-checkered"></i> <strong>Assessment Completed</strong></span>
  </s:if>
  </small>
</h1>
</section>