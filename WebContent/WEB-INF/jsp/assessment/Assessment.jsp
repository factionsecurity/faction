<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link href="../fileupload/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />
<link href="../dist/css/jquery.autocomplete.css" media="all" rel="stylesheet" type="text/css" />
<link href="../dist/css/throbber.css" media="all" rel="stylesheet" type="text/css" />
<link href="../plugins/jquery-confirm/css/jquery-confirm.css" media="all" rel="stylesheet" type="text/css" />
<style>
    		
tr:hover{
	//font-weight: bold;
}
.tempSearch{
width:100%;
}

.text-warning{
color:#f39c12;
}
.text-success{
color:#00a65a

}
.disabled{
	background: lightgray;
}
.lockUser{
color: white;
}
</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
    
     <i class="glyphicon glyphicon-th-list"></i> Assessment
      <b><s:property value="assessment.appId"/> - <s:property value="assessment.name"/></b>
      <small>
      <s:if test="assessment.InPr"> <span class="text-warning fa fa-eye"></span><b class="text-warning"> (in Peer Review)</b></s:if>
      <s:if test="assessment.prComplete"> <span class="text-success fa fa-eye"></span><b class="text-success"> (Peer Review Completed)</b></s:if>
      <s:if test="notowner"> <span class="text-warning fa fa-warning"></span><b  class="text-warning"> (Manager View)</b></s:if>
      </small>
    </h1>
  </section>
  <s:hidden value="%{id}" name="appid"></s:hidden>

  <!-- Main content -->
  <section class="content">
  <div id="infobar" style="width:90%; margin-right:auto;margin-left:auto">
  <bs:row>

  <s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
    	 	<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
    	 	  <div class="col-sm-1">
				<div class="small-box" style="border-width:1px; border-style:solid;">
		            <div class="inner">
		              <h3>${counts.get(riskId)}</h3>
		              <p>${risk }</p>
		            </div>
		            <div class="icon">
		              <s:if test="risk.toLowerCase().startsWith('info') || risk.toLowerCase().startsWith('rec')">
		              	<i class="fa fa-info"></i>
		              </s:if>
		              <s:else>
		              	<i class="fa fa-bug"></i>
		              </s:else>
		            </div>
		             
		          </div>
		         </div>
			</s:if>
</s:iterator>
</bs:row>

</div> 	

  
   <!-- START TABS -->
        
<div class="row">
<div class="col-md-12">
 <div class="nav-tabs-custom">
   <ul class="nav nav-tabs" role="tablist">
     <li class="nav-item active" role="presentation"><a href="#tab_1" data-toggle="tab" role="tab">Overview</a></li>
     <li class="nav-item"><a href="VulnerabilityView">Vulnerabilities</a></li>
     <li class="nav-item"><a href="CheckList">Checklists</a></li>
     <li class="nav-item"><a href="#tab_3" data-toggle="tab" >Finalize</a></li>
     <li class="nav-item"><a href="#tab_4" data-toggle="tab">History</a></li>
     <li class="nav-item"><a href="AuditLog" >Audit</a></li>
   </ul>
   <div class="tab-content">
     <div class="tab-pane active" id="tab_1">
       <jsp:include page="AssessmentTextEditors.jsp" />
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="tab_2">
       
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="tab_3">
       <jsp:include page="Finalize.jsp" />
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="tab_4">
       <jsp:include page="history.jsp" />
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="tab_5">
     </div><!-- /.tab-pane -->
   </div><!-- /.tab-content -->
 </div><!-- nav-tabs-custom -->
</div><!-- /.col -->
</div>
<iframe id="dlFrame" style="display:none;"></iframe>
<s:iterator value="files">
  <s:hidden name="fileIds" value="%{uuid}" fileName="%{name}" contentType="%{contentType}" entityId="%{entityId}" fileExtType="%{fileExtType}"></s:hidden>
</s:iterator>

<jsp:include page="../footer.jsp" />
<script>
let id = "${id}";
</script>
<script src="../dist/js/overview.js"></script>

</body>
</html>