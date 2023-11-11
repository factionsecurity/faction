<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="../header.jsp" />
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<link rel="stylesheet" href="../plugins/iCheck/all.css">

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      <i class="glyphicon glyphicon-wrench"></i> Licensing
      <small></small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
  <bs:row>
	  <bs:mco colsize="6">
	  	<bs:box type="success" title="License Info">
	  		<bs:row>
	  			<bs:mco colsize="12">
	  				Product is licensed to : <b>${company } on ${installed }</b> <br>
	  				Users allowed by license: <b>${userCount }</b><br>
	  				Total Active  Users in Faction: <b>${totalUserCount }</b><br>
	  				License Expires in ${daysLeft } days<br>
	  				
	  			</bs:mco>
	  			<bs:mco colsize="12">
	  				<textarea id="license" style="width:100%"></textarea><br>
	  				<button class="btn btn-primary" onClick="installLicense()"><i class="fa fa-upload"></i> Update License</button>
	  			</bs:mco>
	  		</bs:row>
	  	</bs:box>
	  </bs:mco>

	 </bs:row>
	
  



		<jsp:include page="../footer.jsp" />
 
  
    <script>
    function installLicense(){
    	data = "license=" + encodeURIComponent($("#license").val())
    	data+="&_token=" + _token;
    	$.post("InstallLicense",data).done(function(resp){
    		alertRedirect(resp);
    	});
    }
    
    </script>

  </body>
</html>