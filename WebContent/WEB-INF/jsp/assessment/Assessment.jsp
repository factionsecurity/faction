<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<s:set var="summaryActive" value="'true'" scope="request"/>
<jsp:include page="Top.jsp"/>




     <div class="tab-pane active fade-in" id="Summary">
       <jsp:include page="AssessmentTextEditors.jsp" />
     </div><!-- /.tab-pane -->
     <jsp:include page="AssessmentExtendedContent.jsp"/>
     <div class="tab-pane slide-up" id="Finalize">
       <jsp:include page="Finalize.jsp" />
     </div><!-- /.tab-pane -->
     <div class="tab-pane slide-up" id="History">
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