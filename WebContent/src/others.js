/*
    <!-- REQUIRED JS SCRIPTS -->

    <!-- jQuery 2.1.4 -->
    <script src="../plugins/jQuery/jQuery-2.1.4.min.js"></script>
    <!-- Bootstrap 3.3.5 -->
    <script src="../bootstrap/js/bootstrap.min.js"></script>
    <!-- AdminLTE App -->
    <script src="../dist/js/app.js"></script>
     <script src="../dist/js/base64.min.js"></script>
    <script src="../plugins/jquery-confirm/js/jquery-confirm.js" type="text/javascript"></script>
    <script src="../plugins/loading/js/jquery-loading.js"></script>
    <script>*/
    var _token = "${_token}";
    function getToken(){
    	return _token;
    }
    
    function showLoading(com){
    	$(com).loading({overlay: true, base: 0.3});
    }
    function clearLoading(com){
    	if($(com).hasClass('js-loading'))
    		$(com).loading({destroy: true});
    }
    
    function alertRedirect(resp){
    	if(typeof resp.message == "undefined")
			window.location=window.location
		else
			$.alert(
					{
						title: "Error",
						type:"red",
						content: resp.message,
						columnClass: 'small'
					}
				);
		
		_token = resp.token;
		
    }
    function getData(resp){
    	_token = resp.token;
    	if(typeof resp.message == "undefined")
			return resp.data;
		else{
			$.alert(
					{
						title: "Error",
						type:"red",
						content: resp.message,
						columnClass: 'small'
					}
				);
			return "error";
		}
/*
  <!-- DataTables -->
    /id
	<script src="../plugins/datatables/jquery.dataTables.min.js"></script>
    <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
    <!-- SlimScroll -->
    <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
    <!-- FastClick -->
    <script src="../plugins/fastclick/fastclick.min.js"></script>
    <script src="//cdn.ckeditor.com/4.15.1/standard/ckeditor.js"></script>
    <script>
  */  
    function accept(nid, el){
		$.post("Dashboard", "action=gotIt&nid=" + nid).done(function(){
			table=$('#notify').DataTable();
	    	table.row( $(el).parents('tr') ).remove().draw();
    	});
    	
    	
    }
    
    $(function(){
    
    	 $('#aqueue, #vqueue').DataTable({
             "paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false,
             "order": [0, 'asc' ]
           });
    	 $('#notify').DataTable({
             "paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false,
             "order": [0, 'desc' ]
           });
    	 $.each($("[id^=editor]"), function(index,obj){
    		 CKEDITOR.replace($(obj).attr("id"), {customConfig : 'ckeditor_config.js', toolbar: 'None'});
    	 });
    	 
    	 $('#aqueue tbody').on( 'click', 'tr', function () {
    		 data = $('#aqueue').DataTable().row( this ).data();
    		 document.location="SetAssessment?id="+ data[4];
    	 });
    	 $('#vqueue tbody').on( 'click', 'tr', function () {
    		 data = $('#vqueue').DataTable().row( this ).data();
    		 document.location="Verifications?id=" + data[4];
    	 });
    	 
    	 
    	
  			$.get('../services/getAssessments', function(json){
  					//json = JSON.parse(data);
  					assessments=json.assessments;
  					var table = $("#aqueue").DataTable();
  					for(i=0;i<json.count; i++){
  						table.row.add([assessments[i][2], assessments[i][1], assessments[i][0], assessments[i][3], assessments[i][4]]).draw( false );
  					}
  				});
  			$.get('../services/getVerifications', function(json){
  				//json = JSON.parse(data);
  				verifications=json.verifications;
  				var table = $("#vqueue").DataTable();
  				for(i=0;i<json.count; i++){
					table.row.add([verifications[i][2], verifications[i][0], verifications[i][4], verifications[i][5], verifications[i][3]]).draw( false );
				}
  				colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
  				 <% int count=9; %>
  		    	 <s:iterator value="levels" begin="9" end="0" step="-1" status="stat">
  		    	 	<s:if test="risk != null && risk != 'Unassigned' && risk != ''">
  						$("td:contains('${risk}')").css("color", colors[<%=count %>]).css("font-weight", "bold");
  						$(".small-box:contains('${risk}')").css("background",colors[<%=count-- %>]);
  						$(".small-box:contains('${risk}')").css("color","white");
  					</s:if>
  				</s:iterator>
  				$("th:contains('Vulnerabilities')").css("width", "20px");
  				$("th:contains('Severity')").css("width", "20px");
  				$("th:contains('Start')").css("width", "50px");
  				$("th:contains('ID')").css("width", "50px");
  			});
    });
   
    </script>  
		
		
    }
    function alertMessage(resp, success){
    	if(typeof resp.message == "undefined")
    		$.alert(
					{
						title: "SUCCESS!",
						type:"green",
						content: success,
						columnClass: 'small',
						autoClose: 'ok|2000'
					}
				);
		else
			$.alert(
					{
						title: "Error",
						type:"red",
						content: resp.message,
						columnClass: 'small'
					}
				);
		
		_token = resp.token;
    }
    
    
    setInterval(function () {
    	updateNotificaitons();
    }, 60000);
	
    function updateNotificaitons(){
    	$.get('../services/getAssessments', function(json){
			//json = JSON.parse(data);
			if(json.count != 0)
				$(".assessmentCount").each(function(a,b){$(b).html(json.count);});
			if(json.prcount != 0)
				$(".prCount").each(function(a,b){$(b).html(json.prcount);});
			assessments=json.assessments;
			innerData="<li class='header'>You have "+json.count+ " assessments</li>\n";
			innerData+=" <li>\n";
			innerData+=" <ul class='menu'>\n";

		
			for(i=0;i<json.count; i++){
				innerData+="<li>\n";
				innerData+="<a href='SetAssessment?id=" + assessments[i][4] +"'>\n";
				innerData+="<div class='clipped' >&nbsp;</div><small><i class='fa fa-clock-o'></i> "+assessments[i][2]+"</small>";
				innerData+="<h4>\n";
			    innerData+=assessments[i][1] + " - " + assessments[i][0];
				innerData+="</h4>";
				innerData+="\n";
				innerData+="</a>\n";
				innerData+="</li>\n";
				}
			innerData+="</ul></li>";
			$("#assessmentWidget").html(innerData);
		
		});
		$.get('../services/getVerifications', function(json){
			//json = JSON.parse(data);
			if(json.count != 0)
				$(".verificationCount").each(function(a,b){$(b).html(json.count);});
			verifications=json.verifications;
			innerData="<li class='header'>You have "+json.count+ " Verifications</li>\n";
			innerData+=" <li>\n";
			innerData+=" <ul class='menu'>\n";
	
		
			for(i=0;i<json.count; i++){
				innerData+="<li>\n";
				innerData+="<a href='Verifications?id=" + verifications[i][3] +"'>\n";
				innerData+="<h4>\n"
			    innerData+="<div class='clipped' >" +verifications[i][4] + "</div><small><i class='fa fa-clock-o'></i> "+verifications[i][2]+"</small>"
				innerData+="</h4>"
				innerData+="<p>" + verifications[i][1] + " - " + verifications[i][0] + "</p>";
				innerData+="\n";
				innerData+="</a>\n";
				innerData+="</li>\n";
				}
			innerData+="</ul></li>";
			$("#verificationWidget").html(innerData);
			
		
		});
    	
    }
    $(function(){
    	

	    updateNotificaitons();
		$("#Profile").click(function(){
			$.confirm({
			    content: 'url:Profile',
			    title: 'Update Your Profile',
			    theme: "black",
			    columnClass: 'col-md-8 col-md-offset-2',
			    buttons: {
				    confirm: function (){
				    	data="action=update";
				    	data+="&fname=" + $("#profile_fname").val();
				    	data+="&lname="+ $("#profile_lname").val();
				    	data+="&email="+ $("#profile_email").val();
				    	data+="&current="+ $("#profile_password").val();
				    	data+="&password="+ $("#profile_newpassword").val();
				    	data+="&confirm="+ $("#profile_confirm").val();
				    	$.post("Profile", data).done(function(resp){
				    		title="Success!";
				    		content="Your profile was updated.";
				    		if(resp.message != null){
				    			title="Error!"
				    			content = resp.message;
				    		}
				    		$.alert({
				    			title: title,
				    			content: content
				    		});
				    		
				    	}).error(function(){
			    			$.alert({
				    			title: 'Error!',
				    			content: "There is a problem with your request."
			    			});
				    	});
					},
					cancel:function(){}
			  }
			});
		
		 });
    });

    	//Session Timeout Code since ajax updates keep the sesion alive
    	window.setTimeout(timeouts, 60*60*1000 -120 );
    	var sss=119;
    	sessTimeout=undefined;
    	function tfunc(){ 
			if(sss < 0 ){
				window.location = "../service/logout";
			}else{
				$("#seconds").html(sss);
				sss--;
			}
    	}
    	
    	function timeouts(){
    		$.confirm({
    			title: 'Session Timeout',
    			content: 'Your Session will expire in <span id="seconds">120</span> seconds. <br/><br/>' +
    					 'Do you wish to stay logged in?',
    			onContentReady: function () {
    				sessTimeout = setInterval(tfunc, 2000);	
    			},buttons: {
    				"Still Working" : function(){
    					window.setTimeout(timeouts, 60*60*1000 -120);
    					clearInterval(sessTimeout);
    				},
    				logout: function(){
    					window.location = "../service/logout";
    				}
    			}
    		})
    		
    	}
    	
    	function b64DecodeUnicode(str) {
    		str=decodeURIComponent(str);
    	    return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
    	        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    	    }).join(''))
    	}