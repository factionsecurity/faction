<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<%@ page import="java.util.Random" %>

<jsp:include page="../header.jsp" />

<style>
.ledgend{
	float:right; 
	background:#192338; 
	height:300px;
	padding-top:10px;
	margin-top:-10px;
	margin-right:-10px;
	margin-bottom: -10px;
	width:40%;
	font-size: large;
}
.plot{
	float:left;
	width:60%;
}
.ldgkey{
    text-overflow: ellipsis;
	white-space: nowrap;
    overflow: hidden;
    width:80%;
    float:left;
}


</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      Metrics
      <small>Last 12 months</small>
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
 
   <!-- START TABS -->
        
<div class="row">
<div class="col-md-12">
 <div class="nav-tabs-custom">
   <ul class="nav nav-tabs">
     <li id="application"><a href="#tab_1" data-toggle="tab">Application</a></li>
     <li class="active"><a href="#tab_2" data-toggle="tab">Health</a></li>
   </ul>
   <div class="tab-content">
     <div class="tab-pane" id="tab_1">
     </div><!-- /.tab-pane -->
     <div class="tab-pane active" id="tab_2">
       <jsp:include page="circleCharts.jsp" />
     </div><!-- /.tab-pane -->
   </div><!-- /.tab-content -->
 </div><!-- nav-tabs-custom -->
</div><!-- /.col -->
</div>
                    
 
  
  
  <jsp:include page="../footer.jsp" />
 
    <!-- DataTables -->
    <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="../plugins/fastclick/fastclick.min.js"></script>
    <script src="../plugins/chartjs/Chart.min.js"></script>
   <script>
    <% 
    Random rand = new Random(1313465);

    String colors= "['#f56954', '#f39c12', '#00a65a', '#00c0ef', '#00a65a' , '#3c8dbc' ";
    for(int i = 10; i< 50; i++){
    	colors += ", '#" + Integer.toHexString(rand.nextInt(255)) + Integer.toHexString(rand.nextInt(255)) + Integer.toHexString(rand.nextInt(255)) +"'";
    }
    colors += "]";
    %>
    colors = <%=colors %>;
      console.log("Running");
    	$("#application").click(function(event){
        console.log("clicked it")
    		document.location = "Metrics";
    	});
    	 var pieChartCanvas = $("#openChart").get(0).getContext("2d");
         var pieChart = new Chart(pieChartCanvas);
         var openLedg="<ul class='fa-ul'>";
         
         var ovCount=0;
			<s:iterator value="levels"> 
					<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
					openLedg+="<li style='color:" + colors[ovCount++] +"'><div class='ldgkey'>${risk}</div>: ${data.get(riskId)}</li>";
					</s:if>
			</s:iterator>
			openLedg += "</ul>";
         /*<ul class='fa-ul'><li style='color:" + colors[0] +"'><div class='ldgkey'>Critical</div>: <s:property value="criticalN"/></li>";
         openLedg+="<li style='color:" + colors[1] +"'><div class='ldgkey'>High</div>: <s:property value="highN"/></li>";
         openLedg+="<li style='color:" + colors[2] +"'><div class='ldgkey'>Medium</div>: <s:property value="mediumN"/></li>";
         openLedg+="<li style='color:" + colors[3] +"'><div class='ldgkey'>Low</div>: <s:property value="lowN"/></li>";
         openLedg+="</ul>";*/
         $("#openLeg").html(openLedg);
         var vulnData = [
			<s:set var="count" value="0"></s:set>
			<s:iterator value="levels"> 
					<s:if test="risk != null && risk != 'Unassigned' && risk != ''">${count==0?"":","}
			        {
			          value: ${data.get(riskId)},
			          color: colors[${count}],
			          highlight: colors[${count}],
			          label: "${risk}",
			          junk : ${count = count+1}
			        }
			</s:if>
			</s:iterator>

         ];
         var pieOptions = {
           showTooltips: true,
           //Boolean - Whether we should show a stroke on each segment
           segmentShowStroke: true,
           //String - The colour of each segment stroke
           segmentStrokeColor: "#fff",
           //Number - The width of each segment stroke
           segmentStrokeWidth: 2,
           //Number - The percentage of the chart that we cut out of the middle
           percentageInnerCutout: 50, // This is 0 for Pie charts
           //Number - Amount of animation steps
           animationSteps: 100,
           //String - Animation easing effect
           animationEasing: "easeOutBounce",
           //Boolean - Whether we animate the rotation of the Doughnut
           animateRotate: true,
           //Boolean - Whether we animate scaling the Doughnut from the centre
           animateScale: false,
           //Boolean - whether to make the chart responsive to window resizing
           responsive: true,
           // Boolean - whether to maintain the starting aspect ratio or not when responsive, if set to false, will take up entire container
           maintainAspectRatio: true,
           
           
         };
         pieChart.Doughnut(vulnData, pieOptions);

         
         var taChartCanvas = $("#taChart").get(0).getContext("2d");
         var taChart = new Chart(taChartCanvas);
         var taLedge = "<ul class='fa-ul'>"
         var taData = [
           <% int count=0; %>
          <s:iterator value="topUsers">
           {
             value: <s:property value="value.findings"/>,
             color: colors[<%=count%>],
             highlight: colors[<%=count++%>],
             label: "<s:property value="key"/>"
           },
           </s:iterator>

           ];
         <% count=0; %>
         <s:iterator value="topUsersSorted">
         		taLedge+="<li style='color: " + colors[<%=count++%>] +"'><div class='ldgkey'><s:property value="value"/></div>: <s:property value="key"/></li>";
          </s:iterator>
         taLedge += "</ul>";
         $("#taLedg").html(taLedge);
         taChart.Doughnut(taData, pieOptions);

         var tvChartCanvas = $("#tvChart").get(0).getContext("2d");
         var tvChart = new Chart(tvChartCanvas);
         var tvLedge = "<ul class='fa-ul'>"
         var tvData = [
		<% count=0; %>
          <s:iterator value="topVulns">
           {
             value: <s:property value="value"/>,
             color: colors[<%=count%>],
             highlight: colors[<%=count++%>],
             label: "<s:property value="key"/>"
           },
           </s:iterator>

           ];
         <% count=0; %>
         <s:iterator value="topVulns">
         	tvLedge+="<li style='color: " + colors[<%=count++%>] +"'><div class='ldgkey'><s:property value="key"/></div>: <s:property value="value"/></li>";
          </s:iterator>
         tvLedge += "</ul>";
         $("#tvLedg").html(tvLedge);
         tvChart.Doughnut(tvData, pieOptions);

         
         <s:if test="prEnabled">
         var tprChartCanvas = $("#tprChart").get(0).getContext("2d");
         var tprChart = new Chart(tprChartCanvas);
         var tprLedge = "<ul class='fa-ul'>"
         var tprData = [
		  <% count=0; %>
          <s:iterator value="topReviews">
           {
             value: <s:property value="value"/>,
             color: colors[<%=count%>],
             highlight: colors[<%=count++%>],
             label: "<s:property value="key"/>"
           },
           </s:iterator>

           ];
         <% count=0; %>
         <s:iterator value="topReviews">
         	tprLedge+="<li style='color: " + colors[<%=count++%>] +"'><div class='ldgkey'><s:property value="key"/></div>: <s:property value="value"/></li>";
          </s:iterator>
          tprLedge += "</ul>";
         $("#tprLedg").html(tprLedge);
         tprChart.Doughnut(tprData, pieOptions);
         </s:if>



         var allChartCanvas = $("#allChart").get(0).getContext("2d");
         var allChart = new Chart(allChartCanvas);
         var allLedge = "<ul class='fa-ul'><li style='color:" + colors[0] +"'><div class='ldgkey'>Assessments</div>: <s:property value="pditems"/></li>";
         allLedge+="<li style='color:" + colors[1] +"'><div class='ldgkey'>Verifications</div>: <s:property value="pdvitems"/></li>";
         allLedge+="<li style='color:" + colors[2] +"'><div class='ldgkey'>Peer Review</div>: <s:property value="pritems"/></li>";
         allLedge+="</ul>";
         var allData = [
          
           {
             value: <s:property value="pditems"/>,
             color: colors[0],
             highlight: colors[0],
             label: "Past Due Assessments"
           },
           {
               value: <s:property value="pdvitems"/>,
               color: colors[1],
               highlight: colors[1],
               label: "Past Due Verifications"
             },
            {
                value: <s:property value="pritems"/>,
                color: colors[2],
                highlight: colors[2],
                label: "Items in Peer Review"
              },
           

           ];
         $("#allLedg").html(allLedge);
         allChart.Doughnut(allData, pieOptions);
         $(".ldgkey").prepend("<i class='fa-li fa fa-bolt'></i>");
   
    </script>
   
    
  

  </body>
</html>