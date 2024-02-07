<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="../header.jsp" />
<style>
.page {
cursor: pointer;
}
.page:hover{
	 font-weight: bold;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-book"></i> Report Designer
      <small><a href="https://docs.factionsecurity.com/Custom%20Security%20Report%20Templates/" target="_blank">Listing of Report Designer Tags</a></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  	<jsp:include page="cmsEditors.jsp" />
  
  <jsp:include page="../footer.jsp" />
	<script src="../dist/js/cms.js" charset="utf-8"></script>
 

  </body>
</html>