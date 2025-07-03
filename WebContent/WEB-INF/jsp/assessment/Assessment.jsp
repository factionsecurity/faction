<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<s:set var="summaryActive" value="'true'" scope="request"/>
<jsp:include page="Top.jsp"/>
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
		opacity: 0.2;
		pointer-events: none;
	}
	.lockUser{
	color: white;
	}
	.userTemplate:after {
		content: '\f007';
		font-family: FontAwesome;
		font-style: normal;
		font-weight: normal;
		text-decoration: inherit;
		margin-left: 10px;
	}

	.globalTemplate:after {
		content: '\f0ac';
		font-family: FontAwesome;
		font-style: normal;
		font-weight: normal;
		text-decoration: inherit;
		margin-left: 10px;
	}
	</style>




     <div class="tab-pane active" id="Summary">
       <jsp:include page="AssessmentTextEditors.jsp" />
     </div><!-- /.tab-pane -->
     <jsp:include page="AssessmentExtendedContent.jsp"/>
     <div class="tab-pane" id="Finalize">
       <jsp:include page="Finalize.jsp" />
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="History">
       <jsp:include page="history.jsp" />
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