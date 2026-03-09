<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<s:set var="checklistActive" value="'true'" scope="request"/>
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
.box{
	border-top: white !important;
}
.btn-border{
	padding: 30px;
}
</style>
     <div class="tab-pane active" id="CheckList">
     	<s:if test="!(hideit)">
     	<bs:row>
     	<bs:select name="Select a CheckList to Add to the Assessment" colsize="4" id="selectList">
     		<s:iterator value="lists" var="cl" status="stat">
   			 		<option value="${id }"><s:property value="name"/> Checklist</option>
     		</s:iterator>
     	</bs:select>
     	<bs:mco colsize="4">
     	<button class="btn btn-md btn-success" style="margin-top: 25px" id="addList"><span class='fa fa-plus'></span> Add List</button>
    	</bs:mco>
    	
    	</bs:row>
    	</s:if>
     	<bs:row>
     	<bs:mco colsize="12">
       	<div class="nav-tabs-custom">
   			<ul class="nav nav-tabs">
   			 <s:iterator value="assignedLists" var="cl" status="stat">
   			 	<s:if test="key == checklistid">
		     	 	<li class="active"><a href="#tabcl_${key}" data-toggle="tab"><s:property value="value"/> Checklist</a></li>
		     	 </s:if>
		     	 <s:else>
		     	 	<li><a href="CheckList.action?checklistid=${key }"><s:property value="value"/> Checklist</a></li>
		     	 </s:else>
     				
     		</s:iterator>
   			</ul>
   			<div class="tab-content">
	   			<s:iterator value="checklists" status="stat">
	   				<s:if test="#stat.count == 1">
			     	 	<div class="tab-pane active" id="tabcl_${key}">
			     	 </s:if>
			     	 <s:else>
			     	 	<div class="tab-pane" id="tabcl_${key}">
			     	 </s:else>
			     	 		<s:if test="!(hideit)">
								<div class="btn-border">
				     	 		<bs:row>
				     	 		<bs:mco colsize="2">
				     	 		<button class="btn btn-sm btn-block btn-primary" onclick="setNA(${key})"><i class="fa fa-dot-circle-o"></i> Set All to NA</button>
				     	 		</bs:mco>
				     	 		<bs:mco colsize="2">
				     	 		<button class="btn btn-sm btn-block  btn-danger" onclick="setFailed(${key})"><i class="fa fa-ban"></i> Set All to Failed</button>
				     	 		</bs:mco>
				     	 		<bs:mco colsize="2">
				     	 		<button class="btn btn-sm btn-block  btn-success" onclick="setPassed(${key})"><i class="fa fa-check"></i> Set All to Passed</button>
				     	 		</bs:mco>
				     	 		</bs:row>
								</div>
			     	 		</s:if>
			     	 		
			     	 	
							<bs:row>
									<bs:mco colsize="12">
										
										<bs:datatable columns="Question,Filter: <a class='filter' href=#pass>Pass</a> - <a class='filter' href=#fail>Fail</a> - <a class='filter' href=#incom>Incomplete</a> - <a class='filter' href=#na>NA</a> - <a class='filter' href=#>All</a>,Notes,Save,Status" classname="" id="questionTable">
											
											<s:iterator value="checklists">
											
											<s:iterator value="value" var="val">
												
												<tr>
												<td><s:property value="question"/></td>
												<td>
													
													<s:if test="answer.value == 0">
														<input type=radio name="rd${id }" value="0" checked="checked" >&nbsp;Incomplete&nbsp;&nbsp;</input> 
														<input type=radio name="rd${id }" value="1" <s:if test="hideit">disabled</s:if>>&nbsp;NA&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="3" <s:if test="hideit">disabled</s:if>>&nbsp;Pass&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="2" <s:if test="hideit">disabled</s:if>>&nbsp;Fail</input>
														
													</s:if>
													<s:elseif test="answer.value == 1">
														<input type=radio name="rd${id }" value="0" <s:if test="hideit">disabled</s:if>>&nbsp;Incomplete&nbsp;&nbsp;</input> 
														<input type=radio name="rd${id }" value="1" checked="checked" >&nbsp;NA&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="3" <s:if test="hideit">disabled</s:if>>&nbsp;Pass&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="2" <s:if test="hideit">disabled</s:if>>&nbsp;Fail</input>
														
													</s:elseif>
													<s:elseif test="answer.value == 2">
														<input type=radio name="rd${id }" value="0" <s:if test="hideit">disabled</s:if>>&nbsp;Incomplete&nbsp;&nbsp;</input> 
														<input type=radio name="rd${id }" value="1" <s:if test="hideit">disabled</s:if>>&nbsp;NA&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="3" <s:if test="hideit">disabled</s:if>>&nbsp;Pass&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="2" checked="checked" >&nbsp;Fail</input>
														
													</s:elseif>
													<s:elseif test="answer.value == 3">
														<input type=radio name="rd${id }" value="0" <s:if test="hideit">disabled</s:if>>&nbsp;Incomplete&nbsp;&nbsp;</input> 
														<input type=radio name="rd${id }" value="1" <s:if test="hideit">disabled</s:if>>&nbsp;NA&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="3" checked="checked" >&nbsp;Pass&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="2" <s:if test="hideit">disabled</s:if>>&nbsp;Fail</input>
														
													</s:elseif>
													<s:else>
														<input type=radio name="rd${id }" value="0" checked="checked" >&nbsp;Incomplete&nbsp;&nbsp;</input> 
														<input type=radio name="rd${id }" value="1" <s:if test="hideit">disabled</s:if>>&nbsp;NA&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="3" <s:if test="hideit">disabled</s:if>>&nbsp;Pass&nbsp;&nbsp;</input>  
														<input type=radio name="rd${id }" value="2" <s:if test="hideit">disabled</s:if>>&nbsp;Fail</input>
														
													</s:else>
													
													</td>
												<td><textarea class="form-control" style="width:100%" id="tx${id }" <s:if test="hideit">readOnly</s:if>><s:property value="notes"/></textarea></td>
												<td><button class="btn btn-primary" id="btn${id }" <s:if test="hideit">disabled</s:if>><span class="fa fa-save"></span></button></td>
												<td>
													<s:if test="answer.value == 0">
														Incomplete
													</s:if>
													<s:elseif test="answer.value == 1">
														NA
													</s:elseif>
													<s:elseif test="answer.value == 2">
														Failed
													</s:elseif>
													<s:elseif test="answer.value == 3">
													Passed
													</s:elseif>
													<s:else>
														Incomplete
													</s:else>
												</td>
												</tr>
											</s:iterator>
											</s:iterator>
										</bs:datatable>
									</bs:mco>
							</bs:row>
										     	
			     	
			     	</div><!-- /.tab-pane -->
			     </s:iterator>
   			</div>
   			</bs:mco>
   			</bs:row>
   		</div>
     </div><!-- /.tab-pane -->
   </div><!-- /.tab-content -->
 </div><!-- nav-tabs-custom -->
</div><!-- /.col -->
  
  
 
 

<jsp:include page="../footer.jsp" />


  
    <script src="../dist/js/listbootstrap.js"></script>
<!--
    <script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script> -->
    <script src="../plugins/iCheck/icheck.min.js"></script>
        <!-- DataTables -->
    <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
   <script src="../plugins/fastclick/fastclick.min.js"></script>
    <script src="../dist/js/jquery.autocomplete.min.js"></script>
    <script src="../plugins/jquery-confirm/js/jquery-confirm.js" type="text/javascript"></script>
     <script src="../plugins/select2/select2.full.min.js"></script>
    
    <script>
    
    colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
	 <% int count=9; %>
	 <s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
	 	<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
			$("td:contains('${risk}')").css("color", colors[<%=count %>]).css("font-weight", "bold");
			$(".small-box:contains('${risk}')").css("border-color",colors[<%=count %>]);
			$(".small-box:contains('${risk}')").css("color",colors[<%=count-- %>]);
		</s:if>
	</s:iterator>
	<% count=9; %>
	$('#history, #vulntable').DataTable().on( 'draw', function () {
		console.log("Page Change");
		<s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
			<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
				$("td:contains('${risk}')").css("color", colors[<%=count-- %>]).css("font-weight", "bold");
			</s:if>
		</s:iterator>
	} );
	
	var boxCount = $("#infobar").find("div.row").find("[class^=col-sm]").length;
	var width = 100/boxCount;
	
	$("#infobar").find("div.row").find("[class^=col-sm]").css("width", width+"%").css("min-width","100px");
   
    $(function(){

		$(".filter").click(function(e){
  			e.preventDefault(); 
			console.log($(this).attr("href"));
			href = $(this).attr("href");
			href = href.replace("#","")
			$("#questionTable").DataTable().columns( 4 ).search(href).draw()
		});

      	  $("#addList").click(function(){
      		  
      		  id = $("#selectList").val();
      		  data="id="+id;
      		  data+="&_token=" + _token;
      		  $.post("AddCheckListToAssessment", data).done(function(resp){
      			  alertRedirect(resp);
      			  
      	       });
      		  
      	  });
      	  
    	<s:if test="hideit">
		$("input").attr("disabled","");
		$("textarea").attr("readonly","");
		$("button").attr("disabled","");
		</s:if>
		
  	  $("#questionTable").DataTable({
  		"paging":   false,
        "info":     false,
        "ordering": false,
        "columnDefs": [
        	{
          		 "targets": [0],
                   "visible": true,
                   "searchable": true,
          		
          	},
          {
              "targets": [ 0,1,2,3 ],
              "visible": true,
              "searchable": false
          },
          {
          	"targets" : [4],
          	"visible" : false,
          	"searchable" :true
          },{
          "targets" : [1,2],
          "width":"300px"
          }
          ,{
              "targets" : [3],
              "width":"5%"
              }
        ]

  	  });
  	  
  	  
  	  
  	   $("input[type=radio]").iCheck({
   		radioClass: 'iradio_square-blue'
   	   
   	  });
  	 /*Total Hack cause ..WTF datatables.. you clear checkboxes when using column defs */
  	 $($($("#questionTable").find("tr")[1]).find("input")).each(function(a,b){
  		 if($(b).attr("checked")){
  			 $(b).iCheck('check');
  			 console.log(b);

	}});
  	  
  	  
  	  
  	
  	  $("input[type=radio]").on('ifChanged',function(event){
			changeAnswer(event, $(this));
  	  
  	  });
  	  
  	  $("[id^=tx]").keypress(function (e) {
  		  if (e.which == 13) {
  			var value = $(this).val();
  		  	var id = $(this).attr("id").replace("tx","");
  		  	data="checklistid=" + id;
  		  	data+="&note=" + encodeURIComponent(value);
  		  	data+="&_token=" + _token;
  		  	$.post("UpdateNote", data).done(function(resp){
  		  		getData(resp);
  		  	});
  		  }
  	  });
  	  
  	 $("[id^=btn]").click(function (e) {
 		  
 			
 		  	var id = $(this).attr("id").replace("btn","");
 		  	var value = $("#tx"+id).val();
 		  	data="checklistid=" + id;
 		  	data+="&note=" + encodeURIComponent(value);
 		  	data+="&_token=" + _token;
 		  	$.post("UpdateNote", data).done(function(resp){
 		  		alertMessage(resp,"Note Updated");
 		  	});
 	  });
    });
    
    var stopajax=false;
    function setNA(clid){
		
    	data="checklistid=" + clid;
		data+="&_token=" + _token;
		data+="&answer=1"
		$.post("SetAll",data).done(function(resp){
			alertMessage(resp,"All Items set to NA");
			stopajax=true;
			$("[name^=rd]").each(function(a,b){if($(b).val() == 1){$(b).iCheck('check');}});
			stopajax=false;
			});
		
    }
	function setPassed(clid){
		
    	data="checklistid=" + clid;
		data+="&_token=" + _token;
		data+="&answer=3"
		$.post("SetAll",data).done(function(resp){
			alertMessage(resp,"All Items set to Passed");
			stopajax=true;
			$("[name^=rd]").each(function(a,b){if($(b).val() == 3){$(b).iCheck('check');}});
			stopajax=false;
			});
		
    }
function setFailed(clid){
		
    	data="checklistid=" + clid;
		data+="&_token=" + _token;
		data+="&answer=2"
		$.post("SetAll",data).done(function(resp){
			alertMessage(resp,"All Items set to Failed");
			stopajax=true;
			$("[name^=rd]").each(function(a,b){if($(b).val() == 2){$(b).iCheck('check');}})
			stopajax=false;
			});
		
    }

function changeAnswer(event, el){
  			if(event.target.checked && !stopajax){
  				
  				tr = $(event.target).closest("tr");
	  		  	var value = $(el).val();
	  		  	var id = $(el).attr("name").replace("rd","");
	  		  	var option = "Incomplete";
	  		  	if(value == 1)
	  		  		option="NA";
	  		  	else if(value == 2)
	  		  		option="Failed";
	  		  	else if (value == 3)
	  		  		option="Passed"
	  		  	$("#questionTable").DataTable().row(tr).invalidate()
	  		  	$("#questionTable").DataTable().row(tr).data()[4] = option;
	  		    $("#questionTable").DataTable().row(tr).draw();
	  		  	
	
	  		  	data="checklistid=" + id;
	  		  	data+="&answer=" + value;
	  		  	data+="&_token=" + _token;
	  		  	$.post("UpdateQuestion", data).done(function(resp){
	  		  		getData(resp);
	  		  	});
	  	  }
  	  
  	  }
  
    </script>
 
  </body>
</html>