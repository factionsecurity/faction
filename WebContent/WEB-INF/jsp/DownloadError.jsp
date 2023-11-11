<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="header.jsp" />
<style>
.page {
cursor: pointer;
}
.page:hover{
	 font-weight: bold;
}
.css{
width:100%;
height: 700px;
}
</style>
<link rel="stylesheet" href="../plugins/iCheck/all.css">
<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="fa fa-ban"></i> Download Error
      
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  <bs:box type="success" title="">
<bs:row>
	<bs:mco colsize="12">
	<bs:row>
			<bs:mco colsize="2"></bs:mco>
			<bs:mco colsize="8">
				<div class="alert alert-danger alert-dismissible">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                <h4><i class="icon fa fa-ban"></i> Alert!</h4>
                The Report you're attempting to download is not available. This could mean that it is taking a long time to process and will show up in your dashboard once complete. 
                <br><br>
                <a href="Dashboard"> Click here in a few seconds to see if the report generated correctly.</a>
                <br>
                <br>
                The other reason for this error could be that the administrator has not uploaded a template report for this assessment Team,Type, or Retest. If the report does not 
             	become available from the link above then contact your administrator. 
              </div>
			</bs:mco>
	</bs:row>
	</bs:mco>
	</bs:row>
	</bs:box>
		
		
    <jsp:include page="footer.jsp" />
 
 
  </body>
</html>