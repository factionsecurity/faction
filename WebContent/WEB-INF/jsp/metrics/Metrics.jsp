<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<%@ page import="java.util.Random" %>
<link rel="stylesheet" href="../plugins/jQueryUI/jquery-ui.css">

<jsp:include page="../header.jsp" />

<style>

.circle {
	border-radius: 50%;
	width: 30px;
	height: 30px; 
	padding: 8px;
	font-size: small;
	color: white;
}
.ledgend{
	float:right; 
	background:#F2F2F2; 
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
.childTable{
	width:100%;
	border: 1px solid #AEB6BF !important;
	padding:0px;
}
.form-control{
    background-color: #192338 !important;
}

</style>

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
  <!-- Content Header (Page header) -->
  <section class="content-header">
    <h1>
      Metrics
    </h1>
  </section>

  <!-- Main content -->
  <section class="content">
 
   <!-- START TABS -->
        
<div class="row">
<div class="col-md-12">
 <div class="nav-tabs-custom">
   <ul class="nav nav-tabs">
     <li class="active"><a href="#tab_1" data-toggle="tab">Application</a></li>
     <li id="health"><a href="#tab_2" data-toggle="tab" >Health</a></li>
   </ul>
   <div class="tab-content">
     <div class="tab-pane active" id="tab_1">
      <jsp:include page="lineCharts.jsp"></jsp:include>
     </div><!-- /.tab-pane -->
     <div class="tab-pane" id="tab_2">
       
     </div><!-- /.tab-pane -->
   </div><!-- /.tab-content -->
 </div><!-- nav-tabs-custom -->
</div><!-- /.col -->
</div>
                    
 
  
  
		<jsp:include page="../footer.jsp" />
	 
    <script src="../bootstrap/js/bootstrap.min.js"></script>
    <!-- DataTables -->
    <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="../plugins/fastclick/fastclick.min.js"></script>
    <!--<script src="../plugins/chartjs/Chart.min.js"></script>-->
    <!--<script src="../dist/js/jquery.autocomplete.min.js"></script>-->
    <script src="../plugins/jQueryUI/jquery-ui.min.js"></script>
    <script src="../plugins/Chart.js-2.1.6/dist/Chart.bundle.min.js"></script>
    <link href="../dist/css/jquery.autocomplete.css" media="all" rel="stylesheet" type="text/css" />
    <script>
    function childTable(d){
    	table = '<table class="childTable" >'
    			+"<thead><tr><th>Vulnerability</th><th>Severity</th><th>Tracking</th><th>Opened</th><th>Closed</th></tr><thead>"
    			+"<tbody>";
    	middle="";
    	$(d).each(function(a,b){
	        middle += '<tr>'+
	        '<td width="50%">'+b.name+'</td>'+
	        '<td>'+b.severity+'</td>'+
	        '<td>'+b.tracking+'</td>'+
	        '<td>'+b.start+'</td>'+
	        '<td>'+b.end+'</td>'+
	    	'</tr>';
	   
    	});
    	if(middle == "")
    		middle = "<tr><td colspan=5><center><b>No Vulnerabilities for this Assessment</b></td></tr>";
	table += middle + '</tbody></table>';
	return table;
    }
    
    <% 
    Random rand = new Random(1313465);

    String colors= "['#f56954', '#f39c12', '#00a65a', '#00c0ef', '#00a65a' , '#3c8dbc' ";
    for(int i = 10; i< 50; i++){
    	colors += ", '#" + Integer.toHexString(rand.nextInt(255)) + Integer.toHexString(rand.nextInt(255)) + Integer.toHexString(rand.nextInt(255)) +"'";
    }
    colors += "]";
    %>
    colors = <%=colors %>;
    //columns="Start,AppId,Name,Users,End,Report"
    var myChart;
    function showVulns(el) {
    	console.log("clicked")
        var tr = $(el).closest("tr");
    	console.log(tr);
    	if(!($(tr).hasClass('odd') || $(tr).hasClass('even')))
    		return
        var row = appTable.row( tr );
 		console.log(row);
        if ( row.child.isShown() ) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        }
        else {
            // Open this row
            row.child( childTable(row.data().vulns) ).show();
            
            tr.addClass('shown');
            changeColors();
            
        }
    }
    function showAssessment(el){
    	var tr = $(el).closest("tr");
    	var row = appTable.row( tr );
    	document.location = "SetAssessment?id=app"+ row.data().id;
    }
    $(function () {
    	appTable = $('#appDetails').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": true,
            "pageLength": 100,
            "order": [[ 0, "asc" ]],
            "columns":[
            { "data": "buttons"},
            { "data": "start" },
            { "data": "appid" },
            { "data": "name" },
            { "data": "cname" },
            { "data": "users" },
            { "data": "end" },
            { "data": "report" }
            ]
          
          });
    	// Add event listener for opening and closing details
	    //$('#appDetails tbody').on('click', 'tr', 
	   
    	
    	$("#health").click(function(){
    		document.location = "Metrics?action=health";
    	});
    	$("#userName").autocomplete({
    		source: function(request, response){

			  	username = request.term ;
			  	
			  	var data = "&username=" + username;
		        $.post("../services/metricSearch", data).done(function(resp){

		        	list=[]
		        	for(i=0; i < resp.length; i++){
						list[i]={label: resp[i].name , value : resp[i].id}
		        		}

	        		response(list );

		        });
		    },
		    select: function(e, ui){
		    	$( "#userName" ).val( ui.item.label);
		    	$( "#appId" ).val("");
		    	$( "#appName" ).val("");
		    	$( "#campName" ).val("");
		    	data="id=" + ui.item.value;
		    	data+="&action=getuser";
		    	$.post("Metrics", data).done(function(resp){
		    		$("#nodateMessage").hide();
		    		if(typeof myChart != 'undefined'){
		    			console.log("destroy chart");
		    			myChart.clear();
		    			myChart.destroy();
		    			
		    		}
					var ctx = $("#myChart");
		        	myChart = new Chart(ctx, {
		        	    type: 'line',
		        	    data: resp.lineData
		        	});
		        	
		        	appTable.rows.add(resp.vulns).draw();
		        	
		        	
		        
		    	});
		    	return false;
		    }
    	}).autocomplete( "instance" )._renderItem = function( ul, item ) {
    	      return $( "<li class='ui-menu-item'>" )
    	        .append( "<div class='ui-menu-item-wrapper'>" + item.label + "</div>")
    	        .appendTo( ul );
    	    };
    	    
    	
    	 $("#appId,#appName,#campName").each(function(i, el) {
    		 $(el).autocomplete({
			  minLength: 3,
			  cache: false,
			  source: function(request, response){
				    var appid="";
				    var appname="";
				    var campname="";
				    var username="";
				    console.log($(el).attr('id'));
				  	if($(el).attr("id") == "appId")
				  		appid = $(el).val();
				  	else if($(el).attr("id") == "appName")
				  		appname = $(el).val();
				  	else if($(el).attr("id") == "campName")
				  		campname = $(el).val();
				  	var data="appid=" + appid;
				  	data +="&appname=" + appname;
				  	data +="&campname=" + campname;
				  	data +="&username=" + username;
			        $.post("../services/metricSearch", data).done(function(resp){
			        	var list = [];
		        		for(i=0; i < resp.length; i++){
		        			strlabel = "";
		        			strvalue = ""
		        			if($(el).attr("id") == "appId"){
		        				strlabel = resp[i].appid + " " + resp[i].appname;
		        				strvalue = resp[i].appid;
		        			}else if($(el).attr("id") == "appName"){
						  		strlabel = resp[i].appid + " " + resp[i].appname;
						  		strvalue = resp[i].appname;
		        			}else if($(el).attr("id") == "campName"){
						  		strlabel = resp[i].campName;
						  		strvalue = resp[i].campName;
		        			}
							list[i]= { value : strvalue, 
									appid: resp[i].appid, 
									appname: resp[i].appname, 
									label : strlabel, 
									campid: resp[i].campid, 
									campname: resp[i].campName};
   		        		}
		        		
		        		console.log(list);
		        		response(list);

			        });
			    },
			    select: function(e, ui){
			    	appTable.clear().draw();
			    	console.log(e.item);
					$(e).val(ui.item.label);
			    	var appid = ui.item.appid;
			    	var appName = ui.item.appname;
			    	var campid = ui.item.campid;
			    	var campName = ui.item.campname;
			    	data="";
					console.log(campid);
			    	if(typeof campid != 'undefined'){
						
			    		$("#campName").val(campName);
			    		$("#appName").val("");
			    		$("#appId").val("");
			    		data="action=getcamp";
			    	}else{
			    	 	$("#appId").val(appid);
			    		$("#appName").val(appName);
			    		$("#campName").val("");
			    		campid="-1";
			    		data="action=getapp";
			    	}

			    	data+="&appId=" + $("#appId").val();
			    	data+="&appName=" + $("#appName").val();
			    	data+="&campId=" + campid;
			    	$.post("Metrics", data).done(function(resp){
			    		$("#nodateMessage").hide();
			    		if(typeof myChart != 'undefined'){
			    			console.log("destroy chart");
			    			myChart.clear();
			    			myChart.destroy();
			    			
			    		}
						var ctx = $("#myChart");
			        	myChart = new Chart(ctx, {
			        	    type: 'line',
			        	    data: resp.lineData
			        	});
			        	
			        	appTable.rows.add(resp.vulns).draw();
			        	
			        	
			        
			    	});
			    }
			    	
	    	  }).autocomplete( "instance" )._renderItem = function( ul, item ) {
       	      return $( "<li class='ui-menu-item'>" )
  	        .append( "<div class='ui-menu-item-wrapper'>" + item.label + "</div>")
  	        .appendTo( ul );
  	    };
    	 });
    	 
    	
    	 
    	 
    	
   
      });
    </script>
   
    <script>
    var pieOptions = {
            
            //Number - The percentage of the chart that we cut out of the middle
            cutoutPercentage: 50, // This is 0 for Pie charts
            //String - Animation easing effect
            animationEasing: "easeOutBounce",
            animation:{ 
            	animateRotate: true,
            	animateScale: false
            },
           
           
            
            
          };
    
    </script>
    
    <script>
    function changeColors(){
    	 
    	 
		 var colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
		 <% int count=9; %>
    	 <s:iterator value="levels" begin="0" end="9" step="1" status="stat">
    	 	<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
				$(".childTable td:contains('${risk}')").css("color", colors[<%=count-- %>]).css("font-weight", "bold");
			</s:if>
			
		</s:iterator>


    }
    </script>
  

  </body>
</html>