<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
 <bs:row>
  <bs:mco colsize="6">
  <bs:box type="primary" title="Open Vulns">
    <div id="openLeg" class="ledgend"></div>
    <div class="plot">
  		<canvas id="openChart" style="height:300px"></canvas>
  	</div>
  </bs:box>
  </bs:mco>
  <bs:mco colsize="6">
  <bs:box type="primary" title="Top Vulns">
  	<div id="tvLedg" class="ledgend"></div>
    <div class="plot">
  		<canvas id="tvChart" style="height:300px"></canvas>
  	</div>
  </bs:box>
  </bs:mco>
  <bs:mco colsize="6">
  <bs:box type="primary" title="Top Assessors">
  	<div id="taLedg" class="ledgend"></div>
    <div class="plot">
  		<canvas id="taChart" style="height:300px"></canvas>
  	</div>
  </bs:box>
  </bs:mco>
  <s:if test="prEnabled">
  <bs:mco colsize="6">
  <bs:box type="primary" title="Top Peer Reviewers">
  <div id="tprLedg" class="ledgend"></div>
    <div class="plot">
  	<canvas id="tprChart" style="height:300px"></canvas>
  	</div>
  </bs:box>
  </bs:mco>
  </s:if>
  <bs:mco colsize="6">
  <bs:box type="primary" title="Past Due Items">
  <div id="allLedg" class="ledgend"></div>
    <div class="plot">
  	<canvas id="allChart" style="height:300px"></canvas>
  	</div>
  </bs:box>
  </bs:mco>
  </bs:row>