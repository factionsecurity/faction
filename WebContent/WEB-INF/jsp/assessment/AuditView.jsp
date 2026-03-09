<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>


<s:set var="auditActive" value="'true'" scope="request"/>
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
</style>



     <div class="tab-pane active">
     </div><!-- /.tab-pane -->
     <bs:datatable columns="Timestamp,Description,User" classname="" id="auditlog">
     <s:iterator value="logs">
     <tr><td>${timestamp }</td><td><s:property value="description"/></td><td><s:property value="user.fname"/> <s:property value="user.lname"/></td></tr>
     </s:iterator>
     </bs:datatable>
 
     </div><!-- /.tab-pane -->
   </div><!-- /.tab-content -->
 </div><!-- nav-tabs-custom -->
</div><!-- /.col -->
  
  
 
 

<jsp:include page="../footer.jsp" />
  

    
  
        <!-- DataTables -->
    <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="../plugins/fastclick/fastclick.min.js"></script>
   
    
    
    <script>
function updateColors(){
    var colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
    var boxCount = $("#infobar").find("div.row").find("[class^=col-sm]").length;
    var width = 100/boxCount;
    $("#infobar").find("div.row").find("[class^=col-sm]").css("width", width+"%").css("min-width","100px");
    var boxes = $("#infobar").find("[class=small-box]");
    var colorCount=9;
    boxes.each((index,box) => {
            let risk = $(box).find("p")[0].innerText;
            $(`td:contains('${risk}')`).css("color", colors[colorCount]).css("font-weight", "bold");
            $(box).css("border-color",colors[colorCount]);
            $(box).css("color",colors[colorCount--]);
    });
}
    
   
    $(function(){
      updateColors()

      $("#auditlog").DataTable({
    	  "paging":   false,
      });
		
    });
    </script>
 
  </body>
</html>